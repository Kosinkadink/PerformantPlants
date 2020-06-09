package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlantCommandEffect extends PlantEffect {

    private ScriptBlock command;
    private ScriptBlock console = ScriptResult.TRUE;

    public PlantCommandEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        ScriptResult commandResult = command.loadValue(plantBlock, player);
        String commandString = commandResult.getStringValue();
        if (!commandResult.isHasPlaceholder()) {
            commandString = PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, player, commandString);
        }
        if (isConsole(player, plantBlock)) {
            try {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, commandString);
            } catch (IllegalArgumentException e) {
                // do nothing, just make sure this doesn't cause bad stuff to happen
            }
        } else {
            player.sendMessage("/" + commandString);
        }
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        ScriptResult commandResult = command.loadValue(plantBlock, null);
        String commandString = commandResult.getStringValue();
        if (!commandResult.isHasPlaceholder()) {
            commandString = PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, null, commandString);
        }
        try {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, commandString);
        } catch (IllegalArgumentException e) {
            // do nothing, just make sure this doesn't cause bad stuff to happen
        }
    }

    public ScriptBlock getCommand() {
        return command;
    }

    public String getCommandValue(Player player, PlantBlock plantBlock) {
        return command.loadValue(plantBlock, player).getStringValue();
    }

    public void setCommand(ScriptBlock command) {
        this.command = command;
    }

    public ScriptBlock getConsole() {
        return console;
    }

    public boolean isConsole(Player player, PlantBlock plantBlock) {
        return console.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setConsole(ScriptBlock console) {
        this.console = console;
    }

}
