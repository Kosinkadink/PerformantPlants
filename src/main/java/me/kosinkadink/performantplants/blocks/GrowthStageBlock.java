package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class GrowthStageBlock {

    private final String id;
    private final RelativeLocation location;
    private final BlockData blockData;
    private String skullTexture;
    private RelativeLocation childOf;
    private boolean breakChildren = true;
    private boolean breakParent = false;
    private boolean updateStageOnBreak = false;
    private boolean ignoreSpace = false;
    private boolean stopGrowth = false;
    private boolean randomOrientation = false;
    private boolean placedOrientation = false;
    private PlantInteractStorage onInteract;
    private PlantInteractStorage onClick;
    private PlantInteractStorage onBreak;
    private DropStorage dropStorage = new DropStorage();
    // block replacement variables
    private boolean replacePlantBlock = false;
    private boolean replaceVanillaBlock = false;
    private boolean isVanillaBlock = false;

    public GrowthStageBlock(String id, int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings, String skullTexture) {
        this.id = id;
        location = new RelativeLocation(xRel, yRel, zRel);
        blockData = BlockHelper.createBlockData(material, blockDataStrings);
        this.skullTexture = skullTexture;
    }

    public String getId() {
        return id;
    }

    public RelativeLocation getLocation() {
        return location;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public RelativeLocation getChildOf() {
        return childOf;
    }

    public boolean hasChildOf() {
        return childOf != null;
    }

    public boolean isBreakChildren() {
        return breakChildren;
    }

    public void setBreakChildren(boolean bool) {
        breakChildren = bool;
    }

    public boolean isBreakParent() {
        return breakParent;
    }

    public void setBreakParent(boolean breakParent) {
        this.breakParent = breakParent;
    }

    public void setChildOf(RelativeLocation parentLocation) {
        childOf = parentLocation;
    }

    public boolean isIgnoreSpace() {
        return ignoreSpace;
    }

    public void setIgnoreSpace(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }

    public boolean isStopGrowth() {
        return stopGrowth;
    }

    public void setStopGrowth(boolean stopGrowth) {
        this.stopGrowth = stopGrowth;
    }

    public boolean isUpdateStageOnBreak() {
        return updateStageOnBreak;
    }

    public void setUpdateStageOnBreak(boolean updateStageOnBreak) {
        this.updateStageOnBreak = updateStageOnBreak;
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        this.skullTexture = skullTexture;
    }

    public boolean isRandomOrientation() {
        return randomOrientation;
    }

    public void setRandomOrientation(boolean randomOrientation) {
        this.randomOrientation = randomOrientation;
    }

    public boolean isPlacedOrientation() {
        return placedOrientation;
    }

    public void setPlacedOrientation(boolean placedOrientation) {
        this.placedOrientation = placedOrientation;
    }

    public PlantInteractStorage getOnInteract() {
        return onInteract;
    }

    public PlantInteract getOnInteract(ItemStack itemStack, Player player, PlantBlock plantBlock, BlockFace blockFace) {
        if (onInteract != null) {
            return onInteract.getPlantInteract(itemStack, player, plantBlock, blockFace);
        }
        return null;
    }

    public void setOnInteract(PlantInteractStorage onInteract) {
        this.onInteract = onInteract;
    }

    public PlantInteractStorage getOnClick() {
        return onClick;
    }

    public PlantInteract getOnClick(ItemStack itemStack, Player player, PlantBlock plantBlock, BlockFace blockFace) {
        if (onClick != null) {
            return onClick.getPlantInteract(itemStack, player, plantBlock, blockFace);
        }
        return null;
    }

    public void setOnClick(PlantInteractStorage onClick) {
        this.onClick = onClick;
    }

    public PlantInteractStorage getOnBreak() {
        return onBreak;
    }

    public PlantInteract getOnBreak(ItemStack itemStack, Player player, PlantBlock plantBlock) {
        if (onBreak != null) {
            return onBreak.getPlantInteract(itemStack, player, plantBlock);
        }
        return null;
    }

    public void setOnBreak(PlantInteractStorage onBreak) {
        this.onBreak = onBreak;
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }

    // block replacement
    public boolean isReplacePlantBlock() {
        return replacePlantBlock;
    }

    public void setReplacePlantBlock(boolean replacePlantBlock) {
        this.replacePlantBlock = replacePlantBlock;
    }

    public boolean isReplaceVanillaBlock() {
        return replaceVanillaBlock;
    }

    public void setReplaceVanillaBlock(boolean replaceVanillaBlock) {
        this.replaceVanillaBlock = replaceVanillaBlock;
    }

    public boolean isVanillaBlock() {
        return isVanillaBlock;
    }

    public void setVanillaBlock(boolean vanillaBlock) {
        isVanillaBlock = vanillaBlock;
    }
}
