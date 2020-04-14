package me.jellysquid.mods.lithium.mixin.entity.fluid_checks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.world.FluidPushInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Entities scan the same volume of blocks every tick for different types of fluids. This patch consolidates the checks
 * into a single scan and caches the results for each anticipated fluid type that will be checked. When we anticipate
 * incorrectly (didn't cache a fluid type), the fallback path is taken. Additionally, excessive allocations are avoided
 * and should reduce allocation rate a bit when entities are being pushed by many fluids or underwater.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public World world;

    @Shadow
    public abstract boolean canFly();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow
    protected double waterHeight;

    private static final ObjectArrayList<Tag<Fluid>> DEFAULT_FLUID_TAGS = new ObjectArrayList<>();

    static {
        DEFAULT_FLUID_TAGS.add(FluidTags.LAVA);
        DEFAULT_FLUID_TAGS.add(FluidTags.WATER);
        DEFAULT_FLUID_TAGS.trim();
    }

    private final Reference2ObjectMap<Tag<Fluid>, FluidPushInfo> cachedPushingFluids = new Reference2ObjectOpenHashMap<>();
    private boolean cachedPushingFluidsDirty = true;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void preBaseTick(CallbackInfo ci) {
        // Invalidate the cached data every tick
        this.cachedPushingFluidsDirty = true;
        this.cachedPushingFluids.clear();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void preUpdateMovementInFluid(Tag<Fluid> tag, double d, CallbackInfoReturnable<Boolean> cir) {
        // We only use the fast-path when we recognize the fluid tag being used. This is required because we don't have
        // foreknowledge of what kind of fluid types the entity will be interested in.
        if (!DEFAULT_FLUID_TAGS.contains(tag)) {
            return;
        }

        // Ensure our cache is up-to-date
        this.calculatePushingFluids();

        // Retrieve the computed information for the fluid type. If it's not present, the entity never collided
        // with fluid of that type.
        FluidPushInfo info = this.cachedPushingFluids.get(tag);

        if (info != null) {
            if (info.velocity.lengthSquared() > 0.0D) {
                Vec3d push = info.velocity.toImmutable();

                // Compute the average strength by dividing by the number of sources
                if (info.sources > 0) {
                    push = push.multiply(1.0D / (double) info.sources);
                }

                // [VanillaCopy] Non-player entities are special
                if (!((Object) this instanceof PlayerEntity)) {
                    push = push.normalize();
                }

                // Modify the entity's velocity
                this.setVelocity(this.getVelocity().add(push.multiply(d)));
            }

            // [VanillaCopy]
            // The value of the waterHeight field is *always* that of the last movement check through fluid. This is a
            // bug from vanilla and must be maintained.
            this.waterHeight = info.height;

            // Regardless of the entity actually being pushed by the fluid, we must indicate that it did collide with it
            cir.setReturnValue(true);
        } else {
            // No information was computed for the tag, so the entity never collided with it
            cir.setReturnValue(false);
        }
    }

    public void calculatePushingFluids() {
        // Check if the cache really needs an update
        if (!this.cachedPushingFluidsDirty) {
            return;
        }

        Box box = this.getBoundingBox().contract(0.001D);

        int minX = MathHelper.floor(box.x1);
        int maxX = MathHelper.floor(box.x2);
        int minY = MathHelper.floor(box.y1);
        int maxY = MathHelper.floor(box.y2);
        int minZ = MathHelper.floor(box.z1);
        int maxZ = MathHelper.floor(box.z2);

        boolean canFly = this.canFly();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                // Hoist retrieving the chunk from the y-iteration loop
                Chunk chunk = this.world.getChunk(x >> 4, z >> 4);

                if (chunk == null) {
                    continue;
                }

                for (int y = minY; y <= maxY; ++y) {
                    if (y < 0 || y >= 256) {
                        continue;
                    }

                    ChunkSection section = chunk.getSectionArray()[y >> 4];

                    // If the section only contains air blocks, no fluids will exist in it, so skip it
                    if (section == null || section.isEmpty()) {
                        continue;
                    }

                    FluidState fluidState = section.getFluidState(x & 15, y & 15, z & 15);

                    // Quickly check if there's no liquid
                    if (fluidState == Fluids.EMPTY.getDefaultState()) {
                        continue;
                    }

                    pos.set(x, y, z);

                    double height = (float) y + fluidState.getHeight(this.world, pos);

                    // If the entity doesn't collide with the liquid, skip it
                    if (height < box.y1) {
                        continue;
                    }

                    // Scan each anticipate tag and calculate the fluid push information
                    for (Tag<Fluid> tag : DEFAULT_FLUID_TAGS) {
                        if (!fluidState.matches(tag)) {
                            continue;
                        }

                        FluidPushInfo info = this.cachedPushingFluids.get(tag);

                        // If no push back information exists for this tag, create it. This will also indicate to
                        // other code that this entity intersected with this fluid type.
                        if (info == null) {
                            this.cachedPushingFluids.put(tag, info = new FluidPushInfo());
                        }

                        // From this point on, the implementation is taken from vanilla
                        // [VanillaCopy] Entity#updateMovementInFluid(Tag, double)
                        info.height = Math.max(height - box.y1, info.height);

                        if (canFly) {
                            Vec3d velocity = fluidState.getVelocity(this.world, pos);

                            info.velocity.add(velocity, info.height < 0.4D ? info.height : 1.0D);
                            info.sources++;
                        }
                    }
                }
            }
        }

        // We've updated all our data, so the cache is now clean
        // The calculation of the actual push back made by a type of fluid is delayed until that information is
        // actually retrieved. This allows other mods/vanilla to modify the strength property.
        this.cachedPushingFluidsDirty = false;
    }
}
