package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.locations.ChunkLocation;

import java.util.UUID;

public class PlantHookPlantChunkUnloaded extends PlantHookPlantChunk {

    public PlantHookPlantChunkUnloaded(UUID taskId, HookAction action, String hookConfigId, ChunkLocation chunkLocation) {
        super(taskId, action, hookConfigId, chunkLocation);
    }

    public PlantHookPlantChunkUnloaded(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId, jsonString);
    }

    @Override
    protected boolean meetsConditions() {
        if (chunkLocation != null) {
            PlantChunk chunk = PerformantPlants.getInstance().getPlantManager().getPlantChunk(chunkLocation);
            return chunk == null || !chunk.isLoaded();
        }
        return false;
    }

}
