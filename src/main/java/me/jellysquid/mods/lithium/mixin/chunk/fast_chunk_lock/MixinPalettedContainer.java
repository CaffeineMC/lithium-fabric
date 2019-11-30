package me.jellysquid.mods.lithium.mixin.chunk.fast_chunk_lock;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Mixin(PalettedContainer.class)
public class MixinPalettedContainer<T> {
    @Shadow
    @Final
    private ReentrantLock writeLock;

    /**
     * Try-acquire the lock normally. It should be faster. We also move the crash report generation logic
     * to a new thread to encourage the JVM to inline this method.
     *
     * @author JellySquid
     */
    @Overwrite
    public void lock() {
        if (!this.writeLock.tryLock()) {
            this.crash();
        }
    }


    private void crash() {
        String stacktrace = Thread.getAllStackTraces().keySet().stream().filter(Objects::nonNull).map((thread_1) -> {
            return thread_1.getName() + ": \n\tat " + Arrays.stream(thread_1.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
        }).collect(Collectors.joining("\n"));

        CrashReport report = new CrashReport("Writing into PalettedContainer from multiple threads", new IllegalStateException());
        CrashReportSection section = report.addElement("Thread dumps");
        section.add("Thread dumps", stacktrace);

        throw new CrashException(report);
    }
}
