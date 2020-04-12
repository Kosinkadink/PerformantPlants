package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.statistics.StatisticsAmount;

import java.util.concurrent.ConcurrentHashMap;

public class StatisticsAmountStorage {

    private ConcurrentHashMap<String, StatisticsAmount>  plantItemsMap = new ConcurrentHashMap<>();

    public StatisticsAmountStorage() { }

    public void addStatisticsAmount(StatisticsAmount statisticsAmount) {
        plantItemsMap.put(statisticsAmount.getId(), statisticsAmount);
    }

    public StatisticsAmount getStatisticsAmount(String id) {
        return plantItemsMap.get(id);
    }

    public void removeStatisticsAmount(String id) {
        plantItemsMap.remove(id);
    }

}
