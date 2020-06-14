package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.stages.GrowthStage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlantTypeManager {

    private Main main;
    private ConcurrentHashMap<String, Plant> plantTypeMap = new ConcurrentHashMap<>();

    public PlantTypeManager(Main mainClass) {
        main = mainClass;
    }

    void addPlantType(Plant plantType) {
        plantTypeMap.put(plantType.getId(), plantType);
    }

    public Plant getPlantByItemStack(ItemStack itemStack) {
        for (Plant plant : plantTypeMap.values()) {
            if (plant.getItemByItemStack(itemStack) != null) {
                return plant;
            }
        }
        return null;
    }

    public PlantItem getPlantItemByItemStack(ItemStack itemStack) {
        for (Plant plant : plantTypeMap.values()) {
            PlantItem plantItem = plant.getItemByItemStack(itemStack);
            if (plantItem != null) {
                return plantItem;
            }
        }
        return null;
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
