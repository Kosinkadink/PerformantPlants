package me.kosinkadink.performantplants.scripting;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ScriptPlantData {

    private final PlantData baseData;
    private final HashMap<String, ScriptBlock> scriptBlockMap = new HashMap<>();

    public ScriptPlantData(@Nonnull PlantData data) {
        baseData = data;
    }

    public PlantData getBaseData() {
        return baseData;
    }

    public void addValueScriptBlock(String key, ScriptBlock value) {
        scriptBlockMap.put(key, value);
    }

    public PlantData createPlantData(ExecutionContext context) {
        PlantData currentData = baseData.cloneWithoutParse();
        for (Map.Entry<String, ScriptBlock> entry : scriptBlockMap.entrySet()) {
            baseData.updateVariable(entry.getKey(), entry.getValue());
        }
        return currentData;
    }

}
