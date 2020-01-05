package me.jellysquid.mods.lithium.common.config.parser.types;

import com.moandjiezana.toml.Toml;

import java.lang.reflect.Field;

public class BooleanSerializer implements OptionSerializer {
    @Override
    public void read(Toml toml, String key, Field field, Object inst) throws IllegalAccessException {
        Boolean value = toml.getBoolean(key);

        if (value == null) {
            return;
        }

        field.setBoolean(inst, value);
    }
}
