package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.tasks.PlantTask;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.UUID;

public abstract class PlantHookPlayer extends PlantHook {

    protected static final String JSON_PLAYER_UUID = "playerUUID";

    protected OfflinePlayer offlinePlayer;

    public PlantHookPlayer(UUID taskId, HookAction action, String hookConfigId, OfflinePlayer offlinePlayer) {
        super(taskId, action, hookConfigId);
        this.offlinePlayer = offlinePlayer;
    }

    public PlantHookPlayer(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId);
        loadValuesFromJsonString(jsonString);
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    @Override
    public String createJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_PLAYER_UUID, offlinePlayer.getUniqueId().toString());
        return jsonObject.toJSONString();
    }

    @Override
    public void loadValuesFromJsonString(String jsonString) throws PlantHookJsonParseException {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
        if (jsonObject == null) {
            PerformantPlants.getInstance().getLogger().warning(String.format("Json String could not be converted to " +
                    "JSONObject for PlantHookPlayer: %s,%s with String: %s", taskId, hookConfigId, jsonString));
            throw new PlantHookJsonParseException(String.format("Json String could not be converted to JSONObject: %s", jsonString));
        }
        Object playerUUIDObject = jsonObject.getOrDefault(JSON_PLAYER_UUID, null);
        if (playerUUIDObject == null) {
            throw new PlantHookJsonParseException(String.format("%s String not found in JSONObject from String: %s", JSON_PLAYER_UUID, jsonString));
        }
        String playerUUIDString = (String) playerUUIDObject;
        UUID playerUUID;
        try {
            playerUUID = UUID.fromString(playerUUIDString);
        } catch (IllegalArgumentException e) {
            throw new PlantHookJsonParseException(String.format("PlayerUUID was not a valid UUID: %s", playerUUIDString));
        }
        offlinePlayer = PerformantPlants.getInstance().getServer().getOfflinePlayer(playerUUID);
    }

    @Override
    public boolean performScriptBlock(PlantTask task) {
        if (!hookConfigId.isEmpty()) {
            ScriptTask scriptTask = task.getScriptTask();
            if (scriptTask != null) {
                ScriptBlock scriptBlock = scriptTask.getHookScriptBlock(hookConfigId);
                if (scriptBlock != null) {
                    // perform script block
                    scriptBlock.loadValue(task.getPlantBlock(), PlayerHelper.getFreshPlayer(getOfflinePlayer()));
                }
            }
        }
        return true;
    }
}
