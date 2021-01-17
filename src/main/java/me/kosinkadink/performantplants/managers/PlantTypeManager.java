package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.storage.PlantDataStorage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class PlantTypeManager {

    private PerformantPlants performantPlants;
    // list for tab completion efficiency purposes
    private final ArrayList<String> plantIds = new ArrayList<>();

    private final ConcurrentHashMap<String, Plant> plantTypeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlantDataStorage> plantDataStorageMap = new ConcurrentHashMap<>();

    public PlantTypeManager(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    public ArrayList<String> getPlantIds() {
        return plantIds;
    }

    void addPlantDataStorage(PlantDataStorage storage) {
        plantDataStorageMap.put(storage.getPlantId(), storage);
    }

    public PlantDataStorage getPlantDataStorage(String plantId) {
        return plantDataStorageMap.get(plantId);
    }

    public ConcurrentHashMap<String, PlantDataStorage> getPlantDataStorageMap() {
        return plantDataStorageMap;
    }

    public Object getVariable(String plantId, String scope, String parameter, String variableName) {
        PlantDataStorage plantDataStorage = getPlantDataStorage(plantId);
        if (plantDataStorage != null) {
            return plantDataStorage.getVariable(scope, parameter, variableName);
        }
        return null;
    }

    public boolean updateVariable(String plantId, String scope, String parameter, String variableName, Object value) {
        PlantDataStorage plantDataStorage = getPlantDataStorage(plantId);
        if (plantDataStorage != null) {
            return plantDataStorage.updateVariable(scope, parameter, variableName, value);
        }
        return false;
    }

    public boolean containsScopeParameter(String plantId, String scope, String parameter) {
        PlantDataStorage plantDataStorage = getPlantDataStorage(plantId);
        if (plantDataStorage != null) {
            return plantDataStorage.containsScopeParameter(scope, parameter);
        }
        return false;
    }

    public boolean removeScopeParameter(String plantId, String scope, String parameter) {
        PlantDataStorage plantDataStorage = getPlantDataStorage(plantId);
        if (plantDataStorage != null) {
            return plantDataStorage.removeScopeParameter(scope, parameter);
        }
        return false;
    }

    void addPlantType(Plant plantType) {
        plantTypeMap.put(plantType.getId(), plantType);
        plantIds.add(plantType.getId());
    }

    public Plant getPlantByItemStack(ItemStack itemStack) {
        // don't bother searching if does not have plant prefix
        if (PlantItemBuilder.hasPlantPrefix(itemStack)) {
            for (Plant plant : plantTypeMap.values()) {
                if (plant.getItemByItemStack(itemStack) != null) {
                    return plant;
                }
            }
        }
        return null;
    }

    public PlantItem getPlantItemByItemStack(ItemStack itemStack) {
        // don't bother searching if does not have plant prefix
        if (PlantItemBuilder.hasPlantPrefix(itemStack)) {
            for (Plant plant : plantTypeMap.values()) {
                PlantItem plantItem = plant.getItemByItemStack(itemStack);
                if (plantItem != null) {
                    return plantItem;
                }
            }
        }
        return null;
    }

    public boolean isPlantItemStack(ItemStack itemStack) {
        return getPlantByItemStack(itemStack) != null;
    }

    public Plant getPlantById(String id) {
        return plantTypeMap.get(id);
    }

    public PlantItem getPlantItemById(String itemId) {
        // see if id contains special type
        if (itemId.endsWith(".")) {
            return null;
        }
        String[] plantInfo = itemId.split("\\.", 2);
        String plantId = plantInfo[0];
        String subtype = "";
        String goodId = "";
        if (plantInfo.length > 1) {
            subtype = plantInfo[1];
        }
        String type = "item";
        if (subtype.equals("seed")) {
            type = "seed";
        }
        else if (!subtype.isEmpty()) {
            goodId = subtype;
            type = "good";
        }
        // get plant by id, if exists
        Plant plant = getPlantById(plantId);
        if (plant == null) {
            return null;
        }
        // if is seed, return seed item
        if (type.equals("seed")) {
            return plant.getSeedItem();
        } else if (type.equals("good")) {
            return plant.getGoodItem(goodId);
        }
        // else return main plant item
        return plant.getItem();
    }

    public ItemStack getPlantItemStackById(String itemId) {
        PlantItem plantItem = getPlantItemById(itemId);
        if (plantItem != null) {
            return plantItem.getItemStack();
        }
        return null;
    }

}
