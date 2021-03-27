package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.DestroyReason;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantBrokenEvent;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.data.type.RedstoneWire;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class BlockHelper {

    private static final BlockFace[] rotatableBlockFaces = {
        BlockFace.EAST,BlockFace.EAST_NORTH_EAST,BlockFace.EAST_SOUTH_EAST,
        BlockFace.NORTH,BlockFace.NORTH_NORTH_EAST,BlockFace.NORTH_EAST,BlockFace.NORTH_NORTH_WEST,BlockFace.NORTH_WEST,
        BlockFace.WEST,BlockFace.WEST_NORTH_WEST,BlockFace.WEST_SOUTH_WEST,
        BlockFace.SOUTH,BlockFace.SOUTH_SOUTH_EAST,BlockFace.SOUTH_EAST,BlockFace.SOUTH_SOUTH_WEST,BlockFace.SOUTH_WEST
    };

    private static final BlockFace[] directionalBlockFaces = {
            BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH
    };

    private static final BlockFace[] omnidirectionalBlockFaces = {
            BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN
    };

    public static BlockFace[] getDirectionalBlockFaces() {
        return directionalBlockFaces.clone();
    }

    public static BlockData createBlockData(Material material, ArrayList<String> blockDataStrings) {
        // if any blockData provided, format it into string and include it
        if (blockDataStrings.size() > 0) {
            // create string from blockDataStrings
            StringBuilder stringBuilder = new StringBuilder();
            // start of string
            stringBuilder.append("[");
            for (int i = 0; i < blockDataStrings.size(); i++) {
                if (i < blockDataStrings.size()-1) {
                    stringBuilder.append(blockDataStrings.get(i)).append(",");
                }
                else {
                    stringBuilder.append(blockDataStrings.get(i));
                }
            }
            // end of string
            stringBuilder.append("]");
            return Bukkit.createBlockData(material, stringBuilder.toString());
        }
        // otherwise blockData just contains material
        return Bukkit.createBlockData(material);
    }

    public static Block getAbsoluteBlock(Block anchor, RelativeLocation relative, PlantBlock plantBlock, BlockFace direction) {
        if (plantBlock != null && plantBlock.getPlant().isRotatePlant()) {
            if (direction == null) {
                direction = getDirectionFromYaw(plantBlock.getBlockYaw());
            }
            RelativeLocation newRelative = calculateNewLocation(relative, direction.getOppositeFace());
            return anchor.getWorld().getBlockAt(
                    anchor.getX() + newRelative.getX(),
                    anchor.getY() + newRelative.getY(),
                    anchor.getZ() + newRelative.getZ()
            );
        }
        return anchor.getWorld().getBlockAt(
                anchor.getX() + relative.getX(),
                anchor.getY() + relative.getY(),
                anchor.getZ() + relative.getZ()
        );
    }

    public static RelativeLocation calculateNewLocation(RelativeLocation relative, BlockFace direction) {
        switch(direction) {
            case EAST:
                return new RelativeLocation(-1*relative.getZ(), relative.getY(), relative.getX());
            case SOUTH:
                return new RelativeLocation(-1*relative.getX(), relative.getY(), -1*relative.getZ());
            case WEST:
                return new RelativeLocation(relative.getZ(), relative.getY(), -1*relative.getX());
            default:
                return new RelativeLocation(relative.getX(), relative.getY(), relative.getZ());
        }
    }

    public static BlockData calculateNewBlockData(BlockData blockData, BlockFace rotation) {
        // check if block data is directional
        BlockData newBlockData = null;
        if (blockData instanceof Directional) {
            newBlockData = blockData.clone();
            Directional directional = (Directional) newBlockData;
            // ignore UP and DOWN cases
            switch(directional.getFacing()) {
                case UP:
                case DOWN:
                    return blockData;
                default:
                    directional.setFacing(BlockHelper.getNormalizedBlockFaceDirection(directional.getFacing(), rotation));
            }
        }
        if (blockData instanceof Rotatable) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            Rotatable rotatable = (Rotatable) newBlockData;
            rotatable.setRotation(BlockHelper.getNormalizedBlockFaceRotation(rotatable.getRotation(), rotation));
        }
        if (blockData instanceof Orientable) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            Orientable orientable = (Orientable) newBlockData;
            switch(orientable.getAxis()) {
                case X:
                    switch(rotation) {
                        case EAST:
                        case WEST:
                            orientable.setAxis(Axis.Z);
                    } break;
                case Z:
                    switch(rotation) {
                        case EAST:
                        case WEST:
                            orientable.setAxis(Axis.X);
                    } break;
            }
        }
        if (blockData instanceof MultipleFacing) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            MultipleFacing multipleFacing = (MultipleFacing) newBlockData;
            if (rotation != BlockFace.NORTH) {
                boolean north = multipleFacing.hasFace(BlockFace.NORTH);
                boolean east = multipleFacing.hasFace(BlockFace.EAST);
                boolean south = multipleFacing.hasFace(BlockFace.SOUTH);
                boolean west = multipleFacing.hasFace(BlockFace.WEST);
                switch(rotation) {
                    case EAST:
                        multipleFacing.setFace(BlockFace.NORTH, west);
                        multipleFacing.setFace(BlockFace.EAST, north);
                        multipleFacing.setFace(BlockFace.SOUTH, east);
                        multipleFacing.setFace(BlockFace.WEST, south);
                        break;
                    case SOUTH:
                        multipleFacing.setFace(BlockFace.NORTH, south);
                        multipleFacing.setFace(BlockFace.EAST, west);
                        multipleFacing.setFace(BlockFace.SOUTH, north);
                        multipleFacing.setFace(BlockFace.WEST, east);
                        break;
                    case WEST:
                        multipleFacing.setFace(BlockFace.NORTH, east);
                        multipleFacing.setFace(BlockFace.EAST, south);
                        multipleFacing.setFace(BlockFace.SOUTH, west);
                        multipleFacing.setFace(BlockFace.WEST, north);
                        break;
                }
            }
        }
        if (blockData instanceof RedstoneWire) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            RedstoneWire redstoneWire = (RedstoneWire) newBlockData;
            if (rotation != BlockFace.NORTH) {
                RedstoneWire.Connection north = redstoneWire.getFace(BlockFace.NORTH);
                RedstoneWire.Connection east = redstoneWire.getFace(BlockFace.EAST);
                RedstoneWire.Connection south = redstoneWire.getFace(BlockFace.SOUTH);
                RedstoneWire.Connection west = redstoneWire.getFace(BlockFace.WEST);
                switch(rotation) {
                    case EAST:
                        redstoneWire.setFace(BlockFace.NORTH, west);
                        redstoneWire.setFace(BlockFace.EAST, north);
                        redstoneWire.setFace(BlockFace.SOUTH, east);
                        redstoneWire.setFace(BlockFace.WEST, south);
                        break;
                    case SOUTH:
                        redstoneWire.setFace(BlockFace.NORTH, south);
                        redstoneWire.setFace(BlockFace.EAST, west);
                        redstoneWire.setFace(BlockFace.SOUTH, north);
                        redstoneWire.setFace(BlockFace.WEST, east);
                        break;
                    case WEST:
                        redstoneWire.setFace(BlockFace.NORTH, east);
                        redstoneWire.setFace(BlockFace.EAST, south);
                        redstoneWire.setFace(BlockFace.SOUTH, west);
                        redstoneWire.setFace(BlockFace.WEST, north);
                        break;
                }
            }
        }
        if (blockData instanceof Rail) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            Rail rail = (Rail) newBlockData;
            switch(rotation) {
                case EAST:
                    switch(rail.getShape()) {
                        // ascending
                        case ASCENDING_NORTH:
                            rail.setShape(Rail.Shape.ASCENDING_EAST); break;
                        case ASCENDING_EAST:
                            rail.setShape(Rail.Shape.ASCENDING_SOUTH); break;
                        case ASCENDING_SOUTH:
                            rail.setShape(Rail.Shape.ASCENDING_WEST); break;
                        case ASCENDING_WEST:
                            rail.setShape(Rail.Shape.ASCENDING_NORTH); break;
                        // corner
                        case NORTH_EAST:
                            rail.setShape(Rail.Shape.SOUTH_EAST); break;
                        case SOUTH_EAST:
                            rail.setShape(Rail.Shape.SOUTH_WEST); break;
                        case SOUTH_WEST:
                            rail.setShape(Rail.Shape.NORTH_WEST); break;
                        case NORTH_WEST:
                            rail.setShape(Rail.Shape.NORTH_EAST); break;
                        // straight
                        case NORTH_SOUTH:
                            rail.setShape(Rail.Shape.EAST_WEST); break;
                        case EAST_WEST:
                            rail.setShape(Rail.Shape.NORTH_SOUTH); break;
                    } break;
                case SOUTH:
                    switch(rail.getShape()) {
                        // ascending
                        case ASCENDING_NORTH:
                            rail.setShape(Rail.Shape.ASCENDING_SOUTH); break;
                        case ASCENDING_EAST:
                            rail.setShape(Rail.Shape.ASCENDING_WEST); break;
                        case ASCENDING_SOUTH:
                            rail.setShape(Rail.Shape.ASCENDING_NORTH); break;
                        case ASCENDING_WEST:
                            rail.setShape(Rail.Shape.ASCENDING_EAST); break;
                        // corner
                        case NORTH_EAST:
                            rail.setShape(Rail.Shape.SOUTH_WEST); break;
                        case SOUTH_EAST:
                            rail.setShape(Rail.Shape.NORTH_WEST); break;
                        case SOUTH_WEST:
                            rail.setShape(Rail.Shape.NORTH_EAST); break;
                        case NORTH_WEST:
                            rail.setShape(Rail.Shape.SOUTH_EAST); break;
                    } break;
                case WEST:
                    switch(rail.getShape()) {
                        // ascending
                        case ASCENDING_NORTH:
                            rail.setShape(Rail.Shape.ASCENDING_WEST); break;
                        case ASCENDING_EAST:
                            rail.setShape(Rail.Shape.ASCENDING_NORTH); break;
                        case ASCENDING_SOUTH:
                            rail.setShape(Rail.Shape.ASCENDING_EAST); break;
                        case ASCENDING_WEST:
                            rail.setShape(Rail.Shape.ASCENDING_SOUTH); break;
                        // corner
                        case NORTH_EAST:
                            rail.setShape(Rail.Shape.NORTH_WEST); break;
                        case SOUTH_EAST:
                            rail.setShape(Rail.Shape.NORTH_EAST); break;
                        case SOUTH_WEST:
                            rail.setShape(Rail.Shape.SOUTH_EAST); break;
                        case NORTH_WEST:
                            rail.setShape(Rail.Shape.SOUTH_WEST); break;
                        // straight
                        case NORTH_SOUTH:
                            rail.setShape(Rail.Shape.EAST_WEST); break;
                        case EAST_WEST:
                            rail.setShape(Rail.Shape.NORTH_SOUTH); break;
                    } break;
            }
        }
        if (blockData instanceof Jigsaw) {
            if (newBlockData == null) {
                newBlockData = blockData.clone();
            }
            Jigsaw jigsaw = (Jigsaw) newBlockData;
            switch(rotation) {
                case EAST:
                    switch(jigsaw.getOrientation()) {
                        // down
                        case DOWN_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_EAST); break;
                        case DOWN_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_SOUTH); break;
                        case DOWN_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_WEST); break;
                        case DOWN_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_NORTH); break;
                        // side
                        case NORTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.EAST_UP); break;
                        case EAST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.SOUTH_UP); break;
                        case SOUTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.WEST_UP); break;
                        case WEST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.NORTH_UP); break;
                        // up
                        case UP_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_EAST); break;
                        case UP_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_SOUTH); break;
                        case UP_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_WEST); break;
                        case UP_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_NORTH); break;
                    } break;
                case SOUTH:
                    switch(jigsaw.getOrientation()) {
                        // down
                        case DOWN_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_SOUTH); break;
                        case DOWN_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_WEST); break;
                        case DOWN_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_NORTH); break;
                        case DOWN_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_EAST); break;
                        // side
                        case NORTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.SOUTH_UP); break;
                        case EAST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.WEST_UP); break;
                        case SOUTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.NORTH_UP); break;
                        case WEST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.EAST_UP); break;
                        // up
                        case UP_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_SOUTH); break;
                        case UP_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_WEST); break;
                        case UP_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_NORTH); break;
                        case UP_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_EAST); break;
                    } break;
                case WEST:
                    switch(jigsaw.getOrientation()) {
                        // down
                        case DOWN_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_WEST); break;
                        case DOWN_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_NORTH); break;
                        case DOWN_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_EAST); break;
                        case DOWN_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.DOWN_SOUTH); break;
                        // side
                        case NORTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.WEST_UP); break;
                        case EAST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.NORTH_UP); break;
                        case SOUTH_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.EAST_UP); break;
                        case WEST_UP:
                            jigsaw.setOrientation(Jigsaw.Orientation.SOUTH_UP); break;
                        // up
                        case UP_NORTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_WEST); break;
                        case UP_EAST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_NORTH); break;
                        case UP_SOUTH:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_EAST); break;
                        case UP_WEST:
                            jigsaw.setOrientation(Jigsaw.Orientation.UP_SOUTH); break;
                    } break;
            }
        }
        if (newBlockData != null) {
            return newBlockData;
        }
        return blockData;
    }

    public static BlockFace getNormalizedBlockFaceDirection(BlockFace initial, BlockFace correction) {
        float initialYaw = BlockHelper.getYawFromRotation(initial);
        float correctionYaw = BlockHelper.getYawFromRotation(correction);
        return BlockHelper.getDirectionFromYaw(initialYaw+correctionYaw);
    }

    public static BlockFace getNormalizedBlockFaceDirectionSubtract(BlockFace initial, BlockFace correction) {
        float initialYaw = BlockHelper.getYawFromRotation(initial);
        float correctionYaw = BlockHelper.getYawFromRotation(correction);
        return BlockHelper.getDirectionFromYaw(initialYaw-correctionYaw);
    }

    public static BlockFace getNormalizedBlockFaceRotation(BlockFace initial, BlockFace correction) {
        float initialYaw = BlockHelper.getYawFromRotation(initial);
        float correctionYaw = BlockHelper.getYawFromRotation(correction);
        return BlockHelper.getRotationFromYaw(initialYaw+correctionYaw);
    }

    public static void setBlockData(Block block, GrowthStageBlock stageBlock, PlantBlock plantBlock, BlockFace direction) {
        if (direction != null) {
            block.setBlockData(BlockHelper.calculateNewBlockData(stageBlock.getBlockData(), direction.getOppositeFace()));
        } else {
            block.setBlockData(stageBlock.getBlockData());
        }
        ReflectionHelper.setSkullTexture(block, stageBlock.getSkullTexture());
        if (plantBlock != null && stageBlock.isPlacedOrientation()) {
            setRotation(block, getOppositeRotationFromYaw(plantBlock.getBlockYaw()));
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

    public static BlockFace getOppositeRotationFromYaw(float yaw) {
        return getRotationFromYaw(yaw).getOppositeFace();
    }

    public static float getYawFromRotation(BlockFace blockFace) {
        switch(blockFace) {
            case NORTH_NORTH_EAST:
                return 22.5F;
            case NORTH_EAST:
                return 45.0F;
            case EAST_NORTH_EAST:
                return 67.5F;
            case EAST:
                return 90.0F;
            case EAST_SOUTH_EAST:
                return 112.5F;
            case SOUTH_EAST:
                return 135.0F;
            case SOUTH_SOUTH_EAST:
                return 157.5F;
            case SOUTH:
                return 180.0F;
            case SOUTH_SOUTH_WEST:
                return 202.5F;
            case SOUTH_WEST:
                return 225.0F;
            case WEST_SOUTH_WEST:
                return 247.5F;
            case WEST:
                return 270.0F;
            case WEST_NORTH_WEST:
                return 292.5F;
            case NORTH_WEST:
                return 315.0F;
            case NORTH_NORTH_WEST:
                return 337.5F;
            default:
                return 0.0F;
        }
    }

    public static BlockFace getRotationFromYaw(float yaw) {
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

    public static BlockFace getDirectionFromYaw(float yaw) {
        float rotation = yaw % 360.0F;
        if (rotation < 0.0F) {
            rotation += 360.0F;
        }
        if (0.0F <= rotation && rotation < 45F) {
            return BlockFace.NORTH;
        }
        if (45F <= rotation && rotation < 135F) {
            return BlockFace.EAST;
        }
        if (135F <= rotation && rotation < 225F) {
            return BlockFace.SOUTH;
        }
        if (225F <= rotation && rotation < 315F) {
            return BlockFace.WEST;
        }
        return BlockFace.NORTH;
    }

    public static boolean isOmnidirectionalBlockFace(BlockFace blockFace) {
        for (BlockFace omnidirectionalBlockFace : omnidirectionalBlockFaces) {
            if (blockFace == omnidirectionalBlockFace) {
                return true;
            }
        }
        return false;
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
                !block.getType().toString().endsWith("STAIRS") &&
                !block.getType().toString().endsWith("FENCE");
    }

    public static Location getCenter(Block block) {
        return block.getLocation().add(0.5,0.5,0.5);
    }

    public static boolean destroyPlantBlock(PerformantPlants performantPlants, Block block, PlantBlock plantBlock,
                                            DestroyReason reason, ExecutionContext context) {
        // if destroy behavior was already executed for this plant block, do nothing
        if (plantBlock.isDestroyBehaviorExecuted()) {
            return true;
        }

        DestroyReason newReason = reason.getRelativeEquivalent();
        boolean destroyed = true;
        if (reason != DestroyReason.REPLACE) {
            ScriptBlock destroyBehavior = null;
            // set destroy behavior; use onDestroy if proper ScriptBlock is null
            switch (newReason) {
                case RELATIVE_BREAK:
                    destroyBehavior = plantBlock.getOnBreak();
                    if (destroyBehavior == null) {
                        destroyBehavior = plantBlock.getOnDestroy();
                    }
                    break;
                case RELATIVE_EXPLODE:
                    destroyBehavior = plantBlock.getOnExplode();
                    if (destroyBehavior == null) {
                        destroyBehavior = plantBlock.getOnDestroy();
                    }
                    break;
                case RELATIVE_BURN:
                    destroyBehavior = plantBlock.getOnBurn();
                    if (destroyBehavior == null) {
                        destroyBehavior = plantBlock.getOnDestroy();
                    }
                    break;
                case RELATIVE_PISTON:
                    destroyBehavior = plantBlock.getOnPiston();
                    if (destroyBehavior == null) {
                        destroyBehavior = plantBlock.getOnDestroy();
                    }
                    break;
                case RELATIVE_DESTROY:
                    destroyBehavior = plantBlock.getOnDestroy();
                    break;
            }
            // if destroy behavior set, use it to determine if block should be broken
            if (destroyBehavior != null) {
                if (context == null) {
                    context = new ExecutionContext().set(plantBlock);
                }
                context.setDestroyReason(reason);
                destroyed = destroyBehavior.loadValue(context).getBooleanValue();
            }
            plantBlock.setDestroyBehaviorExecuted(true);
        }

        // ignore destroyed result if reason is relative
        if (!reason.isRelative() && !destroyed) {
            plantBlock.setDestroyBehaviorExecuted(false);
            return false;
        }

        block.setType(Material.AIR);
        boolean removed = performantPlants.getPlantManager().removePlantBlock(plantBlock);
        // call PlantBrokenEvent to signal a plant block has been broken, if removed
        if (removed) {
            performantPlants.getServer().getPluginManager().callEvent(new PlantBrokenEvent(null, plantBlock, block));
        }
        // if block's children should be removed, remove them
        if (plantBlock.isBreakChildren()) {
            ArrayList<BlockLocation> childLocations = new ArrayList<>(plantBlock.getChildLocations());
            for (BlockLocation childLocation : childLocations) {
                destroyPlantBlock(performantPlants, childLocation, newReason, context);
            }
        }
        // if block's parent should be removed, remove it
        if (plantBlock.isBreakParent()) {
            BlockLocation parentLocation = plantBlock.getParentLocation();
            if (parentLocation != null) {
                destroyPlantBlock(performantPlants, parentLocation, newReason, context);
            }
        }
        return true;
    }

    public static boolean destroyPlantBlock(PerformantPlants performantPlants, PlantBlock plantBlock,
                                            DestroyReason reason, ExecutionContext context) {
        if (plantBlock != null) {
            if (context != null) {
                context.set(plantBlock);
            }
            return destroyPlantBlock(performantPlants, plantBlock.getBlock(), plantBlock, reason , context);
        }
        return false;
    }

    public static boolean destroyPlantBlock(PerformantPlants performantPlants, Block block, DestroyReason reason,
                                            ExecutionContext context) {
        PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(block);
        if (plantBlock != null) {
            if (context != null) {
                context.set(plantBlock);
            }
            return destroyPlantBlock(performantPlants, block, plantBlock, reason, context);
        }
        return false;
    }

    public static boolean destroyPlantBlock(PerformantPlants performantPlants, BlockLocation blockLocation,
                                            DestroyReason reason, ExecutionContext context) {
        PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(blockLocation);
        if (plantBlock != null) {
            if (context != null) {
                context.set(plantBlock);
            }
            return destroyPlantBlock(performantPlants, plantBlock.getBlock(), plantBlock, reason, context);
        }
        return false;
    }

}
