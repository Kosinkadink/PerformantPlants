package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class RequiredBlock {

    private RelativeLocation location;
    private BlockData blockData;
    private String skullTexture;
    private boolean required = false;

    public RequiredBlock(int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings) {
        location = new RelativeLocation(xRel, yRel, zRel);
        blockData = BlockHelper.createBlockData(material, blockDataStrings);
    }

    public RequiredBlock(int xRel, int yRel, int zRel, Material material) {
        this(xRel, yRel, zRel, material, new ArrayList<>());
    }

    public RequiredBlock(int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings,
                         boolean required) {
        this(xRel, yRel, zRel, material, blockDataStrings);
        this.required = required;
    }

    public RequiredBlock(int xRel, int yRel, int zRel, Material material, boolean required) {
        this(xRel, yRel, zRel, material, new ArrayList<>(), required);
    }

    public RelativeLocation getLocation() {
        return location;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public boolean checkIfMatches(Block block) {
        // order matters here; only checks that block data components of required block match the block's
        // but not the other way around
        // TODO: match skull texture as well
        return blockData.matches(block.getBlockData());
    }

    public boolean isRequired() {
        return required;
    }
}
