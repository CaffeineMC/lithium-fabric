package me.jellysquid.mods.lithium.common.reflection;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class ReflectionUtil {
    public static boolean isMethodFromSuperclassOverwritten(Class<?> clazz, Class<?> superclass, String methodName, Class<?>... methodArgs) {
        while (clazz != null && clazz != superclass && superclass.isAssignableFrom(clazz)) {
            try {
                clazz.getDeclaredMethod(methodName, methodArgs);
                return true;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
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
