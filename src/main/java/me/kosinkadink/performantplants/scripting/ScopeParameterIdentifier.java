package me.kosinkadink.performantplants.scripting;

import java.util.Objects;

public class ScopeParameterIdentifier {

    private final String plantId;
    private final String scope;
    private final String parameter;

    public ScopeParameterIdentifier(String plantId, String scope, String parameter) {
        this.plantId = plantId;
        this.scope = scope;
        this.parameter = parameter;
    }

    public String getPlantId() {
        return plantId;
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
        ScopeParameterIdentifier fromO = (ScopeParameterIdentifier)o;
        // true if all components match, false otherwise
        return plantId.equals(fromO.plantId) && scope.equals(fromO.scope) && parameter.equals(fromO.parameter);
    }

    public int hashCode() {
        return Objects.hash(plantId, scope, parameter);
    }

    @Override
    public String toString() {
        return plantId + ',' + scope + ',' + parameter;
    }

}
