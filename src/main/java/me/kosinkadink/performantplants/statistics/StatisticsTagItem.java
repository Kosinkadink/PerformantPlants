package me.kosinkadink.performantplants.statistics;

public class StatisticsTagItem {

    private String id;
    private String plantId;

    public StatisticsTagItem(String id, String plantId) {
        this.id = id;
        this.plantId = plantId;
    }

    public String getId() {
        return id;
    }

    public String getPlantId() {
        return plantId;
    }

}
