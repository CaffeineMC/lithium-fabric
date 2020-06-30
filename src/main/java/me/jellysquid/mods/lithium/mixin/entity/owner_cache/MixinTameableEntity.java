package me.jellysquid.mods.lithium.mixin.entity.owner_cache;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.UUID;

/**
 * This mixin patch caches the tameable entity owner to reduce the number of DataTracker calls and
 * speed up some mob patchfindings operations.
 *
 * @author Maity
 */
@Mixin(TameableEntity.class)
public abstract class MixinTameableEntity extends AnimalEntity {
    @Shadow
    @Final
    private static TrackedData<Optional<UUID>> OWNER_UUID;

    private UUID ownerUuid = null;

    private MixinTameableEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @reason Reduce calls to DataTracker using owner cache
     * @author Maity
     */
    @Overwrite
    public UUID getOwnerUuid() {
        if (this.ownerUuid == null) {
            this.ownerUuid = this.dataTracker.get(OWNER_UUID).orElse(null);
        }

        return this.ownerUuid;
    }

    /**
     * @reason Reduce calls to DataTracker using owner cache
     * @author Maity
     */
    @Overwrite
    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(this.ownerUuid));
    }
}
