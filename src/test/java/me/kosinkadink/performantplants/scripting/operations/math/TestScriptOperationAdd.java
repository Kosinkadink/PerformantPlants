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

public class TestScriptOperationAdd {

    @Test
    public void testTypeInt() {
        ScriptResult left = new ScriptResult(-10);
        ScriptResult right = new ScriptResult(10);
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeLong() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(10L);
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeDouble() {
        ScriptResult left = new ScriptResult(-10.0);
        ScriptResult right = new ScriptResult(10.0);
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());

    }

    @Test
    public void testTypeBoolean() {
        ScriptResult left = ScriptResult.TRUE;
        ScriptResult right = ScriptResult.TRUE;
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals(2, result.getLongValue().longValue());
        assertEquals(ScriptType.LONG, result.getType());
    }

    @Test
    public void testTypeString() {
        ScriptResult left = new ScriptResult("hello ");
        ScriptResult right = new ScriptResult("there");
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals("hello there", result.getStringValue());
        assertEquals(ScriptType.STRING, result.getType());
    }

    @Test
    public void testTypeLongAndDouble() {
        ScriptResult left = new ScriptResult(-10L);
        ScriptResult right = new ScriptResult(10.5);
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform();
        assertEquals(0.5, result.getDoubleValue().doubleValue());
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
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(150L, result.getLongValue().longValue());
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
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(25.5, result.getDoubleValue().doubleValue());
        assertEquals(ScriptType.DOUBLE, result.getType());
    }

    @Test
    public void testVariableStringAndDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", "Number of apples: ");
        jsonObject.put("otherAmount", 75.5);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("amount", ScriptType.STRING);
        ScriptResult right = new ScriptResult("otherAmount", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationAdd(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals("Number of apples: 75.5", result.getStringValue());
        assertEquals(ScriptType.STRING, result.getType());
    }

    @Test
    public void testVariableDoubleAndString() {
        assertThrows(IllegalArgumentException.class, () -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("amount", 75.5);
            jsonObject.put("amountDescription", " is the number of apples");
            PlantBlock plantBlock = new PlantBlock(null, null, false);
            plantBlock.setPlantData(new PlantData(jsonObject));
            ScriptResult left = new ScriptResult("amount", ScriptType.DOUBLE);
            ScriptResult right = new ScriptResult("otherAmount", ScriptType.STRING);
            ScriptOperation operation = new ScriptOperationAdd(left, right);
        });
    }

}
