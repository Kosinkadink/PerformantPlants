package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestScriptOperationEqual {

    @Test
    public void testTypeIntTrue() {
        ScriptResult left = new ScriptResult(1);
        ScriptResult right = new ScriptResult(1);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertTrue(operation.perform().getBooleanValue());
    }

    @Test public void testTypeIntFalse() {
        ScriptResult left = new ScriptResult(1);
        ScriptResult right = new ScriptResult(0);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertFalse(operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeBooleanTrue() {
        ScriptResult left = new ScriptResult(true);
        ScriptResult right = new ScriptResult(true);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertTrue(operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeBooleanFalse() {
        ScriptResult left = new ScriptResult(true);
        ScriptResult right = new ScriptResult(false);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertFalse(operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeStringTrue() {
        ScriptResult left = new ScriptResult("hello there");
        ScriptResult right = new ScriptResult("hello there");
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertTrue(operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeStringFalse() {
        ScriptResult left = new ScriptResult("hello there");
        ScriptResult right = new ScriptResult("NOT hello there");
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        assertFalse(operation.perform().getBooleanValue());
    }

    @Test
    public void testVariableStringTrue() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("leftString", "hello there");
        jsonObject.put("rightString", "hello there");
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("leftString", ScriptType.STRING);
        ScriptResult right = new ScriptResult("rightString", ScriptType.STRING);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        ScriptResult result = operation.perform(new ExecutionContext().set(plantBlock));
        assertTrue(result.getBooleanValue());
    }

    @Test
    public void testVariableStringFalse() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("leftString", "hello there");
        jsonObject.put("rightString", "NOT hello there");
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ScriptResult left = new ScriptResult("leftString", ScriptType.STRING);
        ScriptResult right = new ScriptResult("rightString", ScriptType.STRING);
        ScriptOperation operation = new ScriptOperationEqual(left, right);
        ScriptResult result = operation.perform(new ExecutionContext().set(plantBlock));
        assertFalse(result.getBooleanValue());
    }


}
