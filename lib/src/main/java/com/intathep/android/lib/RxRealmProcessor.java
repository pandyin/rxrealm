package com.intathep.android.lib;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by pan on 11/10/16.
 */

@AutoService(Processor.class)
public class RxRealmProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<Name, RxRealmFactory> factoryClasses = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(RxRealm.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(RxRealm.class);
            for (Element annotatedElement : annotatedElements) {
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    error(annotatedElement, "%s, is not class", "");
                    return true;
                }
                TypeElement typeElement = (TypeElement) annotatedElement;
                if (!isValidClass(typeElement)) {
                    return true;
                }
                RxRealmFactory getterAndSetterFactory = factoryClasses.get(typeElement.getSimpleName());
                if (getterAndSetterFactory == null) {
                    getterAndSetterFactory = new RxRealmFactory(annotatedElement);
                    factoryClasses.put(typeElement.getSimpleName(), getterAndSetterFactory);
                }
                getterAndSetterFactory.add(typeElement);
            }
            for (RxRealmFactory getterAndSetterFactory : factoryClasses.values()) {
                try {
                    getterAndSetterFactory.generate(elementUtils, filer);
                } catch (ClassNotFoundException e) {
                    error(getterAndSetterFactory.getAnnotatedElement(), e.getMessage(), "");
                }
            }
            if (factoryClasses.size() > 0) {
                CommandFactory commandFactory = new CommandFactory();
                commandFactory.generate(filer);
            }
            factoryClasses.clear();
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private void error(Element annotatedElement, String message, String... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), annotatedElement);
    }

    private boolean isValidClass(TypeElement classElement) {
// Class must be public:
// classElement.getModifiers().contains(Modifier.PUBLIC)
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "%s, is not public", classElement.getQualifiedName().toString());
            return false;
        }
// Class can not be abstract:
// classElement.getModifiers().contains(Modifier.ABSTRACT)
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "%, is abstract", classElement.getQualifiedName().toString());
            return false;
        }
// Class must be subclass or implement the Class as specified in @Factoy.type().
// First we use elementUtils.getTypeElement(item.getQualifiedFactoryGroupName()) to create a Element of the passed Class (@Factoy.type()).
// Yes you got it, you can create TypeElement (with TypeMirror) just by knowing the qualified class name.
// Next we check if it’s an interface or a class: superClassElement.getKind() == ElementKind.INTERFACE.
// So we have two cases: If it’s an interfaces then classElement.getInterfaces().contains(superClassElement.asType()).
// If it’s a class, then we have to scan the inheritance hierarchy with calling currentClass.getSuperclass().
// Note that this check could also be done with typeUtils.isSubtype().
//        TypeElement superClassElement = elementUtils.getTypeElement(annotatedClass.getQualifiedFactoryGroupName());
//        if (superClassElement.getKind() == ElementKind.INTERFACE) {
//            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
//                error(classElement, "%s, must implement the interface %s", classElement.getQualifiedName().toString(), annotatedClass.getQualifiedFactoryGroupName());
//                return false;
//            }
//        } else {
//            TypeElement currentClass = classElement;
//            while (true) {
//                TypeMirror superClassType = currentClass.getSuperclass();
//                if (superClassType.getKind() == TypeKind.NONE) {
//                    error(classElement, "%s, must inherit from %s", classElement.getQualifiedName().toString(), annotatedClass.getQualifiedFactoryGroupName());
//                    return false;
//                }
//                if (superClassType.toString().equals(annotatedClass.getQualifiedFactoryGroupName())) {
//                    break;
//                }
//                currentClass = (TypeElement) typeUtils.asElement(superClassType);
//            }
//        }
// Class must have a public empty constructor:
// So we iterate over all enclosed elements classElement.getEnclosedElements()
// and check for ElementKind.CONSTRUCTOR, Modifier.PUBLIC and constructorElement.getParameters().size() == 0
        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosedElement;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }
        error(classElement, "%s, no default constructor", classElement.getQualifiedName().toString());
        return false;
    }
}
