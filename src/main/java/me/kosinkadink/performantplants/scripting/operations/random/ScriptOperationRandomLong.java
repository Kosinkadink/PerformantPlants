package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;
import me.kosinkadink.performantplants.util.RandomHelper;
import org.bukkit.entity.Player;

public class ScriptOperationRandomLong extends ScriptOperationBinary {

    public ScriptOperationRandomLong(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        ScriptResult leftInstance = getLeft().loadValue(plantBlock, player);
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        // if left value is greater or equal to right value,
        // prevent error from occurring and just return 0L
        if (leftInstance.getLongValue() >= rightInstance.getLongValue()) {
            return new ScriptResult(0L);
        }
        // otherwise go ahead and perform the random generation
        return new ScriptResult(RandomHelper.generateRandomLongInRange(
                leftInstance.getLongValue(), rightInstance.getLongValue()
        ));
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getLeft().getType() != ScriptType.LONG ||
                getRight().getType() != ScriptType.LONG) {
            throw new IllegalArgumentException("RandomLong operation only supports ScriptType LONG");
        }
    }
}
