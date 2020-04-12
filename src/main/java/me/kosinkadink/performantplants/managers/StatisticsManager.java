package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.statistics.StatisticsAmount;
import me.kosinkadink.performantplants.storage.StatisticsAmountStorage;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {

    private Main main;

    private ConcurrentHashMap<UUID, StatisticsAmountStorage> plantItemsSoldStorageMap = new ConcurrentHashMap<>();

    public StatisticsManager(Main main) {
        this.main = main;
    }

    // region PlantItemsSold

    public void resetAllPlantItemsSoldForPlayer(UUID playerUUID) {
        plantItemsSoldStorageMap.remove(playerUUID);
    }

    public void resetPlantItemsSoldForPlayer(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            statisticsAmountStorage.removeStatisticsAmount(plantItemId);
        }
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
    }

    public StatisticsAmount getPlantItemsSold(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            return statisticsAmountStorage.getStatisticsAmount(plantItemId);
        }
        return null;
    }

    // endregion

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

}
