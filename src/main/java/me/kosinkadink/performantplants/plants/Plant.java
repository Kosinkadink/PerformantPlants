package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.RequirementStorage;
import me.kosinkadink.performantplants.storage.StageStorage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Plant {

    // list for tab completion purposes
    private final ArrayList<String> itemIds = new ArrayList<>();

    private final String id;
    private final PlantItem plantItem;
    private PlantItem plantSeedItem;
    private final HashMap<String, PlantItem> goods = new HashMap<>();
    private final HashMap<String, GrowthStageBlock> growthStageBlockMap = new HashMap<>();
    private final HashMap<String, ScriptBlock> scriptBlockMap = new HashMap<>();
    private final HashMap<String, ScriptTask> scriptTaskMap = new HashMap<>();
    private final StageStorage stageStorage = new StageStorage();
    private PlantData plantData;
    // growth requirements
    private RequirementStorage plantRequirementStorage = new RequirementStorage();
    private RequirementStorage growthRequirementStorage = new RequirementStorage();
    // growth time - overridden by specific stage growth times
    private ScriptBlock growthTime = new ScriptResult(-1);


    public Plant(String id, PlantItem plantItem) {
        this.id = id;
        this.plantItem = plantItem;
        this.plantItem.setId(this.id);
        this.plantItem.setPlant(this);
        plantData = new PlantData(new JSONObject());
        plantData.setPlant(this);
    }

    public ArrayList<String> getItemIds() {
        return itemIds;
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
        goodItem.setPlant(this);
        goods.put(id, goodItem);
        itemIds.add(goodItem.getId());
    }

    public Collection<PlantItem> getGoods() {
        return goods.values();
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
        plantSeedItem.setPlant(this);
        itemIds.add(plantSeedItem.getId());
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

    // drop map
    public HashMap<String, GrowthStageBlock> getGrowthStageBlockMap() {
        return growthStageBlockMap;
    }

    public GrowthStageBlock getGrowthStageBlock(String blockId) {
        return growthStageBlockMap.get(blockId);
    }

    public void addGrowthStageBlock(String blockId, GrowthStageBlock growthStageBlock) {
        growthStageBlockMap.put(blockId, growthStageBlock);
    }

    // plant script block map
    public HashMap<String, ScriptBlock> getScriptBlockMap() {
        return scriptBlockMap;
    }

    public ScriptBlock getScriptBlock(String blockId) {
        return scriptBlockMap.get(blockId);
    }

    public void addScriptBlock(String blockId, ScriptBlock scriptBlock) {
        scriptBlockMap.put(blockId, scriptBlock);
    }

    public void removeAllScriptBlocks() {
        scriptBlockMap.clear();
    }

    // plant task map
    public ScriptTask getScriptTask(String taskId) {
        return scriptTaskMap.get(taskId);
    }

    public void addScriptTask(String taskId, ScriptTask scriptTask) {
        scriptTaskMap.put(taskId, scriptTask);
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
        return plantData != null && !plantData.getData().isEmpty();
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

    public void setGrowthTime(ScriptBlock growthTime) {
        this.growthTime = growthTime;
    }

    public long generateGrowthTime(int stageIndex, ExecutionContext context) {
        if (isValidStage(stageIndex)) {
            // first check if stage has it's own growth time
            GrowthStage growthStage = getGrowthStage(stageIndex);
            long growthTimeValue = growthStage.generateGrowthTime(context);
            if (growthTimeValue >= 0) {
                return growthTimeValue;
            }
            // otherwise use plant's growth time
            growthTimeValue = growthTime.loadValue(context).getLongValue();
            if (growthTimeValue >= 0) {
                return growthTimeValue;
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
//        for (GrowthStage growthStage : stageStorage.getGrowthStages()) {
//            // verify go to stages use valid stage ids
//            for (GrowthStageBlock growthStageBlock : growthStage.getBlocks().values()) {
//                // TODO: parse all ScriptBlocks and if any ChangeStage or Interact operations are found,
//                //  make sure the stages exist
//            }
//        }
        return true;
    }

    public static Plant wrappedPlant = new Plant("_wrappedPlant", new PlantItem(new ItemStack(Material.AIR)));

}
