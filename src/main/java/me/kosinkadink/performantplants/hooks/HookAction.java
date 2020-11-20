package me.kosinkadink.performantplants.hooks;

public enum HookAction {
    START, PAUSE, CANCEL;

    public static HookAction fromString(String action) {
        try {
            return HookAction.valueOf(action);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String fromAction(HookAction action) {
        return action.toString();
    }

}
