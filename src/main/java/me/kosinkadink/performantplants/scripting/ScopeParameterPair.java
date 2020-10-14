package me.kosinkadink.performantplants.scripting;

import java.util.Objects;

public class ScopeParameterPair {

    private final String scope;
    private final String parameter;

    public ScopeParameterPair(String scope, String parameter) {
        this.scope = scope;
        this.parameter = parameter;
    }

    public String getScope() {
        return scope;
    }

    public String getParameter() {
        return parameter;
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        ScopeParameterPair fromO = (ScopeParameterPair)o;
        // true if all components match, false otherwise
        return scope.equals(fromO.scope) && parameter.equals(fromO.parameter);
    }

    public int hashCode() {
        return Objects.hash(scope, parameter);
    }

    @Override
    public String toString() {
        return scope + ',' + parameter;
    }

}
