package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPlaceholderHelper {

    private final String plantId = "testPlant";

    private ExecutionContext createExecutionContext() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 24);
        jsonObject.put("playerName", "TESTPLAYER");
        BlockLocation blockLocation = new BlockLocation(50,50,50,"Test");
        Plant plant = new Plant(plantId, new PlantItem(new ItemStack(Material.AIR)));
        PlantBlock plantBlock = new PlantBlock(blockLocation, plant, false);
        plantBlock.setPlantData(new PlantData(jsonObject));
        ExecutionContext context = new ExecutionContext().set(plantBlock);
        return context;
    }

    @Test
    public void testVariableSingle() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[amount]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("24", replaced);
    }

    @Test
    public void testVariableSingleExtended() {
        ExecutionContext context = createExecutionContext();

        String inputString = "abc [amount] def";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("abc 24 def", replaced);
    }

    @Test
    public void testVariableMultiple() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[amount],[playerName]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("24,TESTPLAYER", replaced);
    }

    @Test
    public void testVariableMultipleExtended() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[amount],[playerName] def";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("24,TESTPLAYER def", replaced);
    }

    @Test
    public void testVariableSingleOnlyOpening() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[amount";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals(inputString, replaced);
    }

    @Test
    public void testVariableSingleOnlyClosing() {
        ExecutionContext context = createExecutionContext();

        String inputString = "amount]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals(inputString, replaced);
    }

    @Test
    public void testVariableSingleNotFound() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[amt]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals(inputString, replaced);
    }

    @Test
    public void testVariableMultipleUnmatched() {
        ExecutionContext context = createExecutionContext();

        String inputString = "hello[there[general[amount]you're]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("hello[there[general24you're]", replaced);
    }

    @Test
    public void testVariableNestedNotFound() {
        ExecutionContext context = createExecutionContext();

        String inputString = "[player.[amount].[notexist]]";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("[player.24.[notexist]]", replaced);
    }

    @Test
    public void testVariableNestedExtendedNotFound() {
        ExecutionContext context = createExecutionContext();

        String inputString = "abc [player.[amount].[notexist]] def";
        String replaced = PlaceholderHelper.setVariablesAndPlaceholders(context, inputString);
        assertEquals("abc [player.24.[notexist]] def", replaced);
    }


}
