package me.jellysquid.mods.lithium.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.function.Predicate;

public interface ExtendedChunk {
    <T> void getEntitiesCustomType(Entity excluded, Class<? extends T> class_1, Box box_1, List<Entity> list_1, Predicate<? super T> predicate_1);
}
