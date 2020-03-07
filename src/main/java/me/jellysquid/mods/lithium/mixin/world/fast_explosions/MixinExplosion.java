package me.jellysquid.mods.lithium.mixin.world.fast_explosions;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(Explosion.class)
public abstract class MixinExplosion {
    @Shadow
    @Final
    private float power;

    @Shadow
    @Final
    private double x;

    @Shadow
    @Final
    private double y;

    @Shadow
    @Final
    private double z;

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    private List<BlockPos> affectedBlocks;

    @Shadow
    public abstract DamageSource getDamageSource();

    @Shadow
    public static float getExposure(Vec3d self, Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    @Final
    private Map<PlayerEntity, Vec3d> affectedPlayers;

    // The cached mutable block position used during block traversal.
    private final BlockPos.Mutable cachedPos = new BlockPos.Mutable();

    // The chunk coordinate of the most recently stepped through block.
    private int prevChunkX = Integer.MIN_VALUE;
    private int prevChunkZ = Integer.MIN_VALUE;

    // The chunk belonging to prevChunkPos.
    private Chunk prevChunk;

    /**
     * @reason Optimizations for explosions
     * @author JellySquid
     */
    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"), cancellable = true)
    public void collectBlocksAndDamageEntities(CallbackInfo ci) {
        // We don't want to use an Overwrite here as it can conflict with other mods modifying this code path and will
        // crash the game. This *will* cause issues with other mods which transform this method, but it shouldn't break
        // anything critical.
        // TODO: Implement patch hints
        ci.cancel();

        // Using integer encoding for the block positions provides a massive speedup and prevents us from needing to
        // allocate a block position for every step we make along each ray, eliminating essentially all the memory
        // allocations of this function. The overhead of packing block positions into integer format is negligible
        // compared to a memory allocation and associated overhead of hashing real objects in a set.
        final LongOpenHashSet touched = new LongOpenHashSet(6 * 6 * 6);

        final Random random = this.world.random;

        // Explosions work by casting many rays through the world from the origin of the explosion
        for (int rayX = 0; rayX < 16; ++rayX) {
            boolean xPlane = rayX == 0 || rayX == 15;

            for (int rayY = 0; rayY < 16; ++rayY) {
                boolean yPlane = rayY == 0 || rayY == 15;

                for (int rayZ = 0; rayZ < 16; ++rayZ) {
                    boolean zPlane = rayZ == 0 || rayZ == 15;

                    // We only fire rays from the surface of our origin volume
                    if (xPlane || yPlane || zPlane) {
                        double vecX = (((float) rayX / 15.0F) * 2.0F) - 1.0F;
                        double vecY = (((float) rayY / 15.0F) * 2.0F) - 1.0F;
                        double vecZ = (((float) rayZ / 15.0F) * 2.0F) - 1.0F;

                        this.performRayCast(random, vecX, vecY, vecZ, touched);
                    }
                }
            }
        }

        // We can now iterate back over the set of positions we modified and re-build BlockPos objects from them
        // This will only allocate as many objects as there are in the set, where otherwise we would allocate them
        // each step of a every ray.
        List<BlockPos> affectedBlocks = this.affectedBlocks;

        LongIterator it = touched.iterator();

        while (it.hasNext()) {
            affectedBlocks.add(BlockPos.fromLong(it.nextLong()));
        }

        this.damageEntities();
    }

    private void performRayCast(Random random, double vecX, double vecY, double vecZ, LongOpenHashSet touched) {
        double dist = Math.sqrt((vecX * vecX) + (vecY * vecY) + (vecZ * vecZ));

        double normX = vecX / dist;
        double normY = vecY / dist;
        double normZ = vecZ / dist;

        float strength = this.power * (0.7F + (random.nextFloat() * 0.6F));

        double stepX = this.x;
        double stepY = this.y;
        double stepZ = this.z;

        int prevX = Integer.MIN_VALUE;
        int prevY = Integer.MIN_VALUE;
        int prevZ = Integer.MIN_VALUE;

        float prevResistance = 0.0f;

        // Step through the ray until it is finally stopped
        while (strength > 0.0F) {
            int blockX = MathHelper.floor(stepX);
            int blockY = MathHelper.floor(stepY);
            int blockZ = MathHelper.floor(stepZ);

            float resistance;

            // Check whether or not we have actually moved into a new block this step. Due to how rays are stepped through,
            // over-sampling of the same block positions will occur. Changing this behaviour would introduce differences in
            // aliasing and sampling, which is unacceptable for our purposes. As a band-aid, we can simply re-use the
            // previous result and get a decent boost.
            if (prevX != blockX || prevY != blockY || prevZ != blockZ) {
                resistance = this.traverseBlock(strength, blockX, blockY, blockZ, touched);

                prevX = blockX;
                prevY = blockY;
                prevZ = blockZ;

                prevResistance = resistance;
            } else {
                resistance = prevResistance;
            }

            // Apply a constant fall-off
            strength -= resistance + 0.225F;

            stepX += normX * 0.3D;
            stepY += normY * 0.3D;
            stepZ += normZ * 0.3D;
        }
    }

    /**
     * Called for every step made by a ray being cast by an explosion.
     * @param strength The strength of the ray during this step
     * @param blockX The x-coordinate of the block the ray is inside of
     * @param blockY The y-coordinate of the block the ray is inside of
     * @param blockZ The z-coordinate of the block the ray is inside of
     * @return The resistance of the current block space to the ray
     */
    private float traverseBlock(float strength, int blockX, int blockY, int blockZ, LongOpenHashSet touched) {
        // Early-exit if the y-coordinate is out of bounds.
        if (World.isHeightInvalid(blockY)) {
            return 0.0f;
        }

        BlockPos pos = this.cachedPos.set(blockX, blockY, blockZ);

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;

        // Avoid calling into the chunk manager as much as possible through managing chunks locally
        if (this.prevChunkX != chunkX || this.prevChunkZ != chunkZ) {
            this.prevChunk = this.world.getChunk(chunkX, chunkZ);

            this.prevChunkX = chunkX;
            this.prevChunkZ = chunkZ;
        }

        final Chunk chunk = this.prevChunk;

        BlockState blockState = Blocks.AIR.getDefaultState();
        float totalResistance = 0.0f;

        // If the chunk is missing or out of bounds, assume that it is air
        if (chunk != null) {
            // We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
            // performance as possible here
            ChunkSection section = chunk.getSectionArray()[blockY >> 4];

            // If the section doesn't exist or it's empty, assume that the block is air
            if (section != null && !section.isEmpty()) {
                // Retrieve the block state from the chunk section directly to avoid associated overhead
                blockState = section.getBlockState(blockX & 15, blockY & 15, blockZ & 15);

                // If the block state is air, it cannot have fluid or any kind of resistance, so just leave
                if (blockState.getBlock() != Blocks.AIR) {
                    // Rather than query the fluid state from the container as we just did with the block state, we can
                    // simply ask the block state we retrieved what fluid it has. This is exactly what the call would
                    // do anyways, except that it would have to retrieve the block state a second time, adding overhead.
                    FluidState fluidState = blockState.getFluidState();

                    // Pick the highest resistance value between the block and fluid
                    float resistance = Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance());

                    // If this explosion was caused by an entity, allow for it to modify the resistance of this position
                    if (this.entity != null) {
                        resistance = this.entity.getEffectiveExplosionResistance((Explosion) (Object) this, this.world, pos, blockState, fluidState, resistance);
                    }

                    // Calculate how much this block space will resist an explosion's ray
                    totalResistance = (resistance + 0.3F) * 0.3F;
                }
            }
        }

        // Check if this ray is still strong enough to break blocks, and if so, add this position to the set
        // of positions to destroy
        if ((strength - totalResistance) > 0.0F) {
            if ((this.entity == null) || this.entity.canExplosionDestroyBlock((Explosion) (Object) this, this.world, pos, blockState, strength)) {
                touched.add(pos.asLong());
            }
        }

        return totalResistance;
    }

