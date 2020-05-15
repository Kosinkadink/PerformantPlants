package me.kosinkadink.performantplants.effects;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlantCommandEffect extends PlantEffect {

    private String command;
    private boolean console = true;

    public PlantCommandEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        String formattedCommand = command;
        // set placeholders, if PlaceholderAPI plugin is present
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            formattedCommand = PlaceholderAPI.setPlaceholders(player, formattedCommand);
        }
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
    void performEffectAction(Block block) {
        try {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, command);
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
