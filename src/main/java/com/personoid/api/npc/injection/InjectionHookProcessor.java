package com.personoid.api.npc.injection;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.personoid.api.npc.injection.Hook")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InjectionHookProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Hook.class)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Hook at " + element);
        }
        return true;
    }

    private void printHookError(Element element, String hookName) {
        boolean returns = returns(hookName);
        String returnType = returns ? "CallbackInfoReturn" : "CallbackInfo";
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Hook method requires " + returnType, element);
    }

    public boolean returns(String hookName) {
        switch (hookName) {
            case "tick": return false;
            case "onDamage": return true;
            default: return false;
        }
    }
}
