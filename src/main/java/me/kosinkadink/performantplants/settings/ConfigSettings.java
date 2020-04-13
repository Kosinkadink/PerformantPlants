package me.kosinkadink.performantplants.settings;

public class ConfigSettings {

    private boolean debug = false;
    private int saveDelayMinutes = 10;

    public ConfigSettings() {
        // do nothing
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getSaveDelayMinutes() {
        return saveDelayMinutes;
    }

    public void setSaveDelayMinutes(int saveDelayMinutes) {
        this.saveDelayMinutes = saveDelayMinutes;
    }
}
