package net.caffeinemc.mods.lithium.mixin.world.explosions.block_raycast;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.caffeinemc.mods.lithium.common.util.Pos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Optimizations for Explosions: Reduce allocations and getChunk/getBlockState calls
 * Original implementation by
 * @author Jellyquid
 * Slight performance and mod compatibility improvements by
 * @author 2No2Name
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private float radius;

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
    private Level level;

    @Shadow
    @Final
    private ExplosionDamageCalculator damageCalculator;
    @Shadow
    @Final
    private boolean fire;
    // The cached mutable block position used during block traversal.
    private final BlockPos.MutableBlockPos cachedPos = new BlockPos.MutableBlockPos();

    // The chunk coordinate of the most recently stepped through block.
    private int prevChunkX = Integer.MIN_VALUE;
    private int prevChunkZ = Integer.MIN_VALUE;

    // The chunk belonging to prevChunkPos.
    private ChunkAccess prevChunk;

    /**
     * Whether the explosion cares about air blocks. If false, air blocks do not have to be added to the set of destroyed blocks.
     * Skipping air blocks reduces the number of BlockPos allocations, shuffling and getBlockState calls in {@link Explosion#finalizeExplosion(boolean)}
     */
    private boolean explodeAirBlocks;

    private int bottomY, topY;

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)V",
            at = @At("TAIL")
    )
    private void init(Level world, Entity entity, DamageSource damageSource, ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, Explosion.BlockInteraction destructionType, ParticleOptions particle, ParticleOptions emitterParticle, Holder<?> soundEvent, CallbackInfo ci) {
        this.bottomY = this.level.getMinBuildHeight();
        this.topY = this.level.getMaxBuildHeight();

        boolean explodeAir = this.fire; // air blocks are only relevant for the explosion when fire should be created inside them
        if (!explodeAir && this.level.dimension() == Level.END && this.level.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
            float overestimatedExplosionRange = (8 + (int) (6f * this.radius));
            int endPortalX = 0;
            int endPortalZ = 0;
            if (overestimatedExplosionRange > Math.abs(this.x - endPortalX) && overestimatedExplosionRange > Math.abs(this.z - endPortalZ)) {
                explodeAir = true;
                // exploding air works around accidentally fixing vanilla bug: an explosion cancelling the dragon fight start can destroy the newly placed end portal
            }
        }
        this.explodeAirBlocks = explodeAir;
    }

    @Redirect(
            method = "explode()V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;", remap = false)
    )
    public HashSet<BlockPos> skipNewHashSet() {
        return null;
    }

    @ModifyConstant(
            method = "explode()V",
            constant = @Constant(intValue = 16, ordinal = 1)
    )
    public int skipLoop(int prevValue) {
        return 0;
    }

    /**
     * @author JellySquid
     */
    @Redirect(method = "explode()V",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;addAll(Ljava/util/Collection;)Z", remap = false))
    public boolean collectBlocks(ObjectArrayList<BlockPos> affectedBlocks, Collection<BlockPos> collection) {
        // Using integer encoding for the block positions provides a massive speedup and prevents us from needing to
        // allocate a block position for every step we make along each ray, eliminating essentially all the memory
        // allocations of this function. The overhead of packing block positions into integer format is negligible
        // compared to a memory allocation and associated overhead of hashing real objects in a set.
        final LongOpenHashSet touched = new LongOpenHashSet(0);

        final RandomSource random = this.level.random;

        // Explosions work by casting many rays through the world from the origin of the explosion
        for (int rayX = 0; rayX < 16; ++rayX) {
            boolean xPlane = rayX == 0 || rayX == 15;
            double vecX = (((float) rayX / 15.0F) * 2.0F) - 1.0F;

            for (int rayY = 0; rayY < 16; ++rayY) {
                boolean yPlane = rayY == 0 || rayY == 15;
                double vecY = (((float) rayY / 15.0F) * 2.0F) - 1.0F;

                for (int rayZ = 0; rayZ < 16; ++rayZ) {
                    boolean zPlane = rayZ == 0 || rayZ == 15;

                    // We only fire rays from the surface of our origin volume
                    if (xPlane || yPlane || zPlane) {
                        double vecZ = (((float) rayZ / 15.0F) * 2.0F) - 1.0F;

                        this.performRayCast(random, vecX, vecY, vecZ, touched);
                    }
                }
            }
        }

        // We can now iterate back over the set of positions we modified and re-build BlockPos objects from them
        // This will only allocate as many objects as there are in the set, where otherwise we would allocate them
        // each step of a every ray.
        LongIterator it = touched.iterator();

        boolean added = false;
        while (it.hasNext()) {
            added |= affectedBlocks.add(BlockPos.of(it.nextLong()));
        }
        return added;
    }

    @Unique
    private void performRayCast(RandomSource random, double vecX, double vecY, double vecZ, LongOpenHashSet touched) {
        double dist = Math.sqrt((vecX * vecX) + (vecY * vecY) + (vecZ * vecZ));

        double normX = (vecX / dist) * 0.3D;
        double normY = (vecY / dist) * 0.3D;
        double normZ = (vecZ / dist) * 0.3D;

        float strength = this.radius * (0.7F + (random.nextFloat() * 0.6F));

        double stepX = this.x;
        double stepY = this.y;
        double stepZ = this.z;

        int prevX = Integer.MIN_VALUE;
        int prevY = Integer.MIN_VALUE;
        int prevZ = Integer.MIN_VALUE;

        float prevResistance = 0.0F;

        int boundMinY = this.bottomY;
        int boundMaxY = this.topY;

        // Step through the ray until it is finally stopped
        while (strength > 0.0F) {
            int blockX = Mth.floor(stepX);
            int blockY = Mth.floor(stepY);
            int blockZ = Mth.floor(stepZ);

            float resistance;

            // Check whether we have actually moved into a new block this step. Due to how rays are stepped through,
            // over-sampling of the same block positions will occur. Changing this behaviour would introduce differences in
            // aliasing and sampling, which is unacceptable for our purposes. As a band-aid, we can simply re-use the
            // previous result and get a decent boost.
            if (prevX != blockX || prevY != blockY || prevZ != blockZ) {
                if (blockY < boundMinY || blockY >= boundMaxY || blockX < -30000000 || blockZ < -30000000 || blockX >= 30000000 || blockZ >= 30000000) {
                    return;
                }
                //The coordinates are within the world bounds, so we can safely traverse the block
                resistance = this.traverseBlock(strength, blockX, blockY, blockZ, touched);

                prevX = blockX;
                prevY = blockY;
                prevZ = blockZ;

                prevResistance = resistance;
            } else {
                resistance = prevResistance;
            }

            strength -= resistance;
            // Apply a constant fall-off
            strength -= 0.22500001F;

            stepX += normX;
            stepY += normY;
            stepZ += normZ;
        }
    }

    /**
     * Called for every step made by a ray being cast by an explosion.
     *
     * @param strength The strength of the ray during this step
     * @param blockX   The x-coordinate of the block the ray is inside of
     * @param blockY   The y-coordinate of the block the ray is inside of
     * @param blockZ   The z-coordinate of the block the ray is inside of
     * @return The resistance of the current block space to the ray
     */
    @Unique
    private float traverseBlock(float strength, int blockX, int blockY, int blockZ, LongOpenHashSet touched) {
        BlockPos pos = this.cachedPos.set(blockX, blockY, blockZ);

        int chunkX = Pos.ChunkCoord.fromBlockCoord(blockX);
        int chunkZ = Pos.ChunkCoord.fromBlockCoord(blockZ);

        // Avoid calling into the chunk manager as much as possible through managing chunks locally
        if (this.prevChunkX != chunkX || this.prevChunkZ != chunkZ) {
            this.prevChunk = this.level.getChunk(chunkX, chunkZ);

            this.prevChunkX = chunkX;
            this.prevChunkZ = chunkZ;
        }

        final ChunkAccess chunk = this.prevChunk;

        BlockState blockState = Blocks.AIR.defaultBlockState();
        float totalResistance = 0.0F;
        Optional<Float> blastResistance;

        labelGetBlastResistance:
        {
            // If the chunk is missing or out of bounds, assume that it is air
            if (chunk != null) {
                // We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
                // performance as possible here
                LevelChunkSection section = chunk.getSections()[Pos.SectionYIndex.fromBlockCoord(chunk, blockY)];

                // If the section doesn't exist or it's empty, assume that the block is air
                if (section != null && !section.hasOnlyAir()) {
                    // Retrieve the block state from the chunk section directly to avoid associated overhead
                    blockState = section.getBlockState(blockX & 15, blockY & 15, blockZ & 15);

                    // If the block state is air, it cannot have fluid or any kind of resistance, so just leave
                    if (blockState.getBlock() != Blocks.AIR) {
                        // Rather than query the fluid state from the container as we just did with the block state, we can
                        // simply ask the block state we retrieved what fluid it has. This is exactly what the call would
                        // do anyways, except that it would have to retrieve the block state a second time, adding overhead.
                        FluidState fluidState = blockState.getFluidState();

                        // Get the explosion resistance like vanilla
                        blastResistance = this.damageCalculator.getBlockExplosionResistance((Explosion) (Object) this, this.level, pos, blockState, fluidState);
                        break labelGetBlastResistance;
                    }
                }
            }
            blastResistance = this.damageCalculator.getBlockExplosionResistance((Explosion) (Object) this, this.level, pos, Blocks.AIR.defaultBlockState(), Fluids.EMPTY.defaultFluidState());
        }
        // Calculate how much this block will resist an explosion's ray
        if (blastResistance.isPresent()) {
            totalResistance = (blastResistance.get() + 0.3F) * 0.3F;
        }

        // Check if this ray is still strong enough to break blocks, and if so, add this position to the set
        // of positions to destroy
        float reducedStrength = strength - totalResistance;
        if (reducedStrength > 0.0F && (this.explodeAirBlocks || !blockState.isAir())) {
            if (this.damageCalculator.shouldBlockExplode((Explosion) (Object) this, this.level, pos, blockState, reducedStrength)) {
                touched.add(pos.asLong());
            }
        }

        return totalResistance;
    }

}
