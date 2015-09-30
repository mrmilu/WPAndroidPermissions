package org.webpartners.wpandroidpermissions.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.webpartners.wpandroidpermissions.annotations.*;
import org.webpartners.wpandroidpermissions.interfaces.*;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by Jorge Garrido Oval on 19/08/15.
 */
@AutoService(Processor.class)
public class PermissionProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private String methodLiteral;

    @Override
    public synchronized void init(ProcessingEnvironment env){
        super.init(env);
        this.filer = env.getFiler();
        this.messager = env.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(
                ActivityWithRuntimePermissions.class.getCanonicalName(),
                FragmentWithRuntimePermissions.class.getCanonicalName(),
                HostFragmentWithPermissions.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element :  env.getElementsAnnotatedWith(ActivityWithRuntimePermissions.class)) {
            JavaFile javaFile = this.createActivity((TypeElement) element);
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }

        for (Element element : env.getElementsAnnotatedWith(FragmentWithRuntimePermissions.class)) {
            JavaFile javaFile = this.createFragment((TypeElement) element);
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }

        for (Element element : env.getElementsAnnotatedWith(HostFragmentWithPermissions.class)) {
            JavaFile javaFile = this.createHostActivity((TypeElement) element);
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }

        return true;
    }

    private MethodSpec addOnRequestPermissionResultMethod() {
        messager.printMessage(Diagnostic.Kind.NOTE, "Generating addOnRequestPermissionResultMethod");

        return MethodSpec.methodBuilder("onRequestPermissionsResult")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(int.class, "requestCode")
                .addParameter(String[].class, "permissions")
                .addParameter(int[].class, "grantResults")
                .addCode(""
                        + "super.onRequestPermissionsResult(requestCode, permissions, grantResults);\n\n"
                        + "if (this instanceof org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse)\n"
                        + "\tif (grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED)\n"
                        + "\t\t((org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse)this).permissionAllowed();\n"
                        + "\telse\n"
                        + "\t\t((org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse)this).permissionDenied();\n"
                )
                .build();
    }

    private MethodSpec addCheckPermissionMethod(boolean isActivity) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Generating addCheckPermissionMethod");

        return MethodSpec.methodBuilder("askForPermission")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(String.class, "permission")
                .addParameter(PermissionRequestResponse.class, "response")
                .addCode(""
                                + "if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {\n"
                                + "\tresponse.permissionAllowed();\n"
                                + "\treturn;\n}\n\n"
                                + "android.support.v7.app.AppCompatActivity act = " + ((isActivity) ? "this" : "this.getActivity()") + ";\n"
                                + "int perm = android.support.v4.content.ContextCompat.checkSelfPermission(act, permission);\n"
                                + "if (perm == android.content.pm.PackageManager.PERMISSION_DENIED) {\n"
                                + "\tresponse.permissionDenied();\n"
                                + "\tact.requestPermissions(new String[]{permission}, 20734);\n"
                                + "\treturn;\n"
                                + "} else if (perm == android.content.pm.PackageManager.PERMISSION_GRANTED) {\n"
                                + "\tresponse.permissionAllowed();\n"
                                + "\treturn;\n}"
                )
                .build();
    }

    private ArrayList<MethodSpec> cloneMethodsWithPermissions(TypeElement typeElement, boolean isActivity) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Generating cloneMethodsWithPermissions");

        ArrayList<MethodSpec> methods = new ArrayList<>();
        for (Element elem :typeElement.getEnclosedElements()) {
            NeedPermissions annotation = elem.getAnnotation(NeedPermissions.class);
            if (annotation != null) {
                String methodLiteralFalse = "this." + elem.getSimpleName().toString() + "_generated(false)";

                // clone
                methods.add(MethodSpec.methodBuilder(elem.getSimpleName().toString())
                        .addModifiers(elem.getModifiers())
                        .addAnnotation(Override.class)
                        .returns(void.class)
                        .addStatement(methodLiteralFalse)
                        .build());

                // extend:callback
                TypeSpec callback = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(PermissionRequestResponse.class)
                        .addMethod(MethodSpec.methodBuilder("permissionAllowed")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addStatement("$N.this.permissionAllowed()", typeElement.getSimpleName().toString()+"_Generated")
                                .build())
                        .addMethod(MethodSpec.methodBuilder("permissionDenied")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addStatement("$N.this.permissionDenied()", typeElement.getSimpleName().toString() + "_Generated")
                                .build())
                        .build();

                // extend
                String root = (isActivity)? "this" :  "getActivity()";
                methods.add(MethodSpec.methodBuilder(elem.getSimpleName().toString() + "_generated")
                        .addModifiers(elem.getModifiers())
                        .addParameter(TypeName.BOOLEAN, "permissionChecked")
                        .returns(void.class)
                        .addStatement("if (!permissionChecked) ((org.webpartners.wpandroidpermissions.interfaces.AskForPermission)$L).askForPermission($S, $L)", root, annotation.value(), callback)
                        .addStatement("else super.$N()", elem.getSimpleName())
                        .build());
                methodLiteral = "this." + elem.getSimpleName().toString() + "_generated(true)";
            }
        }

        methods.add(MethodSpec.methodBuilder("permissionAllowed")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("super.permissionAllowed()")
                .addStatement(methodLiteral)
                .build());

        methods.add(MethodSpec.methodBuilder("permissionDenied")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("super.permissionDenied()")
                .returns(void.class)
                .build());

        return methods;
    }

    private TypeSpec createExtendedFragment(TypeElement typeElement, ArrayList<MethodSpec> methods) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Saving extended fragment file...");
        return TypeSpec.classBuilder(typeElement.getSimpleName() + "_Generated")
                .addModifiers(Modifier.PUBLIC)
                .superclass(TypeName.get(typeElement.asType()))
                .addSuperinterface(PermissionRequestResponse.class)
                .addMethods(methods)
                .build();
    }

    private TypeSpec createExtendedHostActivity(TypeElement typeElement, ArrayList<MethodSpec> methods) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Saving host activity file...");
        return TypeSpec.classBuilder(typeElement.getSimpleName() + "_Generated")
                .addModifiers(Modifier.PUBLIC)
                .superclass(TypeName.get(typeElement.asType()))
                .addSuperinterface(AskForPermission.class)
                .addMethods(methods)
                .build();
    }

    private TypeSpec createExtendedActivity(TypeElement typeElement, ArrayList<MethodSpec> methods) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Saving extended activity file...");
        return TypeSpec.classBuilder(typeElement.getSimpleName() + "_Generated")
                .addModifiers(Modifier.PUBLIC)
                .superclass(TypeName.get(typeElement.asType()))
                .addSuperinterface(AskForPermission.class)
                .addSuperinterface(PermissionRequestResponse.class)
                .addMethods(methods)
                .build();
    }

    private JavaFile createFragment(TypeElement typeElement) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating fragment...");

        ArrayList<MethodSpec> methods = new ArrayList<>();

        methods.addAll(this.cloneMethodsWithPermissions(typeElement, false));

        TypeSpec myClass = this.createExtendedFragment(typeElement, methods);

        messager.printMessage(Diagnostic.Kind.NOTE, "Save location: " +
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""));
        return JavaFile.builder(
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""),
                myClass).build();
    }

    private JavaFile createActivity(TypeElement typeElement) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating activity...");

        ArrayList<MethodSpec> methods = new ArrayList<>();

        methods.addAll(this.cloneMethodsWithPermissions(typeElement, true));

        methods.add(this.addOnRequestPermissionResultMethod());

        methods.add(this.addCheckPermissionMethod(true));

        TypeSpec myClass = this.createExtendedActivity(typeElement, methods);

        messager.printMessage(Diagnostic.Kind.NOTE, "Save location: " +
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""));
        return JavaFile.builder(
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""),
                myClass).build();
    }

    private JavaFile createHostActivity(TypeElement typeElement) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating host activity...");

        ArrayList<MethodSpec> methods = new ArrayList<>();

        methods.add(this.addOnRequestPermissionResultMethod());

        methods.add(this.addCheckPermissionMethod(true));

        TypeSpec myClass = this.createExtendedHostActivity(typeElement, methods);

        messager.printMessage(Diagnostic.Kind.NOTE, "Save location: " +
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""));
        return JavaFile.builder(
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""),
                myClass).build();
    }

}

