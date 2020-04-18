package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.plants.PlantInteract;
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
        for (PlantInteract plantInteract : interactList) {
            if (itemStack.isSimilar(plantInteract.getItemStack())) {
                return plantInteract;
            }
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
