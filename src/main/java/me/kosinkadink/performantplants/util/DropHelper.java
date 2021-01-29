package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.storage.DropStorage;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class DropHelper {

    public static void givePlayerItemStack(Player player, ItemStack giveStack) {
        HashMap<Integer,ItemStack> remaining = player.getInventory().addItem(giveStack);
        // drop any extras in front of player
        for (ItemStack itemToAdd : remaining.values()) {
            DropHelper.emulatePlayerDrop(player, itemToAdd);
        }
    }

    public static void emulatePlayerDrop(Player player, ItemStack itemToDrop) {
        Item droppedItem = player.getWorld().dropItem(player.getEyeLocation().add(0,-0.2,0), itemToDrop);
        droppedItem.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.3));
    }

    public static void performDrops(DropStorage dropStorage, Location location, ExecutionContext context) {
        if (location == null) {
            return;
        }
        ArrayList<Drop> dropsList = dropStorage.getDrops();
        int dropLimit = dropStorage.getDropLimitValue(context);
        boolean limited = dropLimit >= 1;
        int dropCount = 0;
        for (Drop drop : dropsList) {
            // if there is a limit and have reached it, stop dropping
            if (limited && dropCount >= dropLimit) {
                break;
            }
            ItemStack dropStack = drop.generateDrop(context);
            if (dropStack.getAmount() != 0) {
                dropCount++;
                location.getWorld().dropItemNaturally(location, dropStack);
            }
        }
    }

}
