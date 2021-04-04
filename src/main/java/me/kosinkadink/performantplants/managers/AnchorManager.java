package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.AnchorBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;

public class AnchorManager {

    private final PerformantPlants performantPlants;
    private static final HashMap<BlockLocation, AnchorBlock> anchorBlockMap = new HashMap<>();

    public AnchorManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public AnchorBlock getAnchorBlock(BlockLocation blockLocation) {
        return anchorBlockMap.get(blockLocation);
    }

    public AnchorBlock getAnchorBlock(Block block) {
        return getAnchorBlock(new BlockLocation(block));
    }

    public AnchorBlock getAnchorBlock(PlantBlock plantBlock) {
        return anchorBlockMap.get(plantBlock.getLocation());
    }

    public void addAnchorBlock(AnchorBlock anchorBlock) {
        anchorBlockMap.put(anchorBlock.getLocation(), anchorBlock);
    }

    public boolean removePlantBlockFromAnchorBlocks(PlantBlock plantBlock) {
        boolean atLeastOneRemoved = false;
        for (BlockLocation anchorLocation : new ArrayList<>(plantBlock.getAnchorLocations())) {
            AnchorBlock anchorBlock = getAnchorBlock(anchorLocation);
            if (anchorBlock != null) {
                boolean removed = anchorBlock.removePlantBlock(plantBlock.getLocation());
                if (removed) {
                    atLeastOneRemoved = true;
                }
                // if no more plant blocks to anchor, remove anchor block
                if (anchorBlock.isEmpty()) {
                    removeAnchorBlock(anchorLocation);
                }
            }
        }
        return atLeastOneRemoved;
    }

    private boolean removeAnchorBlock(BlockLocation blockLocation) {
        AnchorBlock removedBlock = anchorBlockMap.remove(blockLocation);
        if (removedBlock != null) {
            MetadataHelper.removeAnchorBlockMetadata(performantPlants, removedBlock.getLocation().getBlock());
            return true;
        }
        return false;
    }

}
