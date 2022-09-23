package com.personoid.api.npc.injection;

import com.personoid.api.utils.Parameter;

public class InjectionInfo {
    private final Parameter parameter;

    public InjectionInfo() {
        this.parameter = null;
    }

    public InjectionInfo(Parameter parameter) {
        this.parameter = parameter;
    }

    public boolean isModified() {
        return parameter != null;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
