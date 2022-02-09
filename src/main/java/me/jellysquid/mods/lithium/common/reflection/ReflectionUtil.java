package me.jellysquid.mods.lithium.common.reflection;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReflectionUtil {
    public static boolean hasMethodOverride(Class<?> clazz, Class<?> superclass, boolean fallbackResult, String methodName, Class<?>... methodArgs) {
        while (clazz != null && clazz != superclass && superclass.isAssignableFrom(clazz)) {
            try {
                clazz.getDeclaredMethod(methodName, methodArgs);
                return true;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (NoClassDefFoundError error) {
                Logger logger = LogManager.getLogger("Lithium Class Analysis");
                logger.warn("Lithium Class Analysis Error: Class " + clazz.getName() + " cannot be analysed, because" +
                        " getting declared methods crashes with NoClassDefFoundError: " + error.getMessage() +
                        ". This is usually caused by modded" +
                        " entities declaring methods that have a return type or parameter type that is annotated" +
                        " with @Environment(value=EnvType.CLIENT). Loading the type is not possible, because" +
                        " it only exists in the CLIENT environment. The recommended fix is to annotate the method with" +
                        " this argument or return type with the same annotation." +
                        " Lithium handles this error by assuming the class cannot be included in some optimizations.");
                return fallbackResult;
            } catch (Throwable e) {
                final String crashedClass = clazz.getName();
                CrashReport crashReport = CrashReport.create(e, "Lithium Class Analysis");
                CrashReportSection crashReportSection = crashReport.addElement(e.getClass().toString() + " when getting declared methods.");
                crashReportSection.add("Analyzed class", crashedClass);
                crashReportSection.add("Analyzed method name", methodName);
                crashReportSection.add("Analyzed method args", methodArgs);

                throw new CrashException(crashReport);
            }
        }
        return false;
    }
}
