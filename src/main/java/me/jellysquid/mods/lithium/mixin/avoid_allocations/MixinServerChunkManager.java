package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {
    private static final EntityCategory[] ENTITY_CATEGORIES = EntityCategory.values();

    /**
     * @reason Avoid cloning enum values
     */
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityCategory;values()[Lnet/minecraft/entity/EntityCategory;"))
    private EntityCategory[] redirectEntityCategoryValues() {
        return ENTITY_CATEGORIES;
    }
}
