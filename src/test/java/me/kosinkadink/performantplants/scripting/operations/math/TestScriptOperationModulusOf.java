package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestScriptOperationModulusOf {

    @Test
    public void testVariableLong() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 45L);
        jsonObject.put("right", 10L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("left", ScriptType.LONG);
        ScriptResult right = new ScriptResult("right", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationModulusOf(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(5L, result.getLongValue().longValue());
        assertEquals(5L, left.loadValue(plantBlock).getLongValue().longValue());
        assertEquals(5L, jsonObject.get("left"));
    }

    @Test
    public void testVariableDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 45.5);
        jsonObject.put("right", 10.0);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("left", ScriptType.DOUBLE);
        ScriptResult right = new ScriptResult("right", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationModulusOf(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(5.5, result.getDoubleValue().doubleValue());
        assertEquals(5.5, left.loadValue(plantBlock).getDoubleValue().doubleValue());
        assertEquals(5.5, jsonObject.get("left"));
    }

    @Test
    public void testVariableDoubleInvalid() {
        // BOOLEAN should not be allowed as left argument
        assertThrows(IllegalArgumentException.class, () -> {
                JSONObject jsonObject = new JSONObject();
            jsonObject.put("left", true);
            jsonObject.put("right", false);
            PlantBlock plantBlock = new PlantBlock(null, null, false);
            plantBlock.setPlantData(new PlantData(jsonObject));
            ScriptResult left = new ScriptResult("left", ScriptType.BOOLEAN);
            ScriptResult right = new ScriptResult("right", ScriptType.BOOLEAN);
            ScriptOperation operation = new ScriptOperationModulusOf(left, right);
        });
    }
}