    // [VanillaCopy] Explosion#collectBlocksAndDamageEntities()
    private void damageEntities() {
        float range = this.power * 2.0F;

        int minX = MathHelper.floor(this.x - (double) range - 1.0D);
        int maxX = MathHelper.floor(this.x + (double) range + 1.0D);
        int minY = MathHelper.floor(this.y - (double) range - 1.0D);
        int maxY = MathHelper.floor(this.y + (double) range + 1.0D);
        int minZ = MathHelper.floor(this.z - (double) range - 1.0D);
        int maxZ = MathHelper.floor(this.z + (double) range + 1.0D);

        List<Entity> entities = this.world.getEntities(this.entity, new Box(minX, minY, minZ, maxX, maxY, maxZ));

        Vec3d selfPos = new Vec3d(this.x, this.y, this.z);

        for (Entity entity : entities) {
            if (entity.isImmuneToExplosion()) {
                continue;
            }

            double damageScale = MathHelper.sqrt(entity.squaredDistanceTo(selfPos)) / range;

            if (damageScale > 1.0D) {
                continue;
            }

            double distXSq = entity.getX() - this.x;
            double distYSq = entity.getEyeY() - this.y;
            double distZSq = entity.getZ() - this.z;

            double dist = MathHelper.sqrt((distXSq * distXSq) + (distYSq * distYSq) + (distZSq * distZSq));

            if (dist == 0.0D) {
                continue;
            }

            distXSq = distXSq / dist;
            distYSq = distYSq / dist;
            distZSq = distZSq / dist;

            double exposure = getExposure(selfPos, entity);
            double damage = (1.0D - damageScale) * exposure;

            entity.damage(this.getDamageSource(), (int) (((((damage * damage) + damage) / 2.0D) * 7.0D * (double) range) + 1.0D));

            double knockback = damage;

            if (entity instanceof LivingEntity) {
                knockback = ProtectionEnchantment.transformExplosionKnockback((LivingEntity) entity, damage);
            }

            entity.setVelocity(entity.getVelocity().add(distXSq * knockback, distYSq * knockback, distZSq * knockback));

            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;

                if (!player.isSpectator() && (!player.isCreative() || !player.abilities.flying)) {
                    this.affectedPlayers.put(player, new Vec3d(distXSq * damage, distYSq * damage, distZSq * damage));
                }
            }
        }
    }
}
