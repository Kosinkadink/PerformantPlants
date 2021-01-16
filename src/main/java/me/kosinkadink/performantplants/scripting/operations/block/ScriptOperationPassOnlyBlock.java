package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationPassOnlyBlock extends ScriptOperation {

    public ScriptOperationPassOnlyBlock(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        return getScriptBlock().loadValue(plantBlock, null);
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getScriptBlock() == null) {
            throw new IllegalArgumentException("ScriptBlock cannot be null for ScriptOperationPassOnlyBlock");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.BLOCK;
    }
}
