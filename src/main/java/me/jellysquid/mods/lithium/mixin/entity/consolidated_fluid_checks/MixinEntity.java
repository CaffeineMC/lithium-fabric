package me.jellysquid.mods.lithium.mixin.entity.consolidated_fluid_checks;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import me.jellysquid.mods.lithium.common.entity.fluids.TransientFluidCheckState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Patches the fluid movement checks to avoid scanning the world multiple times.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @SuppressWarnings("unchecked")
    private static final Tag<Fluid>[] SCANNED_FLUID_TAGS = new Tag[] { FluidTags.WATER, FluidTags.LAVA };

    private final Reference2ObjectArrayMap<Tag<Fluid>, TransientFluidCheckState> fluidCheckState =
            new Reference2ObjectArrayMap<>();

    private boolean touchedFluidsDirty = true;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void preBaseTick(CallbackInfo ci) {
        // Clear the touched fluid cache and any transient state we created
        this.touchedFluidsDirty = true;
        this.fluidCheckState.clear();
    }

    /**
     * @author JellySquid
     * @reason Avoid scanning the same area multiple times for different fluid types
     */
    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    public void updateMovementInFluid(Tag<Fluid> tag, double factor, CallbackInfoReturnable<Boolean> cir) {
        // Check that the fluid being scanned is part of our search set
        // If not, we need to abort as we won't have any valid data for this fluid. This allows the original method
        // to execute and perform the necessary scan.
        if (!ArrayUtils.contains(SCANNED_FLUID_TAGS, tag)) {
            return;
        }

        // Update the touched fluid cache if necessary
        // The cache is erased at the start of every entity tick
        if (this.touchedFluidsDirty) {
            this.scanTouchedFluids();

            this.touchedFluidsDirty = false;
        }

        TransientFluidCheckState state = this.fluidCheckState.get(tag);

        // If the entity isn't touching any fluids of the given type (or no results were ever computed for it), then
        // early-exit to mimic vanilla behavior.
        if (state == null || !state.touching) {
            cir.setReturnValue(false);

            return;
        }

        // [VanillaCopy] Entity#updateMovementInFluid(Tag<Fluid>, double)
        // This is what would ordinarily be performed after the given fluid type has been scanned. Since we have
        // already scanned all known fluid types, we simply need to bring back down the local variables responsible
        // for computing the final velocity adjustments to the entity.
        Vec3d velocity = state.totalVelocity.toImmutable();
        int sources = state.totalSourceCount;

        if (velocity.length() > 0.0D) {
            if (sources > 0) {
                velocity = velocity.multiply(1.0D / (double) sources);
            }

            // noinspection ConstantConditions
            if (!((Object) this instanceof PlayerEntity)) {
                velocity = velocity.normalize();
            }

            Vec3d prevVelocity = this.getVelocity();
            velocity = velocity.multiply(factor * 1.0D);

            if (Math.abs(prevVelocity.x) < 0.003D && Math.abs(prevVelocity.z) < 0.003D && velocity.length() < 0.0045D) {
                velocity = velocity.normalize().multiply(0.0045D);
            }

            this.setVelocity(this.getVelocity().add(velocity));
        }

        this.fluidHeight.put(tag, state.fluidHeight);

        cir.setReturnValue(true);
    }

    private void scanTouchedFluids() {
        // [VanillaCopy] Entity#updateMovementInFluid(Tag<Fluid>, double)
        // The vanilla state is not modified until it actually tries to query the push factor of touched fluids. This
        // ensures as much as possible that we won't break in the presence of other mods.
        Box box = this.getBoundingBox()
                .contract(0.001D);

        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);

        int maxX = MathHelper.ceil(box.maxX);
        int maxY = MathHelper.ceil(box.maxY);
        int maxZ = MathHelper.ceil(box.maxZ);

        boolean canFly = this.canFly();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockView view = this.world.getExistingChunk(x >> 4, z >> 4);

                    if (view == null) {
                        // A chunk was absent, discard all state we've computed so far as we can't know the true
                        // push given by touched fluids
                        this.fluidCheckState.clear();

                        return;
                    }

                    pos.set(x, y, z);

                    FluidState fluidState = view.getFluidState(pos);

                    // Quickly skip the fluid tag check if we know it's empty
                    if (fluidState.isEmpty()) {
                        continue;
                    }

                    // Check each fluid tag to see if we've found a match. Uses an array type to keep iteration as
                    // fast as possible.
                    for (Tag<Fluid> tag : SCANNED_FLUID_TAGS) {
                        if (!fluidState.isIn(tag)) {
                            continue;
                        }

                        double fluidHeight = (float) y + fluidState.getHeight(this.world, pos);

                        if (fluidHeight < box.minY) {
                            continue;
                        }

                        // Get or create a new transient state object for this fluid type containing
                        // all the fluid sources and velocities
                        TransientFluidCheckState state = this.fluidCheckState.get(tag);

                        if (state == null) {
                            this.fluidCheckState.put(tag, state = new TransientFluidCheckState());
                        }

                        // [VanillaCopy] Entity#updateMovementInFluid(Tag<Fluid>, double)
                        // The local variables in this method are hoisted to the transient state object so that they
                        // can be tracked per-fluid type.
                        state.touching = true;
                        state.fluidHeight = Math.max(fluidHeight - box.minY, state.fluidHeight);

                        if (canFly) {
                            Vec3d fluidVelocity = fluidState.getVelocity(this.world, pos);

                            if (state.fluidHeight < 0.4D) {
                                fluidVelocity = fluidVelocity.multiply(state.fluidHeight);
                            }

                            state.totalVelocity.add(fluidVelocity);
                            state.totalSourceCount++;
                        }
                    }
                }
            }
        }
    }

    @Shadow
    public World world;

    @Shadow
    protected Object2DoubleMap<Tag<Fluid>> fluidHeight;

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract boolean canFly();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);
}
