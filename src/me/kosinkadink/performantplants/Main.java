package me.kosinkadink.performantplants;

import me.kosinkadink.performantplants.commands.PlantAddCommand;
import me.kosinkadink.performantplants.commands.PlantGiveCommand;
import me.kosinkadink.performantplants.commands.PlantChunksCommand;
import me.kosinkadink.performantplants.listeners.BlockBreakListener;
import me.kosinkadink.performantplants.listeners.ChunkEventListener;
import me.kosinkadink.performantplants.listeners.PlantBlockEventListener;
import me.kosinkadink.performantplants.listeners.PlayerInteractListener;
import me.kosinkadink.performantplants.managers.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PluginManager pluginManager;
    private CommandManager commandManager;
    private PlantManager plantManager;
    private PlantTypeManager plantTypeManager;
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
        plantTypeManager = new PlantTypeManager(this);
        databaseManager = new DatabaseManager(this);
        configManager = new ConfigurationManager(this);
    }

    private void registerListeners() {
        pluginManager.registerEvents(new ChunkEventListener(this), this);
        pluginManager.registerEvents(new PlayerInteractListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);
        pluginManager.registerEvents(new PlantBlockEventListener(this), this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new PlantChunksCommand(this));
        commandManager.registerCommand(new PlantAddCommand(this));
        commandManager.registerCommand(new PlantGiveCommand(this));
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public PlantManager getPlantManager() {
        return plantManager;
    }

    public PlantTypeManager getPlantTypeManager() {
        return plantTypeManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

}
