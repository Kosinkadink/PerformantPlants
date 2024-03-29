package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.PlantData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestScriptHelper {

    @Test
    public void testGetVariableNamePartsAllFour() {
        String variableName = "hello.there.general.kenobi";
        String[] result = ScriptHelper.getVariableNameParts(variableName);
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("hello", result[0]);
        assertEquals("there", result[1]);
        assertEquals("general", result[2]);
        assertEquals("kenobi", result[3]);
    }

    @Test
    public void testGetVariableNamePartsOnlyTwo() {
        String variableName = "hello.kenobi";
        String[] result = ScriptHelper.getVariableNameParts(variableName);
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("hello", result[0]);
        assertEquals("", result[1]);
        assertEquals("", result[2]);
        assertEquals("kenobi", result[3]);
    }

    @Test
    public void testSanitizeVariableNameForLoading() {
        String variableName = "hello.there.[general.haha].kenobi";
        String result = ScriptHelper.sanitizeVariableNameForLoading(variableName);
        assertEquals("hello.there.[general_haha].kenobi", result);
    }

    @Test
    public void testSanitizeVariableNameForLoadingComplex() {
        String variableName = "hello.there.[general.haha.[another.one.the.dust].hehe].kenobi";
        String result = ScriptHelper.sanitizeVariableNameForLoading(variableName);
        assertEquals("hello.there.[general_haha_[another_one_the_dust]_hehe].kenobi", result);
    }

//    @Test
//    public void testVariableSingleReplacement() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("amount", 24);
//        jsonObject.put("playerName", "TESTPLAYER");
//        BlockLocation blockLocation = new BlockLocation(50,50,50,"Test");
//        Plant plant = new Plant("testPlant", new PlantItem(new ItemStack(Material.AIR)));
//        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
//        plantBlock.setPlantData(new PlantData(jsonObject));
//        // create expectations
//        String replaceOne = "There are $amount$ apples here.";
//        String replaceOneExpected = "There are 24 apples here.";
//        assertEquals(replaceOneExpected, ScriptHelper.setVariables(new ExecutionContext().set(plantBlock), replaceOne));
//        }
//
//    @Test
//    public void testVariableMultipleReplacementSame() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("amount", 24);
//        jsonObject.put("playerName", "TESTPLAYER");
//        BlockLocation blockLocation = new BlockLocation(50,50,50,"Test");
//        Plant plant = new Plant("testPlant", new PlantItem(new ItemStack(Material.AIR)));
//        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
//        plantBlock.setPlantData(new PlantData(jsonObject));
//        // create expectations
//        String replaceMultipleSame = "There are $amount$, and I mean $amount$ apples here.";
//        String replaceMultipleSameExpected = "There are 24, and I mean 24 apples here.";
//        assertEquals(replaceMultipleSameExpected, ScriptHelper.setVariables(new ExecutionContext().set(plantBlock), replaceMultipleSame));
//    }
//
//    @Test
//    public void testVariableMultipleReplacementDifferent() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("amount", 24);
//        jsonObject.put("playerName", "TESTPLAYER");
//        BlockLocation blockLocation = new BlockLocation(50,50,50,"Test");
//        Plant plant = new Plant("testPlant", new PlantItem(new ItemStack(Material.AIR)));
//        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
//        plantBlock.setPlantData(new PlantData(jsonObject));
//        // create expectations
//        String replaceMultipleDifferent = "There are $amount$ apples in $playerName$'s inventory.";
//        String replaceMultipleDifferentExpected = "There are 24 apples in TESTPLAYER's inventory.";
//        assertEquals(replaceMultipleDifferentExpected, ScriptHelper.setVariables(new ExecutionContext().set(plantBlock), replaceMultipleDifferent));
//    }
//
//    @Test
//    public void testVariableMultipleReplacementUnrecognized() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("amount", 24);
//        jsonObject.put("playerName", "TESTPLAYER");
//        BlockLocation blockLocation = new BlockLocation(50,50,50,"Test");
//        Plant plant = new Plant("testPlant", new PlantItem(new ItemStack(Material.AIR)));
//        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
//        plantBlock.setPlantData(new PlantData(jsonObject));
//        // create expectations
//        String replaceMultipleDifferent = "There are $amount$ apples in $playerNameBad$'s inventory.";
//        String replaceMultipleDifferentExpected = "There are 24 apples in $playerNameBad$'s inventory.";
//        assertEquals(replaceMultipleDifferentExpected, ScriptHelper.setVariables(new ExecutionContext().set(plantBlock), replaceMultipleDifferent));
//    }
//
//    @Test
//    public void testVariableMultipleReplacementWithGlobals() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("amount", 24);
//        jsonObject.put("playerName", "TESTPLAYER");
//        BlockLocation blockLocation = new BlockLocation(25,50,75,"Test");
//        Plant plant = new Plant("testPlant", new PlantItem(new ItemStack(Material.AIR)));
//        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
//        plantBlock.setPlantData(new PlantData(jsonObject));
//        // create expectations
//        String replaceMultipleDifferent = "There are $amount$ apples in $playerName$'s inventory at coords: ($_x$,$_y$,$_z$) in world: $_world$ with plant id: $_plant_id$.";
//        String replaceMultipleDifferentExpected = "There are 24 apples in TESTPLAYER's inventory at coords: (25,50,75) in world: Test with plant id: testPlant.";
//        assertEquals(replaceMultipleDifferentExpected, ScriptHelper.setVariables(new ExecutionContext().set(plantBlock), replaceMultipleDifferent));
//    }

}
