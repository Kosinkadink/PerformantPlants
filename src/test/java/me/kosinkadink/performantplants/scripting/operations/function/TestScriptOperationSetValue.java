package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestScriptOperationSetValue {

    @Test
    public void testVariableLong() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 0L);
        jsonObject.put("right", 10L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        ScriptResult left = new ScriptResult("left", ScriptType.LONG);
        ScriptResult right = new ScriptResult("right", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationSetValue(left, right);
        ScriptResult result = operation.perform(context);
        assertEquals(10L, result.getLongValue().longValue());
        assertEquals(10L, left.loadValue(context).getLongValue().longValue());
        assertEquals(10L, jsonObject.get("left"));
    }

    @Test
    public void testVariableDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 0.0);
        jsonObject.put("right", 12.725);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        ScriptResult left = new ScriptResult("left", ScriptType.DOUBLE);
        ScriptResult right = new ScriptResult("right", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationSetValue(left, right);
        ScriptResult result = operation.perform(context);
        assertEquals(12.725, result.getDoubleValue().doubleValue());
        assertEquals(12.725, left.loadValue(context).getDoubleValue().doubleValue());
        assertEquals(12.725, jsonObject.get("left"));
    }

    @Test
    public void testVariableString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", "hello there");
        jsonObject.put("right", "NOT hello there");
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        ScriptResult left = new ScriptResult("left", ScriptType.STRING);
        ScriptResult right = new ScriptResult("right", ScriptType.STRING);
        ScriptOperation operation = new ScriptOperationSetValue(left, right);
        ScriptResult result = operation.perform(context);
        assertEquals("NOT hello there", result.getStringValue());
        assertEquals("NOT hello there", left.loadValue(context).getStringValue());
        assertEquals("NOT hello there", jsonObject.get("left"));
    }

}
