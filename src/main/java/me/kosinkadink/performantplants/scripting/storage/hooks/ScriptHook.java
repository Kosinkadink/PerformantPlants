package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ScriptHook {

    protected final HookAction action;

    protected String hookConfigId = "";
    protected ScriptBlock hookScriptBlock;

    public ScriptHook(HookAction action) {
        this.action = action;
    }

    public HookAction getAction() {
        return action;
    }

    public String getHookConfigId() {
        return hookConfigId;
    }

    public void setHookConfigId(String hookConfigId) {
        this.hookConfigId = hookConfigId;
    }

    public ScriptBlock getHookScriptBlock() {
        return hookScriptBlock;
    }

    public void setHookScriptBlock(ScriptBlock hookScriptBlock) {
        this.hookScriptBlock = hookScriptBlock;
    }

    public abstract PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock);

}
