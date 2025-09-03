package processors;

import annotations.AutoRegisterMessage;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_21)
public class MissingMessageAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> nonAbstractMessageClasses = getNonAbstractMessageClasses(roundEnv);
        for (TypeElement inheritor : nonAbstractMessageClasses) {
            boolean hasAutoRegisterAnnotation = inheritor.getAnnotationMirrors().stream().anyMatch(this::isAutoRegisterAnnotation);
            if (!hasAutoRegisterAnnotation) {
                throwError(inheritor, roundEnv);
            }
        }

        return false;
    }

    private void throwError(TypeElement messageClass, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(
                javax.tools.Diagnostic.Kind.ERROR,
                "All non-abstract Message subclasses must have the @AutoRegisterMessage annotations. " + messageClass.getQualifiedName() + " does not.",
                messageClass
        );
    }

    private Set<TypeElement> getNonAbstractMessageClasses(RoundEnvironment roundEnv) {
        Set<TypeElement> nonAbstractMessageClasses = new HashSet<>();
        return roundEnv.getRootElements().stream()
                .filter(this::isNonAbstractMessageInheritor)
                .map(e -> (TypeElement) e)
                .collect(Collectors.toSet());
    }

    private boolean isAutoRegisterAnnotation(AnnotationMirror mirror) {
        return mirror.getAnnotationType().toString().equals(AutoRegisterMessage.class.getCanonicalName());
    }

    private boolean isNonAbstractMessageInheritor(Element element) {
        TypeMirror messageType = processingEnv.getElementUtils().getTypeElement("communication.message.Message").asType();
        return element instanceof TypeElement typeElement
                && typeElement.getKind() == ElementKind.CLASS
                && !typeElement.getModifiers().contains(Modifier.ABSTRACT)
                && processingEnv.getTypeUtils().isSubtype(typeElement.asType(), messageType);
    }
}
