package me.jellysquid.mods.lithium.asm.consumers;

import me.jellysquid.mods.lithium.asm.ASMUtil;
import me.jellysquid.mods.lithium.asm.FieldRef;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Removes matching fields from a class.
 */
public class FieldCleaner implements Consumer<ClassNode> {
    private final List<FieldRef> fields;

    public FieldCleaner(FieldRef... fields) {
        this.fields = Arrays.asList(fields);
    }

    @Override
    public void accept(ClassNode classNode) {
        for (FieldNode fieldNode : ASMUtil.matchFields(classNode, this.fields)) {
            classNode.fields.remove(fieldNode);
        }

        ASMUtil.LOGGER.debug("Removed {} fields from {}", this.fields.size(), classNode.name);
    }
}
