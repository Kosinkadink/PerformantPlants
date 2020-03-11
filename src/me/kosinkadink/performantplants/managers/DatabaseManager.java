package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private Main main;
    private String storageDir = "storage/";
    private HashMap<String, File> databaseFiles = new HashMap<>();

    public DatabaseManager(Main mainClass) {
        main = mainClass;
        loadDatabases();
    }

    void loadDatabases() {
        for (World world : Bukkit.getWorlds()) {
            // check if db for world exists
            String worldName = world.getName();
            File file = new File(main.getDataFolder(),storageDir + worldName);
            if (!file.exists()) {
                // if doesn't exist, make sure directories are created
                file.getParentFile().mkdirs();
            }
            // load db; also created db file if doesn't already exist
            boolean loaded = loadDatabase(file, worldName);
            if (loaded) {
                databaseFiles.put(worldName, file);
            }
        }
    }

    public void saveDatabases() {
        for (Map.Entry<String, File> entry : databaseFiles.entrySet()) {
            saveDatabase(entry.getValue(), entry.getKey());
        }
    }

    Connection connect(File file) {
        String url = getUrlFromFile(file);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            main.getLogger().severe("Could not connect to url: " + url + "; " + e.toString());
        }
        return conn;
    }

    boolean loadDatabase(File file, String worldName) {
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Create table if doesn't already exist
        createTablePlantBlocks(conn);
        // Get and load all plant blocks stored in db
        String sql = "SELECT x, y, z, cx, cz, plant, stage, duration FROM plantblocks;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // TODO: create PlantBlock from database row
                addPlantBlockFromResultSet(rs, worldName);
                main.getLogger().info(String.format("Found plant in db with data: %d,%d,%d,%d,%d,%s,%d,%d",
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("cx"),
                        rs.getInt("cz"),
                        rs.getString("plant"),
                        rs.getInt("stage"),
                        rs.getInt("duration")));
            }
        } catch (SQLException e) {
            main.getLogger().severe("Exception occured loading plants from url: " + url + "; " + e.toString());
            return false;
        }
        // add done now
        main.getLogger().info("Successfully loaded plant db at url: " + url);
        return true;
    }

    boolean saveDatabase(File file, String worldName) {
        // Create dirs if file does not exist
        if (!file.exists()) {
            // if doesn't exist, make sure directories are created
            file.getParentFile().mkdirs();
        }
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Create table if doesn't already exist
        createTablePlantBlocks(conn);
        // get plantChunkStorage for current world
        PlantChunkStorage plantChunkStorage = main.getPlantManager().getPlantChunkStorage(worldName);
        // remove any blocks set for removal
        for (BlockLocation blockLocation : plantChunkStorage.getBlockLocationsToDelete()) {
            removeBlockLocationFromTablePlantBlocks(conn, blockLocation);
        }
        // add/update all blocks for each chunk
        for (PlantChunk plantChunk : plantChunkStorage.getPlantChunks().values()) {
            // for each block in chunk, insert into db
            for (Map.Entry<BlockLocation, PlantBlock> entry : plantChunk.getPlantBlocks().entrySet()) {
                insertPlantBlockIntoTablePlantBlocks(conn, entry.getValue(), plantChunk);
            }
        }

        return true;
    }

    boolean createTablePlantBlocks(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS plantblocks (\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    cx INTEGER,\n"
                + "    cz INTEGER,\n"
                + "    plant TEXT NOT NULL,\n"
                + "    stage INTEGER,\n"
                + "    duration INTEGER,\n"
                + "    PRIMARY KEY (x,y,z)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            main.getLogger().severe(
                    "Exception occured creating table 'plantblocks'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTablePlantBlocks(Connection conn, PlantBlock block, PlantChunk chunk) {
        String sql = "REPLACE INTO plantblocks(x, y, z, cx, cz, plant, stage, duration)\n"
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(   1,block.getLocation().getX());
            pstmt.setInt(   2,block.getLocation().getY());
            pstmt.setInt(   3,block.getLocation().getZ());
            pstmt.setInt(   4,chunk.getLocation().getX());
            pstmt.setInt(   5,chunk.getLocation().getZ());
            pstmt.setString(6,block.getPlant().getId());
            pstmt.setInt(   7,block.getStage());
            pstmt.setLong(  8,block.getDuration());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert PlantBlock: " + block.toString() + "; " + e.toString());
            return false;
        }
        main.getLogger().info("Stored PlantBlock in db: " + block.toString());
        return true;
    }

    boolean removeBlockLocationFromTablePlantBlocks(Connection conn, BlockLocation blockLocation) {
        String sql = "DELETE FROM plantblocks\n"
                + "    WHERE x = ?"
                + "    AND y = ?"
                + "    and z = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(1,blockLocation.getX());
            pstmt.setInt(2,blockLocation.getY());
            pstmt.setInt(3,blockLocation.getZ());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning(
                    "Could not remove BlockLocation: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        main.getLogger().info("Removed BlockLocation: " + blockLocation.toString());
        return true;
    }

    void addPlantBlockFromResultSet(ResultSet rs, String worldName) {
        try {
            // create plantBlock from db entry
            BlockLocation blockLocation = new BlockLocation(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    worldName);
            Plant plant = main.getPlantTypeManager().getPlantById(rs.getString("plant"));
            PlantBlock plantBlock = new PlantBlock(blockLocation, plant);
            // add plantBlock to plantManager
            main.getPlantManager().addPlantBlock(plantBlock);
        } catch (SQLException e) {
            main.getLogger().warning("SQLException occured trying to load plant; " + e.toString());
        }
    }

    String getUrlFromFile(File file) {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }
}
