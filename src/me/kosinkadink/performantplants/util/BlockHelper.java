package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class BlockHelper {

    public static BlockData createBlockData(Material material, ArrayList<String> blockDataStrings) {
        // if any blockData provided, format it into string and include it
        if (blockDataStrings.size() > 0) {
            // create string from blockDataStrings
            StringBuilder stringBuilder = new StringBuilder();
            // start of string
            stringBuilder.append("[");
            for (int i = 0; i < blockDataStrings.size(); i++) {
                if (i < blockDataStrings.size()-1) {
                    stringBuilder.append(blockDataStrings.get(i));
                }
                else {
                    stringBuilder.append(blockDataStrings.get(i)).append(",");
                }
            }
            // end of string
            stringBuilder.append("]");
            return Bukkit.createBlockData(material, stringBuilder.toString());
        }
        // otherwise blockData just contains material
        return Bukkit.createBlockData(material);
    }

    public static Block getAbsoluteBlock(Main main, Block anchor, RelativeLocation relative) {
        return anchor.getWorld().getBlockAt(
                 anchor.getX() + relative.getX(),
                anchor.getY() + relative.getY(),
                anchor.getZ() + relative.getZ()
        );
    }

}
