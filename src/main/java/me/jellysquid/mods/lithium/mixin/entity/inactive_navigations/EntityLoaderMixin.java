package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.EntityNavigationExtended;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerWorld.EntityLoader.class)
public class EntityLoaderMixin {

    private ServerWorld outer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inj(ServerWorld outer, CallbackInfo ci) {
        this.outer = outer;
    }

    @Redirect(method = "onLoadEntity", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private boolean startListeningOnEntityLoad(Set<MobEntity> set, Object mobEntityObj) {
        MobEntity mobEntity = (MobEntity) mobEntityObj;
        EntityNavigation navigation = mobEntity.getNavigation();
        ((EntityNavigationExtended) navigation).setRegisteredToWorld(true);
        if (navigation.getCurrentPath() != null) {
            ((ServerWorldExtended) this.outer).setNavigationActive(mobEntity);
        }
        return set.add(mobEntity);
    }

    @Redirect(method = "onUnloadEntity", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private boolean stopListeningOnEntityUnload(Set<MobEntity> set, Object mobEntityObj) {
        MobEntity mobEntity = (MobEntity) mobEntityObj;
        EntityNavigation navigation = mobEntity.getNavigation();
        ((EntityNavigationExtended) navigation).setRegisteredToWorld(false);
        if (navigation.getCurrentPath() != null) {
            ((ServerWorldExtended) this.outer).setNavigationInactive(mobEntity);
        }
        return set.remove(mobEntity);
    }

}
