package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.statistics.StatisticsAmount;

import java.util.concurrent.ConcurrentHashMap;

public class StatisticsAmountStorage {

    private ConcurrentHashMap<String, StatisticsAmount> statisticsAmountMap = new ConcurrentHashMap<>();

    public StatisticsAmountStorage() { }

    public void addStatisticsAmount(StatisticsAmount statisticsAmount) {
        statisticsAmountMap.put(statisticsAmount.getId(), statisticsAmount);
    }

    public StatisticsAmount getStatisticsAmount(String id) {
        return statisticsAmountMap.get(id);
    }

    public StatisticsAmount removeStatisticsAmount(String id) {
        return statisticsAmountMap.remove(id);
    }

    public void removeAllStatisticsAmount() {
        statisticsAmountMap.clear();
    }

    public ConcurrentHashMap<String, StatisticsAmount> getStatisticsAmountMap() {
        return statisticsAmountMap;
    }

}
