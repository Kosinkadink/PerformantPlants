package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class GrowthStageBlock {

    private String id;
    private RelativeLocation location;
    private BlockData blockData;
    private String skullTexture;
    private RelativeLocation childOf;
    private boolean breakChildren = true;
    private boolean breakParent = false;
    private boolean updateStageOnBreak = false;
    private boolean ignoreSpace = false;
    private boolean keepVanilla = false;
    private int dropLimit = -1;
    private ArrayList<Drop> drops = new ArrayList<>();

    public GrowthStageBlock(String id, int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings) {
        this.id = id;
        location = new RelativeLocation(xRel, yRel, zRel);
        blockData = BlockHelper.createBlockData(material, blockDataStrings);
    }

    public GrowthStageBlock(String id, int xRel, int yRel, int zRel, Material material) {
        this(id, xRel, yRel, zRel, material, new ArrayList<>());
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

    public ArrayList<Drop> getDrops() {
        return drops;
    }

    public void addDrop(Drop drop) {
        drops.add(drop);
    }

    public void setChildOf(RelativeLocation parentLocation) {
        childOf = parentLocation;
    }

    public int getDropLimit() {
        return dropLimit;
    }

    public void setDropLimit(int limit) {
        if (limit >= 0) {
            dropLimit = limit;
        }
    }

    public boolean isIgnoreSpace() {
        return ignoreSpace;
    }

    public void setIgnoreSpace(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }

    public boolean isKeepVanilla() {
        return keepVanilla;
    }

    public void setKeepVanilla(boolean keepVanilla) {
        this.keepVanilla = keepVanilla;
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
}
