package me.jellysquid.mods.lithium.asm.consumers;

import me.jellysquid.mods.lithium.asm.ASMUtil;
import me.jellysquid.mods.lithium.asm.FieldRef;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class FieldRemapper implements Consumer<ClassNode> {
    private final HashMap<FieldRef, FieldRef> mappings;

    public FieldRemapper(FieldMapping... mappings) {
        this.mappings = new HashMap<>();

        for (FieldMapping pair : mappings) {
            this.mappings.put(pair.left, pair.right);
        }
    }

    @Override
    public void accept(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            int count = this.patchMethod(methodNode);

            if (count > 0) {
                ASMUtil.LOGGER.debug("Remapped {} field accesses in {}#{}", count, classNode.name, methodNode.name);
            }
        }
    }

    private int patchMethod(MethodNode method) {
        int count = 0;

        for (AbstractInsnNode insnNode : method.instructions) {
            if (!(insnNode instanceof FieldInsnNode)) {
                continue;
            }

            FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;

            FieldRef ref = new FieldRef(fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc);
            FieldRef mapping = this.mappings.get(ref);

            if (mapping == null) {
                continue;
            }

            fieldInsnNode.name = mapping.name;
            fieldInsnNode.desc = mapping.desc;
            fieldInsnNode.owner = mapping.owner;

            count++;
        }

        return count;
    }

    public static class FieldMapping {
        public final FieldRef left, right;

        public FieldMapping(FieldRef a, FieldRef b) {
            if (!Objects.equals(a.desc, b.desc)) {
                throw new IllegalArgumentException(String.format("Mismatched type descriptions (%s != %s)", a.desc, b.desc));
            }

            this.left = a;
            this.right = b;
        }
    }
}
