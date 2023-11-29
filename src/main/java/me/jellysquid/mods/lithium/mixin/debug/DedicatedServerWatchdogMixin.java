package me.jellysquid.mods.lithium.mixin.debug;

import me.jellysquid.mods.lithium.common.LithiumDebugInfo;
import net.minecraft.server.dedicated.DedicatedServerWatchdog;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

@Mixin(DedicatedServerWatchdog.class)
public class DedicatedServerWatchdogMixin {
    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/crash/CrashReport;addElement(Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReportSection;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ), locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void addLithiumDetails(CallbackInfo ci, long l, long m, long n, ThreadMXBean threadMXBean, ThreadInfo[] threadInfos, StringBuilder stringBuilder, Error error, CrashReport crashReport) {
        CrashReportSection lithiumDebugInfo = crashReport.addElement("Lithium Debug Info");
        lithiumDebugInfo.add("Lithium Text", "Lithium is installed on this server.");
        try {
            if (LithiumDebugInfo.blockCollisionDebugInfo != null) {
                lithiumDebugInfo.add("Block collision time", LithiumDebugInfo.getElapsedTime());
                lithiumDebugInfo.add("Block collision details", LithiumDebugInfo.getBlockCollisionDebugInfo());
            } else {
                lithiumDebugInfo.add("Block collision details", "Block collision details are not available.");
            }
        } catch (Throwable t) {
            lithiumDebugInfo.add("Error when creating lithium debug details:", t);
        }
    }
}
