package io.github.stuff_stuffs.tbcexv3core.api.util;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.InvokerFactory;
import io.github.stuff_stuffs.tbcexv3util.api.util.TBCExUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.CheckMethodAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class EventGenerationUtil {
    private static final CustomClassLoader CLASS_LOADER = new CustomClassLoader();
    private static final Map<Pair<? extends Class<?>, ? extends Class<?>>, Function<?, ?>> CONVERTER_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<Class<?>, Object> INVOKER_FACTORY_CACHE = new Object2ObjectOpenHashMap<>();

    private static <E, V> ConvertInfo setupConverter(final Class<E> eventClass, final Class<V> viewClass) {
        if (!eventClass.isInterface() || !viewClass.isInterface()) {
            throw new RuntimeException("Event and view must be interfaces!");
        }
        final Method[] eventMethods = eventClass.getDeclaredMethods();
        final Method[] viewMethods = viewClass.getDeclaredMethods();
        Method eventMethod = null;
        int eventCount = 0;
        for (final Method method : eventMethods) {
            if ((method.getModifiers() & Opcodes.ACC_STATIC) == 0 && method.getParameterCount() > 0) {
                eventMethod = method;
                eventCount++;
            }
        }
        if (eventCount != 1) {
            throw new RuntimeException("Cannot generate converter for event class " + viewClass);
        }
        Method viewMethod = null;
        int viewCount = 0;
        for (final Method method : viewMethods) {
            if ((method.getModifiers() & Opcodes.ACC_STATIC) == 0 && method.getParameterCount() > 0) {
                viewMethod = method;
                viewCount++;
            }
        }
        if (viewCount != 1) {
            throw new RuntimeException("Cannot generate converter for view " + viewClass);
        }
        if (viewMethod.getReturnType() != void.class) {
            throw new RuntimeException("Event view must have void return!");
        }
        final Class<?>[] eventParameterTypes = eventMethod.getParameterTypes();
        final Class<?>[] viewParameterTypes = viewMethod.getParameterTypes();
        if (eventParameterTypes.length != viewParameterTypes.length) {
            throw new RuntimeException("Event view parameter count mismatch! " + eventClass);
        }
        for (int i = 0; i < eventParameterTypes.length; i++) {
            if (!viewParameterTypes[i].isAssignableFrom(eventParameterTypes[i])) {
                throw new RuntimeException("Event view parameter type mismatch at index " + i + ", " + eventClass);
            }
        }
        return new ConvertInfo(eventMethod, viewMethod);
    }

    public static <E, V> Function<V, E> generateBooleanConverter(final Class<E> eventClass, final Class<V> viewClass, final boolean defaultReturn) {
        final Pair<Class<E>, Class<V>> key = Pair.of(eventClass, viewClass);
        if (CONVERTER_CACHE.containsKey(key)) {
            return (Function<V, E>) CONVERTER_CACHE.get(key);
        }
        final Function<V, E> converter = generateConverter(eventClass, viewClass, boolean.class, defaultReturn, (visitor, s) -> {
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitFieldInsn(Opcodes.GETFIELD, s, "defaultReturn", Type.getType(boolean.class).getDescriptor());
            visitor.visitInsn(Opcodes.IRETURN);
        });
        CONVERTER_CACHE.put(key, converter);
        return converter;
    }

    public static <E, V> Function<V, E> generateDoubleConverter(final Class<E> eventClass, final Class<V> viewClass, final DoubleUnaryOperator defaultReturn, final int returnIndex) {
        final Pair<Class<E>, Class<V>> key = Pair.of(eventClass, viewClass);
        if (CONVERTER_CACHE.containsKey(key)) {
            return (Function<V, E>) CONVERTER_CACHE.get(key);
        }
        final Function<V, E> converter = generateConverter(eventClass, viewClass, DoubleUnaryOperator.class, defaultReturn, (visitor, s) -> {
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitFieldInsn(Opcodes.GETFIELD, s, "defaultReturn", Type.getType(DoubleUnaryOperator.class).getDescriptor());
            visitor.visitVarInsn(Opcodes.DLOAD, returnIndex + 1);
            visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/DoubleUnaryOperator", "applyAsDouble", "(D)D", true);
            visitor.visitInsn(Opcodes.DRETURN);
        });
        CONVERTER_CACHE.put(key, converter);
        return converter;
    }

    public static <E, V> Function<V, E> generateVoidConverter(final Class<E> eventClass, final Class<V> viewClass) {
        final Pair<Class<E>, Class<V>> key = Pair.of(eventClass, viewClass);
        if (CONVERTER_CACHE.containsKey(key)) {
            return (Function<V, E>) CONVERTER_CACHE.get(key);
        }
        final Function<V, E> converter = generateConverter(eventClass, viewClass, void.class, null, (visitor, s) -> visitor.visitInsn(Opcodes.RETURN));
        CONVERTER_CACHE.put(key, converter);
        return converter;
    }

    private static <E, V> Function<V, E> generateConverter(final Class<E> eventClass, final Class<V> viewClass, final Class<?> defaultReturnFactoryClass, final Object defaultReturnFactory, final BiConsumer<MethodVisitor, String> returnAndCast) {
        final ConvertInfo pair = setupConverter(eventClass, viewClass);
        final Type defaultReturnFactoryType = Type.getType(defaultReturnFactoryClass);
        final boolean voidReturn = defaultReturnFactoryType.equals(Type.VOID_TYPE);
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor classVisitor = TBCExUtil.DEBUG ? new CheckClassAdapter(writer, true) : writer;
        final String internalName = "io/github/stuff_stuffs/tbcexv3core/dynamic/event/" + eventClass.getSimpleName();
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", new String[]{Type.getType(eventClass).getInternalName()});
        final Type viewClassType = Type.getType(viewClass);
        classVisitor.visitField(Opcodes.ACC_PRIVATE, "delegate", viewClassType.getDescriptor(), null, null);
        if (!voidReturn) {
            classVisitor.visitField(Opcodes.ACC_PRIVATE, "defaultReturn", defaultReturnFactoryType.getDescriptor(), null, null);
        }
        MethodVisitor constructorVisitor;
        if (voidReturn) {
            constructorVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, viewClassType), null, null);
        } else {
            constructorVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, viewClassType, defaultReturnFactoryType), null, null);
        }
        if (TBCExUtil.DEBUG) {
            constructorVisitor = new CheckMethodAdapter(constructorVisitor);
        }
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "delegate", viewClassType.getDescriptor());
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        if (!voidReturn) {
            constructorVisitor.visitVarInsn(defaultReturnFactoryType.getOpcode(Opcodes.ILOAD), 2);
            constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "defaultReturn", defaultReturnFactoryType.getDescriptor());
        }
        constructorVisitor.visitInsn(Opcodes.RETURN);
        constructorVisitor.visitMaxs(3, 3);
        constructorVisitor.visitEnd();
        MethodVisitor methodVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, pair.eventMethod.getName(), Type.getMethodDescriptor(pair.eventMethod), null, null);
        if (TBCExUtil.DEBUG) {
            methodVisitor = new CheckMethodAdapter(methodVisitor);
        }
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, internalName, "delegate", viewClassType.getDescriptor());
        final int count = pair.eventMethod.getParameterCount();
        for (int i = 0; i < count; i++) {
            final Class<?>[] types = pair.eventMethod.getParameterTypes();
            methodVisitor.visitVarInsn(Type.getType(types[i]).getOpcode(Opcodes.ILOAD), parameterIndex(types, i));
        }
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, viewClassType.getInternalName(), pair.viewMethod.getName(), Type.getMethodDescriptor(pair.viewMethod), true);
        returnAndCast.accept(methodVisitor, internalName);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
        classVisitor.visitEnd();
        final Class<?> custom = CLASS_LOADER.defineClassCustom(internalName.replace('/', '.'), writer.toByteArray());
        try {
            final Constructor<?> constructor;
            if (voidReturn) {
                constructor = custom.getConstructor(viewClass);
            } else {
                constructor = custom.getConstructor(viewClass, defaultReturnFactoryClass);
            }
            return v -> {
                try {
                    final Object o;
                    if (voidReturn) {
                        o = constructor.newInstance(v);
                    } else {
                        o = constructor.newInstance(v, defaultReturnFactory);
                    }
                    return (E) o;
                } catch (final InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
            };
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method invokerMethod(final Class<?> eventClass) {
        if (!eventClass.isInterface()) {
            throw new RuntimeException("Event class must be interface!");
        }
        final Method[] methods = eventClass.getDeclaredMethods();
        int count = 0;
        Method eventMethod = null;
        for (final Method method : methods) {
            if ((method.getModifiers() & Opcodes.ACC_STATIC) == 0 && method.getParameterCount() > 0) {
                eventMethod = method;
                count++;
            }
        }
        if (count != 1) {
            throw new RuntimeException("Event Interface must have single declared method!");
        }
        return eventMethod;
    }

    public static <E> InvokerFactory<E> generateDoubleReuseInvoker(final Class<E> eventClass, final int reuseIndex) {
        if (INVOKER_FACTORY_CACHE.containsKey(eventClass)) {
            return (InvokerFactory<E>) INVOKER_FACTORY_CACHE.get(eventClass);
        }
        final Method method = invokerMethod(eventClass);
        final Class<?>[] types = method.getParameterTypes();
        if (types.length < reuseIndex) {
            throw new RuntimeException("Reuse index out of bounds!");
        }
        if (types[reuseIndex] != double.class) {
            throw new RuntimeException("Expected double class got " + types[reuseIndex]);
        }
        if (method.getReturnType() != double.class) {
            throw new RuntimeException("Expected double class got " + method.getReturnType());
        }
        final InvokerFactory<E> factory = generateInvoker(eventClass, method, new DefaultSetup() {
            @Override
            public void setupReturn(final String name, final MethodVisitor visitor, final Label start, final Label end) {
                visitor.visitVarInsn(Opcodes.DLOAD, reuseIndex + 1);
            }

            @Override
            public void combine(final String name, final MethodVisitor visitor, final Label start, final Label end, final int retLoc) {

            }

            @Override
            public Optional<?> arg() {
                return Optional.empty();
            }

            @Override
            public Class<?> argClass() {
                return null;
            }
        }, reuseIndex);
        INVOKER_FACTORY_CACHE.put(eventClass, factory);
        return factory;
    }

    public static <E> InvokerFactory<E> generateBooleanAndInvoker(final Class<E> eventClass) {
        return generateBooleanCombiningInvoker(eventClass, true, (first, second) -> first & second);
    }

    public static <E> InvokerFactory<E> generateBooleanOrInvoker(final Class<E> eventClass) {
        return generateBooleanCombiningInvoker(eventClass, false, (first, second) -> first | second);
    }

    public static <E> InvokerFactory<E> generateVoidInvoker(Class<E> eventClass) {
        if (INVOKER_FACTORY_CACHE.containsKey(eventClass)) {
            return (InvokerFactory<E>) INVOKER_FACTORY_CACHE.get(eventClass);
        }
        final Method method = invokerMethod(eventClass);
        if (method.getReturnType() != void.class) {
            throw new RuntimeException("Expected void class got " + method.getReturnType());
        }
        InvokerFactory<E> factory = generateInvoker(eventClass, method, new DefaultSetup() {
            @Override
            public void setupReturn(String name, MethodVisitor visitor, Label start, Label end) {

            }

            @Override
            public void combine(String name, MethodVisitor visitor, Label start, Label end, int retLoc) {

            }

            @Override
            public Optional<?> arg() {
                return Optional.empty();
            }

            @Override
            public Class<?> argClass() {
                return null;
            }
        }, -1);
        INVOKER_FACTORY_CACHE.put(eventClass, factory);
        return factory;
    }

    public static <E> InvokerFactory<E> generateBooleanCombiningInvoker(final Class<E> eventClass, final boolean defaultValue, final BooleanCombiner combiner) {
        if (INVOKER_FACTORY_CACHE.containsKey(eventClass)) {
            return (InvokerFactory<E>) INVOKER_FACTORY_CACHE.get(eventClass);
        }
        final Method method = invokerMethod(eventClass);
        if (method.getReturnType() != boolean.class) {
            throw new RuntimeException("Expected boolean class got " + method.getReturnType());
        }
        final InvokerFactory<E> factory = generateInvoker(eventClass, method, new DefaultSetup() {
            @Override
            public void setupReturn(final String name, final MethodVisitor visitor, final Label start, final Label end) {
                visitor.visitInsn(defaultValue ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
            }

            @Override
            public void combine(final String name, final MethodVisitor visitor, final Label start, final Label end, final int retLoc) {
                visitor.visitVarInsn(Opcodes.ALOAD, 0);
                final Type argType = Type.getType(argClass());
                visitor.visitFieldInsn(Opcodes.GETFIELD, name, "arg", argType.getDescriptor());
                visitor.visitInsn(Opcodes.SWAP);
                visitor.visitVarInsn(Opcodes.ILOAD, retLoc);
                visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, argType.getInternalName(), "apply", "(ZZ)Z", true);
            }

            @Override
            public Optional<?> arg() {
                return Optional.of(combiner);
            }

            @Override
            public Class<?> argClass() {
                return BooleanCombiner.class;
            }
        }, -1);
        INVOKER_FACTORY_CACHE.put(eventClass, factory);
        return factory;
    }

    private static <V> InvokerFactory<V> generateInvoker(final Class<V> eventClass, final Method invokerMethod, final DefaultSetup defaultSetup, final int reuseIndex) {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ClassVisitor visitor = TBCExUtil.DEBUG ? new CheckClassAdapter(writer, true) : writer;
        final String internalName = "io/github/stuff_stuffs/tbcexv3core/dynamic/invoker/" + eventClass.getSimpleName();
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", new String[]{Type.getType(eventClass).getInternalName()});
        final Class<?> arrayClass = Array.newInstance(eventClass, 0).getClass();
        final Type arrayType = Type.getType(arrayClass);
        final Type runnableType = Type.getType(Runnable.class);
        visitor.visitField(Opcodes.ACC_PRIVATE, "events", arrayType.getDescriptor(), null, null).visitEnd();
        visitor.visitField(Opcodes.ACC_PRIVATE, "enter", runnableType.getDescriptor(), null, null).visitEnd();
        visitor.visitField(Opcodes.ACC_PRIVATE, "exit", runnableType.getDescriptor(), null, null).visitEnd();
        final String descriptor;
        final boolean argPresent = defaultSetup.arg().isPresent();
        final Type argType;
        if (argPresent) {
            argType = Type.getType(defaultSetup.argClass());
            visitor.visitField(Opcodes.ACC_PRIVATE, "arg", argType.getDescriptor(), null, null);
            descriptor = Type.getMethodDescriptor(Type.VOID_TYPE, arrayType, runnableType, runnableType, argType);
        } else {
            argType = null;
            descriptor = Type.getMethodDescriptor(Type.VOID_TYPE, arrayType, runnableType, runnableType);
        }
        MethodVisitor constructorVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
        if (TBCExUtil.DEBUG) {
            constructorVisitor = new CheckMethodAdapter(constructorVisitor);
        }
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "events", arrayType.getDescriptor());
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 2);
        constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "enter", runnableType.getDescriptor());
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 3);
        constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "exit", runnableType.getDescriptor());
        if (argPresent) {
            constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            constructorVisitor.visitVarInsn(Opcodes.ALOAD, 4);
            constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "arg", argType.getDescriptor());
        }
        constructorVisitor.visitInsn(Opcodes.RETURN);
        constructorVisitor.visitEnd();
        constructorVisitor.visitMaxs(0, 0);

        final MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, invokerMethod.getName(), Type.getMethodDescriptor(invokerMethod), null, null);
        invoker(TBCExUtil.DEBUG ? new CheckMethodAdapter(methodVisitor) : methodVisitor, internalName, Type.getType(eventClass), Type.getReturnType(invokerMethod), arrayType, defaultSetup, reuseIndex, invokerMethod);
        final Class<?> custom = CLASS_LOADER.defineClassCustom(internalName.replace('/', '.'), writer.toByteArray());
        try {
            final Constructor<?> constructor;
            if (argPresent) {
                constructor = custom.getConstructor(arrayClass, Runnable.class, Runnable.class, defaultSetup.argClass());
            } else {
                constructor = custom.getConstructor(arrayClass, Runnable.class, Runnable.class);
            }
            return (listeners, enter, exit) -> {
                try {
                    final Object o;
                    if (argPresent) {
                        o = constructor.newInstance(listeners, enter, enter, defaultSetup.arg().get());
                    } else {
                        o = constructor.newInstance(listeners, enter, exit);
                    }
                    return (V) o;
                } catch (final InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void invoker(final MethodVisitor methodVisitor, final String internalName, final Type type, final Type returnType, final Type arrayType, final DefaultSetup defaultSetup, final int reuseIdx, final Method method) {
        final boolean voidReturn = returnType.equals(Type.VOID_TYPE);
        if (voidReturn && reuseIdx > -1) {
            throw new RuntimeException("Void type must not have reuse index!");
        }
        final int argCount = method.getParameterCount();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Type runnableType = Type.getType(Runnable.class);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, internalName, "enter", runnableType.getDescriptor());
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, runnableType.getInternalName(), "run", Type.getMethodDescriptor(Type.VOID_TYPE), true);
        final Label start = new Label();
        final Label end = new Label();
        methodVisitor.visitLabel(start);
        final int retLoc = parameterIndex(parameterTypes, parameterTypes.length);
        if (!voidReturn) {
            defaultSetup.setupReturn(internalName, methodVisitor, start, end);
            methodVisitor.visitVarInsn(returnType.getOpcode(Opcodes.ISTORE), retLoc);
        }
        final int eventsLoc = retLoc + typeSize(returnType);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, internalName, "events", arrayType.getDescriptor());
        methodVisitor.visitVarInsn(Opcodes.ASTORE, eventsLoc);
        final int countIndex = eventsLoc + typeSize(arrayType);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, internalName, "events", arrayType.getDescriptor());
        methodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
        methodVisitor.visitVarInsn(Opcodes.ISTORE, countIndex);
        final Label loopStart = new Label();
        final Label loopEnd = new Label();
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitLabel(loopStart);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitVarInsn(Opcodes.ILOAD, countIndex);
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, eventsLoc);
        methodVisitor.visitInsn(Opcodes.SWAP);
        methodVisitor.visitInsn(Opcodes.AALOAD);
        for (int i = 0; i < argCount; i++) {
            if (reuseIdx == i) {
                methodVisitor.visitVarInsn(returnType.getOpcode(Opcodes.ILOAD), retLoc);
            } else {
                methodVisitor.visitVarInsn(Type.getType(parameterTypes[i]).getOpcode(Opcodes.ILOAD), parameterIndex(parameterTypes, i));
            }
        }
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, type.getInternalName(), method.getName(), Type.getMethodDescriptor(method), true);
        if (!voidReturn) {
            defaultSetup.combine(internalName, methodVisitor, start, end, retLoc);
            methodVisitor.visitVarInsn(returnType.getOpcode(Opcodes.ISTORE), retLoc);
        }
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitInsn(Opcodes.IADD);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, loopStart);
        methodVisitor.visitLabel(loopEnd);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, internalName, "exit", runnableType.getDescriptor());
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, runnableType.getInternalName(), "run", Type.getMethodDescriptor(Type.VOID_TYPE), true);
        if (!voidReturn) {
            methodVisitor.visitVarInsn(returnType.getOpcode(Opcodes.ILOAD), retLoc);
        }
        methodVisitor.visitLabel(end);
        if (!voidReturn) {
            methodVisitor.visitLocalVariable("ret", returnType.getDescriptor(), null, start, end, retLoc);
        }
        methodVisitor.visitLocalVariable("eventsLoc", arrayType.getDescriptor(), null, start, end, eventsLoc);
        methodVisitor.visitLocalVariable("count", Type.INT_TYPE.getDescriptor(), null, start, end, countIndex);
        methodVisitor.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
        methodVisitor.visitEnd();
        methodVisitor.visitMaxs(0, 0);
    }

    private static int parameterIndex(final Class<?>[] types, final int index) {
        int count = 1;
        for (int i = 0; i < Math.min(types.length, index); i++) {
            final Class<?> type = types[i];
            count += typeSize(Type.getType(type));
        }
        return count;
    }

    private static int parameterIndex(final Type[] types, final int index) {
        int count = 1;
        for (int i = 0; i < Math.min(types.length, index); i++) {
            final Type type = types[i];
            count += typeSize(type);
        }
        return count;
    }

    private static int typeSize(final Type type) {
        if (type.equals(Type.VOID_TYPE)) {
            return 0;
        }
        if (type.equals(Type.DOUBLE_TYPE) || type.equals(Type.LONG_TYPE)) {
            return 2;
        } else {
            return 1;
        }
    }

    private record ConvertInfo(Method eventMethod, Method viewMethod) {
    }

    private static final class CustomClassLoader extends ClassLoader {
        private CustomClassLoader() {
            super("Event generation ClassLoader", EventGenerationUtil.class.getClassLoader());
        }

        public Class<?> defineClassCustom(final String name, final byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }

    private interface DefaultSetup {
        void setupReturn(String name, MethodVisitor visitor, Label start, Label end);

        void combine(String name, MethodVisitor visitor, Label start, Label end, int retLoc);

        Optional<?> arg();

        Class<?> argClass();
    }

    public interface BooleanCombiner {
        boolean apply(boolean first, boolean second);
    }

    private EventGenerationUtil() {
    }
}
