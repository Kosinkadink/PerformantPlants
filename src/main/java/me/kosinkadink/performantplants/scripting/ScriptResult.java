package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ScriptResult extends ScriptBlock {

    public static final ScriptResult TRUE = new ScriptResult(true);
    public static final ScriptResult FALSE = new ScriptResult(false);
    public static final ScriptResult ZERO = new ScriptResult(0);
    public static final ScriptResult EMPTY = new ScriptResult("");

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
    }

    public ScriptResult(String variableName, ScriptType type) {
        this.variableName = variableName;
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
                Long conversion = Long.getLong((String) value);
                if (conversion == null) {
                    conversion = 0L;
                }
                return conversion;
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

    protected ScriptResult loadVariable(PlantBlock plantBlock, Player player) {
        if (!isVariable()) {
            // if has placeholder and is string, return evaluated string
            if (type == ScriptType.STRING && hasPlaceholder) {
                return new ScriptResult(PlaceholderHelper.setVariablesAndPlaceholders(
                        plantBlock, player, this.getStringValue()));
            }
            return this;
        }
        if (plantBlock == null || plantBlock.getPlantData() == null) {
            return null;
        }
        PlantData data = plantBlock.getPlantData();
        JSONObject json = data.getData();
        return new ScriptResult(json.get(variableName));
    }

    @Override
    public ScriptResult loadValue(PlantBlock plantBlock, Player player) {
        return loadVariable(plantBlock, player);
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
    public ScriptCategory getCategory() {
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

}
