package me.kosinkadink.performantplants;

import me.kosinkadink.performantplants.commands.TotalPlantChunksCommand;
import me.kosinkadink.performantplants.listeners.ChunkEventListener;
import me.kosinkadink.performantplants.managers.CommandManager;
import me.kosinkadink.performantplants.managers.ConfigurationManager;
import me.kosinkadink.performantplants.managers.DatabaseManager;
import me.kosinkadink.performantplants.managers.PlantManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.Configuration;

public class Main extends JavaPlugin {

    private PluginManager pluginManager;
    private CommandManager commandManager;
    private PlantManager plantManager;
    private DatabaseManager databaseManager;
    private ConfigurationManager configManager;

    @Override
    public void onEnable() {
        registerManagers();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerManagers() {
        pluginManager = getServer().getPluginManager();
        commandManager = new CommandManager(this);
        plantManager = new PlantManager(this);
        databaseManager = new DatabaseManager(this);
        configManager = new ConfigurationManager(this);
    }

    private void registerListeners() {
        pluginManager.registerEvents(new ChunkEventListener(this), this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new TotalPlantChunksCommand(this));
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public PlantManager getPlantManager() {
        return plantManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

}
