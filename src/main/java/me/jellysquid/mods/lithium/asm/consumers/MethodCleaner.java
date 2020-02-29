package me.jellysquid.mods.lithium.asm.consumers;

import me.jellysquid.mods.lithium.asm.ASMUtil;
import me.jellysquid.mods.lithium.asm.MethodRef;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Removes matching methods from a class.
 */
public class MethodCleaner implements Consumer<ClassNode> {
    private final List<MethodRef> methods;

    public MethodCleaner(MethodRef... methods) {
        this.methods = Arrays.asList(methods);
    }

    @Override
    public void accept(ClassNode classNode) {
        for (MethodNode methodNode : ASMUtil.matchMethods(classNode, this.methods)) {
            classNode.methods.remove(methodNode);
        }

        ASMUtil.LOGGER.debug("Removed {} methods from {}", this.methods.size(), classNode.name);
    }
}
