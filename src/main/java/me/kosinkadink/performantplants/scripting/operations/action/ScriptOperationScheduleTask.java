package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.tasks.PlantTask;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptOperationScheduleTask extends ScriptOperation {

    private final String plantId;
    private final String taskConfigId;

    public ScriptOperationScheduleTask(String plantId, String taskConfigId, ScriptBlock delay, ScriptBlock currentPlayer,
                                       ScriptBlock currentBlock, ScriptBlock playerId) {
        super(delay, currentPlayer, currentBlock, playerId);
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
    }

    public ScriptBlock getDelay() {
        return inputs[0];
    }

    public ScriptBlock getCurrentPlayer() {
        return inputs[1];
    }

    public ScriptBlock getCurrentBlock() {
        return inputs[2];
    }

    public ScriptBlock getPlayerId() {
        return inputs[3];
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        PlantTask plantTask = new PlantTask(plantId, taskConfigId);
        // set delay
        long delay = getDelay().loadValue(plantBlock, player).getLongValue();
        if (delay < 0) {
            delay = 0;
        }
        plantTask.setDelay(delay);
        // set offline player
        String playerId = getPlayerId().loadValue(plantBlock, player).getStringValue();
        boolean currentPlayer = getCurrentPlayer().loadValue(plantBlock, player).getBooleanValue();
        OfflinePlayer offlinePlayer = null;
        if (!playerId.isEmpty()) {
            UUID playerUUID;
            try {
                playerUUID = UUID.fromString(playerId);
                offlinePlayer = Main.getInstance().getServer().getOfflinePlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                // assume the string is a direct name
                offlinePlayer = Main.getInstance().getServer().getOfflinePlayer(playerId);
            }
        } else if (currentPlayer) {
            offlinePlayer = player;
        }
        plantTask.setOfflinePlayer(offlinePlayer);
        // set block location
        boolean currentBlock = getCurrentBlock().loadValue(plantBlock, player).getBooleanValue();
        if (currentBlock && plantBlock != null) {
            plantTask.setBlockLocation(plantBlock.getLocation());
        }
        // attempt to schedule task and return result
        if (Main.getInstance().getTaskManager().scheduleTask(plantTask)) {
            return new ScriptResult(plantTask.getTaskId().toString());
        }
        return ScriptResult.EMPTY;
    }

    @Override
    protected void setType() {
        type = ScriptType.STRING;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }
}
