package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.statistics.StatisticsAmount;
import me.kosinkadink.performantplants.storage.StatisticsAmountStorage;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {

    private Main main;

    private ConcurrentHashMap<UUID, StatisticsAmountStorage> plantItemsSoldStorageMap = new ConcurrentHashMap<>();
    private HashSet<StatisticsAmount> statisticsAmountsToDelete = new HashSet<>();

    public StatisticsManager(Main main) {
        this.main = main;
    }

    // region PlantItemsSold

    public void resetAllPlantItemsSoldForAllPlayers() {
        for (StatisticsAmountStorage storage : plantItemsSoldStorageMap.values()) {
            statisticsAmountsToDelete.addAll(storage.getStatisticsAmountMap().values());
            storage.removeAllStatisticsAmount();
        }
        plantItemsSoldStorageMap.clear();
    }

    public void resetAllPlantItemsSoldForPlayer(UUID playerUUID) {
        StatisticsAmountStorage storage = plantItemsSoldStorageMap.remove(playerUUID);
        if (storage != null) {
            statisticsAmountsToDelete.addAll(storage.getStatisticsAmountMap().values());
            storage.removeAllStatisticsAmount();
        }
    }

    public boolean resetPlantItemsSoldForPlayer(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            StatisticsAmount statisticsAmount = statisticsAmountStorage.removeStatisticsAmount(plantItemId);
            if (statisticsAmount != null) {
                statisticsAmountsToDelete.add(statisticsAmount);
                return true;
            }
        }
        return false;
    }

    public void addPlantItemsSold(UUID playerUUID, String plantItemId, int addAmount) {
        StatisticsAmount plantItemsSold = getPlantItemsSold(playerUUID, plantItemId);
        if (plantItemsSold != null) {
            plantItemsSold.setAmount(plantItemsSold.getAmount() + addAmount);
            return;
        }
        plantItemsSold = new StatisticsAmount(playerUUID, plantItemId, addAmount);
        addPlantItemsSold(plantItemsSold);
    }

    public void addPlantItemsSold(StatisticsAmount plantItemsSold) {
        addStatisticsAmount(plantItemsSold, plantItemsSoldStorageMap);
        removeStatisticsAmountFromRemoval(plantItemsSold);
    }

    public StatisticsAmount getPlantItemsSold(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            return statisticsAmountStorage.getStatisticsAmount(plantItemId);
        }
        return null;
    }

    // endregion


    public HashSet<StatisticsAmount> getStatisticsAmountsToDelete() {
        return statisticsAmountsToDelete;
    }

    void removeStatisticsAmountFromRemoval(StatisticsAmount statisticsAmount) {
        statisticsAmountsToDelete.remove(statisticsAmount);
    }

    void addStatisticsAmount(StatisticsAmount statisticsAmount, ConcurrentHashMap<UUID, StatisticsAmountStorage> map) {
        StatisticsAmountStorage statisticsAmountStorage = map.get(statisticsAmount.getPlayerUUID());
        if (statisticsAmountStorage != null) {
            statisticsAmountStorage.addStatisticsAmount(statisticsAmount);
        } else {
            statisticsAmountStorage = new StatisticsAmountStorage();
            statisticsAmountStorage.addStatisticsAmount(statisticsAmount);
            map.put(statisticsAmount.getPlayerUUID(), statisticsAmountStorage);
        }
    }

    ConcurrentHashMap<UUID, StatisticsAmountStorage> getPlantItemsSoldStorageMap() {
        return plantItemsSoldStorageMap;
    }

}
