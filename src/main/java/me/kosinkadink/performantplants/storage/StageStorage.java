package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.stages.GrowthStage;

import java.util.ArrayList;
import java.util.HashMap;

public class StageStorage {

    private ArrayList<GrowthStage> stages = new ArrayList<>();
    private HashMap<String, Integer> stageIdMap = new HashMap<>();

    public StageStorage() { }

    public GrowthStage getGrowthStage(int stageIndex) {
        return stages.get(stageIndex);
    }

    public GrowthStage getGrowthStage(String stageId) {
        Integer stageIndex = stageIdMap.get(stageId);
        if (isValidStage(stageIndex)) {
            return getGrowthStage(stageIndex);
        }
        return null;
    }

    public void addGrowthStage(GrowthStage growthStage) {
        stageIdMap.put(growthStage.getId(), stages.size());
        stages.add(growthStage);
    }

    public int getGrowthStageIndex(String stageId) {
        return stageIdMap.get(stageId);
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

    public boolean isValidStage(String stageId) {
        Integer stageIndex = stageIdMap.get(stageId);
        return isValidStage(stageIndex);
    }

}
