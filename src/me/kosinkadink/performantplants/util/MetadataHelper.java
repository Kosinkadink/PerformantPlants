package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.locations.BlockLocation;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

public class MetadataHelper {

    public static void setPlantBlockMetadata(Main main, Block block) {
        block.setMetadata(
                "performantplants-plant",
                new FixedMetadataValue(main, new BlockLocation(block).toString())
        );
    }

    public static void removePlantBlockMetadata(Main main, Block block) {
        block.removeMetadata("performantplants-plant", main);
    }

}
