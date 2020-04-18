package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.storage.StageStorage;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Plant {

    private String id;
    private PlantItem plantItem;
    private PlantItem plantSeedItem;
    private HashMap<String, PlantItem> goods = new HashMap<>();
    private StageStorage stageStorage = new StageStorage();
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

    public StageStorage getStageStorage() {
        return stageStorage;
    }

    public GrowthStage getGrowthStage(int stageIndex) {
        return stageStorage.getGrowthStage(stageIndex);
    }

    public void addGrowthStage(GrowthStage growthStage) {
        stageStorage.addGrowthStage(growthStage);
    }

    public int getTotalGrowthStages() {
        return stageStorage.getTotalGrowthStages();
    }

    public boolean hasGrowthStages() {
        return stageStorage.hasGrowthStages();
    }

    public boolean isValidStage(int stage) {
        return stageStorage.isValidStage(stage);
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

    public boolean isStopGrowth(int stageIndex) {
        // if valid, return isStopGrowth
        if (isValidStage(stageIndex)) {
            return getGrowthStage(stageIndex).isStopGrowth();
        }
        // otherwise return false (default value)
        return false;
    }

    public boolean validateStages() {
        for (GrowthStage growthStage : stageStorage.getGrowthStages()) {
            // verify go to stages use valid stage ids
            for (GrowthStageBlock growthStageBlock : growthStage.getBlocks().values()) {
                PlantInteractStorage interactStorage = growthStageBlock.getOnInteract();
                if (interactStorage == null) {
                    continue;
                }
                ArrayList<PlantInteract> checkInteractList = new ArrayList<>();
                // add default interact to check, if present
                if (interactStorage.getDefaultInteract() != null) {
                    checkInteractList.add(interactStorage.getDefaultInteract());
                }
                // add all item interacts to check
                checkInteractList.addAll(interactStorage.getInteractList());
                // check interacts
                for (PlantInteract plantInteract : checkInteractList) {
                    if (plantInteract.getGoToStage() != null) {
                        if (!stageStorage.isValidStage(plantInteract.getGoToStage())) {
                            Bukkit.getLogger().warning(String.format("Stage with id '%s' does not exist for plant: %s",
                                    plantInteract.getGoToStage(), id));
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
