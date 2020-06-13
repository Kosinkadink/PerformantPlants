package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {

    private static Pattern variablesPattern = Pattern.compile("\\$([_a-z-A-Z0-9]+?)\\$");

    public static ScriptType getType(Object o) {
        if (o == null) {
            return ScriptType.NULL;
        } else if (o instanceof Integer || o instanceof Long) {
            return ScriptType.LONG;
        } else if (o instanceof Double || o instanceof Float) {
            return ScriptType.DOUBLE;
        } else if (o instanceof Boolean) {
            return ScriptType.BOOLEAN;
        } else if (o instanceof String) {
            return ScriptType.STRING;
        }
        return null;
    }

    public static boolean isNumeric(ScriptBlock scriptBlock) {
        switch(scriptBlock.getType()) {
            case LONG:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLong(ScriptBlock scriptBlock) {
        return scriptBlock.getType() == ScriptType.LONG;
    }

    public static boolean isDouble(ScriptBlock scriptBlock) {
        return scriptBlock.getType() == ScriptType.DOUBLE;
    }

    public static boolean isString(ScriptBlock scriptBlock) {
        return scriptBlock.getType() == ScriptType.STRING;
    }

    public static boolean isBoolean(ScriptBlock scriptBlock) {
        return scriptBlock.getType() == ScriptType.BOOLEAN;
    }

    public static boolean isNull(ScriptBlock scriptBlock) {
        return scriptBlock == null || scriptBlock.getType() == ScriptType.NULL;
    }

    public static String setVariables(PlantBlock plantBlock, String text) {
        PlantData plantData = plantBlock.getEffectivePlantData();
        if (plantData == null) {
            return text;
        }
        // figure out which variables are present in the string
        Matcher matcher = variablesPattern.matcher(text);
        StringBuffer stringBuffer = new StringBuffer(text.length());
        while (matcher.find()) {
            String variableName = matcher.group(1);
            // see if variable is recognized;
            String value = getVariableValue(plantBlock, plantData, variableName);
            if (value == null) {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement("$"+variableName+"$"));
            } else {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private static String getVariableValue(PlantBlock plantBlock, PlantData plantData, String variableName) {
        // check if it is a property name
        if (variableName.startsWith("_")) {
            switch (variableName) {
                case "_x":
                    return Integer.toString(plantBlock.getLocation().getX());
                case "_y":
                    return Integer.toString(plantBlock.getLocation().getY());
                case "_z":
                    return Integer.toString(plantBlock.getLocation().getZ());
                case "_world":
                    return plantBlock.getLocation().getWorldName();
                case "_plantId":
                    return plantBlock.getPlant().getId();
                default:
                    return null;
            }
        }
        // check if variable exists in plant block data
        if (plantData.getData().containsKey(variableName)) {
            // create a ScriptResult for easy conversion to string
            try {
                return new ScriptResult(plantData.getData().get(variableName)).getStringValue();
            } catch (IllegalArgumentException e) {
                // something went wrong, return null
                return null;
            }
        }
        // variable name not recognized, return null
        return null;
    }

}
