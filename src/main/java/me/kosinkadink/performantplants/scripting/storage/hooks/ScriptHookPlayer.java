package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ScriptHookPlayer extends ScriptHook {

    protected ScriptBlock currentPlayer = ScriptResult.TRUE;
    protected ScriptBlock playerId = ScriptResult.EMPTY;

    public ScriptHookPlayer(HookAction action) {
        super(action);
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

    public ScriptBlock getPlayerId() {
        return playerId;
    }

    public String getPlayerIdValue(Player player, PlantBlock plantBlock) {
        return playerId.loadValue(plantBlock, player).getStringValue();
    }

    public void setPlayerId(ScriptBlock playerId) {
        this.playerId = playerId;
    }

    protected OfflinePlayer createOfflinePlayer(Player player, PlantBlock plantBlock) {
        OfflinePlayer offlinePlayer = null;
        // if playerId explicitly set, try to get it
        if (playerId != ScriptResult.EMPTY) {
            String playerIdValue = getPlayerIdValue(player, plantBlock);
            try {
                UUID playerUUID = UUID.fromString(playerIdValue);
                offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                offlinePlayer = Bukkit.getOfflinePlayer(playerIdValue);
            }
        }
        // otherwise, use current player
        else if (getCurrentPlayerValue(player, plantBlock)) {
            offlinePlayer = player;
        }
        return offlinePlayer;
    }

}
