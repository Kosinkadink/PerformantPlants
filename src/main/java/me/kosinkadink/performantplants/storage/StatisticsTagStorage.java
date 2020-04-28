package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.statistics.StatisticsTagItem;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsTagStorage {

    private ConcurrentHashMap<String, StatisticsTagItem> statisticsTagMap = new ConcurrentHashMap<>();

    public StatisticsTagStorage() { }

    public void addStatisticsTag(StatisticsTagItem statisticsTagItem) {
        statisticsTagMap.put(statisticsTagItem.getPlantId(), statisticsTagItem);
    }

    public StatisticsTagItem getStatisticsTag(String plantId) {
        return statisticsTagMap.get(plantId);
    }

    public StatisticsTagItem removeStatisticsTag(String plantId) {
        return statisticsTagMap.remove(plantId);
    }

    public void removeAllStatisticsTag() {
        statisticsTagMap.clear();
    }

    public ConcurrentHashMap<String, StatisticsTagItem> getStatisticsTagMap() {
        return statisticsTagMap;
    }

    public ArrayList<String> getAllPlantIds() {
        ArrayList<String> plantIds = new ArrayList<>();
        for (Map.Entry<String, StatisticsTagItem> entry : statisticsTagMap.entrySet()) {
            plantIds.add(entry.getValue().getPlantId());
        }
        return plantIds;
    }

}
