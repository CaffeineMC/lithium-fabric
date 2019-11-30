package me.jellysquid.mods.lithium.mixin.entity.fast_pathfind_chunk_cache;

import me.jellysquid.mods.lithium.common.cache.NavigationChunkCache;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(EntityNavigation.class)
public abstract class MixinEntityNavigation {
    @Shadow
    @Final
    protected MobEntity entity;

    @Shadow
    protected abstract boolean isAtValidPosition();

    @Shadow
    protected Path currentPath;

    @Shadow
    @Final
    protected World world;

    @Shadow
    public abstract float getFollowRange();

    @Shadow
    private PathNodeNavigator pathNodeNavigator;

    @Shadow
    private BlockPos field_20293;

    @Shadow
    private int field_20294;

    /**
     * We can't replace the creation of ChunkCache because the return type is different (although it still implements
     * the right type for the variable it is stored into.)
     * <p>
     * TODO: Avoid the Overwrite here.
     *
     * @author JellySquid
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public Path findPathTo(Set<BlockPos> set, int additionalRange, boolean startAbove, int int_2) {
        if (set.isEmpty()) {
            return null;
        }

        if (this.entity.y < 0.0D) {
            return null;
        }

        if (!this.isAtValidPosition()) {
            return null;
        }

        if (this.currentPath != null && !this.currentPath.isFinished() && set.contains(this.field_20293)) {
            return this.currentPath;
        }

        this.world.getProfiler().push("pathfind");

        float range = this.getFollowRange();

        BlockPos blockPos_1 = startAbove ? (new BlockPos(this.entity)).up() : new BlockPos(this.entity);

        int radius = (int) range + additionalRange;

        ViewableWorld worldAccess = new NavigationChunkCache(this.world, blockPos_1.add(-radius, -radius, -radius), blockPos_1.add(radius, radius, radius));

        Path path = this.pathNodeNavigator.pathfind(worldAccess, this.entity, set, range, int_2);

        this.world.getProfiler().pop();

        if (path != null && path.method_48() != null) {
            this.field_20293 = path.method_48();
            this.field_20294 = int_2;
        }

        return path;
    }
}
