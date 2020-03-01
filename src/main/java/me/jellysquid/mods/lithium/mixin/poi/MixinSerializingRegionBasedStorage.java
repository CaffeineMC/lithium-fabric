package me.jellysquid.mods.lithium.mixin.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.poi.IExtendedRegionBasedStorage;
import me.jellysquid.mods.lithium.common.util.ListeningLong2ObjectOpenHashMap;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // We don't get a choice, this is Minecraft's doing!
@Mixin(SerializingRegionBasedStorage.class)
public class MixinSerializingRegionBasedStorage<R> implements IExtendedRegionBasedStorage<R> {
    @Mutable
    @Shadow
    @Final
    private Long2ObjectMap<Optional<R>> loadedElements;

    private Long2ObjectOpenHashMap<BitSet> columns;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(File file_1, BiFunction<Runnable, Dynamic<?>, R> serializer, Function<Runnable, R> factory, DataFixer fixer, DataFixTypes type, CallbackInfo ci) {
        this.columns = new Long2ObjectOpenHashMap<>();
        this.loadedElements = new ListeningLong2ObjectOpenHashMap<>(this::onEntryAdded, this::onEntryRemoved);
    }

    private void onEntryRemoved(long key, Optional<R> value) {
        // NO-OP... vanilla never removes anything, leaking entries.
        // We might want to fix this.
    }

    private void onEntryAdded(long key, Optional<R> value) {
        long pos = ChunkPos.toLong(ChunkSectionPos.getX(key), ChunkSectionPos.getZ(key));

        BitSet flags = this.columns.get(pos);

        if (flags == null) {
            this.columns.put(pos, flags = new BitSet(16));
        }

        flags.set(ChunkSectionPos.getY(key), value.isPresent());
    }

    @Override
    public Stream<R> getWithinChunkColumn(int chunkX, int chunkZ) {
        BitSet flags = this.columns.get(ChunkPos.toLong(chunkX, chunkZ));

        // No items are present in this column
        if (flags == null || flags.isEmpty()) {
            return Stream.empty();
        }

        return flags.stream()
                .mapToObj((chunkY) -> {
                    return this.loadedElements.get(ChunkSectionPos.asLong(chunkX, chunkY, chunkZ)).orElse(null);
                })
                .filter(Objects::nonNull);
    }
}
