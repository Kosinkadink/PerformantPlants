package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

    public boolean getCurrentPlayerValue(ExecutionContext context) {
        return currentPlayer.loadValue(context).getBooleanValue();
    }

    public void setCurrentPlayer(ScriptBlock currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ScriptBlock getPlayerId() {
        return playerId;
    }

    public String getPlayerIdValue(ExecutionContext context) {
        return playerId.loadValue(context).getStringValue();
    }

    public void setPlayerId(ScriptBlock playerId) {
        this.playerId = playerId;
    }

    protected OfflinePlayer createOfflinePlayer(ExecutionContext context) {
        OfflinePlayer offlinePlayer = null;
        // if playerId explicitly set, try to get it
        if (playerId != ScriptResult.EMPTY) {
            String playerIdValue = getPlayerIdValue(context);
            try {
                UUID playerUUID = UUID.fromString(playerIdValue);
                offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                offlinePlayer = Bukkit.getOfflinePlayer(playerIdValue);
            }
        }
        // otherwise, use current player
        else if (getCurrentPlayerValue(context)) {
            offlinePlayer = context.getPlayer();
        }
        return offlinePlayer;
    }

}
