package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScriptOperationPower {

    @Test
    public void testTypeInt() {
        ScriptResult left = new ScriptResult(2);
        ScriptResult right = new ScriptResult(3);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform();
        assertEquals(8, result.getLongValue().longValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testTypeLong() {
        ScriptResult left = new ScriptResult(2L);
        ScriptResult right = new ScriptResult(3L);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform();
        assertEquals(8, result.getLongValue().longValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testTypeDouble() {
        ScriptResult left = new ScriptResult(2);
        ScriptResult right = new ScriptResult(0.5);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform();
        assertEquals(Math.pow(2, 0.5), result.getDoubleValue().doubleValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());

    }

    @Test
    public void testTypeBoolean() {
        ScriptResult left = ScriptResult.TRUE;
        ScriptResult right = ScriptResult.TRUE;
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform();
        assertEquals(1, result.getLongValue().longValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testTypeLongAndDouble() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(2.5);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform();
        assertEquals(Math.pow(-10, 2.5), result.getDoubleValue().doubleValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testVariableLong() {
        // create json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 3);
        jsonObject.put("otherAmount", 3);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(27, result.getLongValue().longValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testVariableLongAndDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 3L);
        jsonObject.put("otherAmount", 2.5);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.LONG);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationPower(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(Math.pow(3, 2.5), result.getDoubleValue().doubleValue());
        // should always be a double returned
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

}
