package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.RequiredItem;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantConsumableStorage {

    private ArrayList<PlantConsumable> consumableList = new ArrayList<>();

    public PlantConsumableStorage() { }

    public ArrayList<PlantConsumable> getConsumableList() {
        return consumableList;
    }

    public PlantConsumable getConsumable(Player player, EquipmentSlot hand) {
        PlantConsumable matchConsumable = null;
        for (PlantConsumable plantConsumable : consumableList) {
            // if needs messing food and is full, continue searching
            if (plantConsumable.isMissingFood()) {
                if (!PlayerHelper.hasMissingFood(player)) {
                    continue;
                }
            }
            // if no requirement, set match to this and continue searching
            if (plantConsumable.getRequiredItems().isEmpty()) {
                matchConsumable = plantConsumable;
                continue;
            }
            // set held item stacks
            ItemStack callStack;
            ItemStack otherStack;
            if (hand == EquipmentSlot.OFF_HAND) {
                callStack = player.getInventory().getItemInOffHand();
                otherStack = player.getInventory().getItemInMainHand();
            } else {
                callStack = player.getInventory().getItemInMainHand();
                otherStack = player.getInventory().getItemInOffHand();
            }
            // check requirements
            boolean matches = true;
            for (RequiredItem requirement : plantConsumable.getRequiredItems()) {
                // if in hand required, check that other hand contains item
                if (requirement.isInHand()) {
                    matches = ItemHelper.checkIfMatches(requirement.getItemStack(), otherStack);
                } // else check that item exists somewhere in player's inventory
                else {
                    matches = ItemHelper.inventoryContains(player.getInventory(), requirement.getItemStack());
                }
                // if not a match, stop searching since this isn't it
                if (!matches) {
                    break;
                }
            }
            if (matches) {
                matchConsumable = plantConsumable;
            }
        }
        return matchConsumable;
    }

    public void addConsumable(PlantConsumable plantConsumable) {
        consumableList.add(plantConsumable);
    }

}
