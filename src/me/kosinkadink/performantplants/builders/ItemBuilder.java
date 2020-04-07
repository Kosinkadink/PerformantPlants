package me.kosinkadink.performantplants.builders;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kosinkadink.performantplants.util.ReflectionHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private ItemStack item;

    /**
     * @param material
     * Create ItemBuilder with selected material
     */
    public ItemBuilder(Material material) {
        item = new ItemStack(material);
    }

    /**
     * @param itemStack
     * Create ItemBuilder with selected itemStack
     */
    public ItemBuilder(ItemStack itemStack) {
        item = itemStack.clone();
    }

    public ItemBuilder itemMeta(ItemMeta itemMeta) {
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder material(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder displayName(String name) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder appendLore(List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasLore()) {
                List<String> presentLore = itemMeta.getLore();
                assert presentLore != null;
                presentLore.addAll(lore);
                itemMeta.setLore(presentLore);
            }
            else {
                itemMeta.setLore(lore);
            }
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder skullTexture(String encodedUrl) {
        if (encodedUrl == null || encodedUrl.isEmpty()) {
            return this;
        }
        // if not a player head, do nothing
        if (!(item.getType() == Material.PLAYER_HEAD)) {
            return this;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            GameProfile profile = ReflectionHelper.createProfile(encodedUrl);
            try {
                Field profileField = itemMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(itemMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return this;
            }
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
