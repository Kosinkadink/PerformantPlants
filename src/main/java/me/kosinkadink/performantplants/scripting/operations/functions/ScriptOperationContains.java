package me.kosinkadink.performantplants.scripting.operations.functions;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;
import org.bukkit.entity.Player;

public class ScriptOperationContains extends ScriptOperationBinary {

    public ScriptOperationContains(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        ScriptResult leftInstance = getLeft().loadValue(plantBlock, player);
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        return new ScriptResult(leftInstance.getStringValue().contains(rightInstance.getStringValue()));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {
        if (getLeft().getType() != ScriptType.STRING) {
            throw new IllegalArgumentException("Contains operation only supports ScriptType STRING");
        }
    }
}
