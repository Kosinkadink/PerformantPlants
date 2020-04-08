package me.kosinkadink.performantplants.managers;

import com.sun.istack.internal.NotNull;
import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.stages.GrowthStage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlantTypeManager {

    private Main main;
    private ArrayList<Plant> plantTypes = new ArrayList<>();

    public PlantTypeManager(Main mainClass) {
        main = mainClass;
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

    public Plant getPlantPlacedWith(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        String displayName = itemMeta.getDisplayName();
        for (Plant plantType : plantTypes) {
            //if (plantType.isPlaceable()) {
            if (plantType.getDisplayName().equalsIgnoreCase(displayName)) {
                return plantType;
            }
            //}
            if (plantType.hasSeed()) {
                if (plantType.getSeedDisplayName().equalsIgnoreCase(displayName)) {
                    return plantType;
                }
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
