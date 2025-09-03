package processors;

import annotations.AutoRegisterMessage;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("annotations.AutoRegisterMessage")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class MessageTypeRegistryProcessor extends AbstractProcessor {
    private boolean fileCreated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (fileCreated)
        { return false; }
        // implemented based on https://www.baeldung.com/java-annotation-processing-builder
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(AutoRegisterMessage.class);
        String registryCode = buildRegistryFile(annotatedElements);

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile("communication.message.MessageTypeRegistry");
            try (java.io.Writer writer = file.openWriter()) {
                writer.write(registryCode);
            }
            fileCreated = true;
        } catch (Exception e) { processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write MessageTypeRegistry: " + e.getMessage()); }

        return false;
    }

    private String buildRegistryFile(Set<? extends Element> annotatedElements) {
        StringBuilder registryCode = new StringBuilder();

        addPackageAndDependencies(registryCode);

        registryCode.append("public class MessageTypeRegistry {\n");
        registryCode.append("    private static final Map<String, Class<? extends Message>> nameToClass = new HashMap<>();\n\n");

        addStaticRegistrationBlock(registryCode, annotatedElements);
        addisSupportedMessageTypeMethod(registryCode);
        addGetter(registryCode);
        addRegisterMethod(registryCode);

        registryCode.append("}\n");
        return registryCode.toString();
    }

    private void addisSupportedMessageTypeMethod(StringBuilder registryCode) {
        registryCode.append("    public static boolean isSupportedMessageType(String simpleClassName) { return nameToClass.containsKey(simpleClassName); }\n\n");
    }

    private void addPackageAndDependencies(StringBuilder registryCode) {
        registryCode.append("package communication.message;\n\n");
        registryCode.append("import java.util.HashMap;\n");
        registryCode.append("import java.util.Map;\n\n");
    }

    private void addStaticRegistrationBlock(StringBuilder registryCode, Set<? extends Element> annotatedElements) {
        registryCode.append("    static {\n");
        for (Element element : annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            String fullyQualifiedClassName = ((TypeElement) element).getQualifiedName().toString();
            String simpleClassName = element.getSimpleName().toString();
            registryCode.append("        register(\"" + simpleClassName + "\", " + fullyQualifiedClassName + ".class);\n");
        }
        registryCode.append("    }\n\n");
    }

    private void addGetter(StringBuilder registryCode) {
        registryCode.append("    public static Class<? extends Message> getMessageType(String simpleClassName) {\n");
        registryCode.append("        if (!nameToClass.containsKey(simpleClassName)) { throw new IllegalArgumentException(\"Unknown message type: \" + simpleClassName);}\n");
        registryCode.append("        return nameToClass.get(simpleClassName);\n");
        registryCode.append("    }\n\n");
    }

    private void addRegisterMethod(StringBuilder registryCode) {
        registryCode.append("    private static void register(String simpleClassName, Class<? extends Message> messageClass) {\n");
        registryCode.append("        nameToClass.put(simpleClassName, messageClass);\n");
        registryCode.append("    }\n");
    }

}