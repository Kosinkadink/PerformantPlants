package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class RequiredCondition {

    // vanilla block properties
    private BlockData blockData;
    private String skullTexture;
    // plant block properties
    private String plantId = "";
    private String stage = "";
    private String blockId = "";
    // general properties
    private boolean blacklisted = false;

    public RequiredCondition() {}

    public boolean checkIfMatches(Block block) {
        // order matters here; only checks that block data components of required block match the block's
        // but not the other way around
        // TODO: match skull texture as well
        return blockData.matches(block.getBlockData());
    }

    public boolean checkIfMatches(PlantBlock block) {
        if (block == null) {
            return false;
        }
        // check that plantId matches
        if (!plantId.equals(block.getPlant().getId())) {
            return false;
        }
        // check that stage matches, if set
        if (!stage.isEmpty()) {
            if (!block.getPlant().getStageStorage().isValidStage(stage)) {
                return false;
            }
            int stageIndex = block.getPlant().getStageStorage().getGrowthStageIndex(stage);
            if (stageIndex != block.getDropStageIndex()) {
                return false;
            }
        }
        // check that blockId matches, if set
        return blockId.isEmpty() || blockId.equals(block.getStageBlockId());
    }

    public boolean isVanillaMatch() {
        return blockData != null;
    }

    //region vanilla properties
    public BlockData getBlockData() {
        return blockData;
    }

    public void setBlockData(Material material, ArrayList<String> blockDataStrings) {
        this.blockData = BlockHelper.createBlockData(material, blockDataStrings);
    }

    public void setBlockData(Material material) {
        setBlockData(material, new ArrayList<>());
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        this.skullTexture = skullTexture;
    }
    //endregion

    //region plant properties
    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }
    //endregion

    //region general properties
    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }
    //endregion
}
