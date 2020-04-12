package me.kosinkadink.performantplants;

import me.kosinkadink.performantplants.commands.*;
import me.kosinkadink.performantplants.expansions.PerformantPlantExpansion;
import me.kosinkadink.performantplants.listeners.BlockBreakListener;
import me.kosinkadink.performantplants.listeners.ChunkEventListener;
import me.kosinkadink.performantplants.listeners.PlantBlockEventListener;
import me.kosinkadink.performantplants.listeners.PlayerInteractListener;
import me.kosinkadink.performantplants.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Economy economy;

    private PluginManager pluginManager;
    private CommandManager commandManager;
    private PlantManager plantManager;
    private PlantTypeManager plantTypeManager;
    private DatabaseManager databaseManager;
    private ConfigurationManager configManager;
    private StatisticsManager statisticsManager;

    @Override
    public void onEnable() {
        registerManagers();
        registerListeners();
        registerCommands();
        if (setupEconomy()) {
            registerVaultCommands();
        } else {
            getServer().getConsoleSender().sendMessage(String.format("%s[PerformantPlants] Vault not found; buy/sell commands will be disabled",
                    ChatColor.YELLOW));
        }
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new PerformantPlantExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        plantManager.unloadAll(); // unload all plant chunks, pausing any growth tasks
        databaseManager.saveDatabases(); // save all plant blocks
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void registerManagers() {
        pluginManager = getServer().getPluginManager();
        commandManager = new CommandManager(this);
        plantManager = new PlantManager(this);
        plantTypeManager = new PlantTypeManager(this);
        configManager = new ConfigurationManager(this);
        statisticsManager = new StatisticsManager(this);
        databaseManager = new DatabaseManager(this);
    }

    private void registerListeners() {
        pluginManager.registerEvents(new ChunkEventListener(this), this);
        pluginManager.registerEvents(new PlayerInteractListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);
        pluginManager.registerEvents(new PlantBlockEventListener(this), this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new PlantChunksCommand(this));
        commandManager.registerCommand(new PlantGiveCommand(this));
        commandManager.registerCommand(new PlantInfoCommand(this));
    }

    private void registerVaultCommands() {
        commandManager.registerCommand(new PlantBuyCommand(this));
        commandManager.registerCommand(new PlantSellCommand(this));
    }

    public Economy getEconomy() {
        return economy;
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

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

}
