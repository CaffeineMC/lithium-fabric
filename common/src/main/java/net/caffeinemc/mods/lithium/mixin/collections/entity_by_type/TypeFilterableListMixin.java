package net.caffeinemc.mods.lithium.mixin.collections.entity_by_type;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ClassInstanceMultiMap;

@Mixin(ClassInstanceMultiMap.class)
public class TypeFilterableListMixin {

    @Mutable
    @Shadow
    @Final
    private Map<Class<?>, List<?>> byClass;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Class<?> elementType, CallbackInfo ci) {
        this.byClass = new Reference2ReferenceOpenHashMap<>(this.byClass);
    }
}
