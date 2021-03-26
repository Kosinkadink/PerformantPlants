package me.kosinkadink.performantplants.blocks;

public enum DestroyReason {
    BREAK, EXPLODE, BURN, PISTON, DESTROY,
    RELATIVE_BREAK, RELATIVE_EXPLODE, RELATIVE_BURN, RELATIVE_PISTON, RELATIVE_DESTROY,
    REPLACE;

    public static DestroyReason fromString(String reason) {
        try {
            return DestroyReason.valueOf(reason);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String fromAction(DestroyReason reason) {
        return reason.toString();
    }

    public boolean isRelative() {
        switch(this) {
            case RELATIVE_BREAK:
            case RELATIVE_EXPLODE:
            case RELATIVE_BURN:
            case RELATIVE_PISTON:
            case RELATIVE_DESTROY:
                return true;
            default:
                return false;
        }
    }

    public DestroyReason getRelativeEquivalent() {
        switch(this) {
            case BREAK:
            case RELATIVE_BREAK:
                return RELATIVE_BREAK;
            case EXPLODE:
            case RELATIVE_EXPLODE:
                return RELATIVE_EXPLODE;
            case BURN:
            case RELATIVE_BURN:
                return RELATIVE_BURN;
            case PISTON:
            case RELATIVE_PISTON:
                return RELATIVE_PISTON;
            case DESTROY:
            case RELATIVE_DESTROY:
                return RELATIVE_DESTROY;
            default:
                return REPLACE;
        }
    }
}
