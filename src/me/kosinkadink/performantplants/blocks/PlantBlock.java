package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.BlockLocation;

public class PlantBlock {
    private final BlockLocation location;

    PlantBlock(BlockLocation blockLocation) {
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

}
