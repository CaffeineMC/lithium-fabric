package me.jellysquid.mods.lithium.mixin.worldfixer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ListIterator;

@Mixin(WorldUpdater.class)
public class WorldUpdaterMixin {

    @Shadow
    @Final
    private static org.apache.logging.log4j.Logger LOGGER;

    static {
        LOGGER.info("Lithium chunk section palette fixer: Preparing to fix chunks broken by Lithium versions <= 0.7.4!. \n" +
                "This prevents chunks from being regenerated when upgrading to 1.18 later.");
    }

    @Unique
    private ThreadLocal<Boolean> lithiumEditedCurrentChunk = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "updateWorld()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/SharedConstants;getGameVersion()Lcom/mojang/bridge/game/GameVersion;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void fixMismatchedChunkPaletteDataSize(CallbackInfo ci, ImmutableMap.Builder<?, ?> builder, float f, ImmutableMap<?, ?> immutableMap, ImmutableMap.Builder<?, ?> builder2, ImmutableMap<?, ?> immutableMap2, long l, boolean bl, float g, UnmodifiableIterator<?> var10, RegistryKey<?> registryKey3, ListIterator<?> listIterator, VersionedChunkStorage versionedChunkStorage, ChunkPos chunkPos, boolean bl2, NbtCompound nbtCompound, int i, NbtCompound nbtCompound2, NbtCompound nbtCompound3) {
        try {
            boolean editedSection = false;

            NbtList sectionList = nbtCompound2.getCompound("Level").getList("Sections", 10);
            for (int y = 0, sectionListSize = sectionList.size(); y < sectionListSize; y++) {
                NbtCompound section = sectionList.getCompound(y);
                long[] blockStates = section.getLongArray("BlockStates");
                NbtList palette = section.getList("Palette", 10);
                if (palette.size() > 0 && blockStates.length > 0) {
                    //calculate how large the data array should be according to the palette size
                    int expectedBitsPerState = Math.max(4, MathHelper.log2DeBruijn(palette.size()));
                    int expectedStatesPerLong = 64 / expectedBitsPerState;
                    int expectedDataSize = MathHelper.ceil(4096 / (double) expectedStatesPerLong);
                    //fix mismatched sizes. 1.18 vanilla
                    if (expectedDataSize != blockStates.length) {
                        LOGGER.info("Lithium chunk section palette fixer: Detected bits per blockstate mismatch in palette/data at {}.", ChunkSectionPos.from(chunkPos.x, y, chunkPos.z));

                        //find out how large the palette needs to be
                        int bitsPerState = 1;
                        for (; bitsPerState <= 64; bitsPerState++) {
                            int statesPerLong = 64 / bitsPerState;
                            int dataSize = MathHelper.ceil(4096 / (double) statesPerLong);

                            if (dataSize == blockStates.length) {
                                break;
                            }
                            if (dataSize > blockStates.length) {
                                LOGGER.error("Lithium chunk section palette fixer: Could not fix size mismatch. There exists no number of bits per blockstate matching the data's length! Skipping section and rest of the chunk!");
                                return;
                            }
                        }
                        //resize the palette to match the data array
                        int maxPaletteSize = 1 << bitsPerState;
                        if (maxPaletteSize < palette.size()) {
                            // the palette is too large, this issue isn't caused by previous lithium versions
                            LOGGER.info("Lithium chunk section palette fixer: Palette is too large, not fixing this issue!");
                        }
                        if (bitsPerState > Math.max(4, MathHelper.log2DeBruijn(palette.size()))) {
                            // the palette is too small, duplicate the first entry
                            do {
                                palette.add(palette.get(0));
                                editedSection = true;
                            }
                            while (bitsPerState > Math.max(4, MathHelper.log2DeBruijn(palette.size())));

                            LOGGER.info("Lithium chunk section palette fixer: Palette was too small, fixed by adding more entries!");
                        }
                    }
                    if (editedSection) {
                        this.lithiumEditedCurrentChunk.set(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
            LOGGER.error("Lithium chunk section palette fixer: Failed while fixing chunks! Updating to 1.18 will still not work correctly.");
        }
    }

    @ModifyVariable(
            method = "updateWorld()V",
            ordinal = 2,
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/updater/WorldUpdater;eraseCache:Z",
                    shift = At.Shift.BEFORE
            )
    )
    private boolean setLithiumChunkModified(boolean chunkModified) {
        boolean chunkModifiedByLithium = this.lithiumEditedCurrentChunk.get();
        this.lithiumEditedCurrentChunk.remove();

        return chunkModified || chunkModifiedByLithium;
    }
}
