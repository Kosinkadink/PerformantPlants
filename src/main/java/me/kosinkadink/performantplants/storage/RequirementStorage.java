package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.blocks.RequiredBlock;

import java.util.ArrayList;

public class RequirementStorage {

    private Boolean waterRequired = null;
    private Boolean lavaRequired = null;

    private int lightLevelMinimum = -1;
    private int lightLevelMaximum = -1;

    private final ArrayList<RequiredBlock> requiredBlocks = new ArrayList<>();

    public RequirementStorage() {}

    public boolean isSet() {
        return hasWaterRequirement() ||
                hasLavaRequirement() ||
                hasLightRequirement() ||
                hasRequiredBlocks();
    }

    // water requirement
    public boolean hasWaterRequirement() {
        return waterRequired != null;
    }

    public boolean isWaterRequired() {
        return waterRequired;
    }

    public void setWaterRequired(boolean waterRequired) {
        this.waterRequired = waterRequired;
    }

    // lava requirement
    public boolean hasLavaRequirement() {
        return lavaRequired != null;
    }

    public boolean isLavaRequired() {
        return lavaRequired;
    }

    public void setLavaRequired(boolean lavaRequired) {
        this.lavaRequired = lavaRequired;
    }

    // light requirement
    public boolean hasLightRequirement() {
        return hasLightLevelMinimum() || hasLightLevelMaximum();
    }

    public boolean hasLightLevelMinimum() {
        return lightLevelMinimum >= 0;
    }

    public int getLightLevelMinimum() {
        return lightLevelMinimum;
    }

    public void setLightLevelMinimum(int lightLevelMinimum) {
        this.lightLevelMinimum = lightLevelMinimum;
    }

    public boolean hasLightLevelMaximum() {
        return lightLevelMaximum >= 0;
    }

    public int getLightLevelMaximum() {
        return lightLevelMaximum;
    }

    public void setLightLevelMaximum(int lightLevelMaximum) {
        this.lightLevelMaximum = lightLevelMaximum;
    }

    // block requirement
    public boolean hasRequiredBlocks() {
        return !requiredBlocks.isEmpty();
    }

    public ArrayList<RequiredBlock> getRequiredBlocks() {
        return requiredBlocks;
    }

    public void addRequiredBlock(RequiredBlock block) {
        requiredBlocks.add(block);
    }

}
