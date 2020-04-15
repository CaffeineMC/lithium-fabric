package me.jellysquid.mods.lithium.mixin.entity.collision_only_boat_shulker;

import me.jellysquid.mods.lithium.common.world.ExtendedChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements ExtendedChunk {
    @Shadow
    @Final
    private TypeFilterableList<Entity>[] entitySections;

    //[VanillaCopy] custom: class filtered entity list together with excluding one entity
    @Override
    public <T> void getEntitiesCustomType(Entity excluded, Class<? extends T> class_1, Box box_1, List<Entity> list_1, Predicate<? super T> predicate_1) {
        int int_1 = MathHelper.floor((box_1.y1 - 2.0D) / 16.0D);
        int int_2 = MathHelper.floor((box_1.y2 + 2.0D) / 16.0D);
        int_1 = MathHelper.clamp(int_1, 0, this.entitySections.length - 1);
        int_2 = MathHelper.clamp(int_2, 0, this.entitySections.length - 1);

        for(int int_3 = int_1; int_3 <= int_2; ++int_3) {
            for(T entity_1 : this.entitySections[int_3].getAllOfType(class_1)) {
                if (entity_1 != excluded && ((Entity)entity_1).getBoundingBox().intersects(box_1) && (predicate_1 == null || predicate_1.test(entity_1))) {
                    list_1.add((Entity)entity_1);
                }
            }
        }
    }
}
