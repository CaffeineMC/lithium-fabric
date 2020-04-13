package me.jellysquid.mods.lithium.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.function.Predicate;

public interface ExtendedWorld2 {
    <T> List<Entity> getEntitiesCustomType(Entity excluded, Class<? extends T> class_1, Box box_1, Predicate<? super T> predicate_1);
}
