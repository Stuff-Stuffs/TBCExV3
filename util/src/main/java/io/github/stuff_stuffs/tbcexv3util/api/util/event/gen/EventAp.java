package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.SimpleEventInfo", "io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.PassthroughEventInfo"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EventAp extends AbstractProcessor {
    private static final Map<Type, String> PREFIXES = Map.of(Type.PRE, "Pre", Type.POST, "Post", Type.SUCCESS, "Successful", Type.FAIL, "Failed");
    private static final String MUT_SUFFIX = "Event";
    private static final String VIEW_SUFFIX = "EventView";
    private static final String COMPARE_BY = "{type} {name}();";
    private static final String SIMPLE_TEMPLATE = """
            package {package};
                        
            {imports}

            @javax.annotation.processing.Generated("io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventAp")
            public interface {name} {
                {type} {methodName}({parameters});
                
                {compareBy}
            }
            """;
    private static final String CONVERTER_RETURN = """
                            return {defaultReturn};\
            """;
    private static final String NO_RETURN_INVOKER_LOOP = """
                            for(var listener: listeners) {
                                listener.{methodName}({args});
                            }\
            """;
    private static final String NO_RETURN_INVOKER_RETURN = "";
    private static final String INVOKER_LOOP = """
                            {type} ret = {defaultReturn};
                            for(var listener: listeners) {
                                ret = {combiner}(ret, listener.{methodName}({args}));
                            }\
            """;
    private static final String INVOKER_PASSTHROUGH_LOOP = """
                            for(var listener: listeners) {
                                {passthrough} = {combiner}({passthrough}, listener.{methodName}({args}));
                            }\
            """;
    private static final String INVOKER_RETURN = """
                            return ret;\
            """;
    private static final String INVOKER_PASSTHROUGH_RETURN = """
                            return {passthrough};\
            """;
    private static final String CONVERTER_COMPARE_BY = """
                        public {type} {name}() {
                            return view.{name}();
                        }\
            """;
    private static final String INVOKER_COMPARE_BY = """
                        public {type} {name}() {
                            throw new UnsupportedOperationException("Tried to call comparison method on invoker!");
                        }\
            """;
    private static final String INVOKING_TEMPLATE = """
            package {package};
                        
            {imports}

            @javax.annotation.processing.Generated("io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventAp")
            public interface {name} {
                {type} {methodName}({parameters});
                
                {compareBy}
                
                static {name} converter({viewName} view) {
                    return new {name}() {
                        public {type} {methodName}({parameters}) {
                            view.{methodName}({args});
            {converterReturn}
                        }
                        
            {converterCompareBy}
                    };
                }
                
                static InvokerFactory<{name}> invoker() {
                    return (listeners, enter, exit) -> new {name}() {
                        public {type} {methodName}({parameters}) {
                            enter.run();
            {invokerLoop}
                            exit.run();
            {invokerReturn}
                        }
                        
            {invokerCompareBy}
                    };
                }
            }
            """;
    private static final String KEY_TEMPLATE = """
                EventKey<{view}, {mut}> {name} = new EventKey<>({id}, {mut}.class, {view}.class, {mut}.invoker(), {mut}::converter, {comparator});\
            """;
    private static final String KEY_ADD_TEMPLATE = """
                    builder.add({name});
            """;
    private static final String KEY_FILE_TEMPLATE = """
            package {package};
                        
            {imports}
                        
            @javax.annotation.processing.Generated("EventAp")
            public interface {name} {
            {keys}
                static void addAll(io.github.stuff_stuffs.tbcexv3util.api.util.event.EventMap.Builder builder) {
            {keyAdd}
                }
            }
            """;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Map<String, List<EventGrouping>> keys = new HashMap<>();
        for (final TypeElement annotation : annotations) {
            for (final Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                    throw new IllegalStateException("Tried to process event on class!");
                }
                final TypeElement element = (TypeElement) annotatedElement;
                if (!element.getInterfaces().isEmpty()) {
                    throw new IllegalStateException("Tried to process interface with super interfaces!");
                }
                final EventInfo info = extract(element);
                final EventInfo postInfo = createPostEventInfo(info);
                if (!info.comparator().isEmpty() && info.compareBy().isEmpty()) {
                    throw new RuntimeException();
                }
                final String qualifiedName = element.getQualifiedName().toString();
                final String packagePath = findPackage(element);
                final String name = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
                final String mutName = name + MUT_SUFFIX;
                final String viewName = name + VIEW_SUFFIX;
                final MethodInfo methodInfo = findEventMethod(element, info);
                final TypeMirror voidType = processingEnv.getTypeUtils().getNoType(TypeKind.VOID);
                final String keyLocation = findKeyLocation(methodInfo.eventMethod());
                final EventGrouping grouping;
                if (info.type == EventType.SINGLE) {
                    final EventPair single = create(packagePath, "", name, mutName, viewName, methodInfo, info, methodInfo.eventMethod().getReturnType(), EventPhase.SINGLE);
                    grouping = new EventGrouping(new EventPair[]{single}, name);
                } else if (info.type == EventType.PRE_POST) {
                    final String prePrefix = PREFIXES.get(Type.PRE);
                    final EventPair pre = create(packagePath, prePrefix, name, mutName, viewName, methodInfo, info, methodInfo.eventMethod().getReturnType(), EventPhase.PRE);
                    final String postPrefix = PREFIXES.get(Type.POST);
                    final EventPair post = create(packagePath, postPrefix, name, mutName, viewName, methodInfo, postInfo, voidType, EventPhase.POST);
                    grouping = new EventGrouping(new EventPair[]{pre, post}, name);
                } else {
                    final String prePrefix = PREFIXES.get(Type.PRE);
                    final EventPair pre = create(packagePath, prePrefix, name, mutName, viewName, methodInfo, info, methodInfo.eventMethod().getReturnType(), EventPhase.PRE);
                    final String successPrefix = PREFIXES.get(Type.SUCCESS);
                    final EventPair success = create(packagePath, successPrefix, name, mutName, viewName, methodInfo, postInfo, voidType, EventPhase.SUCCESS);
                    final String failurePrefix = PREFIXES.get(Type.FAIL);
                    final EventPair failure = create(packagePath, failurePrefix, name, mutName, viewName, methodInfo, postInfo, voidType, EventPhase.FAILURE);
                    grouping = new EventGrouping(new EventPair[]{pre, success, failure}, name);
                }
                keys.computeIfAbsent(keyLocation, i -> new ArrayList<>()).add(grouping);
            }
        }
        for (final Map.Entry<String, List<EventGrouping>> entry : keys.entrySet()) {
            final String path = entry.getKey();
            final String prefix = path.substring(0, path.lastIndexOf('.'));
            final String name = path.substring(path.lastIndexOf('.') + 1);
            createKeyFile(prefix, name, entry.getValue());
        }
        return true;
    }

    private static String eventToName(final String event) {
        final int length = event.length();
        final StringBuilder builder = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            final char c = event.charAt(i);
            if (i != 0 && Character.isUpperCase(c)) {
                builder.append('_');
            }
            builder.append(Character.toUpperCase(c));
        }
        return builder.toString();
    }

    private void createKeyFile(final String packagePath, final String name, final List<EventGrouping> events) {
        try {
            final JavaFileObject keyFile = processingEnv.getFiler().createSourceFile(packagePath + '.' + name);
            try (final Writer writer = keyFile.openWriter()) {
                final ImportManager manager = new ImportManager(processingEnv.getElementUtils());
                manager.get("io.github.stuff_stuffs.tbcexv3util.api.util.event.EventKey");
                final StringBuilder keys = new StringBuilder();
                final StringBuilder keyAdd = new StringBuilder();
                final List<EventGrouping> sorted = new ArrayList<>(events);
                sorted.sort(Comparator.comparing(EventGrouping::key));
                boolean firstGrouping = true;
                for (final EventGrouping grouping : sorted) {
                    if (!firstGrouping) {
                        keys.append('\n');
                        keys.append('\n');
                    }
                    firstGrouping = false;
                    boolean firstPair = true;
                    keys.append("//Grouping ").append(grouping.key).append('\n');
                    for (final EventPair event : grouping.events()) {
                        if (!firstPair) {
                            keys.append('\n');
                        }
                        firstPair = false;
                        keyAdd.append(KEY_ADD_TEMPLATE.replace("{name}", event.name()));
                        String replace = KEY_TEMPLATE.replace("{mut}", manager.get(event.mut())).replace("{view}", manager.get(event.view())).replace("{name}", event.name()).replace("{id}", '"' + event.name().toLowerCase(Locale.ROOT) + '"');
                        if (!event.comparator().isEmpty()) {
                            replace = replace.replace("{comparator}", event.comparator());
                        } else {
                            replace = replace.replace("{comparator}", "null");
                        }
                        keys.append(replace);
                    }
                }
                final String data = KEY_FILE_TEMPLATE.replace("{package}", packagePath).replace("{name}", name).replace("{keys}", keys.toString()).replace("{keyAdd}", keyAdd.toString()).replace("{imports}", manager.imports());
                writer.write(data);
            }
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while try to create " + packagePath + name);
        }
    }

    private String createComparator(final String comparator, final ExecutableElement compareBy) {
        final String extract = compareBy.getSimpleName().toString() + "()";
        final String comp;
        if (comparator.isEmpty()) {
            if (compareBy.getReturnType().getKind().isPrimitive()) {
                comp = switch (compareBy.getReturnType().getKind()) {
                    case BOOLEAN -> "Boolean";
                    case BYTE -> "Byte";
                    case SHORT -> "Short";
                    case INT -> "Integer";
                    case LONG -> "Long";
                    case CHAR -> "Char";
                    case FLOAT -> "Float";
                    case DOUBLE -> "Double";
                    default -> throw new UnsupportedOperationException();
                };
            } else {
                comp = "java.util.Comparator.naturalOrder()";
            }
        } else {
            comp = comparator;
        }
        return "(o1,o2) -> " + comp + ".compare(o1." + extract + ", o2." + extract + ")";

    }

    private EventPair create(final String packagePath, final String methodPrefix, final String name, final String mutName, final String viewName, final MethodInfo methodInfo, final EventInfo eventInfo, final TypeMirror type, final EventPhase phase) {
        final String mutPath = packagePath + '.' + methodPrefix + mutName;
        final String viewPath = packagePath + '.' + methodPrefix + viewName;
        final String comparator;
        if (methodInfo.compareByMethod() == null) {
            comparator = "";
        } else {
            comparator = createComparator(eventInfo.comparator(), methodInfo.compareByMethod());
        }
        try {
            final JavaFileObject mutFile = processingEnv.getFiler().createSourceFile(mutPath);
            final JavaFileObject viewFile = processingEnv.getFiler().createSourceFile(viewPath);
            tryCreate(methodInfo, type, mutFile, viewFile, eventInfo, packagePath, methodPrefix + mutName, methodPrefix + viewName, methodPrefix.toLowerCase(Locale.ROOT), phase);
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while try to create " + mutName);
        }
        return new EventPair(mutPath, viewPath, eventToName(methodPrefix + name), comparator);
    }

    private String findPackage(Element element) {
        while (element != null) {
            final EventPackageLocation annotation = element.getAnnotation(EventPackageLocation.class);
            if (annotation != null) {
                return annotation.value();
            }
            element = element.getEnclosingElement();
        }
        throw new IllegalStateException("Unspecified package location!");
    }

    private String findKeyLocation(Element element) {
        while (element != null) {
            final EventKeyLocation annotation = element.getAnnotation(EventKeyLocation.class);
            if (annotation != null) {
                return annotation.value();
            }
            element = element.getEnclosingElement();
        }
        throw new IllegalStateException("Unspecified key location!");
    }

    private EventInfo createPostEventInfo(final EventInfo info) {
        return new EventInfo(info.data(), info.compareBy(), info.comparator(), info.combiner(), false, info.type());
    }

    private EventInfo extract(final Element element) {
        final SimpleEventInfo simple = element.getAnnotation(SimpleEventInfo.class);
        if (simple != null) {
            return new EventInfo(simple.defaultValue(), simple.compareBy(), simple.comparator(), simple.combiner(), false, simple.type());
        } else {
            final PassthroughEventInfo passthrough = element.getAnnotation(PassthroughEventInfo.class);
            if (passthrough == null) {
                throw new RuntimeException();
            }
            return new EventInfo(passthrough.passthrough(), passthrough.compareBy(), passthrough.comparator(), passthrough.combiner(), true, passthrough.type());
        }
    }

    private MethodInfo findEventMethod(final TypeElement element, final EventInfo info) {
        final List<? extends Element> elements = element.getEnclosedElements();
        final Map<String, ExecutableElement> execs = new HashMap<>();
        for (final Element e : elements) {
            if (e.getKind() == ElementKind.METHOD) {
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    execs.put(e.getSimpleName().toString(), (ExecutableElement) e);
                }
            }
        }
        int size = execs.size();
        ExecutableElement compareBy = null;
        if (!info.compareBy().isEmpty()) {
            if ((compareBy = execs.remove(info.compareBy())) == null) {
                throw new RuntimeException();
            }
            size--;
        }
        if (size != 1) {
            throw new RuntimeException("Could not find event method!");
        }
        ExecutableElement eventMethod = null;
        for (final ExecutableElement value : execs.values()) {
            eventMethod = value;
        }
        return new MethodInfo(eventMethod, compareBy);
    }

    private static String capitalize(final String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    private void tryCreate(final MethodInfo methodInfo, final TypeMirror returnType, final JavaFileObject mutFile, final JavaFileObject viewFile, final EventInfo info, final String packageName, final String mutName, final String viewName, final String methodNamePrefix, final EventPhase phase) {
        final ExecutableElement eventMethod = methodInfo.eventMethod();
        final ExecutableElement compareByMethod = methodInfo.compareByMethod();
        final ImportManager mutImportManager = new ImportManager(processingEnv.getElementUtils());
        final ImportManager viewImportManager = new ImportManager(processingEnv.getElementUtils());
        if (!(eventMethod.getReturnType() instanceof NoType)) {
            if (info.combiner().isEmpty()) {
                throw new RuntimeException("Cannot have returning event with empty combiner!");
            }
        } else {
            if (!info.combiner().isEmpty()) {
                throw new RuntimeException("Cannot have non-returning event with non-empty combiner!");
            }
        }
        String simpleTemplate = SIMPLE_TEMPLATE;
        String invokingTemplate = INVOKING_TEMPLATE;
        final String methodName;
        if (!methodNamePrefix.isEmpty()) {
            methodName = methodNamePrefix + capitalize(eventMethod.getSimpleName().toString());
        } else {
            methodName = eventMethod.getSimpleName().toString();
        }
        final String compareBy;
        final String converterCompareBy;
        final String invokerCompareBy;
        if (compareByMethod == null) {
            compareBy = "";
            converterCompareBy = "";
            invokerCompareBy = "";
        } else {
            final String compareByName = compareByMethod.getSimpleName().toString();
            compareBy = COMPARE_BY.replace("{type}", compareByMethod.getReturnType().toString()).replace("{name}", compareByName);
            converterCompareBy = CONVERTER_COMPARE_BY.replace("{type}", compareByMethod.getReturnType().toString()).replace("{name}", compareByName);
            invokerCompareBy = INVOKER_COMPARE_BY.replace("{type}", compareByMethod.getReturnType().toString()).replace("{name}", compareByName);
        }
        if (returnType instanceof NoType) {
            invokingTemplate = invokingTemplate.replace("{converterReturn}", "");
        } else {
            if (info.data().isEmpty()) {
                throw new RuntimeException("Cannot have non void return with no defaultValue!");
            }
            invokingTemplate = invokingTemplate.replace("{converterReturn}", CONVERTER_RETURN.replace("{defaultReturn}", info.data()));
        }
        final String invokerLoop;
        final String invokerReturn;
        if (info.passthrough()) {
            if (returnType instanceof NoType) {
                throw new RuntimeException("Cannot have a passthrough event with void return!");
            }
            invokerLoop = INVOKER_PASSTHROUGH_LOOP.replace("{passthrough}", info.data()).replace("{combiner}", info.combiner());
            invokerReturn = INVOKER_PASSTHROUGH_RETURN.replace("{passthrough}", info.data());
        } else {
            if (returnType instanceof NoType) {
                invokerLoop = NO_RETURN_INVOKER_LOOP;
                invokerReturn = NO_RETURN_INVOKER_RETURN;
            } else {
                invokerLoop = INVOKER_LOOP.replace("{defaultReturn}", info.data()).replace("{combiner}", info.combiner());
                invokerReturn = INVOKER_RETURN;
            }
        }
        mutImportManager.get("io.github.stuff_stuffs.tbcexv3util.api.util.event.InvokerFactory");
        simpleTemplate = simpleTemplate.replace("{compareBy}", compareBy);
        invokingTemplate = invokingTemplate.replace("{compareBy}", compareBy).replace("{converterCompareBy}", converterCompareBy).replace("{invokerCompareBy}", invokerCompareBy).replace("{invokerLoop}", invokerLoop).replace("{invokerReturn}", invokerReturn).replace("{viewName}", viewName).replace("{args}", argList(eventMethod.getParameters(), phase));
        write(invokingTemplate, packageName, mutName, returnType.toString(), methodName, parameterList(eventMethod.getParameters(), false, mutImportManager, phase), mutFile, mutImportManager);
        write(simpleTemplate, packageName, viewName, "void", methodName, parameterList(eventMethod.getParameters(), true, viewImportManager, phase), viewFile, viewImportManager);
    }

    private String variableName(final VariableElement element, final EventPhase phase) {
        final EventVarRename[] annotations = element.getAnnotationsByType(EventVarRename.class);
        for (final EventVarRename rename : annotations) {
            if (rename.phase() == phase) {
                return rename.name();
            }
        }
        return element.getSimpleName().toString();
    }

    private String argList(final List<? extends VariableElement> elements, final EventPhase phase) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final VariableElement element : elements) {
            if (!first) {
                builder.append(',');
                builder.append(' ');
            }
            first = false;
            builder.append(variableName(element, phase));
        }
        return builder.toString();
    }

    private String parameterList(final List<? extends VariableElement> elements, final boolean view, final ImportManager manager, final EventPhase phase) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final VariableElement element : elements) {
            if (!first) {
                builder.append(',');
                builder.append(' ');
            }
            first = false;
            final String className;
            if (view) {
                className = walkUp(element.asType()).toString();
            } else {
                className = element.asType().toString();
            }
            builder.append(manager.get(className));
            builder.append(' ');
            builder.append(variableName(element, phase));
        }
        return builder.toString();
    }

    private TypeMirror mirrorFromViewable(EventViewable viewable) {
        try {
            boolean b = viewable.viewClass().isInstance(null);
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
        return null;
    }

    private TypeMirror walkUp(final TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            final EventViewable annotation = ((DeclaredType) type).asElement().getAnnotation(EventViewable.class);
            if (annotation != null) {
                final TypeMirror mirror = mirrorFromViewable(annotation);
                final Element element = processingEnv.getTypeUtils().asElement(mirror);
                final ElementKind kind = element.getKind();
                if(!kind.isClass() && !kind.isInterface()) {
                    throw new RuntimeException("Invalid View class!");
                }
                final TypeElement typeElement = (TypeElement) element;
                final Types utils = processingEnv.getTypeUtils();
                if (mirror.getKind() == TypeKind.DECLARED && type.getKind() == TypeKind.DECLARED) {
                    final List<? extends TypeMirror> mutArguments = ((DeclaredType) type).getTypeArguments();
                    final List<? extends TypeMirror> viewArguments = ((DeclaredType) mirror).getTypeArguments();
                    if (viewArguments.size() != mutArguments.size()) {
                        throw new IllegalStateException();
                    }
                    final DeclaredType withArgs = utils.getDeclaredType(typeElement, mutArguments.toArray(TypeMirror[]::new));
                    if (!utils.isSubtype(type, withArgs)) {
                        throw new RuntimeException("Mut not a subtype of View! " + type + " " + withArgs);
                    }
                    return walkUp(withArgs);
                } else {
                    if (!utils.isSubtype(type, mirror)) {
                        throw new RuntimeException("Mut not a subtype of View! " + type + " " + mirror);
                    }
                    return mirror;
                }
            }
        }
        return type;
    }

    private void write(final String template, final String packageName, final String name, final String type, final String methodName, final String parameters, final JavaFileObject file, final ImportManager importManager) {
        try (final Writer writer = file.openWriter()) {
            writer.write(template.replace("{imports}", importManager.imports()).replace("{package}", packageName).replace("{name}", name).replace("{type}", importManager.get(type)).replace("{methodName}", methodName).replace("{parameters}", parameters));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record EventGrouping(EventPair[] events, String key) {
    }

    private record MethodInfo(ExecutableElement eventMethod, @Nullable ExecutableElement compareByMethod) {
    }

    private record EventPair(String mut, String view, String name, String comparator) {
    }

    private record EventInfo(String data, String compareBy, String comparator, String combiner, boolean passthrough,
                             EventType type) {
    }

    private enum Type {
        PRE,
        POST,
        SUCCESS,
        FAIL
    }
}
