package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptType;

public abstract class ScriptOperationItemInputs extends ScriptOperationItem {

    public ScriptOperationItemInputs(ScriptBlock... inputs) {
        super(inputs);
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        for (ScriptBlock input : inputs) {
            if (input.getType() != ScriptType.ITEMSTACK) {
                throw new IllegalArgumentException(String.format("Requires ScriptType ITEMSTACK for all inputs, but found %s", input.getType()));
            }
        }
    }

}
