package me.jellysquid.mods.lithium.asm.patches;

import com.chocohead.mm.api.ClassTinkerers;
import me.jellysquid.mods.lithium.asm.FieldRef;
import me.jellysquid.mods.lithium.asm.MethodRef;
import me.jellysquid.mods.lithium.asm.consumers.*;
import me.jellysquid.mods.lithium.asm.consumers.FieldRemapper.FieldMapping;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;

/**
 * The block position vector types in Minecraft (more formally known as BlockPos) can introduce a surprising amount of
 * overhead in many situations due to how the mutable variant is implemented.
 *
 * In most (if not all) situations, the JVM will perform inlining on virtual "getter" methods through compiling an
 * optimized and unoptimized variant of the method call. If the caller's code path does not encounter different
 * implementations of this virtual method, the method can be treated as if it were non-virtual and the optimization will
 * be applied.
 *
 * However, if multiple implementations of the method are encountered, things become more complex. If this only happens
 * rarely, the JVM will often choose to use the inlining optimization and simply fall back to the slow
 * path when the edge case is encountered. In cases where different implementations are encountered very frequently, the
 * optimization, possibly along with others, can be completely disqualified by the JVM's heuristics.
 *
 * This de-optimization occurs frequently when dealing with block positions because the mutable BlockPos maintains its
 * own non-final fields in order to work around those in Vec3i being marked as final, which requires it to also override
 * the frequently called getters in Vec3i.
 *
 * Due to this quirk, code paths which encounter both mutable and immutable block positions are often unable to perform
 * inlining, leading to much slower code. The effects of this can most notably be observed with World#isValid(BlockPos),
 * the code paths to get/set a block in the world, and the various helper methods in BlockPos.
 *
 * This patch makes the fields in Vec3i non-final and then redirects usages of the copied fields in BlockPos$Mutable to
 * those in Vec3i. With this change, the fields and overrides in BlockPos$Mutable are made redundant, and as such, they
 * are deleted afterwards by the patch to allow the JVM to more trivially inline the base methods.
 *
 * In testing, this patch provides a significant improvement to the aforementioned methods, but also to essentially
 * any code which deals with both mutable and immutable block positions. Dumping the compiled machine code for
 * these methods with the patch present reveals that the function calls have in fact been replaced with simple
 * memory fetches.
 */
public class DevirtualizeBlockPosTransformer {
    // net.minecraft.util.math.BlockPos$Mutable
    private static final String BLOCK_POS_MUTABLE_CLASS_NAME = "net.minecraft.class_2338$class_2339";

    // net.minecraft.util.math.Vec3i
    private static final String VEC3I_CLASS_NAME = "net.minecraft.class_2382";

    public static void install() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        final String blockPosTarget = remapper.mapClassName("intermediary", BLOCK_POS_MUTABLE_CLASS_NAME);
        final String vec3iTarget = remapper.mapClassName("intermediary", VEC3I_CLASS_NAME);

        // The mutable coordinate values which are re-implemented by BlockPos$Mutable
        final FieldRef blockPosX = FieldRef.intermediary(BLOCK_POS_MUTABLE_CLASS_NAME, "field_10986", "I"); // x
        final FieldRef blockPosY = FieldRef.intermediary(BLOCK_POS_MUTABLE_CLASS_NAME, "field_10985", "I"); // y
        final FieldRef blockPosZ = FieldRef.intermediary(BLOCK_POS_MUTABLE_CLASS_NAME, "field_10984", "I"); // z

        // The immutable coordinate values in Vec3i which will be replacing those in BlockPos$Mutable
        final FieldRef vecX = FieldRef.intermediary(VEC3I_CLASS_NAME, "field_11175", "I"); // x
        final FieldRef vecY = FieldRef.intermediary(VEC3I_CLASS_NAME, "field_11174", "I"); // y
        final FieldRef vecZ = FieldRef.intermediary(VEC3I_CLASS_NAME, "field_11173", "I"); // z

        ClassTinkerers.addTransformation(blockPosTarget, new PatchSetTransformer("Use mutable fields in Vec3i", Arrays.asList(
                // Remove the copied x/y/z fields in BlockPos$Mutable
                // This shouldn't break anything, as these fields could only be accessed through the getters and setters
                // we will modify later. We also shouldn't expect that other mods will reflect-hack into them as again,
                // they were exposed through getters/setters.
                new FieldCleaner(blockPosX, blockPosY, blockPosZ),

                // Remove the overrides for getX/Y/Z from Vec3i in BlockPos$Mutable
                // The INVOKE instruction used by callees will typically point to the base methods in Vec3i, meaning
                // that they will not know (or care) about whether or not these methods are being overridden. In the
                // semi-rare case that a callee directly refers to these methods, the JVM will automatically use the
                // super-class's base implementation.
                new MethodCleaner(
                        MethodRef.intermediary(VEC3I_CLASS_NAME, "method_10263", "()I"), // int getX()
                        MethodRef.intermediary(VEC3I_CLASS_NAME, "method_10264", "()I"), // int getY()
                        MethodRef.intermediary(VEC3I_CLASS_NAME, "method_10260", "()I")  // int getZ()
                ),

                // Replace all references to BlockPos$Mutable's x/y/z fields with Vec3i's x/y/z fields
                // This will also take care of initializing the fields in Vec3i despite the super constructor call
                // passing all zeroes, as the code immediately after which would originally update the cloned fields
                // will now point to those in Vec3i.
                new FieldRemapper(
                        new FieldMapping(blockPosX, vecX),
                        new FieldMapping(blockPosY, vecY),
                        new FieldMapping(blockPosZ, vecZ)
                )
        )));

        ClassTinkerers.addTransformation(vec3iTarget, new PatchSetTransformer("Modify mutability flags in Vec3i", Collections.singletonList(
                // Set the access flags for the x/y/z fields in Vec3i to PROTECTED, stripping the final flag
                new FieldAccessTransformer(Opcodes.ACC_PROTECTED, vecX, vecY, vecZ)
        )));
    }
}
