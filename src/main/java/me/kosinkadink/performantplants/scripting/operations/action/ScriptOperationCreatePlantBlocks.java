package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ScriptOperationCreatePlantBlocks extends ScriptOperation {

    private final String[] growthStageBlockIds;

    public ScriptOperationCreatePlantBlocks(String... growthStageBlockIds) {
        this.growthStageBlockIds = growthStageBlockIds;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (!context.isPlantBlockSet()) {
            return ScriptResult.FALSE;
        }
        PlantBlock effectivePlantBlock = context.getEffectivePlantBlock();
        HashMap<GrowthStageBlock,PlantBlock> blocksWithGuardiansAdded = new HashMap<>();
        for (String blockId : growthStageBlockIds) {
            // get growthStageBlock with id, if exists
            GrowthStageBlock growthStageBlock = effectivePlantBlock.getPlant().getGrowthStageBlock(blockId);
            if (growthStageBlock == null) {
                continue;
            }
            // don't replace block for parent blocks
            if (growthStageBlock.getLocation().equals(new RelativeLocation(0,0,0))) {
                // update block data at location
                BlockHelper.setBlockData(effectivePlantBlock.getBlock(), growthStageBlock, effectivePlantBlock);
                // set drop stage index
                effectivePlantBlock.setDropStageIndex(-1);
                // set growth stage block id
                effectivePlantBlock.setStageBlockId(blockId);
            } else {
                Block effectiveBlock = effectivePlantBlock.getBlock();
                Block absoluteBlock = BlockHelper.getAbsoluteBlock(effectiveBlock, growthStageBlock.getLocation());
                // update block data at location, if not another plant's plant block
                if (!MetadataHelper.hasPlantBlockMetadata(absoluteBlock, effectivePlantBlock.getPlantUUID())) {
                    if (MetadataHelper.hasPlantBlockMetadata(absoluteBlock) && !growthStageBlock.isReplacePlantBlock()) {
                        continue;
                    } else {
                        BlockHelper.destroyPlantBlock(PerformantPlants.getInstance(),absoluteBlock, false);
                    }
                    if (!absoluteBlock.isEmpty() && !growthStageBlock.isReplaceVanillaBlock()) {
                        continue;
                    }
                }
                // create plant block at location
                PlantBlock newPlantBlock = new PlantBlock(
                        new BlockLocation(absoluteBlock),
                        effectivePlantBlock.getPlant(),
                        effectivePlantBlock.getPlayerUUID(),
                        false,
                        effectivePlantBlock.getPlantUUID());
                // set stage index and stage block id
                newPlantBlock.setStageIndex(effectivePlantBlock.getStageIndex());
                newPlantBlock.setDropStageIndex(-1);
                newPlantBlock.setStageBlockId(blockId);
                // set parent to be effectivePlantBlock
                newPlantBlock.setParentLocation(effectivePlantBlock.getLocation());
                // add plant block via plantManager
                PerformantPlants.getInstance().getPlantManager().addPlantBlock(newPlantBlock);
                // if has childOf set, add to blocksAdded
                if (growthStageBlock.hasChildOf()) {
                    blocksWithGuardiansAdded.put(growthStageBlock, newPlantBlock);
                }
            }
        }
        // add children/guardians to any blocks that require them
        for (Map.Entry<GrowthStageBlock,PlantBlock> entry : blocksWithGuardiansAdded.entrySet()) {
            GrowthStageBlock growthStageBlock = entry.getKey();
            PlantBlock newPlantBlock = entry.getValue();
            // get guardian location
            BlockLocation guardianLocation = effectivePlantBlock.getLocation().getLocationFromRelative(growthStageBlock.getChildOf());
            // get guardian block
            PlantBlock guardianBlock = PerformantPlants.getInstance().getPlantManager().getPlantBlock(guardianLocation);
            if (guardianBlock != null) {
                // add guardian to plant block
                newPlantBlock.setGuardianLocation(guardianLocation);
                // add child to guardian block
                guardianBlock.addChildLocation(newPlantBlock.getLocation());
            }
        }
        return ScriptResult.TRUE;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

}
