package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

public class MetadataHelper {

    public static void setPlantBlockMetadata(PerformantPlants performantPlants, PlantBlock plantblock) {
        plantblock.getBlock().setMetadata(
                "performantplants-plant",
                new FixedMetadataValue(performantPlants, plantblock.getPlantUUID().toString())
        );
    }

    public static void removePlantBlockMetadata(PerformantPlants performantPlants, Block block) {
        block.removeMetadata("performantplants-plant", performantPlants);
    }

    public static boolean hasPlantBlockMetadata(Block block) {
        return block.hasMetadata("performantplants-plant");
    }

    public static boolean hasPlantBlockMetadata(Block block, UUID plantUUID) {
        List<MetadataValue> value = block.getMetadata("performantplants-plant");
        if (value.size() > 0) {
            return value.get(0).asString().equals(plantUUID.toString());
        }
        return false;
    }

}
