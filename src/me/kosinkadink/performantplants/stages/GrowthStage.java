package me.kosinkadink.performantplants.stages;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.plants.Drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GrowthStage {

    private HashMap<String,GrowthStageBlock> blocks = new HashMap<>();
    private int minGrowthTime = -1;
    private int maxGrowthTime = -1;
    private int dropLimit = 0;
    private ArrayList<Drop> drops = new ArrayList<>();

    public GrowthStage() { }

    public GrowthStage(int minTime, int maxTime) {
        setMinGrowthTime(minTime);
        setMaxGrowthTime(maxTime);
    }

    public GrowthStage(int minTime, int maxTime, int limit) {
        this(minTime, maxTime);
        setDropLimit(limit);
    }

    public ArrayList<Drop> getDrops() {
        return drops;
    }

    public void addDrop(Drop drop) {
        drops.add(drop);
    }

    public void addGrowthStageBlock(GrowthStageBlock block) {
        blocks.put(block.getId(),block);
    }

    public GrowthStageBlock getGrowthStageBlock(String id) {
        return blocks.get(id);
    }

    public HashMap<String,GrowthStageBlock> getBlocks() {
        return blocks;
    }

    public void setMinGrowthTime(int time) {
        minGrowthTime = time;
    }

    public void setMaxGrowthTime(int time) {
        maxGrowthTime = time;
    }

    public boolean hasValidGrowthTimeSet() {
        return minGrowthTime >= 0 && maxGrowthTime >= 0;
    }

    public int generateGrowthTime() {
        if (minGrowthTime >= 0 && maxGrowthTime >= 0) {
            if (minGrowthTime == maxGrowthTime) {
                return minGrowthTime;
            }
            return ThreadLocalRandom.current().nextInt(minGrowthTime, maxGrowthTime + 1);
        }
        return 0;
    }

    public int getDropLimit() {
        return dropLimit;
    }

    public int getDropLimit(String growthBlockId) {
        GrowthStageBlock growthStageBlock = getGrowthStageBlock(growthBlockId);
        if (growthStageBlock != null) {
            return growthStageBlock.getDropLimit();
        }
        return getDropLimit();
    }

    public void setDropLimit(int limit) {
        if (limit >= 0) {
            dropLimit = limit;
        }
    }

}
