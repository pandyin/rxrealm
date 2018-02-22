package com.intathep.rxrealm;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Date;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class CommandFactory {

    protected static final String COMMAND_PACKAGE_NAME = "com.intathep.android.rxrealm.command";
    protected static final String EXECUTOR_PACKAGE_NAME = "com.intathep.android.rxrealm.executor";
    protected static final ClassName COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "Command");
    protected static final ClassName KEY_VALUE_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "KeyValueCommand");
    protected static final ClassName KEY_VALUES_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "KeyValuesCommand");
    protected static final ClassName OR_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "OrCommand");
    protected static final ClassName EQUAL_TO_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "EqualToCommand");
    protected static final ClassName NOT_EQUAL_TO_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "NotEqualToCommand");
    protected static final ClassName IN_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "InCommand");
    protected static final ClassName NOT_IN_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "NotInCommand");
    protected static final ClassName LESS_THAN_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "LessThanCommand");
    protected static final ClassName GREATER_THAN_COMMAND = ClassName.get(COMMAND_PACKAGE_NAME, "GreaterThanCommand");
    protected static final ClassName EXECUTOR = ClassName.get(EXECUTOR_PACKAGE_NAME, "Executor");

    private TypeSpec generateCommand() {
        MethodSpec execute = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .build();
        return TypeSpec.classBuilder("Command")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addMethod(execute)
                .build();
    }

    private TypeSpec generateKeyValueCommand(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("this.$N = $N", "key", "key")
                .addStatement("this.$N = $N", "executor", "executor")
                .build();
        return TypeSpec.classBuilder("KeyValueCommand")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(superClass)
                .addField(FieldSpec.builder(String.class, "key", Modifier.FINAL).build())
                .addField(FieldSpec.builder(EXECUTOR, "executor", Modifier.FINAL).build())
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(String.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Byte.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Short.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Integer.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute").addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Long.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute").addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Double.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute").addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Float.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute").addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Boolean.class, "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute").addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(Date.class, "value")
                        .build())
                .build();
    }

    private TypeSpec generateKeyValuesCommand(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("this.$N = $N", "key", "key")
                .addStatement("this.$N = $N", "executor", "executor")
                .build();
        return TypeSpec.classBuilder("KeyValuesCommand")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(superClass)
                .addField(FieldSpec.builder(String.class, "key", Modifier.FINAL).build())
                .addField(FieldSpec.builder(EXECUTOR, "executor", Modifier.FINAL).build())
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(String.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Byte.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Short.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Integer.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Long.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Double.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Float.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Boolean.class), "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(ArrayTypeName.of(Date.class), "values")
                        .build())
                .build();
    }

    private TypeSpec generateOr(ClassName superClass) {
        MethodSpec execute = MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addStatement("$N.or()", "realmQuery")
                .build();
        return TypeSpec.classBuilder("OrCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(execute)
                .build();
    }

    private TypeSpec generateEqualTo(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("EqualToCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateEqualToExecution(String.class))
                .addMethod(generateEqualToExecution(Byte.class))
                .addMethod(generateEqualToExecution(Short.class))
                .addMethod(generateEqualToExecution(Integer.class))
                .addMethod(generateEqualToExecution(Long.class))
                .addMethod(generateEqualToExecution(Double.class))
                .addMethod(generateEqualToExecution(Float.class))
                .addMethod(generateEqualToExecution(Boolean.class))
                .addMethod(generateEqualToExecution(Date.class))
                .build();
    }

    private MethodSpec generateEqualToExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ClassName.get(clazz), "value")
                .addStatement("$N.equalTo($N, $N)", "realmQuery", "key", "value")
                .build();
    }

    private TypeSpec generateNotEqualTo(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("NotEqualToCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateNotEqualToExecution(String.class))
                .addMethod(generateNotEqualToExecution(Byte.class))
                .addMethod(generateNotEqualToExecution(Short.class))
                .addMethod(generateNotEqualToExecution(Integer.class))
                .addMethod(generateNotEqualToExecution(Long.class))
                .addMethod(generateNotEqualToExecution(Double.class))
                .addMethod(generateNotEqualToExecution(Float.class))
                .addMethod(generateNotEqualToExecution(Boolean.class))
                .addMethod(generateNotEqualToExecution(Date.class))
                .build();
    }

    private MethodSpec generateNotEqualToExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ClassName.get(clazz), "value")
                .addStatement("$N.notEqualTo($N, $N)", "realmQuery", "key", "value")
                .build();
    }

    private TypeSpec generateIn(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("InCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateInExecution(String.class))
                .addMethod(generateInExecution(Byte.class))
                .addMethod(generateInExecution(Short.class))
                .addMethod(generateInExecution(Integer.class))
                .addMethod(generateInExecution(Long.class))
                .addMethod(generateInExecution(Double.class))
                .addMethod(generateInExecution(Float.class))
                .addMethod(generateInExecution(Boolean.class))
                .addMethod(generateInExecution(Date.class))
                .build();
    }

    private MethodSpec generateInExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ArrayTypeName.of(ClassName.get(clazz)), "values")
                .addStatement("$N.in($N, $N)", "realmQuery", "key", "values")
                .build();
    }

    private TypeSpec generateNotIn(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("NotInCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateNotInExecution(String.class))
                .addMethod(generateNotInExecution(Byte.class))
                .addMethod(generateNotInExecution(Short.class))
                .addMethod(generateNotInExecution(Integer.class))
                .addMethod(generateNotInExecution(Long.class))
                .addMethod(generateNotInExecution(Double.class))
                .addMethod(generateNotInExecution(Float.class))
                .addMethod(generateNotInExecution(Boolean.class))
                .addMethod(generateNotInExecution(Date.class))
                .build();
    }

    private MethodSpec generateNotInExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ArrayTypeName.of(ClassName.get(clazz)), "values")
                .addStatement("$N.not().in($N, $N)", "realmQuery", "key", "values")
                .build();
    }

    private TypeSpec generateLessThan(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("LessThanCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateLessThanExecution(Integer.class))
                .addMethod(generateLessThanExecution(Long.class))
                .addMethod(generateLessThanExecution(Double.class))
                .addMethod(generateLessThanExecution(Float.class))
                .build();
    }

    private MethodSpec generateLessThanExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ClassName.get(clazz), "value")
                .addStatement("$N.lessThan($N, $N)", "realmQuery", "key", "value")
                .build();
    }

    private TypeSpec generateGreaterThan(ClassName superClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(EXECUTOR, "executor")
                .addStatement("super($N, $N)", "key", "executor")
                .build();
        return TypeSpec.classBuilder("GreaterThanCommand")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addMethod(constructor)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addStatement("$N.execute($N, this)", "executor", "realmQuery")
                        .build())
                .addMethod(generateGreaterThanExecution(Integer.class))
                .addMethod(generateGreaterThanExecution(Long.class))
                .addMethod(generateGreaterThanExecution(Double.class))
                .addMethod(generateGreaterThanExecution(Float.class))
                .build();
    }

    private MethodSpec generateGreaterThanExecution(Class clazz) {
        return MethodSpec.methodBuilder("execute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Classes.REALM_QUERY, "realmQuery")
                .addParameter(ClassName.get(clazz), "value")
                .addStatement("$N.greaterThan($N, $N)", "realmQuery", "key", "value")
                .build();
    }

    private TypeSpec generateExecutor() {
        return TypeSpec.classBuilder("Executor")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(KEY_VALUE_COMMAND, "command")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(KEY_VALUES_COMMAND, "command")
                        .build())
                .build();
    }

    private TypeSpec generateExecutor(ClassName superClass, Class clazz) {
        return TypeSpec.classBuilder(clazz.getSimpleName() + "Executor")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addField(FieldSpec.builder(ClassName.get(clazz), "value").build())
                .addField(FieldSpec.builder(ArrayTypeName.of(ClassName.get(clazz)), "values").build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(clazz), "value")
                        .addStatement("this.$N = $N", "value", "value")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ArrayTypeName.of(ClassName.get(clazz)), "values")
                        .addStatement("this.$N = $N", "values", "values")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(KEY_VALUE_COMMAND, "command")
                        .addStatement("$N.execute($N, $N)", "command", "realmQuery", "value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Classes.REALM_QUERY, "realmQuery")
                        .addParameter(KEY_VALUES_COMMAND, "command")
                        .addStatement("$N.execute($N, $N)", "command", "realmQuery", "values")
                        .build())
                .build();
    }

    public void generate(Filer filer) throws IOException {
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateCommand()).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateKeyValueCommand(COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateKeyValuesCommand(COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateOr(COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateEqualTo(KEY_VALUE_COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateNotEqualTo(KEY_VALUE_COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateIn(KEY_VALUES_COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateNotIn(KEY_VALUES_COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateLessThan(KEY_VALUE_COMMAND)).build().writeTo(filer);
        JavaFile.builder(COMMAND_PACKAGE_NAME, generateGreaterThan(KEY_VALUE_COMMAND)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor()).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, String.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Byte.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Short.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Integer.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Long.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Double.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Float.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Boolean.class)).build().writeTo(filer);
        JavaFile.builder(EXECUTOR_PACKAGE_NAME, generateExecutor(EXECUTOR, Date.class)).build().writeTo(filer);
    }
}
