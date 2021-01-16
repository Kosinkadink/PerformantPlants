package me.kosinkadink.performantplants.scripting;

public enum ScriptCategory {
    NONE, RESULT, ACTION, CAST, COMPARE, FLOW, FUNCTION, LOGIC, MATH, RANDOM, PLAYER, BLOCK, WORLD, INVENTORY;

    public static ScriptCategory fromString(String category) {
        try {
            return ScriptCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String fromCategory(ScriptCategory category) {
        return category.toString();
    }

}
