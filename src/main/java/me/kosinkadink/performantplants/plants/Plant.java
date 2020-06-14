package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.storage.RequirementStorage;
import me.kosinkadink.performantplants.storage.StageStorage;
import org.bukkit.Material;
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
    private PlantData plantData;
    // growth requirements
    private RequirementStorage plantRequirementStorage = new RequirementStorage();
    private RequirementStorage growthRequirementStorage = new RequirementStorage();
    // growth time - overridden by specific stage growth times
    private long minGrowthTime = -1;
    private long maxGrowthTime = -1;


    public Plant(String id, PlantItem plantItem) {
        this.id = id;
        this.plantItem = plantItem;
        this.plantItem.setId(this.id);
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
        goodItem.setId(this.id + "." + id);
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
        plantSeedItem.setId(id + ".seed");
    }

    public PlantItem getItemByItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        if (plantItem != null && itemStack.isSimilar(plantItem.getItemStack())) {
            return plantItem;
        }
        if (plantSeedItem != null && itemStack.isSimilar(plantSeedItem.getItemStack())) {
            return plantSeedItem;
        }
        for (PlantItem good : goods.values()) {
            if (itemStack.isSimilar(good.getItemStack())) {
                return good;
            }
        }
        return null;
    }

    // requirements
    public boolean hasPlantRequirements() {
        return plantRequirementStorage != null && plantRequirementStorage.isSet();
    }

    public RequirementStorage getPlantRequirementStorage() {
        return plantRequirementStorage;
    }

    public void setPlantRequirementStorage(RequirementStorage plantRequirementStorage) {
        this.plantRequirementStorage = plantRequirementStorage;
    }

    public boolean hasGrowthRequirements() {
        return growthRequirementStorage != null && growthRequirementStorage.isSet();
    }

    public RequirementStorage getGrowthRequirementStorage() {
        return growthRequirementStorage;
    }

    public void setGrowthRequirementStorage(RequirementStorage growthRequirementStorage) {
        this.growthRequirementStorage = growthRequirementStorage;
    }

    // plant data
    public PlantData getPlantData() {
        return plantData;
    }

    public void setPlantData(PlantData plantData) {
        this.plantData = plantData;
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

    public boolean hasPlantData() {
        return plantData != null;
    }

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

    public void setMinGrowthTime(long time) {
        minGrowthTime = time;
    }

    public void setMaxGrowthTime(long time) {
        maxGrowthTime = time;
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

    public boolean isGrowthCheckpoint(int stageIndex) {
        // if valid, return isStopGrowth
        if (isValidStage(stageIndex)) {
            return getGrowthStage(stageIndex).isGrowthCheckpoint();
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
                // check interacts for any stage changes
                // TODO: parse all ScriptBlocks and if any ChangeStage or Interact operations are found,
                //  make sure the stages exist
                for (PlantInteract plantInteract : checkInteractList) {
//                    if (plantInteract.getGoToStage() != null) {
//                        if (!stageStorage.isValidStage(plantInteract.getGoToStage())) {
//                            Bukkit.getLogger().warning(String.format("Stage with id '%s' does not exist for plant: %s",
//                                    plantInteract.getGoToStage(), id));
//                            return false;
//                        }
//                    }
                }
            }
        }
        return true;
    }

    public static Plant wrappedPlant = new Plant("_wrappedPlant", new PlantItem(new ItemStack(Material.AIR)));

}
