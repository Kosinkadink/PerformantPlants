package me.kosinkadink.performantplants.util;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemHelper {

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        // if same type, keep comparing
        if (stack1.getType() == stack2.getType()) {
            // if player heads, compare in a way that won't throw NullPointerException
            if (stack1.getType() == Material.PLAYER_HEAD) {
                ItemMeta stack1meta = stack1.getItemMeta();
                ItemMeta stack2meta = stack2.getItemMeta();
                if (stack1meta == null || stack2meta == null) {
                    Bukkit.getLogger().info("one of heads' meta was null");
                    return false;
                }
                return stack1meta.getDisplayName().equals(stack2meta.getDisplayName());
                /*
                if (!stack1meta.getDisplayName().equals(stack2meta.getDisplayName())) {
                    Bukkit.getLogger().info("head's display names don't match");
                }
                if (!stack1meta.getLore().toArray().equals(stack2meta.getLore().toArray())) {
                    Bukkit.getLogger().info("head's lore don't match; " + stack1meta.getLore().toString() + "\n" + stack2meta.getLore().toString());
                }
                if (!stack1meta.getItemFlags().toArray().equals(stack2meta.getItemFlags().toArray())) {
                    Bukkit.getLogger().info("head's item flags don't match");
                }
                return stack1meta.getDisplayName().equals(stack2meta.getDisplayName()) &&
                        stack1meta.getLore().toArray().equals(stack2meta.getLore().toArray()) &&
                        stack1meta.getItemFlags().toArray().equals(stack2meta.getItemFlags().toArray());

                 */
            }
            // otherwise use normal isSimilar
            return stack1.isSimilar(stack2);
        }
        return false;
    }

}
