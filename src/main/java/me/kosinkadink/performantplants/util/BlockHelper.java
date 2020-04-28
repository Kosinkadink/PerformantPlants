package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    public static void setBlockData(Block block, GrowthStageBlock stageBlock, PlantBlock plantBlock) {
        block.setBlockData(stageBlock.getBlockData());
        ReflectionHelper.setSkullTexture(block, stageBlock.getSkullTexture());
        if (plantBlock != null && stageBlock.isPlacedOrientation()) {
            setRotation(block, getOppositeDirectionFromYaw(plantBlock.getBlockYaw()));
        } else if (stageBlock.isRandomOrientation()) {
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

    public static BlockFace getOppositeDirectionFromYaw(float yaw) {
        return getDirectionFromYaw(yaw).getOppositeFace();
    }

    public static BlockFace getDirectionFromYaw(float yaw) {
        float rotation = yaw % 360.0F;
        if (rotation < 0.0F) {
            rotation += 360.0F;
        }
        if (0.0F <= rotation && rotation < 11.25F) {
            return BlockFace.NORTH;
        }
        if ((11.25F <= rotation) && (rotation < 33.75F)) {
            return BlockFace.NORTH_NORTH_EAST;
        }
        if ((33.75F <= rotation) && (rotation < 56.25F)) {
            return BlockFace.NORTH_EAST;
        }
        if ((56.25F <= rotation) && (rotation < 78.75F)) {
            return BlockFace.EAST_NORTH_EAST;
        }
        if ((78.75F <= rotation) && (rotation < 101.25F)) {
            return BlockFace.EAST;
        }
        if ((101.25F <= rotation) && (rotation < 123.75F)) {
            return BlockFace.EAST_SOUTH_EAST;
        }
        if ((123.75F <= rotation) && (rotation < 146.25F)) {
            return BlockFace.SOUTH_EAST;
        }
        if ((146.25F <= rotation) && (rotation < 168.75F)) {
            return BlockFace.SOUTH_SOUTH_EAST;
        }
        if ((168.75F <= rotation) && (rotation < 191.25F)) {
            return BlockFace.SOUTH;
        }
        if ((191.25F <= rotation) && (rotation < 213.75F)) {
            return BlockFace.SOUTH_SOUTH_WEST;
        }
        if ((213.75F <= rotation) && (rotation < 236.25F)) {
            return BlockFace.SOUTH_WEST;
        }
        if ((236.25F <= rotation) && (rotation < 258.75F)) {
            return BlockFace.WEST_SOUTH_WEST;
        }
        if ((258.75F <= rotation) && (rotation < 281.25F)) {
            return BlockFace.WEST;
        }
        if ((281.25F <= rotation) && (rotation < 303.75F)) {
            return BlockFace.WEST_NORTH_WEST;
        }
        if ((303.75F <= rotation) && (rotation < 326.25F)) {
            return BlockFace.NORTH_WEST;
        }
        if ((326.2F <= rotation) && (rotation < 348.75F)) {
            return BlockFace.NORTH_NORTH_WEST;
        }
        // if ((348.75F <= rotation) && (rotation < 360.0F))
        return BlockFace.NORTH;
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

    public static boolean isInteractable(Block block) {
        return block.getType().isInteractable() &&
                block.getType() != Material.PISTON_HEAD &&
                !block.getType().toString().endsWith("STAIRS");
    }

    public static Location getCenter(Block block) {
        return block.getLocation().add(0.5,0.5,0.5);
    }

}
