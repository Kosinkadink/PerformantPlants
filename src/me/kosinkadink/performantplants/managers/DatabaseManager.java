package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class DatabaseManager {

    private Main main;

    public DatabaseManager(Main mainClass) {
        main = mainClass;
    }

    void createDatabases() {
        for (World world : Bukkit.getWorlds()) {
            // TODO: create database for world
        }
    }
}
