package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.settings.ConfigSettings;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationManager {

    private Main main;
    private File configFile;
    private YamlConfiguration config = new YamlConfiguration();
    private ConfigSettings configSettings = new ConfigSettings();

    public ConfigurationManager(Main mainClass) {
        main = mainClass;
        configFile = new File(main.getDataFolder(), "config.yml");
        loadMainConfig();
        loadPlantConfigs();
    }

    void loadMainConfig() {
        // create file if doesn't exist
        if (!configFile.exists()) {
            main.saveResource("config.yml", false);
        }
        try {
            config.load(configFile);
        } catch (Exception e) {
            main.getLogger().severe("Error occurred trying to load configFile");
            e.printStackTrace();
        }
        // get parameters from config.yml
        if (config.isSet("debug")) {
            configSettings.setDebug(config.getBoolean("debug"));
        }
    }

    void loadPlantConfigs() {
        String plantsPath = Paths.get(main.getDataFolder().getPath(), "plants").toString();
        // create plants directory if doesn't exist
//
//        try {
//            Files files = Files.list(Paths.get(plantsPath)).;
//        } catch (IOException e) {
//
//        }
    }

    void loadPlantConfig(YamlConfiguration yaml) {

    }

}
