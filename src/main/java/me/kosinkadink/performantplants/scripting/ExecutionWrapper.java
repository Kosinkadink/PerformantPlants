package me.kosinkadink.performantplants.scripting;

import java.util.HashSet;

public class ExecutionWrapper {

    private ExecutionWrapper innerWrapper = null;

    private PlantData localData = PlantData.createEmpty();

    private HashSet<String> totalKeySet = new HashSet<>();

    public ExecutionWrapper() {

    }

    public ExecutionWrapper(PlantData localData) {
        this.localData = localData;
        fillOutKeySet();
    }

    public void wrap(ExecutionContext context) {
        // check if context already is not already wrapped with this instance of wrapper
        if (context.getWrapper() != this) {
            setInnerWrapper(context.getWrapper());
            context.setWrapper(this);
        }
    }

    public void unwrap(ExecutionContext context) {
        // check that context is wrapped with this instance of wrapper
        if (context.getWrapper() == this) {
            context.setWrapper(innerWrapper);
            this.innerWrapper = null;
        }
    }

    public ExecutionWrapper getInnerWrapper() {
        return innerWrapper;
    }

    protected void setInnerWrapper(ExecutionWrapper wrapper) {
        this.innerWrapper = wrapper;
        fillOutKeySet();
    }

    public PlantData getLocalData() {
        return localData;
    }

    //region Variable
    public boolean isVariable(String variableName) {
        return totalKeySet.contains(variableName);
    }

    public Object getVariable(String variableName) {
        // if not recognized as local variable, return null
        if (!isVariable(variableName)) {
            return null;
        }
        // get variable unchecked, since we know the variable must exist somewhere in the layers
        return getVariableUnchecked(variableName);
    }

    protected Object getVariableUnchecked(String variableName) {
        Object variableValue = localData.getVariable(variableName);
        // if not null, return value found
        if (variableValue != null) {
            return variableValue;
        }
        // if not in this layer, check inner layers for local variable
        if (innerWrapper != null) {
            return innerWrapper.getVariableUnchecked(variableName);
        }
        // otherwise, return null (not found)
        return null;
    }

    public boolean updateVariable(String variableName, Object value) {
        if (!isVariable(variableName)) {
            return false;
        }
        // update variable unchecked, since we know the variable must exist somewhere in the layers
        return updateVariableUnchecked(variableName, value);
    }

    protected boolean updateVariableUnchecked(String variableName, Object value) {
        // if local data contains variable, return update status
        if (localData.containsVariable(variableName)) {
            return localData.updateVariable(variableName, value);
        }
        // if not in this layer, check inner layers for local variable
        if (innerWrapper != null) {
            return innerWrapper.updateVariableUnchecked(variableName, value);
        }
        // otherwise, return false (not found, so not updated
        return false;
    }
    //endregion

    //region Key Set
    private void fillOutKeySet() {
        // if inner wrapper exists, clone set from inner layer
        if (innerWrapper != null) {
            totalKeySet = (HashSet<String>) innerWrapper.getTotalKeySet().clone();
        }
        // add keys from local data into this layer's total key set
        totalKeySet.addAll(localData.getData().keySet());
    }

    protected HashSet<String> getTotalKeySet() {
        return totalKeySet;
    }
    //endregion
}
