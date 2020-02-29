package me.jellysquid.mods.lithium.asm.consumers;

import me.jellysquid.mods.lithium.asm.ASMUtil;
import me.jellysquid.mods.lithium.asm.FieldRef;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modifies the access flags of the specified fields. This is used in lieu of access-transformers as it allows us
 * to conditionally apply the transformation depending on whether or not a patch set is enabled.
 */
public class FieldAccessTransformer implements Consumer<ClassNode> {
    private final List<FieldRef> fields;

    private final int access;

    public FieldAccessTransformer(int access, FieldRef... fields) {
        this.access = access;
        this.fields = Arrays.asList(fields);
    }

    @Override
    public void accept(ClassNode classNode) {
        for (FieldNode node : ASMUtil.matchFields(classNode, this.fields)) {
            node.access = this.access;
        }

        ASMUtil.LOGGER.debug("Transformed access flags of {} fields in {}", this.fields.size(), classNode.name);
    }
}
