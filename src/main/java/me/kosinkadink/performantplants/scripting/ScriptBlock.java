package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.entity.Player;

public abstract class ScriptBlock {

    protected ScriptType type = ScriptType.NULL;

    public ScriptResult loadValue(PlantBlock plantBlock) {
        return loadValue(plantBlock, null);
    }

    public abstract ScriptResult loadValue(PlantBlock plantBlock, Player player);

    public abstract boolean containsVariable();

    public ScriptType getType() {
        return type;
    }

}
