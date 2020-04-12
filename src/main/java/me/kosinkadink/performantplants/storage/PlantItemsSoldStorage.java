package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.statistics.PlantItemsSold;

import java.util.concurrent.ConcurrentHashMap;

public class PlantItemsSoldStorage {

    private ConcurrentHashMap<String, PlantItemsSold>  plantItemsMap = new ConcurrentHashMap<>();

    public PlantItemsSoldStorage() {

    }

    public void addPlantItemsSold(PlantItemsSold plantItemsSold) {
        plantItemsMap.put(plantItemsSold.getPlantItemId(), plantItemsSold);
    }

    public PlantItemsSold getPlantItemsSold(String plantItemId) {
        return plantItemsMap.get(plantItemId);
    }

}
