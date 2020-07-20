package me.jellysquid.mods.lithium.mixin.alloc.chunk_ticking;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(ChunkTicketManager.class)
public abstract class ChunkTicketManagerMixin {
    @Shadow
    private long age;

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;

    @Shadow
    @Final
    private ChunkTicketManager.TicketDistanceLevelPropagator distanceFromTicketTracker;

    @Shadow
    private static int getLevel(SortedArraySet<ChunkTicket<?>> sortedArraySet) {
        throw new UnsupportedOperationException();
    }

    /**
     * @reason Remove lambda allocation in every iteration
     * @author JellySquid
     */
    @Overwrite
    public void purge() {
        ++this.age;

        ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>>> iterator =
                this.ticketsByPosition.long2ObjectEntrySet().fastIterator();
        Predicate<ChunkTicket<?>> predicate = (chunkTicket) -> chunkTicket.isExpired(this.age);

        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>> entry = iterator.next();
            SortedArraySet<ChunkTicket<?>> value = entry.getValue();

            if (value.removeIf(predicate)) {
                this.distanceFromTicketTracker.updateLevel(entry.getLongKey(), getLevel(entry.getValue()), false);
            }

            if (value.isEmpty()) {
                iterator.remove();
            }
        }

    }
}
