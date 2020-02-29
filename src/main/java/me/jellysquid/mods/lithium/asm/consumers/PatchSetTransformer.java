package me.jellysquid.mods.lithium.asm.consumers;

import me.jellysquid.mods.lithium.asm.ASMUtil;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.function.Consumer;

public class PatchSetTransformer implements Consumer<ClassNode> {
    private final String patchName;
    private final List<Consumer<ClassNode>> transformers;

    public PatchSetTransformer(String patchName, List<Consumer<ClassNode>> transformers) {
        this.patchName = patchName;
        this.transformers = transformers;
    }

    @Override
    public void accept(ClassNode classNode) {
        ASMUtil.LOGGER.info("Transforming class {} using patch set '{}'", classNode.name, this.patchName);

        for (Consumer<ClassNode> transformer : this.transformers) {
            transformer.accept(classNode);
        }
    }
}
