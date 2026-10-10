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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates a concrete subclass of an (abstract) base class at runtime, without knowing the base class' exact method
 * signatures at compile time. This keeps the simulated client level independent of the many abstract methods of
 * {@code Level}, which change between Minecraft versions.
 * <p>
 * The generated class has no constructor; instances are created without running any constructor (see
 * {@link ReflectionUtils#allocate(Class)}) and get their state copied from an existing instance of the base class.
 * For every overridable method of the base class, in this order:
 * <ol>
 *     <li>if the behaviour class declares a public method with the same name and parameter types (and a compatible return
 *     type), the generated method calls it on the {@value #BEHAVIOUR_FIELD} field;</li>
 *     <li>if the method name is in the no-op set, the generated method does nothing and returns a default value
 *     ({@code false}, {@code 0} or {@code null});</li>
 *     <li>if the method is public and abstract (or its name is in the forward set), it is forwarded to the
 *     {@value #DELEGATE_FIELD} field;</li>
 *     <li>if the method is abstract (and protected), it returns a default value;</li>
 *     <li>otherwise the base class implementation is inherited.</li>
 * </ol>
 */
public final class ProxyClassGenerator {
    public static final String BEHAVIOUR_FIELD = "behaviour";
    public static final String DELEGATE_FIELD = "delegate";

    public enum Strategy {
        BEHAVIOUR,
        NO_OP,
        FORWARD
    }

    /**
     * @param method the base class method that is overridden
     * @param behaviourMethod the behaviour method that is called for {@link Strategy#BEHAVIOUR}
     */
    public record PlannedMethod(Method method, Strategy strategy, @Nullable Method behaviourMethod) {

        @Override
        public String toString() {
            return strategy + " " + method.getDeclaringClass().getSimpleName() + "#" + method.getName() + Type.getMethodDescriptor(method);
        }
    }

    /**
     * Decides which methods to override and how; see the class documentation.
     */
    public static List<PlannedMethod> plan(Class<?> base, Class<?> behaviour, Set<String> noOpNames, Set<String> forwardNames) {
        Map<String, Method> behaviourMethods = new HashMap<>();
        for (Method method : behaviour.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !method.isSynthetic()) {
                behaviourMethods.put(method.getName() + parameterDescriptor(method), method);
            }
        }

        // Most specific declaration per name + descriptor in the superclass chain
        Map<String, Method> classMethods = new LinkedHashMap<>();
        for (Class<?> clazz = base; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                classMethods.putIfAbsent(method.getName() + Type.getMethodDescriptor(method), method);
            }
        }

        // Interface methods not declared anywhere in the superclass chain
        Map<String, List<Method>> interfaceMethods = new LinkedHashMap<>();
        for (Class<?> iface : allInterfaces(base)) {
            for (Method method : iface.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers) || method.isSynthetic()) {
                    continue;
                }
                String key = method.getName() + Type.getMethodDescriptor(method);
                if (!classMethods.containsKey(key)) {
                    interfaceMethods.computeIfAbsent(key, $ -> new ArrayList<>()).add(method);
                }
            }
        }

        List<PlannedMethod> plan = new ArrayList<>();
        for (Method method : classMethods.values()) {
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers) || Modifier.isFinal(modifiers)
                || method.isSynthetic() || method.isBridge()
                || !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))) {
                continue;
            }
            PlannedMethod planned = decide(method, Modifier.isAbstract(modifiers), behaviourMethods, noOpNames, forwardNames);
            if (planned != null) {
                plan.add(planned);
            }
        }
        for (List<Method> methods : interfaceMethods.values()) {
            boolean isAbstract = methods.stream().allMatch(method -> Modifier.isAbstract(method.getModifiers()));
            PlannedMethod planned = decide(methods.getFirst(), isAbstract, behaviourMethods, noOpNames, forwardNames);
            if (planned != null) {
                plan.add(planned);
            }
        }
        return plan;
    }

    private static @Nullable PlannedMethod decide(Method method, boolean isAbstract, Map<String, Method> behaviourMethods,
                                                  Set<String> noOpNames, Set<String> forwardNames) {
        Method behaviourMethod = behaviourMethods.get(method.getName() + parameterDescriptor(method));
        if (behaviourMethod != null && isReturnCompatible(method.getReturnType(), behaviourMethod.getReturnType())) {
            return new PlannedMethod(method, Strategy.BEHAVIOUR, behaviourMethod);
        }
        if (noOpNames.contains(method.getName())) {
            return new PlannedMethod(method, Strategy.NO_OP, null);
        }
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        if (isPublic && (isAbstract || forwardNames.contains(method.getName()))) {
            return new PlannedMethod(method, Strategy.FORWARD, null);
        }
        if (isAbstract) {
            return new PlannedMethod(method, Strategy.NO_OP, null);
        }
        return null;
    }

    private static boolean isReturnCompatible(Class<?> expected, Class<?> actual) {
        if (expected.isPrimitive() || actual.isPrimitive()) {
            return expected == actual;
        }
        return expected.isAssignableFrom(actual);
    }

    private static String parameterDescriptor(Method method) {
        String descriptor = Type.getMethodDescriptor(method);
        return descriptor.substring(0, descriptor.indexOf(')') + 1);
    }

    private static Set<Class<?>> allInterfaces(Class<?> base) {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> clazz = base; clazz != null; clazz = clazz.getSuperclass()) {
            collectInterfaces(clazz, result);
        }
        return result;
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> result) {
        for (Class<?> iface : clazz.getInterfaces()) {
            if (result.add(iface)) {
                collectInterfaces(iface, result);
            }
        }
    }

    /**
     * Generates the class file of the subclass. The class has two public fields: {@value #BEHAVIOUR_FIELD} (of the
     * behaviour type) and {@value #DELEGATE_FIELD} (of the base type).
     */
    public static byte[] generate(String className, Class<?> base, Class<?> behaviour, List<PlannedMethod> plan) {
        String internalName = className.replace('.', '/');
        String baseName = Type.getInternalName(base);
        String behaviourName = Type.getInternalName(behaviour);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC,
            internalName, null, baseName, null);
        writer.visitField(Opcodes.ACC_PUBLIC, BEHAVIOUR_FIELD, Type.getDescriptor(behaviour), null, null).visitEnd();
        writer.visitField(Opcodes.ACC_PUBLIC, DELEGATE_FIELD, Type.getDescriptor(base), null, null).visitEnd();

        for (PlannedMethod planned : plan) {
            Method method = planned.method();
            String descriptor = Type.getMethodDescriptor(method);
            int access = Modifier.isPublic(method.getModifiers()) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PROTECTED;
            MethodVisitor visitor = writer.visitMethod(access, method.getName(), descriptor, null, null);
            visitor.visitCode();
            Type returnType = Type.getReturnType(method);
            switch (planned.strategy()) {
                case BEHAVIOUR -> {
                    Method behaviourMethod = planned.behaviourMethod();
                    assert behaviourMethod != null;
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitFieldInsn(Opcodes.GETFIELD, internalName, BEHAVIOUR_FIELD, Type.getDescriptor(behaviour));
                    loadArguments(visitor, method);
                    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, behaviourName, behaviourMethod.getName(),
                        Type.getMethodDescriptor(behaviourMethod), false);
                    returnValue(visitor, returnType);
                }
                case FORWARD -> {
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitFieldInsn(Opcodes.GETFIELD, internalName, DELEGATE_FIELD, Type.getDescriptor(base));
                    loadArguments(visitor, method);
                    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, baseName, method.getName(), descriptor, false);
                    returnValue(visitor, returnType);
                }
                case NO_OP -> {
                    pushDefault(visitor, returnType);
                    returnValue(visitor, returnType);
                }
            }
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
        }
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void loadArguments(MethodVisitor visitor, Method method) {
        int slot = 1;
        for (Type argument : Type.getArgumentTypes(method)) {
            visitor.visitVarInsn(argument.getOpcode(Opcodes.ILOAD), slot);
            slot += argument.getSize();
        }
    }

    private static void pushDefault(MethodVisitor visitor, Type type) {
        switch (type.getSort()) {
            case Type.VOID -> {
            }
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> visitor.visitInsn(Opcodes.ICONST_0);
            case Type.FLOAT -> visitor.visitInsn(Opcodes.FCONST_0);
            case Type.LONG -> visitor.visitInsn(Opcodes.LCONST_0);
            case Type.DOUBLE -> visitor.visitInsn(Opcodes.DCONST_0);
            default -> visitor.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    private static void returnValue(MethodVisitor visitor, Type type) {
        if (type.getSort() == Type.VOID) {
            visitor.visitInsn(Opcodes.RETURN);
        } else {
            visitor.visitInsn(type.getOpcode(Opcodes.IRETURN));
        }
    }

    /**
     * Defines generated classes as children of the class loader that loaded the base class, so they can see both
     * the base class and the behaviour class.
     */
    static final class GeneratedClassLoader extends ClassLoader {

        GeneratedClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    private ProxyClassGenerator() {
    }
}
