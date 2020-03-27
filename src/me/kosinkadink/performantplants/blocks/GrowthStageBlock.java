package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.util.BlockDataHelper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class GrowthStageBlock {

    private String id;
    private RelativeLocation location;
    private BlockData blockData;
    private RelativeLocation childOf;
    private boolean breakChildren = true;
    private int dropLimit = 0;
    private ArrayList<Drop> drops = new ArrayList<>();

    public GrowthStageBlock(String id, int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings) {
        this.id = id;
        location = new RelativeLocation(xRel, yRel, zRel);
        blockData = BlockDataHelper.createBlockData(material, blockDataStrings);
    }

    public String getId() {
        return id;
    }

    public RelativeLocation getRelativeLocation() {
        return location;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public RelativeLocation getChildOf() {
        return childOf;
    }

    public boolean getBreakChildren() {
        return breakChildren;
    }

    public ArrayList<Drop> getDrops() {
        return drops;
    }

    public void setChildOf(RelativeLocation parentLocation) {
        childOf = parentLocation;
    }

    public void setBreakChildren(boolean bool) {
        breakChildren = bool;
    }

}
