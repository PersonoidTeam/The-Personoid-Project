package com.personoid.nms.mappings;

import java.util.List;
import java.util.Map;

public class MappedClass {
    private Map<String, MappedMethod> methods;
    private Map<String, MappedField> fields;
    private List<MappedConstructor> constructors;

    public MappedClass(Map<String, MappedMethod> methods, Map<String, MappedField> fields, List<MappedConstructor> constructors) {
        this.methods = methods;
        this.fields = fields;
        this.constructors = constructors;
    }

    public Map<String, MappedMethod> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MappedMethod> methods) {
        this.methods = methods;
    }

    public Map<String, MappedField> getFields() {
        return fields;
    }

    public void setFields(Map<String, MappedField> fields) {
        this.fields = fields;
    }

    public List<MappedConstructor> getConstructors() {
        return constructors;
    }

    public void setConstructors(List<MappedConstructor> constructors) {
        this.constructors = constructors;
    }
}
