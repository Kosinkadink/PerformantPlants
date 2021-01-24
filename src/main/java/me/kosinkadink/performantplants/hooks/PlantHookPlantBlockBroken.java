package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.locations.BlockLocation;

import java.util.UUID;

public class PlantHookPlantBlockBroken extends PlantHookPlantBlock {

    public PlantHookPlantBlockBroken(UUID taskId, HookAction action, String hookConfigId, BlockLocation blockLocation) {
        super(taskId, action, hookConfigId, blockLocation);
    }

    public PlantHookPlantBlockBroken(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId, jsonString);
    }

    @Override
    protected boolean meetsConditions() {
        if (blockLocation != null) {
            return PerformantPlants.getInstance().getPlantManager().getPlantBlock(blockLocation) == null;
        }
        return false;
    }

}
