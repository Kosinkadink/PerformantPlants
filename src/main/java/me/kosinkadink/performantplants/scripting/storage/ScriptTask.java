package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.storage.hooks.ScriptHook;
import me.kosinkadink.performantplants.tasks.PlantTask;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptTask {

    private final String plantId;
    private final String taskConfigId;

    private ScriptBlock taskScriptBlock;
    private ScriptBlock autostart = ScriptResult.TRUE;
    private ScriptBlock delay = ScriptResult.ZERO;
    private ScriptBlock currentPlayer = ScriptResult.TRUE;
    private ScriptBlock currentBlock = ScriptResult.TRUE;
    private ScriptBlock playerId = ScriptResult.EMPTY;
    private final ArrayList<ScriptHook> hooks = new ArrayList<>();
    private final ConcurrentHashMap<String, ScriptHook> hookMap = new ConcurrentHashMap<>();


    public ScriptTask(String plantId, String taskConfigId) {
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
    }

    public ScriptTask clone() {
        ScriptTask cloned = new ScriptTask(plantId, taskConfigId);
        cloned.setTaskScriptBlock(taskScriptBlock);
        cloned.setAutostart(autostart);
        cloned.setDelay(delay);
        cloned.setCurrentPlayer(currentPlayer);
        cloned.setCurrentBlock(currentBlock);
        cloned.setPlayerId(playerId);
        for (ScriptHook hook : hooks) {
            cloned.addHook(hook);
        }
        return cloned;
    }

    public PlantTask createPlantTask(Player player, PlantBlock plantBlock) {
        PlantTask plantTask = new PlantTask(plantId, taskConfigId);
        // set delay
        long delay = getDelayValue(player, plantBlock);
        if (delay < 0) {
            delay = 0;
        }
        plantTask.setDelay(delay);
        // set current block location, if true
        boolean currentBlock = getCurrentBlockValue(player, plantBlock);
        if (currentBlock && plantBlock != null) {
            plantTask.setBlockLocation(plantBlock.getLocation());
        }
        // if playerId explicitly set, try to get it
        if (playerId != ScriptResult.EMPTY) {
            OfflinePlayer offlinePlayer;
            String playerIdValue = getPlayerIdValue(player, plantBlock);
            if (!playerIdValue.isEmpty()) {
                try {
                    UUID playerUUID = UUID.fromString(playerIdValue);
                    offlinePlayer = Main.getInstance().getServer().getOfflinePlayer(playerUUID);
                } catch (IllegalArgumentException e) {
                    return null;
                }
                plantTask.setOfflinePlayer(offlinePlayer);
            }
        }
        // otherwise, use current player
        else if (getCurrentPlayerValue(player, plantBlock)) {
            plantTask.setOfflinePlayer(player);
        }
        // set autostart
        plantTask.setAutostart(getAutostartValue(player, plantBlock));
        // add hooks
        for (ScriptHook scriptHook : hooks) {
            PlantHook plantHook = scriptHook.createPlantHook(plantTask.getTaskId(), player, plantBlock);
            if (plantHook != null) {
                plantTask.addHook(plantHook);
            }
        }
        return plantTask;
    }

    public String getPlantId() {
        return plantId;
    }

    public String getTaskConfigId() {
        return taskConfigId;
    }

    public ScriptBlock getTaskScriptBlock() {
        return taskScriptBlock;
    }

    public void setTaskScriptBlock(ScriptBlock taskScriptBlock) {
        this.taskScriptBlock = taskScriptBlock;
    }

    public ScriptBlock getDelay() {
        return delay;
    }

    public long getDelayValue(Player player, PlantBlock plantBlock) {
        return delay.loadValue(plantBlock, player).getLongValue();
    }

    public void setDelay(ScriptBlock delay) {
        this.delay = delay;
    }

    public ScriptBlock getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCurrentPlayerValue(Player player, PlantBlock plantBlock) {
        return currentPlayer.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setCurrentPlayer(ScriptBlock currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ScriptBlock getCurrentBlock() {
        return currentBlock;
    }

    public boolean getCurrentBlockValue(Player player, PlantBlock plantBlock) {
        return currentBlock.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setCurrentBlock(ScriptBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    public ScriptBlock getPlayerId() {
        return playerId;
    }

    public String getPlayerIdValue(Player player, PlantBlock plantBlock) {
        return playerId.loadValue(plantBlock, player).getStringValue();
    }

    public void setPlayerId(ScriptBlock playerId) {
        this.playerId = playerId;
    }

    public ArrayList<ScriptHook> getHooks() {
        return hooks;
    }

    public void addHook(ScriptHook scriptHook) {
        hooks.add(scriptHook);
        if (!scriptHook.getHookConfigId().isEmpty()) {
            hookMap.put(scriptHook.getHookConfigId(), scriptHook);
        }
    }

    public ScriptHook getHook(String hookConfigId) {
        return hookMap.get(hookConfigId);
    }

    public ScriptBlock getHookScriptBlock(String hookConfigId) {
        ScriptHook hook = getHook(hookConfigId);
        if (hook != null) {
            return hook.getHookScriptBlock();
        }
        return null;
    }

    public ScriptBlock getAutostart() {
        return autostart;
    }

    public boolean getAutostartValue(Player player, PlantBlock plantBlock) {
        return autostart.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setAutostart(ScriptBlock autostart) {
        this.autostart = autostart;
    }
}
