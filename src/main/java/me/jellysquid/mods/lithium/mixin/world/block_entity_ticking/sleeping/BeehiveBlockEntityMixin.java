package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.util.collections.ListeningList;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin extends BlockEntity {

    @Mutable
    @Shadow
    @Final
    private List<?> bees;

    @Unique
    private boolean isTicking;
    @Unique
    private boolean doInit;

    public BeehiveBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void createInhabitantListener(CallbackInfo ci) {
        this.bees = new ListeningList<>(this.bees, this::checkSleepState);
        this.doInit = true;
        this.isTicking = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void firstTick(CallbackInfo ci) {
        if (this.doInit) {
            this.doInit = false;
            this.checkSleepState();
        }
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void wakeUpAfterFromTag(CallbackInfo ci) {
        this.checkSleepState();
    }

    private void checkSleepState() {
        if (this.world != null ) {
            if (this.world.isClient()) {
                //in the initializer we can't know whether we are in a client world
                this.bees = new ArrayList<>(this.bees);
                ((BlockEntitySleepTracker)this.world).setAwake(this, this.isTicking = false);
                return;
            }
            if ((this.bees.size() == 0) == this.isTicking) {
                this.isTicking = !this.isTicking;
                ((BlockEntitySleepTracker)this.world).setAwake(this, this.isTicking);
            }
        }

    }
}
