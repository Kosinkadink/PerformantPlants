package me.kosinkadink.performantplants.stages;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.util.TimeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GrowthStage implements Droppable {

    private String id;
    private HashMap<String,GrowthStageBlock> blocks = new HashMap<>();
    private long minGrowthTime = -1;
    private long maxGrowthTime = -1;
    private boolean stopGrowth = false;
    private int dropLimit = 0;
    private ArrayList<Drop> drops = new ArrayList<>();

    public GrowthStage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
        minGrowthTime = TimeHelper.secondsToTicks(time);
    }

    public void setMaxGrowthTime(int time) {
        maxGrowthTime = TimeHelper.secondsToTicks(time);
    }

    public boolean hasValidGrowthTimeSet() {
        return minGrowthTime >= 0 && maxGrowthTime >= 0;
    }

    public long generateGrowthTime() {
        if (minGrowthTime >= 0 && maxGrowthTime >= 0) {
            if (minGrowthTime == maxGrowthTime) {
                return minGrowthTime;
            }
            return ThreadLocalRandom.current().nextLong(minGrowthTime, maxGrowthTime + 1);
        }
        return 0;
    }

    public int getDropLimit() {
        return dropLimit;
    }

    public int getDropLimit(String growthBlockId) {
        GrowthStageBlock growthStageBlock = getGrowthStageBlock(growthBlockId);
        if (growthStageBlock != null) {
            int blockDropLimit = growthStageBlock.getDropLimit();
            if (blockDropLimit >= 0) {
                return blockDropLimit;
            }
        }
        return getDropLimit();
    }

    public void setDropLimit(int limit) {
        if (limit >= 0) {
            dropLimit = limit;
        }
    }

    public boolean isStopGrowth() {
        return stopGrowth;
    }

    public void setStopGrowth(boolean stopGrowth) {
        this.stopGrowth = stopGrowth;
    }
}
