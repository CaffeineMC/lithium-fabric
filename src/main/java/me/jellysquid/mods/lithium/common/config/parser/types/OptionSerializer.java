package me.jellysquid.mods.lithium.common.config.parser.types;

import com.moandjiezana.toml.Toml;

import java.lang.reflect.Field;

public interface OptionSerializer {
    void read(Toml toml, String key, Field field, Object inst) throws IllegalAccessException;
}
