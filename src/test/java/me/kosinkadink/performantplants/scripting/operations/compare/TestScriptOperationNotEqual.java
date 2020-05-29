package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScriptOperationNotEqual {

    @Test
    public void testTypeIntTrue() {
        ScriptResult left = new ScriptResult(1);
        ScriptResult right = new ScriptResult(1);
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(false, operation.perform().getBooleanValue());
    }

    @Test public void testTypeIntFalse() {
        ScriptResult left = new ScriptResult(1);
        ScriptResult right = new ScriptResult(0);
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(true, operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeBooleanTrue() {
        ScriptResult left = new ScriptResult(true);
        ScriptResult right = new ScriptResult(true);
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(false, operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeBooleanFalse() {
        ScriptResult left = new ScriptResult(true);
        ScriptResult right = new ScriptResult(false);
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(true, operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeStringTrue() {
        ScriptResult left = new ScriptResult("hello there");
        ScriptResult right = new ScriptResult("hello there");
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(false, operation.perform().getBooleanValue());
    }

    @Test
    public void testTypeStringFalse() {
        ScriptResult left = new ScriptResult("hello there");
        ScriptResult right = new ScriptResult("NOT hello there");
        ScriptOperation operation = new ScriptOperationNotEqual(left, right);
        assertEquals(true, operation.perform().getBooleanValue());
    }


}
