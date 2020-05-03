package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.plants.Drop;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class DropHelper {

    public static void emulatePlayerDrop(Player player, ItemStack itemToDrop) {
        Item droppedItem = player.getWorld().dropItem(player.getEyeLocation().add(0,-0.2,0), itemToDrop);
        droppedItem.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.3));
    }

    public static void performDrops(Droppable droppable, Location location) {
        if (location == null) {
            return;
        }
        ArrayList<Drop> dropsList = droppable.getDrops();
        int dropLimit = droppable.getDropLimit();
        boolean limited = dropLimit >= 1;
        int dropCount = 0;
        for (Drop drop : dropsList) {
            // if there is a limit and have reached it, stop dropping
            if (limited && dropCount >= dropLimit) {
                break;
            }
            ItemStack dropStack = drop.generateDrop();
            if (dropStack.getAmount() != 0) {
                dropCount++;
                location.getWorld().dropItemNaturally(location, dropStack);
            }
        }
    }

    public static void performDrops(Droppable droppable, Block block) {
        performDrops(droppable, block.getLocation());
    }
}
