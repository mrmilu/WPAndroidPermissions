package org.webpartners.wpandroidpermissions.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.webpartners.wpandroidpermissions.annotations.HasFragmentsWithPermissions;
import org.webpartners.wpandroidpermissions.annotations.HasRuntimePermissions;
import org.webpartners.wpandroidpermissions.annotations.NeedPermissions;
import org.webpartners.wpandroidpermissions.interfaces.AskForPermission;
import org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse;

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
    public boolean process(Set<? extends TypeElement> annotaions, RoundEnvironment env) {
        Set<? extends Element> elements = env.getElementsAnnotatedWith(HasRuntimePermissions.class);
        for (Element element : elements) {
            JavaFile javaFile = this.createFragment((TypeElement) element);
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        elements.clear();
        elements = env.getElementsAnnotatedWith(HasFragmentsWithPermissions.class);
        for (Element element : elements) {
            JavaFile javaFile = this.createActivity((TypeElement) element);
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private MethodSpec addOnRequestPermissionResultMethod(String methodLiteral) {
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
                                + "if (act.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {\n"
                                + "\tresponse.permissionDenied();\n"
                                + "\treturn;\n"
                                + "} else if (act.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {\n"
                                + "\tresponse.permissionAllowed();\n"
                                + "\treturn;\n}\n\n"
                                + "act.requestPermissions(new String[]{permission}, 20734);"
                )
                .build();
    }

    private ArrayList<MethodSpec> cloneMethodsWithPermissions(TypeElement typeElement) {
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
                methods.add(MethodSpec.methodBuilder(elem.getSimpleName().toString()+"_generated")
                        .addModifiers(elem.getModifiers())
                        .addParameter(TypeName.BOOLEAN, "permissionChecked")
                        .returns(void.class)
                        .addStatement("if (!permissionChecked) ((org.webpartners.wpandroidpermissions.interfaces.AskForPermission)getActivity()).askForPermission($S, $L)", annotation.value(), callback)
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
        return TypeSpec.classBuilder(typeElement.getSimpleName() + "_Generated")
                .addModifiers(Modifier.PUBLIC)
                .superclass(TypeName.get(typeElement.asType()))
                .addSuperinterface(PermissionRequestResponse.class)
                .addMethods(methods)
                .build();
    }

    private TypeSpec createExtendedActivity(TypeElement typeElement, ArrayList<MethodSpec> methods) {
        return TypeSpec.classBuilder(typeElement.getSimpleName() + "_Generated")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(TypeName.get(typeElement.asType()))
                .addSuperinterface(AskForPermission.class)
                .addMethods(methods)
                .build();
    }

    private JavaFile createFragment(TypeElement typeElement) {
        ArrayList<MethodSpec> methods = new ArrayList<>();

        methods.addAll(this.cloneMethodsWithPermissions(typeElement));

        TypeSpec myClass = this.createExtendedFragment(typeElement, methods);

        return JavaFile.builder(
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""),
                myClass).build();
    }

    private JavaFile createActivity(TypeElement typeElement) {
        ArrayList<MethodSpec> methods = new ArrayList<>();

        methods.add(this.addOnRequestPermissionResultMethod(this.methodLiteral));

        methods.add(this.addCheckPermissionMethod(true));

        TypeSpec myClass = this.createExtendedActivity(typeElement, methods);

        return JavaFile.builder(
                typeElement.getQualifiedName().toString().replace("."+typeElement.getSimpleName(), ""),
                myClass).build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(HasRuntimePermissions.class.getCanonicalName(), HasFragmentsWithPermissions.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

