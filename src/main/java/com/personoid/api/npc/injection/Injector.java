package com.personoid.api.npc.injection;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.Parameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Injector {
    private final NPC npc;

    public Injector(NPC npc) {
        this.npc = npc;
    }

    public void callHook(String hook, Object... args) {
        for (Feature feature : npc.getFeatures()) {
            for (Method method : feature.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Hook.class)) {
                    Hook methodHook = method.getAnnotation(Hook.class);
                    if (methodHook.value().equals(hook)) {
                        try {
                            method.invoke(feature, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public InjectionInfo callHookReturn(String hook, CallbackInfo info, Object... args) {
        for (Feature feature : npc.getFeatures()) {
            for (Method method : feature.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Hook.class)) {
                    Hook methodHook = method.getAnnotation(Hook.class);
                    if (methodHook.value().equals(hook)) {
                        try {
                            Object[] newArgs = new Object[args.length + 1];
                            System.arraycopy(args, 0, newArgs, 0, args.length);
                            newArgs[args.length] = info;
                            method.invoke(feature, newArgs);
                            return new InjectionInfo(new Parameter(info.getReturnType(), info.getReturnValue()));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return new InjectionInfo();
    }
}
