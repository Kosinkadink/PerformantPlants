package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.RequiredItem;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
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

    public PlantConsumable getConsumable(ExecutionContext context, EquipmentSlot hand) {
        PlantConsumable matchConsumable = null;
        Player player = context.getPlayer();
        for (PlantConsumable plantConsumable : consumableList) {
            // if needs messing food and is full, continue searching
            if (plantConsumable.isMissingFood(context)) {
                if (!PlayerHelper.hasMissingFood(player)) {
                    continue;
                }
            }
            // if condition not met, continue searching
            if (!plantConsumable.isConditionMet(context)) {
                continue;
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
                if (requirement.isInHand(context)) {
                    matches = ItemHelper.checkIfMatches(requirement.getItemStack(), otherStack);
                } // else check that item exists somewhere in player's inventory
                else {
                    matches = ItemHelper.inventoryContains(player.getInventory(), requirement.getItemStack());
                }
                // if not a match, stop searching since this isn't it
                if (!matches) {
                    break;
                }
                // if condition not met, stop searching since this isn't it
                if (!requirement.isConditionMet(context)) {
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
