package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {

    private static final Pattern variablesPattern = Pattern.compile("\\$([-_a-zA-Z0-9%\\\\.{}]+?)\\$");

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
        } else if (o instanceof ItemStack) {
            return ScriptType.ITEMSTACK;
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

    public static boolean isItemStack(ScriptBlock scriptBlock) {
        return scriptBlock.getType() == ScriptType.ITEMSTACK;
    }

    public static boolean isNull(ScriptBlock scriptBlock) {
        return scriptBlock == null || scriptBlock.getType() == ScriptType.NULL;
    }

    public static boolean isNotNull(ScriptBlock scriptBlock) {
        return !isNull(scriptBlock);
    }

    public static boolean isSimpleType(ScriptBlock scriptBlock) {
        return scriptBlock != null && isSimpleType(scriptBlock.getType());
    }

    public static boolean isSimpleType(ScriptType scriptType) {
        return scriptType == ScriptType.BOOLEAN || scriptType == ScriptType.STRING
                || scriptType == ScriptType.LONG || scriptType == ScriptType.DOUBLE;
    }

    /**
     * Checks if variable name is valid, and returns reason if so
     * @param variableName String variable name
     * @return String with reason if invalid, null if valid
     */
    public static String checkIfValidVariableName(String variableName) {
        if (variableName.isEmpty()) {
            return "Variables names cannot be empty";
        }
        if (variableName.startsWith("_")) {
            return String.format("Variable name '%s' cannot begin with '_'", variableName);
        }
        if (Character.isDigit(variableName.charAt(0))) {
            return String.format("Variable name '%s' cannot begin with a digit", variableName);
        }
        if (variableName.contains(".")) {
            return String.format("Variable name '%s' cannot contain a period", variableName);
        }
        return null;
    }

    public static String setVariables(ExecutionContext context, String text) {
        // figure out which variables are present in the string
        Matcher matcher = variablesPattern.matcher(text);
        StringBuffer stringBuffer = new StringBuffer(text.length());
        while (matcher.find()) {
            String variableName = matcher.group(1);
            // see if variable is recognized;
            String value = getVariableValue(context, variableName);
            if (value == null) {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement("$"+variableName+"$"));
            } else {
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public static boolean updateAnyDataVariableValue(ExecutionContext context, String variableName, Object value) {
        // if variable name contains period, then it refers to a plant variable
        if (variableName.contains(".")) {
            String[] variableParts = getVariableNameParts(variableName);
            if (variableParts != null) {
                return PerformantPlants.getInstance().getPlantTypeManager().updateVariable(
                        variableParts[0],
                        variableParts[1],
                        variableParts[2],
                        variableParts[3],
                        value);
            }
        }
        // otherwise it could be referring to a specific plant block's PlantData or local variable
        else {
            // check if local variable
            if (context.getWrapper().isVariable(variableName)) {
                return context.getWrapper().updateVariable(variableName, value);
            }
            // check if plant block data
            if (context.isPlantDataPossible()) {
                PlantData plantData = context.getPlantData();
                if (plantData != null) {
                    return plantData.updateVariable(variableName, value);
                }
            }
        }
        return false;
    }

    public static Object getAnyDataVariableValue(ExecutionContext context, String variableName) {
        // if variable name contains period, then it refers to a plant variable
        if (variableName.contains(".")) {
            String[] variableParts = getVariableNameParts(variableName);
            if (variableParts != null) {
                return PerformantPlants.getInstance().getPlantTypeManager().getVariable(
                        variableParts[0],
                        variableParts[1],
                        variableParts[2],
                        variableParts[3]);
            }
        }
        // otherwise it could be referring to a specific plant block's PlantData or local variable
        else {
            // check if local variable
            Object variableValue = context.getWrapper().getVariable(variableName);
            if (variableValue != null) {
                return variableValue;
            }
            // check if plant block data
            if (context.isPlantDataPossible()) {
                PlantData plantData = context.getPlantData();
                if (plantData != null) {
                    return plantData.getVariable(variableName);
                }
            }
        }
        return null;
    }

    private static String[] getVariableNameParts(String variableName) {
        String[] variableParts = variableName.split("\\.");
        if (variableParts.length == 4) {
            return variableParts;
        } else if (variableParts.length == 2) {
            return new String[] { variableParts[0], "", "", variableParts[1] };
        }
        return null;
    }

    private static String getVariableValue(ExecutionContext context, String variableName) {
        Object variableValue = null;
        // check if it is a property name
        if (variableName.startsWith("_")) {
            if ("_random_uuid".equals(variableName)) {
                return UUID.randomUUID().toString();
            }
            // check for player-related properties
            if (context.isPlayerSet() && variableName.startsWith("_player")) {
                String relevantVariableName;
                try {
                    relevantVariableName = variableName.substring("_player".length());
                } catch (IndexOutOfBoundsException e) {
                    relevantVariableName = "";
                }
                Player player = context.getPlayer();
                switch(relevantVariableName) {
                    case "_x":
                        return Double.toString(player.getLocation().getX());
                    case "_y":
                        return Double.toString(player.getLocation().getY());
                    case "_z":
                        return Double.toString(player.getLocation().getZ());
                    case "_block_x":
                        return Double.toString(player.getLocation().getBlockX());
                    case "_block_y":
                        return Double.toString(player.getLocation().getBlockY());
                    case "_block_z":
                        return Double.toString(player.getLocation().getBlockZ());
                    case "_world":
                        try {
                            return player.getLocation().getWorld().getName();
                        } catch (NullPointerException e) {
                            return null;
                        }
                    case "_uuid":
                        return player.getUniqueId().toString();
                    default:
                        return null;
                }
            }
            // check for block-related properties
            if (context.isPlantBlockSet()) {
                PlantBlock plantBlock = context.getPlantBlock();
                String relevantVariableName = variableName;
                PlantBlock relevantPlantBlock = plantBlock;
                // try to use parent block, if specified
                if (variableName.startsWith("_parent")) {
                    if (plantBlock.hasParent()) {
                        PlantBlock parentBlock = PerformantPlants.getInstance().getPlantManager().getPlantBlock(plantBlock.getParentLocation());
                        if (parentBlock != null) {
                            relevantPlantBlock = parentBlock;
                        }
                    }
                    try {
                        relevantVariableName = variableName.substring("_parent".length());
                    } catch (IndexOutOfBoundsException e) {
                        relevantVariableName = "";
                    }
                }
                switch (relevantVariableName) {
                    case "_x":
                        return Integer.toString(relevantPlantBlock.getLocation().getX());
                    case "_y":
                        return Integer.toString(relevantPlantBlock.getLocation().getY());
                    case "_z":
                        return Integer.toString(relevantPlantBlock.getLocation().getZ());
                    case "_x_center":
                        return Double.toString(relevantPlantBlock.getLocation().getX() + 0.5);
                    case "_y_center":
                        return Double.toString(relevantPlantBlock.getLocation().getY() + 0.5);
                    case "_z_center":
                        return Double.toString(relevantPlantBlock.getLocation().getZ() + 0.5);
                    case "_world":
                        return relevantPlantBlock.getLocation().getWorldName();
                    case "_plant_id":
                        return relevantPlantBlock.getPlant().getId();
                    case "_plant_uuid":
                        return relevantPlantBlock.getPlantUUID().toString();
                    default:
                        return null;
                }
            }
        }
        // get plant variable value
        variableValue = getAnyDataVariableValue(context, variableName);
        // convert variable value to string, if not null
        if (variableValue != null) {
            // create a ScriptResult for easy conversion to string
            try {
                return new ScriptResult(variableValue).getStringValue();
            } catch (IllegalArgumentException e) {
                // something went wrong, return null
                return null;
            }
        }
        // variable name not recognized, return null
        return null;
    }

}
