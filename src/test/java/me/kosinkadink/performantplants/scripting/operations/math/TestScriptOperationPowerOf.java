package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestScriptOperationPowerOf {

    @Test
    public void testVariableLong() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 2L);
        jsonObject.put("right", 3L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        ScriptResult left = new ScriptResult("left", ScriptType.LONG);
        ScriptResult right = new ScriptResult("right", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationPowerOf(left, right);
        ScriptResult result = operation.perform(context);
        assertEquals(8L, result.getLongValue().longValue());
        assertEquals(8L, left.loadValue(context).getLongValue().longValue());
        assertEquals(8L, jsonObject.get("left"));
    }

    @Test
    public void testVariableDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 2.0);
        jsonObject.put("right", 3.0);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        ScriptResult left = new ScriptResult("left", ScriptType.DOUBLE);
        ScriptResult right = new ScriptResult("right", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationPowerOf(left, right);
        ScriptResult result = operation.perform(context);
        assertEquals(8.0, result.getDoubleValue().doubleValue());
        assertEquals(8.0, left.loadValue(context).getDoubleValue().doubleValue());
        assertEquals(8.0, jsonObject.get("left"));
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
            ScriptOperation operation = new ScriptOperationPowerOf(left, right);
        });
    }
}
