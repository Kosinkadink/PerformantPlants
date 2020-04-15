package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Plant {

    private String id;
    private PlantItem plantItem;
    private PlantItem plantSeedItem;
    private HashMap<String, PlantItem> goods = new HashMap<>();
    private ArrayList<GrowthStage> stages = new ArrayList<>();
    // growth requirements
    private boolean waterRequired = false;
    private boolean lavaRequired = false;
    private ArrayList<RequiredBlock> requiredBlocksToGrow = new ArrayList<>();
    // growth time - overridden by specific stage growth times
    private long minGrowthTime = -1;
    private long maxGrowthTime = -1;


    public Plant(String id, PlantItem plantItem) {
        this.id = id;
        this.plantItem = plantItem;
    }

    public String getId() {
        return id;
    }

    public PlantItem getItem() {
        return plantItem;
    }

    public ItemStack getItemStack() {
        return plantItem.getItemStack();
    }

    public PlantItem getSeedItem() {
        return plantSeedItem;
    }

    public PlantItem getGoodItem(String id) {
        return goods.get(id);
    }

    public ItemStack getGoodItemStack(String id) {
        PlantItem good = goods.get(id);
        if (good != null) {
            return good.getItemStack();
        }
        return null;
    }

    public void addGoodItem(String id, PlantItem goodItem) {
        goods.put(id, goodItem);
    }

    public ItemStack getSeedItemStack() {
        if (plantSeedItem != null) {
            return plantSeedItem.getItemStack();
        }
        return null;
    }

    public void setSeedItem(PlantItem seedItem) {
        plantSeedItem = seedItem;
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

    public boolean hasSeed() {
        return plantSeedItem != null;
    }

    public boolean hasGoods() {
        return !goods.isEmpty();
    }

    public boolean isSimilarToAnyGood(ItemStack itemToCheck) {
        for (PlantItem item : goods.values()) {
            if (item.getItemStack().isSimilar(itemToCheck)) {
                return true;
            }
        }
        return false;
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
        minGrowthTime = TimeHelper.secondsToTicks(time);
    }

    public void setMaxGrowthTime(int time) {
        maxGrowthTime = TimeHelper.secondsToTicks(time);
    }

    public long generateGrowthTime(int stageIndex) {
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
                return ThreadLocalRandom.current().nextLong(minGrowthTime, maxGrowthTime + 1);
            }
        }
        return 0;
    }

}
