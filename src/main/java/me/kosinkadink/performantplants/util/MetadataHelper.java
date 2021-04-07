package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.AnchorBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

public class MetadataHelper {

    private static final String plantMeta = "pp-plant";
    private static final String anchorMeta = "pp-anchor";

    //region PlantBlock Metadata
    public static void setPlantBlockMetadata(PerformantPlants performantPlants, PlantBlock plantblock) {
        plantblock.getBlock().setMetadata(
                plantMeta,
                new FixedMetadataValue(performantPlants, plantblock.getPlantUUID().toString())
        );
    }

    public static void removePlantBlockMetadata(PerformantPlants performantPlants, Block block) {
        block.removeMetadata(plantMeta, performantPlants);
    }

    public static boolean hasPlantBlockMetadata(Block block) {
        return block.hasMetadata(plantMeta);
    }

    public static boolean hasPlantBlockMetadata(Block block, UUID plantUUID) {
        List<MetadataValue> value = block.getMetadata(plantMeta);
        if (value.size() > 0) {
            return value.get(0).asString().equals(plantUUID.toString());
        }
        return false;
    }

    public static String getPlantBlockMetadata(Block block) {
        List<MetadataValue> metadataValues = block.getMetadata(plantMeta);
        for (MetadataValue value : metadataValues) {
            if (value.getOwningPlugin() != null && value.getOwningPlugin() instanceof PerformantPlants) {
                return value.asString();
            }
        }
        return "";
    }

    public static boolean haveMatchingPlantMetadata(Block block1, Block block2) {
        return getPlantBlockMetadata(block1).equals(getPlantBlockMetadata(block2));
    }
    //endregion

    //region AnchorBlock Metadata
    public static void setAnchorBlockMetadata(PerformantPlants performantPlants, AnchorBlock anchorBlock) {
        anchorBlock.getLocation().getBlock().setMetadata(
                anchorMeta,
                new FixedMetadataValue(performantPlants, ""));
    }

    public static void removeAnchorBlockMetadata(PerformantPlants performantPlants, Block block) {
        block.removeMetadata(anchorMeta, performantPlants);
    }

    public static boolean hasAnchorBlockMetadata(Block block) {
        return block.hasMetadata(anchorMeta);
    }

    public static String getAnchorBlockMetadata(Block block) {
        List<MetadataValue> metadataValues = block.getMetadata(anchorMeta);
        for (MetadataValue value : metadataValues) {
            if (value.getOwningPlugin() != null && value.getOwningPlugin() instanceof PerformantPlants) {
                return value.asString();
            }
        }
        return "";
    }
    //endregion

}
