package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlantCommandEffect extends PlantEffect {

    private String command;
    private boolean console = true;

    public PlantCommandEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        String formattedCommand = PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, player, command);
        if (console) {
            try {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, formattedCommand);
            } catch (IllegalArgumentException e) {
                // do nothing, just make sure this doesn't cause bad stuff to happen
            }
        } else {
            player.sendMessage("/" + formattedCommand);
        }
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        try {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console,
                    PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, null, command)
            );
        } catch (IllegalArgumentException e) {
            // do nothing, just make sure this doesn't cause bad stuff to happen
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isConsole() {
        return console;
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

}
