package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.scheduler.BukkitTask;

public class PlantBlock {
    private final BlockLocation location;
    private Plant plant;
    private long duration;
    private BukkitTask task;

    public PlantBlock(BlockLocation blockLocation, Plant plant) {
        location = blockLocation;
        this.plant = plant;
    }

    public BlockLocation getLocation() {
        return location;
    }

    public Plant getPlant() {
        return plant;
    }

    public void startTask() {
        // do stuff here
    }

    public void pauseTask() {
        // do stuff here
    }

    @Override
    public String toString() {
        return "PlantBlock @ " + location.toString();
    }

}
