package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlantTypeManager {

    private Main main;
    private ArrayList<Plant> plantTypes = new ArrayList<>();

    public PlantTypeManager(Main mainClass) {
        main = mainClass;
        // TODO: remove once done testing
        String testPlantName = "Test Seed";
        String testPlantId = "test.seed";
        ItemStack testItemStack = new ItemBuilder(Material.STICK)
                .lore(Collections.singletonList("Plant for testing purposes"))
                .build();
        Plant testPlant = new Plant(testPlantName, testPlantId, testItemStack);
        addPlantType(testPlant);
    }

    void addPlantType(Plant plantType) {
        plantTypes.add(plantType);
    }

    public Plant getPlantByDisplayName(String displayName) {
        for (Plant plantType : plantTypes) {
            if (plantType.getDisplayName().equalsIgnoreCase(displayName)) {
                return plantType;
            }
        }
        return null;
    }

    public Plant getPlant(ItemStack itemStack) {
        return getPlantByDisplayName(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
    }

    public Plant getPlantById(String id) {
        for (Plant plantType : plantTypes) {
            if (plantType.getId().equalsIgnoreCase(id)) {
                return plantType;
            }
        }
        return null;
    }

    public ArrayList<Plant> getPlantTypes() {
        return plantTypes;
    }
}
