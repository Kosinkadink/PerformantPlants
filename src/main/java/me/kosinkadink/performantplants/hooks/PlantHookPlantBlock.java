package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.tasks.PlantTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.UUID;

public abstract class PlantHookPlantBlock extends PlantHook {

    protected static final String JSON_X = "x";
    protected static final String JSON_Y = "y";
    protected static final String JSON_Z = "z";
    protected static final String JSON_WORLD = "w";

    protected BlockLocation blockLocation;

    public PlantHookPlantBlock(UUID taskId, HookAction action, String hookConfigId, BlockLocation blockLocation) {
        super(taskId, action, hookConfigId);
        this.blockLocation = blockLocation;
    }

    public PlantHookPlantBlock(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId);
        loadValuesFromJsonString(jsonString);
    }

    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    @Override
    public String createJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_X, blockLocation.getX());
        jsonObject.put(JSON_Y, blockLocation.getY());
        jsonObject.put(JSON_Z, blockLocation.getZ());
        jsonObject.put(JSON_WORLD, blockLocation.getWorldName());
        return jsonObject.toJSONString();
    }

    @Override
    public void loadValuesFromJsonString(String jsonString) throws PlantHookJsonParseException {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
        if (jsonObject == null) {
            PerformantPlants.getInstance().getLogger().warning(String.format("Json String could not be converted to " +
                    "JSONObject for PlantHookPlantBlock: %s,%s with String: %s", taskId, hookConfigId, jsonString));
            throw new PlantHookJsonParseException(String.format("Json String could not be converted to JSONObject: %s", jsonString));
        }
        Object xObject = jsonObject.getOrDefault(JSON_X, null);
        if (xObject == null) {
            throw new PlantHookJsonParseException(String.format("%s int not found in JSONObject from String: %s", JSON_X, jsonString));
        }
        Object yObject = jsonObject.getOrDefault(JSON_Y, null);
        if (yObject == null) {
            throw new PlantHookJsonParseException(String.format("%s int not found in JSONObject from String: %s", JSON_Y, jsonString));
        }
        Object zObject = jsonObject.getOrDefault(JSON_Z, null);
        if (zObject == null) {
            throw new PlantHookJsonParseException(String.format("%s int not found in JSONObject from String: %s", JSON_Z, jsonString));
        }
        Object worldObject = jsonObject.getOrDefault(JSON_WORLD, null);
        if (worldObject == null) {
            throw new PlantHookJsonParseException(String.format("%s String not found in JSONObject from String: %s", JSON_WORLD, jsonString));
        }
        int x = ((Long) xObject).intValue();
        int y = ((Long) yObject).intValue();
        int z = ((Long) zObject).intValue();
        String world = (String) worldObject;
        blockLocation = new BlockLocation(x, y, z, world);
    }

    @Override
    public boolean performScriptBlock(PlantTask task) {
        if (!hookConfigId.isEmpty()) {
            ScriptTask scriptTask = task.getScriptTask();
            if (scriptTask != null) {
                ScriptBlock scriptBlock = scriptTask.getHookScriptBlock(hookConfigId);
                if (scriptBlock != null) {
                    // perform script block
                    scriptBlock.loadValue(
                            new ExecutionContext()
                                    .set(PerformantPlants.getInstance().getPlantManager().getPlantBlock(blockLocation)));
                }
            }
        }
        return true;
    }

}
