package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.blocks.RequiredBlock;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.HashSet;

public class RequirementStorage {

    private Boolean waterRequired = null;
    private Boolean lavaRequired = null;

    private int lightLevelMinimum = -1;
    private int lightLevelMaximum = -1;

    private long timeMinimum = -1;
    private long timeMaximum = -1;

    private double temperatureMinimum = -1;
    private double temperatureMaximum = -1;

    private final HashSet<String> worldWhitelist = new HashSet<>();
    private final HashSet<String> worldBlacklist = new HashSet<>();

    private final HashSet<Biome> biomeWhitelist = new HashSet<>();
    private final HashSet<Biome> biomeBlacklist = new HashSet<>();

    private final HashSet<World.Environment> environmentWhitelist = new HashSet<>();
    private final HashSet<World.Environment> environmentBlacklist = new HashSet<>();

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

    // time requirement
    public boolean hasTimeRequirement() {
        return hasTimeMinimum() || hasTimeMaximum();
    }

    public boolean hasTimeMinimum() {
        return timeMinimum >= 0;
    }

    public long getTimeMinimum() {
        return timeMinimum;
    }

    public void setTimeMinimum(long timeMinimum) {
        this.timeMinimum = timeMinimum;
    }

    public boolean hasTimeMaximum() {
        return timeMaximum >= 0;
    }

    public long getTimeMaximum() {
        return timeMaximum;
    }

    public void setTimeMaximum(long timeMaximum) {
        this.timeMaximum = timeMaximum;
    }

    // temperature requirement
    public boolean hasTemperatureRequirement() {
        return hasTemperatureMinimum() || hasTemperatureMaximum();
    }

    public boolean hasTemperatureMinimum() {
        return temperatureMinimum >= 0;
    }

    public double getTemperatureMinimum() {
        return temperatureMinimum;
    }

    public void setTemperatureMinimum(double timeMinimum) {
        this.temperatureMinimum = timeMinimum;
    }

    public boolean hasTemperatureMaximum() {
        return temperatureMaximum >= 0;
    }

    public double getTemperatureMaximum() {
        return temperatureMaximum;
    }

    public void setTemperatureMaximum(double temperatureMaximum) {
        this.temperatureMaximum = temperatureMaximum;
    }

    // world requirement
    public boolean hasWorldRequirement() {
        return hasWorldWhitelist() || hasWorldBlacklist();
    }

    public boolean hasWorldWhitelist() {
        return !worldWhitelist.isEmpty();
    }

    public boolean isInWorldWhitelist(String world) {
        return worldWhitelist.contains(world);
    }

    public void addToWorldWhitelist(String world) {
        worldWhitelist.add(world);
    }

    public boolean hasWorldBlacklist() {
        return !worldBlacklist.isEmpty();
    }

    public boolean isInWorldBlacklist(String world) {
        return worldBlacklist.contains(world);
    }

    public void addToWorldBlacklist(String world) {
        worldBlacklist.add(world);
    }

    // biome requirement
    public boolean hasBiomeRequirement() {
        return hasBiomeWhitelist() || hasBiomeBlacklist();
    }

    public boolean hasBiomeWhitelist() {
        return !biomeWhitelist.isEmpty();
    }

    public boolean isInBiomeWhitelist(Biome biome) {
        return biomeWhitelist.contains(biome);
    }

    public void addToBiomeWhitelist(Biome biome) {
        biomeWhitelist.add(biome);
    }

    public boolean hasBiomeBlacklist() {
        return !biomeBlacklist.isEmpty();
    }

    public boolean isInBiomeBlacklist(Biome biome) {
        return biomeBlacklist.contains(biome);
    }

    public void addToBiomeBlacklist(Biome biome) {
        biomeBlacklist.add(biome);
    }

    // environment requirement
    public boolean hasEnvironmentRequirement() {
        return hasEnvironmentWhitelist() || hasEnvironmentBlacklist();
    }

    public boolean hasEnvironmentWhitelist() {
        return !environmentWhitelist.isEmpty();
    }

    public boolean isInEnvironmentWhitelist(World.Environment environment) {
        return environmentWhitelist.contains(environment);
    }

    public void addToEnvironmentWhitelist(World.Environment environment) {
        environmentWhitelist.add(environment);
    }

    public boolean hasEnvironmentBlacklist() {
        return !environmentBlacklist.isEmpty();
    }
    public boolean isInEnvironmentBlacklist(World.Environment environment) {
        return environmentBlacklist.contains(environment);
    }

    public void addToEnvironmentBlacklist(World.Environment environment) {
        environmentBlacklist.add(environment);
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
