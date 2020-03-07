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

}
