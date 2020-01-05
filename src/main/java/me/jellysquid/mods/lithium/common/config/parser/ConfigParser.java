package me.jellysquid.mods.lithium.common.config.parser;

import com.moandjiezana.toml.Toml;
import me.jellysquid.mods.lithium.common.config.annotations.Category;
import me.jellysquid.mods.lithium.common.config.annotations.Option;
import me.jellysquid.mods.lithium.common.config.parser.types.BooleanSerializer;
import me.jellysquid.mods.lithium.common.config.parser.types.OptionSerializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ConfigParser {
    private static final HashMap<Class<?>, OptionSerializer> optionSerializers = new HashMap<>();

    static {
        optionSerializers.put(boolean.class, new BooleanSerializer());
    }

    public static <T> T deserialize(Class<T> type, File file) throws ParseException {
        return deserialize(type, new Toml().read(file));
    }

    public static <T> T deserialize(Class<T> type, Toml toml) throws ParseException {
        T obj = create(type);
        deserializeInto(obj, toml);

        return obj;
    }

    private static <T> T create(Class<T> clazz) throws ParseException {
        Constructor<T> constructor;

        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ParseException("The config type is missing a no-arg constructor");
        }

        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ParseException("The config type could not be instantiated", e);
        }
    }

    private static void deserializeInto(Object config, Toml toml) throws ParseException {
        Class<?> configType = config.getClass();
        Field[] fields = configType.getDeclaredFields();

        for (Field field : fields) {
            Category categoryMarker = field.getType().getAnnotation(Category.class);

            if (categoryMarker == null) {
                continue;
            }

            Toml categoryToml = toml.getTable(categoryMarker.value());

            if (categoryToml == null) {
                continue;
            }

            Object categoryInst;

            try {
                categoryInst = field.get(config);
            } catch (IllegalAccessException e) {
                throw new ParseException("Could not retrieve category object instance");
            }

            deserializeCategory(field, categoryToml, categoryInst);
        }
    }

    private static void deserializeCategory(Field categoryField, Toml toml, Object inst) throws ParseException {
        Field[] categoryOptionFields = categoryField.getType().getDeclaredFields();

        for (Field optionField : categoryOptionFields) {
            Option optionMarker = optionField.getAnnotation(Option.class);

            if (optionMarker == null) {
                continue;
            }

            deserializeOption(toml, optionField, inst, optionMarker.value());
        }
    }

    private static void deserializeOption(Toml toml, Field field, Object inst, String name) throws ParseException {
        OptionSerializer serializer = optionSerializers.get(field.getType());

        if (serializer == null) {
            throw new ParseException("Cannot de-serialize type: " + field.getType().getName());
        }

        try {
            serializer.read(toml, name, field, inst);
        } catch (IllegalAccessException e) {
            throw new ParseException("Could not mutate field", e);
        }
    }

    public static class ParseException extends IOException {
        public ParseException(String msg) {
            super(msg);
        }

        public ParseException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
