package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ScriptResult extends ScriptBlock {

    public static final ScriptResult TRUE = new ScriptResult(true);
    public static final ScriptResult FALSE = new ScriptResult(false);
    public static final ScriptResult ZERO = new ScriptResult(0);
    public static final ScriptResult EMPTY = new ScriptResult("");
    public static final ScriptResult NULL = new ScriptResult(null);

    private Object value;
    private String variableName = null;
    private boolean hasPlaceholder = true;

    public ScriptResult(Object value) {
        if (value instanceof Integer) {
            this.value = new Long((Integer) value);
        } else {
            this.value = value;
        }
        this.type = ScriptHelper.getType(this.value);
        if (this.type == null) {
            throw new IllegalArgumentException("Value type not recognized");
        }
        setupHasPlaceholder();
    }

    public ScriptResult(String variableName, ScriptType type) {
        this.variableName = variableName;
        this.type = type;
        setupHasPlaceholder();
    }

    public void setType(ScriptType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isVariable() {
        return variableName != null && !variableName.isEmpty();
    }

    public String getVariableName() {
        return variableName;
    }

    private void setupHasPlaceholder() {
        if (this.type != ScriptType.STRING) {
            hasPlaceholder = false;
        }
    }

    public boolean isHasPlaceholder() {
        return hasPlaceholder;
    }

    public void setHasPlaceholder(boolean value) {
        hasPlaceholder = value;
    }

    public String getStringValue() {
        switch (type) {
            case STRING:
                return (String) value;
            case LONG:
                return ((Long) value).toString();
            case DOUBLE:
                return ((Double) value).toString();
            case BOOLEAN:
                return ((Boolean) value).toString();
            default:
                return "";
        }
    }

    public Boolean getBooleanValue() {
        switch (type) {
            case BOOLEAN:
                return (Boolean) value;
            case LONG:
                return ((Long) value) != 0;
            case DOUBLE:
                return ((Double) value) != 0.0;
            case STRING:
                return !((String) value).isEmpty();
            default:
                return false;
        }
    }

    public Long getLongValue() {
        switch (type) {
            case LONG:
                return (Long) value;
            case DOUBLE:
                return ((Double) value).longValue();
            case BOOLEAN:
                return (Boolean) value ? 1L : 0L;
            case STRING:
                try {
                    return Long.parseLong((String) value);
                } catch (NullPointerException | NumberFormatException e) {
                    return 0L;
                }
            default:
                return 0L;
        }
    }

    public Double getDoubleValue() {
        switch (type) {
            case LONG:
                return ((Long) value).doubleValue();
            case DOUBLE:
                return (Double) value;
            case BOOLEAN:
                return (Boolean) value ? 1.0 : 0.0;
            case STRING:
                try {
                    return Double.parseDouble((String) value);
                } catch (NullPointerException | NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    public Integer getIntegerValue() {
        return getLongValue().intValue();
    }

    public Float getFloatValue() {
        return getDoubleValue().floatValue();
    }

    public JSONObject getObjectValue() {
        if (type == ScriptType.OBJECT) {
            return (JSONObject) value;
        }
        return null;
    }

    public JSONArray getArrayValue() {
        if (type == ScriptType.ARRAY) {
            return (JSONArray) value;
        }
        return null;
    }

    public Object getNullValue() {
        return null;
    }

    public static ScriptResult getDefaultOfType(ScriptType defaultType) {
        switch (defaultType) {
            case BOOLEAN:
                return FALSE;
            case STRING:
                return new ScriptResult("");
            case LONG:
                return new ScriptResult(0L);
            case DOUBLE:
                return new ScriptResult(0.0);
            default:
                return new ScriptResult(null);
        }
    }

    protected ScriptResult loadVariable(ExecutionContext context) {
        if (!isVariable()) {
            // if has placeholder and is string, return evaluated string
            if (type == ScriptType.STRING && hasPlaceholder) {
                return new ScriptResult(PlaceholderHelper.setVariablesAndPlaceholders(
                        context, this.getStringValue()));
            }
            return this;
        }
        Object variableValue = ScriptHelper.getAnyDataVariableValue(context,
                PlaceholderHelper.setVariablesAndPlaceholders(context, variableName));
        return new ScriptResult(variableValue);
    }

    @Override
    public @Nonnull ScriptResult loadValue(@Nonnull ExecutionContext context) {
        return loadVariable(context);
    }

    @Override
    public boolean containsVariable() {
        return isVariable();
    }

    @Override
    public boolean containsCategories(ScriptCategory... categories) {
        for (ScriptCategory category : categories) {
            if (getCategory() == category) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.RESULT;
    }

    @Override
    public ScriptBlock optimizeSelf() {
        return this;
    }

    @Override
    public boolean shouldOptimize() {
        return true;
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptResult fromO = (ScriptResult)o;
        // check if equal variableName or value
        if (variableName != null) {
            return variableName.equals(fromO.variableName);
        }
        return value.equals(fromO.value);
    }

    public int hashCode() {
        return Objects.hash(value, variableName);
    }

}
