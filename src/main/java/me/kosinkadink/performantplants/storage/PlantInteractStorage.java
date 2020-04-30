package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.util.ItemHelper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantInteractStorage {

    private ArrayList<PlantInteract> interactList = new ArrayList<>();
    private PlantInteract defaultInteract = null;

    public PlantInteractStorage() { }

    public ArrayList<PlantInteract> getInteractList() {
        return interactList;
    }

    public PlantInteract getPlantInteract(ItemStack itemStack) {
        PlantInteract matchInteract = null;
        for (PlantInteract plantInteract : interactList) {
            // if no match interact and less exclusive properties should be checked, check them
            if (plantInteract.isMatchMaterial() || plantInteract.isMatchEnchantments()) {
                if (plantInteract.isMatchMaterial()) {
                    if (PlantItemBuilder.isPlantName(itemStack) ||
                            itemStack.getType() != plantInteract.getItemStack().getType()) {
                        continue;
                    }
                }
                if (plantInteract.isMatchEnchantments()) {
                    if (!ItemHelper.checkContainsEnchantments(plantInteract.getItemStack(), itemStack,
                            plantInteract.isMatchEnchantmentLevel())) {
                        continue;
                    }
                }
                // set match interact to this one
                matchInteract = plantInteract;
                break;
            } else {
                if (itemStack.isSimilar(plantInteract.getItemStack())) {
                    return plantInteract;
                }
            }
        }
        // if cached interact not null, use it
        if (matchInteract != null) {
            return matchInteract;
        }
        if (defaultInteract != null) {
            return defaultInteract;
        }
        return null;
    }

    public void addPlantInteract(PlantInteract plantInteract) {
        interactList.add(plantInteract);
    }

    public PlantInteract getDefaultInteract() {
        return defaultInteract;
    }

    public void setDefaultInteract(PlantInteract plantInteract) {
        defaultInteract = plantInteract;
    }

}
