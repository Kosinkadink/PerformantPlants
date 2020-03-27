package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.stages.GrowthStage;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Plant {

    private String name;
    private String displayName;
    private String id;
    private boolean placeable = false;
    private ItemStack plantItem;
    private ItemStack plantSeedItem;
    private ArrayList<GrowthStage> stages = new ArrayList<>();

    public Plant(String name, String id, ItemStack itemStack) {
        this.name = name;
        displayName = ChatColor.LIGHT_PURPLE + name + ChatColor.RESET;
        this.id = id;
        plantItem = itemStack;
        updateItemDisplayName();
    }

    public Plant(String name, String id, ItemStack itemstack, ItemStack seedItemStack) {
        this(name, id, itemstack);
        plantSeedItem = seedItemStack;
        updateSeedItemDisplayName();
    }

    void updateItemDisplayName() {
        ItemMeta itemMeta = plantItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            plantItem.setItemMeta(itemMeta);
        }
    }

    void updateSeedItemDisplayName() {
        ItemMeta itemMeta = plantSeedItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + name + " Seed" + ChatColor.RESET);
            plantSeedItem.setItemMeta(itemMeta);
        }
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSeedDisplayName() {
        if (plantSeedItem != null) {
            ItemMeta itemMeta = plantSeedItem.getItemMeta();
            if (itemMeta != null) {
                return itemMeta.getDisplayName();
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return plantItem;
    }

    public ItemStack getSeedItem() {
        return plantSeedItem;
    }

    public ItemStack getClonedItem() {
        return plantItem.clone();
    }

    public GrowthStage getGrowthStage(int stageIndex) {
        return stages.get(stageIndex);
    }

    public void addGrowthStage(GrowthStage growthStage) {
        stages.add(growthStage);
    }

    public int getTotalGrowthStages() {
        return stages.size();
    }

    public boolean isSimilar(ItemStack itemStack) {
        return plantItem.isSimilar(itemStack);
    }

    public boolean hasSeed() {
        return plantSeedItem != null;
    }

    public boolean isPlaceable() {
        return placeable;
    }

    public void setPlaceable(boolean placeable) {
        this.placeable = placeable;
    }
}
