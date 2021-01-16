package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.util.ItemHelper;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantInteractStorage {

    private ArrayList<PlantInteract> interactList = new ArrayList<>();
    private PlantInteract defaultInteract = null;

    public PlantInteractStorage() { }

    public ArrayList<PlantInteract> getInteractList() {
        return interactList;
    }

    public PlantInteract getPlantInteract(ItemStack itemStack, Player player, PlantBlock plantBlock) {
        return getPlantInteract(itemStack, player, plantBlock, null);
    }

    public PlantInteract getPlantInteract(ItemStack itemStack, Player player, PlantBlock plantBlock, BlockFace blockFace) {
        PlantInteract matchInteract = null;
        for (PlantInteract plantInteract : interactList) {
            // if blockFace provided and plantInteract has a blockFace requirement, check it
            if (blockFace != null && plantInteract.hasRequiredBlockFaces()) {
                if (!plantInteract.isRequiredBlockFace(blockFace)) {
                    continue;
                }
            }
            // if condition not met, continue searching
            if (!plantInteract.isConditionMet(player, plantBlock)) {
                continue;
            }
            // if no match interact and less exclusive properties should be checked, check them
            if (plantInteract.isMatchMaterial() || plantInteract.isMatchEnchantments()) {
                if (plantInteract.isMatchMaterial()) {
                    if (itemStack.getType() != plantInteract.getItemStack().getType() ||
                            PerformantPlants.getInstance().getPlantTypeManager().isPlantItemStack(itemStack)) {
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
            }
            // check if item is similar
            if (itemStack.isSimilar(plantInteract.getItemStack())) {
                return plantInteract;
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
