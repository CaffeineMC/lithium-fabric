package me.jellysquid.mods.lithium.asm;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.Function;

public class ASMUtil {
    public static final Logger LOGGER = LogManager.getLogger();

    public static Collection<FieldNode> matchFields(ClassNode classNode, Collection<FieldRef> refs) {
        return matchRefs(classNode.fields, refs,
                (method) -> new FieldRef(classNode.name, method.name, method.desc));
    }

    public static Collection<MethodNode> matchMethods(ClassNode classNode, Collection<MethodRef> refs) {
        return matchRefs(classNode.methods, refs,
                (method) -> new MethodRef(method.name, method.desc));
    }

    /**
     * Converts a dot-delimited intermediary name to a forward slash-delimited path notation.
     */
    public static String getPathNotation(String intermediary) {
        return intermediary.replace('.', '/');
    }

    private static <K, T> Collection<K> matchRefs(Collection<K> nodes, Collection<T> refs, Function<K, T> nameFunction) {
        final HashSet<T> missing = new HashSet<>(refs);
        final List<K> matched = new ArrayList<>();

        Iterator<K> nodeIterator = nodes.iterator();

        // Early exit if we have found everything
        while (nodeIterator.hasNext() && !missing.isEmpty()) {
            K node = nodeIterator.next();
            T ref = nameFunction.apply(node);

            if (missing.remove(ref)) {
                matched.add(node);
            }
        }

        if (!missing.isEmpty()) {
            throw new RuntimeException("Could not locate: " + ArrayUtils.toString(missing));
        }

        return matched;
    }
}
