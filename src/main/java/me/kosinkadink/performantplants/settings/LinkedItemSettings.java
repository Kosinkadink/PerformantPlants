package me.kosinkadink.performantplants.settings;

public class LinkedItemSettings {

    private final String plantId;
    private final ItemSettings linkedItemSettings;

    public LinkedItemSettings(String plantId, ItemSettings linkedItemSettings) {
        this.plantId = plantId;
        this.linkedItemSettings = linkedItemSettings;
    }


    public String getPlantId() {
        return plantId;
    }

    public ItemSettings getLinkedItemSettings() {
        return linkedItemSettings;
    }
}
