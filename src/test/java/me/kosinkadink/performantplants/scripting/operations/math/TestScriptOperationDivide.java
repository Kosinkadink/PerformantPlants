package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScriptOperationDivide {

    @Test
    public void testTypeInt() {
        ScriptResult left = new ScriptResult(10);
        ScriptResult right = new ScriptResult(2);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(5, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLong() {
        ScriptResult left = new ScriptResult(10L);
        ScriptResult right = new ScriptResult(5L);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(2, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLongRemainder() {
        ScriptResult left = new ScriptResult(10L);
        ScriptResult right = new ScriptResult(4L);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(2, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeDouble() {
        ScriptResult left = new ScriptResult(10.0);
        ScriptResult right = new ScriptResult(4.0);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(2.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testTypeBoolean() {
        ScriptResult left = ScriptResult.TRUE;
        ScriptResult right = ScriptResult.TRUE;
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(1, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLongAndDouble() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(4.0);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(-2.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testVariableLong() {
        // create json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 200L);
        jsonObject.put("otherAmount", 50L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform(new ExecutionContext().set(plantBlock));
        assertEquals(4L, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testVariableLongAndDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 10L);
        jsonObject.put("otherAmount", 4.0);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform(new ExecutionContext().set(plantBlock));
        assertEquals(2.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testLongDivideByZero() {
        ScriptResult left = new ScriptResult(10L);
        ScriptResult right = new ScriptResult(0L);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0L, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testDoubleDivideByZero() {
        ScriptResult left = new ScriptResult(10.0);
        ScriptResult right = new ScriptResult(0.0);
        ScriptOperation operation = new ScriptOperationDivide(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

}
