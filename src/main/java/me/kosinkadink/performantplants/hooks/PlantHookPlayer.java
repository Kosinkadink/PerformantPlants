package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.tasks.PlantTask;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class PlantHookPlayer extends PlantHook {

    protected OfflinePlayer offlinePlayer;

    public PlantHookPlayer(UUID taskId, HookAction action, String hookConfigId, OfflinePlayer offlinePlayer) {
        super(taskId, action, hookConfigId);
        this.offlinePlayer = offlinePlayer;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    @Override
    public boolean performScriptBlock(PlantTask task) {
        if (!hookConfigId.isEmpty()) {
            ScriptTask scriptTask = task.getScriptTask();
            if (scriptTask != null) {
                ScriptBlock scriptBlock = scriptTask.getHookScriptBlock(hookConfigId);
                if (hookConfigId != null) {
                    // perform script block
                    scriptBlock.loadValue(task.getPlantBlock(), PlayerHelper.getFreshPlayer(getOfflinePlayer()));
                }
            }
        }
        return true;
    }
}
