package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.statistics.PlantItemsSold;
import me.kosinkadink.performantplants.storage.PlantItemsSoldStorage;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {

    private Main main;

    private ConcurrentHashMap<UUID, PlantItemsSoldStorage> plantItemsSoldStorageMap = new ConcurrentHashMap<>();

    public StatisticsManager(Main main) {
        this.main = main;
    }

    public void addPlantItemsSold(UUID playerUUID, String plantItemId, int addAmount) {
        PlantItemsSold plantItemsSold = getPlantItemsSold(playerUUID, plantItemId);
        if (plantItemsSold != null) {
            plantItemsSold.setAmount(plantItemsSold.getAmount() + addAmount);
            return;
        }
        plantItemsSold = new PlantItemsSold(playerUUID, plantItemId, addAmount);
        addPlantItemsSold(plantItemsSold);
    }

    public void addPlantItemsSold(PlantItemsSold plantItemsSold) {
        PlantItemsSoldStorage plantItemsSoldStorage = plantItemsSoldStorageMap.get(plantItemsSold.getPlayerUUID());
        // if storage already exists for user, add PlantItemsSold
        if (plantItemsSoldStorage != null) {
            plantItemsSoldStorage.addPlantItemsSold(plantItemsSold);
        } else {
            // otherwise create new, assign, and add to map
            plantItemsSoldStorage = new PlantItemsSoldStorage();
            plantItemsSoldStorage.addPlantItemsSold(plantItemsSold);
            plantItemsSoldStorageMap.put(plantItemsSold.getPlayerUUID(), plantItemsSoldStorage);
        }
    }

    public PlantItemsSold getPlantItemsSold(UUID playerUUID, String plantItemId) {
        PlantItemsSoldStorage plantItemsSoldStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (plantItemsSoldStorage != null) {
            return plantItemsSoldStorage.getPlantItemsSold(plantItemId);
        }
        return null;
    }

}
