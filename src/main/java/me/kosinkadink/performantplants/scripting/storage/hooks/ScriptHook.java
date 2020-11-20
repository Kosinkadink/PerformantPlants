package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ScriptHook {

    protected final HookAction action;

    public ScriptHook(HookAction action) {
        this.action = action;
    }

    public HookAction getAction() {
        return action;
    }

    public abstract PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock);

}
