package me.jellysquid.mods.lithium.mixin.entity.fast_retrieval;

import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntitySectionStorage.class)
public abstract class SectionedEntityCacheMixin<T extends EntityAccess> {
    @Shadow
    @Nullable
    public abstract EntitySection<T> getSection(long sectionPos);

    /**
     * @author 2No2Name
     * @reason avoid iterating through LongAVLTreeSet, possibly iterating over hundreds of irrelevant longs to save up to 8 hash set gets
     */
    @Inject(
            method = "forEachAccessibleNonEmptySection",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/core/SectionPos;posToSectionCoord(D)I",
                    ordinal = 5
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void forEachInBox(AABB box, AbortableIterationConsumer<EntitySection<T>> action, CallbackInfo ci, int i, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxX >= minX + 4 || maxZ >= minZ + 4) {
            return; // Vanilla is likely more optimized when shooting entities with TNT cannons over huge distances.
            // Choosing a cutoff of 4 chunk size, as it becomes more likely that these entity sections do not exist when
            // they are far away from the shot entity (player despawn range, position maybe not on the ground, etc)
        }
        ci.cancel();

        // Vanilla order of the AVL long set is sorting by ascending long value. The x, y, z positions are packed into
        // a long with the x position's lowest 22 bits placed at the MSB.
        // Therefore the long is negative iff the 22th bit of the x position is set, which happens iff the x position
        // is negative. A positive x position will never have its 22th bit set, as these big coordinates are far outside
        // the world. y and z positions are treated as unsigned when sorting by ascending long value, as their sign bits
        // are placed somewhere inside the packed long

        for (int x = minX; x <= maxX; x++) {
            for (int z = Math.max(minZ, 0); z <= maxZ; z++) {
                if (this.forEachInColumn(x, minY, maxY, z, action).shouldAbort()) {
                    return;
                }
            }

            int bound = Math.min(-1, maxZ);
            for (int z = minZ; z <= bound; z++) {
                if (this.forEachInColumn(x, minY, maxY, z, action).shouldAbort()) {
                    return;
                }
            }
        }
    }

    private AbortableIterationConsumer.Continuation forEachInColumn(int x, int minY, int maxY, int z, AbortableIterationConsumer<EntitySection<T>> action) {
        AbortableIterationConsumer.Continuation ret = AbortableIterationConsumer.Continuation.CONTINUE;
        //y from negative to positive, but y is treated as unsigned
        for (int y = Math.max(minY, 0); y <= maxY; y++) {
            if ((ret = this.consumeSection(SectionPos.asLong(x, y, z), action)).shouldAbort()) {
                return ret;
            }
        }
        int bound = Math.min(-1, maxY);
        for (int y = minY; y <= bound; y++) {
            if ((ret = this.consumeSection(SectionPos.asLong(x, y, z), action)).shouldAbort()) {
                return ret;
            }
        }
        return ret;
    }

    private AbortableIterationConsumer.Continuation consumeSection(long pos, AbortableIterationConsumer<EntitySection<T>> action) {
        EntitySection<T> section = this.getSection(pos);
        //noinspection SizeReplaceableByIsEmpty
        if (section != null &&
                0 != section.size() /* util.entity_movement_tracking mixins modify isEmpty to include listener objects */
                && section.getStatus().isAccessible()) {
            return action.accept(section);
        }
        return AbortableIterationConsumer.Continuation.CONTINUE;
    }
}
