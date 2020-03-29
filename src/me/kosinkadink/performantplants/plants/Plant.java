package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.stages.GrowthStage;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Plant {

    private String name;
    private String displayName;
    private String id;
    private boolean placeable = false;
    private ItemStack plantItem;
    private ItemStack plantSeedItem;
    private ArrayList<GrowthStage> stages = new ArrayList<>();
    // growth requirements
    private boolean waterRequired = false;
    private boolean lavaRequired = false;
    private ArrayList<RequiredBlock> requiredBlocksToGrow = new ArrayList<>();
    // growth time - overridden by specific stage growth times
    private int minGrowthTime = -1;
    private int maxGrowthTime = -1;


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

    //region Growth Stage Actions

    public GrowthStage getGrowthStage(int stageIndex) {
        return stages.get(stageIndex);
    }

    public void addGrowthStage(GrowthStage growthStage) {
        stages.add(growthStage);
    }

    public int getTotalGrowthStages() {
        return stages.size();
    }

    public boolean hasGrowthStages() {
        return getTotalGrowthStages() > 0;
    }

    public boolean isValidStage(int stage) {
        return stage >=0 && stage < stages.size();
    }

    //endregion

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

    public boolean isWaterRequired() {
        return waterRequired;
    }

    public void setWaterRequired(boolean waterRequired) {
        this.waterRequired = waterRequired;
    }

    public boolean isLavaRequired() {
        return lavaRequired;
    }

    public void setLavaRequired(boolean lavaRequired) {
        this.lavaRequired = lavaRequired;
    }

    public ArrayList<RequiredBlock> getRequiredBlocksToGrow() {
        return requiredBlocksToGrow;
    }

    public boolean hasRequiredBlocksToGrow() {
        return requiredBlocksToGrow.size() > 0;
    }

    public void addRequiredBlockToGrow(RequiredBlock block) {
        requiredBlocksToGrow.add(block);
    }

    public void setMinGrowthTime(int time) {
        minGrowthTime = time;
    }

    public void setMaxGrowthTime(int time) {
        maxGrowthTime = time;
    }

    public int generateGrowthTime(int stageIndex) {
        if (isValidStage(stageIndex)) {
            // first check if stage has it's own growth time
            GrowthStage growthStage = getGrowthStage(stageIndex);
            if (growthStage.hasValidGrowthTimeSet()) {
                return growthStage.generateGrowthTime();
            }
            // otherwise use plant's growth time
            if (minGrowthTime >= 0 && maxGrowthTime >= 0) {
                if (minGrowthTime == maxGrowthTime) {
                    return minGrowthTime;
                }
                return ThreadLocalRandom.current().nextInt(minGrowthTime, maxGrowthTime + 1);
            }
        }
        return 0;
    }

}
