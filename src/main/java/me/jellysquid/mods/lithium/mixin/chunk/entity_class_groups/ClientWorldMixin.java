package me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups;

import me.jellysquid.mods.lithium.common.client.ClientWorldAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientLevel.class)
public class ClientWorldMixin implements ClientWorldAccessor {
    @Shadow
    @Final
    private TransientEntitySectionManager<Entity> entityStorage;

    @Override
    public TransientEntitySectionManager<Entity> lithium$getEntityManager() {
        return this.entityStorage;
    }
}

