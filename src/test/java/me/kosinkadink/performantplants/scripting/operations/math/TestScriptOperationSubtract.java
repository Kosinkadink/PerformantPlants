package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScriptOperationSubtract {

    @Test
    public void testTypeInt() {
        ScriptResult left = new ScriptResult(-10);
        ScriptResult right = new ScriptResult(10);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform();
        assertEquals(-20, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLong() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(10L);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform();
        assertEquals(-20, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeDouble() {
        ScriptResult left = new ScriptResult(-10.0);
        ScriptResult right = new ScriptResult(10.0);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform();
        assertEquals(-20.0, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());

    }

    @Test
    public void testTypeBoolean() {
        ScriptResult left = ScriptResult.TRUE;
        ScriptResult right = ScriptResult.TRUE;
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLongAndDouble() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(10.5);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform();
        assertEquals(-20.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testVariableLong() {
        // create json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", -50L);
        jsonObject.put("otherAmount", 200L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(-250L, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testVariableLongAndDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", -50L);
        jsonObject.put("otherAmount", 75.5);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationSubtract(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(-125.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

}
