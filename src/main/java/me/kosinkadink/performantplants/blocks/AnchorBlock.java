package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.BlockLocation;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class AnchorBlock {
    private final BlockLocation location;
    private final HashSet<BlockLocation> blockLocations = new HashSet<>();

    public AnchorBlock(BlockLocation location) {
        this.location = location;
    }

    public BlockLocation getLocation() {
        return location;
    }

    public HashSet<BlockLocation> getBlockLocations() {
        return blockLocations;
    }

    public void addPlantBlock(@Nonnull BlockLocation location) {
        blockLocations.add(location);
    }

    public void addPlantBlock(@Nonnull PlantBlock plantBlock) {
        addPlantBlock(plantBlock.getLocation());
    }

    public boolean removePlantBlock(@Nonnull BlockLocation location) {
        return blockLocations.remove(location);
    }

    public boolean removePlantBlock(@Nonnull PlantBlock plantBlock) {
        return removePlantBlock(plantBlock.getLocation());
    }

    public boolean isEmpty() {
        return blockLocations.isEmpty();
    }

}
