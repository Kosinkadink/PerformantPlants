package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class BlockHelper {

    private static BlockFace[] rotatableBlockFaces = {
        BlockFace.EAST,BlockFace.EAST_NORTH_EAST,BlockFace.EAST_SOUTH_EAST,
        BlockFace.NORTH,BlockFace.NORTH_NORTH_EAST,BlockFace.NORTH_EAST,BlockFace.NORTH_NORTH_WEST,BlockFace.NORTH_WEST,
        BlockFace.WEST,BlockFace.WEST_NORTH_WEST,BlockFace.WEST_SOUTH_WEST,
        BlockFace.SOUTH,BlockFace.SOUTH_SOUTH_EAST,BlockFace.SOUTH_EAST,BlockFace.SOUTH_SOUTH_WEST,BlockFace.SOUTH_WEST
    };

    private static BlockFace[] directionalBlockFaces = {
            BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH
    };

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

    public static void setBlockData(Block block, GrowthStageBlock stageBlock) {
        block.setBlockData(stageBlock.getBlockData());
        ReflectionHelper.setSkullTexture(block, stageBlock.getSkullTexture());
        if (stageBlock.isRandomOrientation()) {
            boolean rotated = setRotation(block, getRandomRotatableBlockFace());
            if (!rotated) {
                setDirection(block, getRandomDirectionalBlockFace());
            }
        }
    }

    public static boolean setRotation(Block block, BlockFace heading) {
        if (block.getBlockData() instanceof Rotatable) {
            Rotatable rotatable = (Rotatable) block.getBlockData();
            rotatable.setRotation(heading);
            block.setBlockData(rotatable);
            return true;
        }
        return false;
    }

    public static void setDirection(Block block, BlockFace heading) {
        if (block.getBlockData() instanceof Directional) {
            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(heading);
            block.setBlockData(directional);
        }
    }

    public static BlockFace getRandomRotatableBlockFace() {
        return rotatableBlockFaces[ThreadLocalRandom.current().nextInt(rotatableBlockFaces.length)];
    }

    public static BlockFace getRandomDirectionalBlockFace() {
        return directionalBlockFaces[ThreadLocalRandom.current().nextInt(directionalBlockFaces.length)];
    }

    public static boolean hasWater(Block block) {
        if (block.getType() != Material.WATER) {
            if (block.getBlockData() instanceof Waterlogged) {
                if (((Waterlogged) block.getBlockData()).isWaterlogged()) {
                    return true;
                }
            }
            return block.getType() == Material.SEAGRASS ||
                    block.getType() == Material.TALL_SEAGRASS ||
                    block.getType() == Material.KELP_PLANT;
        } else {
            return true;
        }
    }

}
