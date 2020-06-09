package me.kosinkadink.performantplants.scripting;

public enum ScriptType {
    LONG, DOUBLE, BOOLEAN, STRING, OBJECT, ARRAY, NULL;

    public static ScriptType fromString(String type) {
        try {
            return ScriptType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String fromType(ScriptType type) {
        return type.toString();
    }

}
