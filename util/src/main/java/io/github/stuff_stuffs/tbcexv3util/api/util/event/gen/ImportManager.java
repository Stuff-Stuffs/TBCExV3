package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

class ImportManager {
    private final Map<String, String> nameToPath;
    private final Elements elements;

    ImportManager(final Elements elements) {
        this.elements = elements;
        nameToPath = new HashMap<>();
    }

    String get(final String path) {
        return switch (path) {
            case "byte" -> "byte";
            case "short" -> "short";
            case "int" -> "int";
            case "long" -> "long";
            case "float" -> "float";
            case "double" -> "double";
            case "void" -> "void";
            case "boolean" -> "boolean";
            default -> {
                final Decomposed decomposed = decompose(path);
                String realPath = decomposed.path() + '.' + decomposed.name();
                final String p = nameToPath.get(decomposed.name);
                if (p == null || p.equals(realPath)) {
                    if (p == null) {
                        nameToPath.put(decomposed.name, decomposed.path + '.' + decomposed.name);
                    }
                    yield parameterize(decomposed.name, decomposed.args);
                }
                yield parameterize(realPath, decomposed.args);
            }
        };
    }

    private String parameterize(final String type, final String[] args) {
        if (args.length == 0) {
            return type;
        }
        final StringBuilder builder = new StringBuilder(type.length() * 2);
        builder.append(type);
        builder.append('<');
        boolean first = true;
        for (final String arg : args) {
            if (!first) {
                builder.append(',');
                builder.append(' ');
            }
            first = false;
            builder.append(arg);
        }
        builder.append('>');
        return builder.toString();
    }

    String imports() {
        final List<String> sorted = new ArrayList<>(nameToPath.values());
        sorted.removeIf(s -> s.startsWith("java.lang."));
        sorted.sort(Comparator.naturalOrder());
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final String path : sorted) {
            if (!first) {
                builder.append('\n');
            }
            first = false;
            builder.append("import ");
            builder.append(path);
            builder.append(';');
        }
        return builder.toString();
    }

    public static PackageBoundary findPackageBoundary(final Elements elements, final Element element) {
        final String packagePath = elements.getPackageOf(element).getQualifiedName().toString();
        final String fullPath = element.toString();
        return new PackageBoundary(packagePath, fullPath.substring(packagePath.length() + 1));
    }

    private static PackageBoundary findPackageBoundarySynthetic(Elements elements, String path) {
        final TypeElement element = elements.getTypeElement(path);
        if(element!=null) {
            return findPackageBoundary(elements, element);
        } else {
            int packageEnd = path.lastIndexOf('.');
            return new PackageBoundary(path.substring(0, packageEnd), path.substring(packageEnd+1));
        }
    }

    private Decomposed decompose(String path) {
        if(path.startsWith("@")) {
            path = path.substring(path.lastIndexOf(')') + 1).strip();
        }
        final int parameterStart = path.indexOf('<');
        if (parameterStart == -1) {
            PackageBoundary boundary = findPackageBoundarySynthetic(elements, path);
            return new Decomposed(boundary.packagePath(), boundary.elementPath(), new String[]{});
        } else {
            PackageBoundary boundary = findPackageBoundarySynthetic(elements, path.substring(0, parameterStart));
            final String[] unProcessedArgs = path.substring(parameterStart + 1, path.length() - 1).split(",");
            final String[] processed = new String[unProcessedArgs.length];
            for (int i = 0; i < unProcessedArgs.length; i++) {
                processed[i] = get(unProcessedArgs[i].strip());
            }
            return new Decomposed(boundary.packagePath(), boundary.elementPath(), processed);
        }
    }

    public record PackageBoundary(String packagePath, String elementPath) {
    }

    private record Decomposed(String path, String name, String[] args) {
    }
}
