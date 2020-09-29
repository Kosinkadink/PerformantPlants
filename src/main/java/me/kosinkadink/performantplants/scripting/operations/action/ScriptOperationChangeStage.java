package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.storage.StageStorage;
import org.bukkit.entity.Player;

public class ScriptOperationChangeStage extends ScriptOperation {

    Main main;

    public ScriptOperationChangeStage(Main main, ScriptBlock stage, ScriptBlock ifNext) {
        super(stage, ifNext);
        this.main = main;
    }

    public ScriptBlock getStage() {
        return inputs[0];
    }

    public ScriptBlock getIfNext() {
        return inputs[1];
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        // if no block or is a child (has parent), do nothing
        if (plantBlock == null) {
            return ScriptResult.TRUE;
        }
        // use self or parent block, if has one
        PlantBlock effectivePlantBlock = plantBlock.getEffectivePlantBlock();
        String stageName = getStage().loadValue(effectivePlantBlock, player).getStringValue();
        boolean ifNext = getIfNext().loadValue(effectivePlantBlock, player).getBooleanValue();
        // try to go to stage or next stage; if worked, return true; otherwise return false
        boolean success = false;
        if (!stageName.isEmpty()) {
            StageStorage stageStorage = effectivePlantBlock.getPlant().getStageStorage();
            if (stageStorage.isValidStage(stageName)) {
                int stageIndex = stageStorage.getGrowthStageIndex(stageName);
                success = effectivePlantBlock.goToStageForcefully(main, stageIndex);
            } else {
                main.getLogger().warning(String.format("OperationChangeStage: stage name '%s' not recognized for " +
                        "block: %s", stageName, plantBlock.toString()));
            }
        }
        // if goToNext is set to true, then advance to next growth stage as if plant grew
        else if (ifNext) {
            success = effectivePlantBlock.goToNextStage(main);
        }
        return new ScriptResult(success);
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getStage() == null && getIfNext() == null) {
            throw new IllegalArgumentException("ChangeStage operation does not allow for both stage and ifNext to be " +
                    "null");
        }
        if (getStage().getType() != ScriptType.STRING) {
            throw new IllegalArgumentException("ChangeStage operation only accepts ScriptType STRING for its stage " +
                    "argument");
        }
        if (getIfNext().getType() != ScriptType.BOOLEAN) {
            throw new IllegalArgumentException("ChangeStage operation only accepts ScriptType BOOLEAN for its ifNext " +
                    "argument");
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }
}
