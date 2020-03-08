package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.BlockLocation;
import org.bukkit.scheduler.BukkitTask;

public class PlantBlock {
    private final BlockLocation location;
    private long duration;
    private BukkitTask task;

    public PlantBlock(BlockLocation blockLocation) {
        location = blockLocation;
    }

    public BlockLocation getLocation() {
        return location;
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
