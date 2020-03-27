package me.kosinkadink.performantplants.settings;

public class ConfigSettings {

    private boolean debug = false;

    public ConfigSettings() {
        // do nothing
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug() {
        return debug;
    }


}
