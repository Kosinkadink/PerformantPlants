package me.kosinkadink.performantplants.scripting.operations;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.math.ScriptOperationAdd;
import me.kosinkadink.performantplants.scripting.operations.math.ScriptOperationAddTo;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBranchedExecution {

    @Test
    public void testOptimization() {
        // tests if non-variable operations get simplified into ScriptResult
        // create left branch -> 10 + 25 = 35
        ScriptResult l_left = new ScriptResult(10);
        ScriptResult l_right = new ScriptResult(25);
        ScriptOperation operationL = new ScriptOperationAdd(l_left, l_right);
        // create right branch -> -20 + 100 = 80
        ScriptResult r_left = new ScriptResult(-20);
        ScriptResult r_right = new ScriptResult(100);
        ScriptOperation operationR = new ScriptOperationAdd(r_left, r_right);
        // add together -> 35 + 80 = 115
        ScriptOperationAdd mainOperation = new ScriptOperationAdd(operationL, operationR);
        // left operation should be simplified into 35L
        assertTrue(mainOperation.getLeft() instanceof ScriptResult);
        assertEquals(35L, mainOperation.getLeft().loadValue(new ExecutionContext()).getLongValue().longValue());
        // right operation should be simplified into 80L
        assertTrue(mainOperation.getRight() instanceof ScriptResult);
        assertEquals(80L, mainOperation.getRight().loadValue(new ExecutionContext()).getLongValue().longValue());
    }

    @Test
    public void testLong() {
        // create left branch -> 10 + 25 = 35
        ScriptResult l_left = new ScriptResult(10);
        ScriptResult l_right = new ScriptResult(25);
        ScriptOperation operationL = new ScriptOperationAdd(l_left, l_right);
        // create right branch -> -20 + 100 = 80
        ScriptResult r_left = new ScriptResult(-20);
        ScriptResult r_right = new ScriptResult(100);
        ScriptOperation operationR = new ScriptOperationAdd(r_left, r_right);
        // add together -> 35 + 80 = 115
        ScriptOperation mainOperation = new ScriptOperationAdd(operationL, operationR);
        ScriptResult result = mainOperation.perform();
        assertEquals(115L, result.getLongValue().longValue());
    }

    @Test
    public void testVariableLong() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left", 0L);
        PlantBlock plantBlock = new PlantBlock(null, null, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        // create variable
        ScriptResult variable = new ScriptResult("left", ScriptType.LONG);
        // create right branch -> -20 + 100 = 80
        ScriptResult r_left = new ScriptResult(-20);
        ScriptResult r_right = new ScriptResult(100);
        ScriptOperation operationR = new ScriptOperationAdd(r_left, r_right);
        // add together -> var["left"] += 80
        ScriptOperation mainOperation = new ScriptOperationAddTo(variable, operationR);
        ScriptResult result = mainOperation.perform(context);
        // confirm that value of variable has changed to value of right branch
        assertEquals(80L, variable.loadValue(context).getLongValue().longValue());
        assertEquals(80L, result.getLongValue().longValue());
    }

}
