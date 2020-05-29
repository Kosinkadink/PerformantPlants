package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.blocks.PlantBlock;

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

    public static String setVariables(PlantBlock plantBlock, String text) {
        if (!plantBlock.hasPlantData()) {
            return text;
        }
        // figure out which variables are present in the string
        Matcher matcher = variablesPattern.matcher(text);
        StringBuffer stringBuffer = new StringBuffer(text.length());
        while (matcher.find()) {
            String variableName = matcher.group(1);
            // see if variable is recognized;
            String value = getVariableValue(plantBlock, variableName);
            if (value == null) {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement("$"+variableName+"$"));
            } else {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private static String getVariableValue(PlantBlock plantBlock, String variableName) {
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
        PlantData plantData = plantBlock.getPlantData();
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
