package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;
import me.kosinkadink.performantplants.util.RandomHelper;
import org.bukkit.entity.Player;

public class ScriptOperationRandomDouble extends ScriptOperationBinary {

    public ScriptOperationRandomDouble(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        ScriptResult leftInstance = getLeft().loadValue(plantBlock, player);
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        // perform the random generation
        return new ScriptResult(RandomHelper.generateRandomDoubleInRange(
                leftInstance.getDoubleValue(), rightInstance.getDoubleValue()
        ));
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if ((getLeft().getType() != ScriptType.DOUBLE && getLeft().getType() != ScriptType.LONG) ||
                (getRight().getType() != ScriptType.DOUBLE && getRight().getType() != ScriptType.LONG)) {
            throw new IllegalArgumentException("RandomDouble operation only supports ScriptType DOUBLE and LONG");
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.RANDOM;
    }

}
