package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.entity.Player;

public abstract class ScriptOperation extends ScriptBlock {

    protected ScriptBlock[] inputs;

    protected ScriptOperation(ScriptBlock... inputs) throws IllegalArgumentException {
        this.inputs = inputs;
        validateInputsAndSetType();
    }

    public ScriptResult perform() throws IllegalArgumentException {
        return perform(null, null);
    }

    public ScriptResult perform(PlantBlock plantBlock) throws IllegalArgumentException {
        return perform(plantBlock, null);
    }

    public abstract ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException;

    @Override
    public boolean containsVariable() {
        for (ScriptBlock input : inputs) {
            if (input.containsVariable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsCategories(ScriptCategory... categories) {
        for (ScriptBlock input : inputs) {
            // check if input is one of the categories
            for (ScriptCategory category : categories) {
                if (input.getCategory() == category) {
                    return true;
                }
            }
            // check if ScriptBlocks in input are one of the categories
            if (input.containsCategories(categories)) {
                return true;
            }
        }
        return false;
    }

    protected abstract void setType();

    protected abstract void validateInputs() throws IllegalArgumentException;

    protected void validateInputsAndSetType() throws IllegalArgumentException {
        validateInputs();
        setType();
        optimizeInputs();
    }

    protected void optimizeInputs() {
        for (int i = 0; i < inputs.length; i++) {
            ScriptBlock input = inputs[i];
            // if operation and does not contain a variable, replace with result
            if (input instanceof ScriptOperation && input.shouldOptimize() && !input.containsVariable()) {
                inputs[i] = ((ScriptOperation) input).perform();
            }
        }
    }

    @Override
    public ScriptResult loadValue(PlantBlock plantBlock, Player player) {
        return perform(plantBlock, player);
    }

    @Override
    public ScriptBlock optimizeSelf() {
        if (shouldOptimize() && !containsVariable()) {
            return perform();
        }
        return this;
    }

    @Override
    public boolean shouldOptimize() {
        return true;
    }

}
