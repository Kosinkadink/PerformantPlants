package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class PlantScriptEffect extends PlantEffect {

    private ScriptBlock scriptBlock = ScriptResult.EMPTY;

    public PlantScriptEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        getScriptBlockValue(context);
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        getScriptBlockValue(context);
    }

    public ScriptBlock getScriptBlock() {
        return scriptBlock;
    }

    public ScriptResult getScriptBlockValue(ExecutionContext context) {
        return scriptBlock.loadValue(context);
    }

    public void setScriptBlock(ScriptBlock scriptBlock) {
        this.scriptBlock = scriptBlock;
    }
}
