package me.kosinkadink.performantplants;

import me.kosinkadink.performantplants.commands.TotalPlantChunksCommand;
import me.kosinkadink.performantplants.managers.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        registerManagers();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerManagers() {
        commandManager = new CommandManager(this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new TotalPlantChunksCommand(this));
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
