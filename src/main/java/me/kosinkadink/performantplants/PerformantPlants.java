package me.kosinkadink.performantplants;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.kosinkadink.performantplants.adapters.ClientDropPacketAdapter;
import me.kosinkadink.performantplants.commands.*;
import me.kosinkadink.performantplants.expansions.PerformantPlantExpansion;
import me.kosinkadink.performantplants.listeners.*;
import me.kosinkadink.performantplants.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PerformantPlants extends JavaPlugin {
    private static PerformantPlants performantPlants;

    private Economy economy;
    private ProtocolManager protocolManager;

    private PluginManager pluginManager;
    private CommandManager commandManager;
    private PlantManager plantManager;
    private PlantTypeManager plantTypeManager;
    private DatabaseManager databaseManager;
    private ConfigurationManager configManager;
    private StatisticsManager statisticsManager;
    private RecipeManager recipeManager;
    private VanillaDropManager vanillaDropManager;
    private TaskManager taskManager;
    private AnchorManager anchorManager;

    private PlayerInteractListener playerInteractListener;

    private boolean enabled;

    @Override
    public void onEnable() {
        performantPlants = this;
        enablePP();
        setupEconomy();
        setupProtocolLib();
        setupMetrics();
        registerManagers();
        registerListeners();
        registerCommands();
        if (!hasEconomy()) {
            getServer().getConsoleSender().sendMessage(
                    String.format("%s[PerformantPlants] Vault not found; buy/sell commands will be disabled",
                    ChatColor.YELLOW));
        }
        if (!hasProtocolLib()) {
            getServer().getConsoleSender().sendMessage(
                    String.format("%s[PerformantPlants] ProtocolLib not found; on-drop and on-drop-all " +
                                    "item interaction will be disabled",
                    ChatColor.YELLOW));
        }
        if (hasPlaceholderAPI()) {
            new PerformantPlantExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        disablePP();
        plantManager.unloadAll(); // unload all plant chunks, pausing any growth tasks
        configManager.getConfigSettings().setDebug(true); // enable debug mode to get extra logging on shutdown
        taskManager.freezeAllTasks(); // freeze tasks so that they can be properly saved in the db
        databaseManager.cancelTask(); // cancel queued up save task, to prevent possible double-run
        databaseManager.saveDatabases(); // save all plant blocks
    }

    public void manualReload() {
        // clear all recipes - will remove any custom recipes from the server to allow replacement
        recipeManager.clearRecipes();
        // act as if disabling plugin
        onDisable();
        // replace registered managers
        registerManagers();
        // register commands
        registerCommands();
        // re-enable status
        enablePP();
    }

    public static PerformantPlants getInstance() {
        return performantPlants;
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    private void setupProtocolLib() {
        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }
    }

    private void setupMetrics() {
        int bStatsId = 10015;
        Metrics metrics = new Metrics(this, bStatsId);
    }

    private void registerManagers() {
        pluginManager = getServer().getPluginManager();
        commandManager = new CommandManager(this);
        plantManager = new PlantManager(this);
        plantTypeManager = new PlantTypeManager(this);
        anchorManager = new AnchorManager(this);
        recipeManager = new RecipeManager(this);
        vanillaDropManager = new VanillaDropManager(this);
        taskManager = new TaskManager(this);
        configManager = new ConfigurationManager(this);
        statisticsManager = new StatisticsManager(this);
        databaseManager = new DatabaseManager(this);
    }

    private void registerListeners() {
        // setup player interact listener
        playerInteractListener = new PlayerInteractListener(this);
        pluginManager.registerEvents(playerInteractListener, this);
        // setup other listeners
        pluginManager.registerEvents(new ChunkEventListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);
        pluginManager.registerEvents(new PlantBlockEventListener(this), this);
        pluginManager.registerEvents(new RecipeEventListener(this), this);
        pluginManager.registerEvents(new VanillaDropListener(this), this);
        pluginManager.registerEvents(new HookListener(this), this);
        pluginManager.registerEvents(new InventoryEventListener(this), this);
        if (hasProtocolLib()) {
            ClientDropPacketAdapter.register(this, protocolManager);
        }
    }

    private void registerCommands() {
        commandManager.registerCommand(new PlantChunksCommand(this));
        commandManager.registerCommand(new PlantReloadCommand(this));
        commandManager.registerCommand(new PlantHelpCommand(this));
        commandManager.registerCommand(new PlantPermsCommand(this));
        commandManager.registerCommand(new PlantGiveCommand(this));
        commandManager.registerCommand(new PlantInfoCommand(this));
        // Plant Stat Commands
        commandManager.registerCommand(new PlantStatsResetCommand(this));
        commandManager.registerCommand(new PlantStatsResetPlayerCommand(this));
        commandManager.registerCommand(new PlantStatsResetAllPlayersCommand(this));
        // Plant Tag Commands
        commandManager.registerCommand(new PlantTagRegisterCommand(this));
        commandManager.registerCommand(new PlantTagAddCommand(this));
        commandManager.registerCommand(new PlantTagUnregisterCommand(this));
        commandManager.registerCommand(new PlantTagRemoveCommand(this));
        commandManager.registerCommand(new PlantTagListCommand(this));
        commandManager.registerCommand(new PlantTagInfoCommand(this));
        // Vault Commands, if economy present
        if (hasEconomy()) {
            registerVaultCommands();
        }
    }

    private void registerVaultCommands() {
        commandManager.registerCommand(new PlantBuyCommand(this));
        commandManager.registerCommand(new PlantSellCommand(this));
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean hasProtocolLib() {
        return protocolManager != null;
    }

    public boolean hasPlaceholderAPI() {
        return getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
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

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public VanillaDropManager getVanillaDropManager() {
        return vanillaDropManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public AnchorManager getAnchorManager() {
        return anchorManager;
    }

    public PlayerInteractListener getPlayerInteractListener() {
        return playerInteractListener;
    }

    public boolean isPPEnabled() {
        return enabled;
    }

    private void disablePP() {
        enabled = false;
    }

    private void enablePP() {
        enabled = true;
    }

}
