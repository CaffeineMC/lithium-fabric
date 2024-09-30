package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.NavigatingEntity;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerLevel.EntityCallbacks.class)
public class ServerEntityHandlerMixin {

    private ServerLevel outer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inj(ServerLevel outer, CallbackInfo ci) {
        this.outer = outer;
    }

    @Redirect(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private boolean startListeningOnEntityLoad(Set<Mob> set, Object mobEntityObj) {
        Mob mobEntity = (Mob) mobEntityObj;
        PathNavigation navigation = mobEntity.getNavigation();
        ((NavigatingEntity) mobEntity).lithium$setRegisteredToWorld(navigation);
        if (navigation.getPath() != null) {
            ((ServerWorldExtended) this.outer).lithium$setNavigationActive(mobEntity);
        }
        return set.add(mobEntity);
    }

    @Redirect(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private boolean stopListeningOnEntityUnload(Set<Mob> set, Object mobEntityObj) {
        Mob mobEntity = (Mob) mobEntityObj;
        NavigatingEntity navigatingEntity = (NavigatingEntity) mobEntity;
        if (navigatingEntity.lithium$isRegisteredToWorld()) {
            PathNavigation registeredNavigation = navigatingEntity.lithium$getRegisteredNavigation();
            if (registeredNavigation.getPath() != null) {
                ((ServerWorldExtended) this.outer).lithium$setNavigationInactive(mobEntity);
            }
            navigatingEntity.lithium$setRegisteredToWorld(null);
        }
        return set.remove(mobEntity);
    }

}
