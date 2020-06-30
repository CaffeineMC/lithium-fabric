/*
 * Turtle Mod
 * Copyright (C) 2020 Maity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * speed up some mob patchfinding operations.
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
