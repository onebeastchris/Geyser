/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.gametest.interaction.world;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection helpers to create instances without running constructors and to copy their state.
 */
public final class ReflectionUtils {
    private static final Map<Class<?>, Field[]> INSTANCE_FIELDS = new ConcurrentHashMap<>();
    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Creates an instance of the class without running any constructor. All fields are zero/null.
     */
    public static <T> T allocate(Class<T> clazz) {
        try {
            return clazz.cast(UNSAFE.allocateInstance(clazz));
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to allocate " + clazz, e);
        }
    }

    /**
     * Copies (shallowly) all instance fields declared by {@code upTo} and its superclasses from {@code source} to {@code target}.
     */
    public static void copyFields(Class<?> upTo, Object source, Object target) {
        try {
            for (Field field : instanceFields(upTo)) {
                field.set(target, field.get(source));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to copy fields of " + upTo, e);
        }
    }

    /**
     * @return the first instance field declared by the class or its superclasses that has exactly the given type
     */
    public static @Nullable Field findField(Class<?> clazz, Class<?> type) {
        for (Field field : instanceFields(clazz)) {
            if (field.getType() == type) {
                return field;
            }
        }
        return null;
    }

    /**
     * @return the instance field with the given name and type declared by the class or its superclasses
     */
    public static @Nullable Field findField(Class<?> clazz, String name, Class<?> type) {
        for (Field field : instanceFields(clazz)) {
            if (field.getType() == type && field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static void set(Field field, Object target, @Nullable Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set " + field, e);
        }
    }

    public static @Nullable Object get(Field field, @Nullable Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to get " + field, e);
        }
    }

    /**
     * @return all static fields of the class whose type is assignable to the given type, made accessible
     */
    public static List<Field> staticFields(Class<?> clazz, Class<?> type) {
        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                result.add(field);
            }
        }
        return result;
    }

    private static Field[] instanceFields(Class<?> clazz) {
        return INSTANCE_FIELDS.computeIfAbsent(clazz, $ -> {
            List<Field> fields = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        fields.add(field);
                    }
                }
            }
            return fields.toArray(Field[]::new);
        });
    }

    private ReflectionUtils() {
    }
}
