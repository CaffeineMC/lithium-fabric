package me.jellysquid.mods.lithium.mixin.world.poi_columns;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import me.jellysquid.mods.lithium.common.poi.IExtendedRegionBasedStorage;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PointOfInterestStorage.class)
public class MixinPointOfInterestStorage extends SerializingRegionBasedStorage<PointOfInterestSet> {
    public MixinPointOfInterestStorage(File file, BiFunction<Runnable, Dynamic<?>, PointOfInterestSet> deserializer, Function<Runnable, PointOfInterestSet> factory, DataFixer fixer, DataFixTypes type) {
        super(file, deserializer, factory, fixer, type);
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public Stream<PointOfInterest> getInChunk(Predicate<PointOfInterestType> predicate, ChunkPos pos, PointOfInterestStorage.OccupationStatus status) {
        return ((IExtendedRegionBasedStorage<PointOfInterestSet>) this)
                .getWithinChunkColumn(pos.x, pos.z)
                .flatMap((set) -> set.get(predicate, status));
    }
}
