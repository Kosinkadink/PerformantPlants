package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.tasks.PlantTask;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptTask {

    private final String plantId;
    private final String taskConfigId;

    private ScriptBlock taskScriptBlock;
    private ScriptBlock delay = ScriptResult.ZERO;
    private ScriptBlock currentPlayer = ScriptResult.TRUE;
    private ScriptBlock currentBlock = ScriptResult.TRUE;
    private ScriptBlock playerId = ScriptResult.EMPTY;
    // TODO: add hooks


    public ScriptTask(String plantId, String taskConfigId) {
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
    }

    public PlantTask createPlantTask(Player player, PlantBlock plantBlock) {
        PlantTask plantTask = new PlantTask(plantId, taskConfigId);
        // set delay
        plantTask.setDelay(getDelayValue(player, plantBlock));
        // set current block location, if true
        if (getCurrentBlockValue(player, plantBlock)) {
            plantTask.setBlockLocation(plantBlock.getLocation());
        }
        // if playerId explicitly set, try to get it
        if (playerId != ScriptResult.EMPTY) {
            OfflinePlayer offlinePlayer;
            String playerIdValue = getPlayerIdValue(player, plantBlock);
            try {
                UUID playerUUID = UUID.fromString(playerIdValue);
                offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                offlinePlayer = Bukkit.getOfflinePlayer(playerIdValue);
            }
            plantTask.setOfflinePlayer(offlinePlayer);
        }
        // otherwise, use current player
        else if (getCurrentPlayerValue(player, plantBlock)) {
            plantTask.setOfflinePlayer(player);
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
}
