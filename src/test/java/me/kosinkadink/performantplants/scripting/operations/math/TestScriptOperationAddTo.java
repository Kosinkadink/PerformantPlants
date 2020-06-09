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

public class TestScriptOperationAddTo {

    @Test
    public void testVariableLong() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 0L);
        jsonObject.put("right", 10L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("left", ScriptType.LONG);
        ScriptResult right = new ScriptResult("right", ScriptType.LONG);
        ScriptOperation operation = new ScriptOperationAddTo(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(10L, result.getLongValue().longValue());
        assertEquals(10L, left.loadValue(plantBlock).getLongValue().longValue());
        assertEquals(10L, jsonObject.get("left"));
    }

    @Test
    public void testVariableDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 0.0);
        jsonObject.put("right", 12.725);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("left", ScriptType.DOUBLE);
        ScriptResult right = new ScriptResult("right", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationAddTo(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals(12.725, result.getDoubleValue().doubleValue());
        assertEquals(12.725, left.loadValue(plantBlock).getDoubleValue().doubleValue());
        assertEquals(12.725, jsonObject.get("left"));
    }

    @Test
    public void testVariableStringDouble() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", "Amount: ");
        jsonObject.put("right", 12.725);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("left", ScriptType.STRING);
        ScriptResult right = new ScriptResult("right", ScriptType.DOUBLE);
        ScriptOperation operation = new ScriptOperationAddTo(left, right);
        ScriptResult result = operation.perform(plantBlock);
        assertEquals("Amount: 12.725", result.getStringValue());
        assertEquals("Amount: 12.725", left.loadValue(plantBlock).getStringValue());
        assertEquals("Amount: 12.725", jsonObject.get("left"));
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
            ScriptOperation operation = new ScriptOperationAddTo(left, right);
        });
    }

    @Test
    public void testVariableDoubleStringInvalid() {
        // STRING should not be allowed to be added into non-STRING
        assertThrows(IllegalArgumentException.class, () -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("left", 12.725);
            jsonObject.put("right", "Amount: ");
            PlantBlock plantBlock = new PlantBlock(null, null, false);
            plantBlock.setPlantData(new PlantData(jsonObject));
            ScriptResult left = new ScriptResult("left", ScriptType.DOUBLE);
            ScriptResult right = new ScriptResult("right", ScriptType.STRING);
            ScriptOperation operation = new ScriptOperationAddTo(left, right);
        });
    }
}
