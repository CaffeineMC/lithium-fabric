package me.jellysquid.mods.lithium.mixin.entity.use_classed_collision_box_retrieval;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.world.ExtendedChunk;
import me.jellysquid.mods.lithium.common.world.ExtendedWorld2;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class MixinWorld implements ExtendedWorld2 {

    @Shadow
    public abstract Profiler getProfiler();

    @Shadow
    public abstract ChunkManager getChunkManager();

    //[VanillaCopy] custom: class filtered entity list together with excluding one entity
    @Override
    public <T> List<Entity> getEntitiesCustomType(Entity excluded, Class<? extends T> class_1, Box box_1, Predicate<? super T> predicate_1) {
        this.getProfiler().visit("getEntities");
        int int_1 = MathHelper.floor((box_1.x1 - 2.0D) / 16.0D);
        int int_2 = MathHelper.ceil((box_1.x2 + 2.0D) / 16.0D);
        int int_3 = MathHelper.floor((box_1.z1 - 2.0D) / 16.0D);
        int int_4 = MathHelper.ceil((box_1.z2 + 2.0D) / 16.0D);
        List<Entity> list_1 = Lists.newArrayList();
        ChunkManager chunkManager_1 = this.getChunkManager();

        for(int int_5 = int_1; int_5 < int_2; ++int_5) {
            for(int int_6 = int_3; int_6 < int_4; ++int_6) {
                WorldChunk worldChunk_1 = chunkManager_1.getWorldChunk(int_5, int_6, false);
                if (worldChunk_1 != null) {
                    ((ExtendedChunk)worldChunk_1).getEntitiesCustomType(excluded, class_1, box_1, list_1, predicate_1);
                }
            }
        }

        return list_1;
    }
}
