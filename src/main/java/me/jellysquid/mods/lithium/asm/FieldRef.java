package me.jellysquid.mods.lithium.asm;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.util.Objects;

public class FieldRef {
    public final String owner, name, desc;

    public FieldRef(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    /**
     * Creates a {@link FieldRef} using intermediary names which will be mapped to the appropriate field in the current
     * environment. The owner's name after remapping will use path notation instead of dot notation.
     *
     * @param owner The intermediary name of the field's owning class
     * @param name The intermediary name of the field
     * @param desc The type descriptor of the field
     */
    public static FieldRef intermediary(String owner, String name, String desc) {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String className = remapper.mapClassName("intermediary", owner);
        String fieldName = remapper.mapFieldName("intermediary", owner, name, desc);

        return new FieldRef(ASMUtil.getPathNotation(className), fieldName, desc);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other != null && this.getClass() == other.getClass()) {
            FieldRef ref = (FieldRef) other;

            return Objects.equals(this.name, ref.name) && Objects.equals(this.desc, ref.desc) && Objects.equals(this.owner, ref.owner);
        }

        return false;

    }

    @Override
    public int hashCode() {
        int h = this.name.hashCode();
        h = 31 * h + this.desc.hashCode();
        h = 31 * h + this.owner.hashCode();

        return h;
    }

    @Override
    public String toString() {
        return String.format("FieldRef{owner='%s', name='%s', desc='%s'}", owner, name, desc);
    }
}
