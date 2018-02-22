package com.intathep.rxrealm;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class RxRealmFactory {

    private static final String PACKAGE_NAME = "com.ekoapp.realm";
    private static final String CREATOR_SUFFIX = "Creator";
    private static final String GETTER_SUFFIX = "Getter";
    private static final String SETTER_SUFFIX = "Setter";
    private static final String SINGLE_GETTER_SUFFIX = "SingleGetter";

    private Element annotatedElement;

    private Map<Name, TypeElement> factoryAnnotatedClasses = new LinkedHashMap<>();

    public RxRealmFactory(Element annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    protected Element getAnnotatedElement() {
        return annotatedElement;
    }

    public void add(TypeElement classElement) throws ProcessingException {
        TypeElement existing = factoryAnnotatedClasses.get(classElement.getSimpleName());
        if (existing != null) {
            throw new ProcessingException(existing, "%s, already existing", classElement.getQualifiedName().toString());
        }
        factoryAnnotatedClasses.put(classElement.getSimpleName(), classElement);
    }

    private boolean isPrimaryKey(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType()
                    .asElement()
                    .getSimpleName()
                    .toString().equals(Classes.PRIMARY_KEY.simpleName())) {
                return true;
            }
        }
        return false;
    }

    private TypeSpec generateCreator(String packageName, String className) {
        final ClassName annotatedClass = ClassName.get(packageName, className);
        final ClassName creatorClass = ClassName.get(PACKAGE_NAME, className + CREATOR_SUFFIX);
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        TypeName primaryKeyType = null;
        String primaryKeyName = null;
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                if (isPrimaryKey(element)) {
                    primaryKeyType = TypeName.get(element.asType());
                    primaryKeyName = element.getSimpleName().toString();
                    break;
                }
            }
        }
        if (primaryKeyType == null && primaryKeyName == null) {
            return null;
        }
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addStatement("this.$N = $T.newLinkedHashMap()", "data", Classes.MAPS)
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(creatorClass)
                .addStatement("return new $T()", creatorClass)
                .build());
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                TypeName type = TypeName.get(element.asType());
                if (type.isPrimitive()
                        || type.isBoxedPrimitive()
                        || type.toString().equals(String.class.getName())) {
                    final String fieldName = element.getSimpleName().toString();
                    final String capitalizedFieldName = element.getSimpleName().toString().substring(0, 1)
                            .toUpperCase() + element.getSimpleName().toString().substring(1);
                    methodSpecs.add(MethodSpec.methodBuilder("set" + capitalizedFieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(element.asType()), fieldName)
                            .returns(creatorClass)
                            .addStatement("this.$N.put(\"$N\", $N)", "data", fieldName, fieldName)
                            .addStatement("return this")
                            .build());
                }
            }
        }
        methodSpecs.add(MethodSpec.methodBuilder("createOrUpdateObjectFromJson")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, annotatedClass))
                .addStatement("final $T asyncSubject = $T.create()", ParameterizedTypeName.get(Classes.ASYNC_SUBJECT, annotatedClass), Classes.ASYNC_SUBJECT)
                .addCode("if ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\n$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addCode("$N.executeTransactionAsync(new $T.Transaction() {", "temp", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) {", Classes.REALM)
                .addStatement("\n$N.onNext($N.copyFromRealm($N.createOrUpdateObjectFromJson($T.class, new $T($N))))", "asyncSubject", "realm", "realm", annotatedClass, Classes.JSON_OBJECT, "data")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnSuccess() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onSuccess() {")
                .addStatement("\n$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnError() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onError($T error) {", Classes.THROWABLE)
                .addStatement("\n$N.onError($N)", "asyncSubject", "error")
                .addCode("}")
                .addStatement("\n})")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\n$N.onError($N)", "asyncSubject", "e")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}")
                .addCode("\n} else {")
                .addCode("\n$T.executeTransaction(new $T.BetterTransaction() {", Classes.REALMS, Classes.REALMS)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) throws $T {", Classes.REALM, Classes.EXCEPTION)
                .addStatement("\n$N.onNext($N.createOrUpdateObjectFromJson($T.class, new $T($N)))", "asyncSubject", "realm", annotatedClass, Classes.JSON_OBJECT, "data")
                .addStatement("$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addStatement("\n})")
                .addCode("}")
                .addStatement("\nreturn $N", "asyncSubject")
                .build());
        final List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LINKED_HASH_MAP, ClassName.get(String.class), ClassName.get(Object.class)), "data",
                Modifier.PRIVATE, Modifier.FINAL).build());
        return TypeSpec.classBuilder(className + CREATOR_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();
    }

    private TypeSpec generateGetter(String packageName, String className) throws ClassNotFoundException {
        final ClassName annotatedClass = ClassName.get(packageName, className);
        final ClassName getterClass = ClassName.get(PACKAGE_NAME, className + GETTER_SUFFIX);
        final ClassName setterClass = ClassName.get(PACKAGE_NAME, className + SETTER_SUFFIX);
        final ClassName singerGetterClass = ClassName.get(PACKAGE_NAME, className + SINGLE_GETTER_SUFFIX);
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addStatement("this.$N = $T.newArrayList()", "commands", Classes.LISTS)
                .addStatement("this.$N = $T.newLinkedHashMap()", "sortFields", Classes.MAPS)
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(getterClass)
                .addStatement("return new $T()", getterClass)
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("acceptEmpty")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$N = true", "acceptEmpty")
                .returns(getterClass)
                .addStatement("return this")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("or")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$N.add(new $T())", "commands", CommandFactory.OR_COMMAND)
                .returns(getterClass)
                .addStatement("return this")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("first")
                .addModifiers(Modifier.PUBLIC)
                .returns(singerGetterClass)
                .addStatement("return new $T($N)", singerGetterClass, "commands")
                .build());
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                TypeName type = TypeName.get(element.asType());
                if (type.isPrimitive()
                        || type.isBoxedPrimitive()
                        || type.toString().equals(String.class.getName())) {
                    final String typeName = Class.forName(type.box().toString()).getSimpleName();
                    final String fieldName = element.getSimpleName().toString();
                    final String capitalizedFieldName = element.getSimpleName().toString().substring(0, 1)
                            .toUpperCase() + element.getSimpleName().toString().substring(1);
                    if (isPrimaryKey(element)) {
                        methodSpecs.add(MethodSpec.methodBuilder(fieldName + "EqualTo")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, fieldName)
                                .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.EQUAL_TO_COMMAND, fieldName,
                                        ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), fieldName)
                                .returns(singerGetterClass)
                                .addStatement("return new $T($N)", singerGetterClass, "commands")
                                .build());
                    } else {
                        methodSpecs.add(MethodSpec.methodBuilder(fieldName + "EqualTo")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, fieldName)
                                .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.EQUAL_TO_COMMAND, fieldName,
                                        ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), fieldName)
                                .returns(getterClass)
                                .addStatement("return this")
                                .build());
                    }
                    methodSpecs.add(MethodSpec.methodBuilder(fieldName + "NotEqualTo")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(type, fieldName)
                            .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.NOT_EQUAL_TO_COMMAND, fieldName,
                                    ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), fieldName)
                            .returns(getterClass)
                            .addStatement("return this")
                            .build());
                    methodSpecs.add(MethodSpec.methodBuilder(fieldName + "In")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ArrayTypeName.of(type.box()), fieldName + "Array")
                            .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.IN_COMMAND, fieldName,
                                    ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), fieldName + "Array")
                            .returns(getterClass)
                            .addStatement("return this")
                            .build());
                    methodSpecs.add(MethodSpec.methodBuilder(fieldName + "NotIn")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ArrayTypeName.of(type.box()), fieldName + "Array")
                            .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.NOT_IN_COMMAND, fieldName,
                                    ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), fieldName + "Array")
                            .returns(getterClass)
                            .addStatement("return this")
                            .build());
                    if (type.equals(TypeName.INT) ||
                            type.equals(TypeName.LONG) ||
                            type.equals(TypeName.DOUBLE) ||
                            type.equals(TypeName.FLOAT)) {
                        methodSpecs.add(MethodSpec.methodBuilder(fieldName + "LessThan")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, "value")
                                .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.LESS_THAN_COMMAND, fieldName,
                                        ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), "value")
                                .returns(getterClass)
                                .addStatement("return this")
                                .build());
                        methodSpecs.add(MethodSpec.methodBuilder(fieldName + "GreaterThan")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, "value")
                                .addStatement("$N.add(new $T(\"$N\", new $T($N)))", "commands", CommandFactory.GREATER_THAN_COMMAND, fieldName,
                                        ClassName.get(CommandFactory.EXECUTOR_PACKAGE_NAME, typeName + "Executor"), "value")
                                .returns(getterClass)
                                .addStatement("return this")
                                .build());
                    }
                    methodSpecs.add(MethodSpec.methodBuilder("sortBy" + capitalizedFieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("$N.put(\"$N\", $T.ASCENDING)", "sortFields", fieldName, Classes.SORT)
                            .returns(getterClass)
                            .addStatement("return this")
                            .build());
                    methodSpecs.add(MethodSpec.methodBuilder("sortBy" + capitalizedFieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(Classes.SORT, "sort")
                            .addStatement("$N.put(\"$N\", $N)", "sortFields", fieldName, "sort")
                            .returns(getterClass)
                            .addStatement("return this")
                            .build());
                }
            }
        }
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                if (isPrimaryKey(element)) {
                    methodSpecs.add(MethodSpec.methodBuilder("edit")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(setterClass)
                            .addStatement("return $T.with($N)", setterClass, "commands")
                            .build());
                    break;
                }
            }
        }
        methodSpecs.add(MethodSpec.methodBuilder("applyCommandsToRealmQuery")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass))
                .addStatement("$T realmQuery = $N.where($T.class)", ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realm", annotatedClass)
                .addCode("for ($T command : $N) {", CommandFactory.COMMAND, "commands")
                .addStatement("\n$N.execute($N)", "command", "realmQuery")
                .addCode("}")
                .addStatement("\nreturn realmQuery")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, ClassName.get(Boolean.class)))
                .addStatement("final $T asyncSubject = $T.create()", ParameterizedTypeName.get(Classes.ASYNC_SUBJECT, ClassName.get(Boolean.class)), Classes.ASYNC_SUBJECT)
                .addCode("if ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\n$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addCode("$N.executeTransactionAsync(new $T.Transaction() {", "temp", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) {", Classes.REALM)
                .addStatement("\nget(applyCommandsToRealmQuery($N)).deleteAllFromRealm()", "realm")
                .addStatement("$N.onNext(true)", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnSuccess() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onSuccess() {")
                .addStatement("\n$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnError() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onError($T error) {", Classes.THROWABLE)
                .addStatement("\n$N.onError($N)", "asyncSubject", "error")
                .addCode("}")
                .addStatement("\n})")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\n$N.onError($N)", "asyncSubject", "e")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}")
                .addCode("\n} else {")
                .addCode("\n$T.executeTransaction(new $T.BetterTransaction() {", Classes.REALMS, Classes.REALMS)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) throws $T {", Classes.REALM, Classes.EXCEPTION)
                .addStatement("\nget(applyCommandsToRealmQuery($N)).deleteAllFromRealm()", "realm")
                .addStatement("$N.onNext(true)", "asyncSubject")
                .addStatement("$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addStatement("\n})")
                .addCode("}")
                .addStatement("\nreturn $N", "asyncSubject")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("count")
                .addModifiers(Modifier.PUBLIC)
                .returns(Long.class)
                .addStatement("$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addStatement("return count(applyCommandsToRealmQuery($N))", "temp")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\nreturn 0l")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}\n")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("count")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realmQuery")
                .returns(Long.class)
                .addStatement("return $N.count()", "realmQuery")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Classes.LIST, annotatedClass))
                .addStatement("$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addStatement("$T realmResults = get(applyCommandsToRealmQuery($N))", ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass), "temp")
                .addStatement("$T copied = $T.newArrayList()", ParameterizedTypeName.get(Classes.LIST, annotatedClass), Classes.LISTS)
                .addCode("if ($N.size() > 0) {", "realmResults")
                .addStatement("\n$N.addAll($N.copyFromRealm($N))", "copied", "temp", "realmResults")
                .addCode("}")
                .addStatement("\nreturn $N", "copied")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\nreturn $T.newArrayList()", Classes.LISTS)
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}\n")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass))
                .addStatement("return get(applyCommandsToRealmQuery($N))", "realm")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realmQuery")
                .returns(ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass))
                .addCode("if ($N.size() > 0) {", "sortFields")
                .addStatement("\nreturn $N.findAllSorted($N.keySet().toArray(new String[$N.size()]), " +
                                "$N.values().toArray(new Sort[$N.size()]))",
                        "realmQuery", "sortFields", "sortFields", "sortFields", "sortFields")
                .addCode("} else {")
                .addStatement("\nreturn $N.findAll()", "realmQuery")
                .addCode("}\n")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("getAsync")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass)))
                .addCode("if ($N) {", "acceptEmpty")
                .addStatement("\nreturn getAsync(applyCommandsToRealmQuery($N))", "realm")
                .addCode("} else {")
                .addStatement("\nreturn getAsync(applyCommandsToRealmQuery($N)).filter($T.<$T>filterNotEmptyRealmResults())", "realm",
                        Classes.REALMS, ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass))
                .addCode("}\n")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("getAsync")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realmQuery")
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, ParameterizedTypeName.get(Classes.REALM_RESULTS, annotatedClass)))
                .addCode("if ($N.size() > 0) {", "sortFields")
                .addCode("\nif ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\nreturn $N.findAllSortedAsync($N.keySet().toArray(new String[$N.size()]), " +
                                "$N.values().toArray(new Sort[$N.size()]))" +
                                ".<$N>asObservable().filter($T.filterValidRealmResults())",
                        "realmQuery", "sortFields", "sortFields", "sortFields", "sortFields", className, Classes.REALMS)
                .addCode("} else {")
                .addStatement("\nreturn $T.just($N.findAllSorted($N.keySet().toArray(new String[$N.size()]), " +
                                "$N.values().toArray(new Sort[$N.size()])))",
                        Classes.OBSERVABLE, "realmQuery", "sortFields", "sortFields", "sortFields", "sortFields")
                .addCode("}")
                .addCode("\n} else {")
                .addCode("\nif ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\nreturn $N.findAllAsync()" +
                                ".<$N>asObservable().filter($T.filterValidRealmResults())",
                        "realmQuery", className, Classes.REALMS)
                .addCode("} else {")
                .addStatement("\nreturn $T.just($N.findAll())", Classes.OBSERVABLE, "realmQuery")
                .addCode("}")
                .addCode("\n}\n")
                .build());
        final List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec.builder(TypeName.BOOLEAN, "acceptEmpty",
                Modifier.PRIVATE).build());
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands",
                Modifier.PRIVATE,
                Modifier.FINAL).build());
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LINKED_HASH_MAP, ClassName.get(String.class), Classes.SORT), "sortFields",
                Modifier.PRIVATE,
                Modifier.FINAL).build());
        return TypeSpec.classBuilder(className + GETTER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();
    }

    private TypeSpec generateSetter(String packageName, String className) {
        final ClassName annotatedClass = ClassName.get(packageName, className);
        final ClassName setterClass = ClassName.get(PACKAGE_NAME, className + SETTER_SUFFIX);
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        TypeName primaryKeyType = null;
        String primaryKeyName = null;
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                if (isPrimaryKey(element)) {
                    primaryKeyType = TypeName.get(element.asType());
                    primaryKeyName = element.getSimpleName().toString();
                    break;
                }
            }
        }
        if (primaryKeyType == null && primaryKeyName == null) {
            return null;
        }
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands")
                .addStatement("this.$N = $T.newLinkedHashMap()", "data", Classes.MAPS)
                .addStatement("this.$N = $N", "commands", "commands")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                .addParameter(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands")
                .returns(setterClass)
                .addStatement("return new $T($N)", setterClass, "commands")
                .build());
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                TypeName type = TypeName.get(element.asType());
                if (type.isPrimitive()
                        || type.isBoxedPrimitive()
                        || type.toString().equals(String.class.getName())) {
                    if (isPrimaryKey(element)) {
                        continue;
                    }
                    final String fieldName = element.getSimpleName().toString();
                    final String capitalizedFieldName = element.getSimpleName().toString().substring(0, 1)
                            .toUpperCase() + element.getSimpleName().toString().substring(1);
                    methodSpecs.add(MethodSpec.methodBuilder("set" + capitalizedFieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(element.asType()), fieldName)
                            .returns(setterClass)
                            .addStatement("this.$N.put(\"$N\", $N)", "data", fieldName, fieldName)
                            .addStatement("return this")
                            .build());
                }
            }
        }
        methodSpecs.add(MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, ParameterizedTypeName.get(Classes.LIST, annotatedClass)))
                .addStatement("final $T asyncSubject = $T.create()", ParameterizedTypeName.get(Classes.ASYNC_SUBJECT, ParameterizedTypeName.get(Classes.LIST, annotatedClass)), Classes.ASYNC_SUBJECT)
                .addCode("if ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\n$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addCode("$N.executeTransactionAsync(new $T.Transaction() {", "temp", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) {", Classes.REALM)
                .addStatement("\n$N.onNext(applyChanges($N))", "asyncSubject", "realm")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnSuccess() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onSuccess() {")
                .addStatement("\n$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnError() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onError($T error) {", Classes.THROWABLE)
                .addStatement("\n$N.onError($N)", "asyncSubject", "error")
                .addCode("}")
                .addStatement("\n})")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\n$N.onError($N)", "asyncSubject", "e")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}")
                .addCode("\n} else {")
                .addCode("\n$T.executeTransaction(new $T.BetterTransaction() {", Classes.REALMS, Classes.REALMS)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) throws $T {", Classes.REALM, Classes.EXCEPTION)
                .addStatement("\n$N.onNext(applyChanges($N))", "asyncSubject", "realm")
                .addStatement("$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addStatement("\n})")
                .addCode("}")
                .addStatement("\nreturn $N", "asyncSubject")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("applyChanges")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.LIST, annotatedClass))
                .addStatement("$T realmQuery = $N.where($T.class)", ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realm", annotatedClass)
                .addCode("for ($T command : $N) {", CommandFactory.COMMAND, "commands")
                .addStatement("\n$N.execute($N)", "command", "realmQuery")
                .addCode("}")
                .addStatement("\n$T realmList = $T.newArrayList()", ParameterizedTypeName.get(Classes.LIST, annotatedClass), Classes.LISTS)
                .addCode("for ($T db : $N.findAll()) {", annotatedClass, "realmQuery")
                .addStatement("\n$N.put(\"$N\", db.get$N())", "data", primaryKeyName, primaryKeyName.substring(0, 1).toUpperCase() + primaryKeyName.substring(1))
                .addStatement("$T json = new $T($N)", Classes.JSON_OBJECT, Classes.JSON_OBJECT, "data")
                .addStatement("$N.add($N.copyFromRealm($N.createOrUpdateObjectFromJson($T.class, $N)))", "realmList", "realm", "realm", annotatedClass, "json")
                .addCode("}")
                .addStatement("\nreturn realmList")
                .build());
        final List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LINKED_HASH_MAP, ClassName.get(String.class), ClassName.get(Object.class)), "data",
                Modifier.PRIVATE, Modifier.FINAL).build());
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands",
                Modifier.PRIVATE).build());
        return TypeSpec.classBuilder(className + SETTER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();
    }

    private TypeSpec generateSingleGetter(String packageName, String className) throws IOException {
        final ClassName annotatedClass = ClassName.get(packageName, className);
        final ClassName setterClass = ClassName.get(PACKAGE_NAME, className + SETTER_SUFFIX);
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands")
                .addStatement("this.$N = $N", "commands", "commands")
                .build());
        for (Element element : annotatedElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                if (isPrimaryKey(element)) {
                    methodSpecs.add(MethodSpec.methodBuilder("edit")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(setterClass)
                            .addStatement("return $T.with($N)", setterClass, "commands")
                            .build());
                    break;
                }
            }
        }
        methodSpecs.add(MethodSpec.methodBuilder("applyCommandsToRealmQuery")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass))
                .addStatement("$T realmQuery = $N.where($T.class)", ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realm", annotatedClass)
                .addCode("for ($T command : $N) {", CommandFactory.COMMAND, "commands")
                .addStatement("\n$N.execute($N)", "command", "realmQuery")
                .addCode("}")
                .addStatement("\nreturn realmQuery")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, ClassName.get(Boolean.class)))
                .addStatement("final $T asyncSubject = $T.create()", ParameterizedTypeName.get(Classes.ASYNC_SUBJECT, ClassName.get(Boolean.class)), Classes.ASYNC_SUBJECT)
                .addCode("if ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\n$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addCode("$N.executeTransactionAsync(new $T.Transaction() {", "temp", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) {", Classes.REALM)
                .addStatement("\nget(applyCommandsToRealmQuery($N)).deleteFromRealm()", "realm")
                .addStatement("$N.onNext(true)", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnSuccess() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onSuccess() {")
                .addStatement("\n$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addCode("\n}, new $T.Transaction.OnError() {", Classes.REALM)
                .addCode("\n@Override")
                .addCode("\npublic void onError($T error) {", Classes.THROWABLE)
                .addStatement("\n$N.onError($N)", "asyncSubject", "error")
                .addCode("}")
                .addStatement("\n})")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\n$N.onError($N)", "asyncSubject", "e")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}")
                .addCode("\n} else {")
                .addCode("\n$T.executeTransaction(new $T.BetterTransaction() {", Classes.REALMS, Classes.REALMS)
                .addCode("\n@Override")
                .addCode("\npublic void execute($T realm) throws $T {", Classes.REALM, Classes.EXCEPTION)
                .addStatement("\nget(applyCommandsToRealmQuery($N)).deleteFromRealm()", "realm")
                .addStatement("$N.onNext(true)", "asyncSubject")
                .addStatement("$N.onCompleted()", "asyncSubject")
                .addCode("}")
                .addStatement("\n})")
                .addCode("}")
                .addStatement("\nreturn $N", "asyncSubject")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addAnnotation(Classes.NULLABLE)
                .addModifiers(Modifier.PUBLIC)
                .returns(annotatedClass)
                .addStatement("$T temp = null", Classes.REALM)
                .addCode("try {")
                .addStatement("\n$N = $T.get()", "temp", Classes.REALMS)
                .addStatement("$T realmObject = get(applyCommandsToRealmQuery($N))", annotatedClass, "temp")
                .addStatement("$T copied = null", annotatedClass)
                .addCode("if ($N != null) {", "realmObject")
                .addStatement("\n$N = $N.copyFromRealm($N)", "copied", "temp", "realmObject")
                .addCode("}")
                .addStatement("\nreturn $N", "copied")
                .addCode("} catch ($T e) {", Classes.EXCEPTION)
                .addStatement("\nreturn null")
                .addCode("} finally {")
                .addStatement("\n$T.close($N)", Classes.REALMS, "temp")
                .addCode("}\n")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addAnnotation(Classes.NULLABLE)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM, "realm")
                .returns(annotatedClass)
                .addStatement("return get(applyCommandsToRealmQuery($N))", "realm")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realmQuery")
                .returns(annotatedClass)
                .addStatement("return $N.findFirst()", "realmQuery")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("getAsync")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM, "realm")
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, annotatedClass))
                .addStatement("return getAsync(applyCommandsToRealmQuery($N)).filter($T.<$T>filterNotNullRealmObject())", "realm", Classes.REALMS, annotatedClass)
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("getAsync")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(Classes.REALM_QUERY, annotatedClass), "realmQuery")
                .returns(ParameterizedTypeName.get(Classes.OBSERVABLE, annotatedClass))
                .addCode("if ($T.isMainThread()) {", Classes.THREADS)
                .addStatement("\nreturn $N.findFirstAsync().<$T>asObservable().filter($T.filterValidRealmObject())", "realmQuery", annotatedClass, Classes.REALMS)
                .addCode("} else {")
                .addStatement("\nreturn $T.just($N.findFirst())", Classes.OBSERVABLE, "realmQuery")
                .addCode("}\n")
                .build());
        final List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec.builder(ParameterizedTypeName.get(Classes.LIST, CommandFactory.COMMAND), "commands",
                Modifier.PRIVATE,
                Modifier.FINAL).build());
        return TypeSpec.classBuilder(className + SINGLE_GETTER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();
    }

    public void generate(Elements elementUtils, Filer filer) throws IOException, ClassNotFoundException {
        final String packageName = elementUtils.getPackageOf(annotatedElement).toString();
        final String className = annotatedElement.getSimpleName().toString();
        final TypeSpec creator = generateCreator(packageName, className);
        final TypeSpec singerGetter = generateSingleGetter(packageName, className);
        final TypeSpec getter = generateGetter(packageName, className);
        final TypeSpec setter = generateSetter(packageName, className);
        if (creator != null) {
            JavaFile.builder(PACKAGE_NAME, creator).build().writeTo(filer);
        }
        if (singerGetter != null) {
            JavaFile.builder(PACKAGE_NAME, singerGetter).build().writeTo(filer);
        }
        if (getter != null) {
            JavaFile.builder(PACKAGE_NAME, getter).build().writeTo(filer);
        }
        if (setter != null) {
            JavaFile.builder(PACKAGE_NAME, setter).build().writeTo(filer);
        }
    }
}
