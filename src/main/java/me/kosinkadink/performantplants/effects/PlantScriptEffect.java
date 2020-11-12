package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantScriptEffect extends PlantEffect {

    private ScriptBlock scriptBlock = ScriptResult.EMPTY;

    public PlantScriptEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        getScriptBlockValue(player, plantBlock);
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        getScriptBlockValue(null, plantBlock);
    }

    public ScriptBlock getScriptBlock() {
        return scriptBlock;
    }

    public ScriptResult getScriptBlockValue(Player player, PlantBlock plantBlock) {
        return scriptBlock.loadValue(plantBlock, player);
    }

    public void setScriptBlock(ScriptBlock scriptBlock) {
        this.scriptBlock = scriptBlock;
    }
}
