package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private Main main;
    private String storageDir = "storage/";
    private HashMap<String, File> databaseFiles = new HashMap<>();
    private BukkitTask saveTask;

    public DatabaseManager(Main mainClass) {
        main = mainClass;
        loadDatabases();
        // start up async background task to autosave blocks in db without the need to stop server
        saveTask = main.getServer().getScheduler().runTaskTimerAsynchronously(main, this::saveDatabases,
                TimeHelper.secondsToTicks(60),
                TimeHelper.secondsToTicks(60));
    }

    //region Load Data

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

    boolean loadDatabase(File file, String worldName) {
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Load plant blocks from PlantBlocks table
        boolean success = addPlantBlocksFromDatabase(conn, worldName, url);
        if (!success) {
            return false;
        }
        // Load children/parents from parent table
        success = addParentsFromDatabase(conn, worldName, url);
        if (!success) {
            return false;
        }
        // Load children/guardians from parent table
        success = addGuardiansFromDatabase(conn, worldName, url);
        if (!success) {
            return false;
        }

        // all done now
        main.getLogger().info("Successfully loaded plant db at url: " + url);
        return true;
    }

    //endregion

    //region Save Data

    public void saveDatabases() {
        for (Map.Entry<String, File> entry : databaseFiles.entrySet()) {
            saveDatabase(entry.getValue(), entry.getKey());
        }
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
        // Create tables if don't already exist
        createTablePlantBlocks(conn);
        createTableParents(conn);
        createTableGuardians(conn);
        // get plantChunkStorage for current world
        PlantChunkStorage plantChunkStorage = main.getPlantManager().getPlantChunkStorage(worldName);
        // remove any blocks set for removal
        ArrayList<BlockLocation> blocksToRemoveCache = new ArrayList<>();
        for (BlockLocation blockLocation : plantChunkStorage.getBlockLocationsToDelete()) {
            boolean success = removeBlockLocationFromTablePlantBlocks(conn, blockLocation);
            removeBlockLocationFromTableParents(conn, blockLocation);
            removeBlockLocationFromTableGuardians(conn, blockLocation);
            // if deleted from db, remove from plantChunkStorage
            if (success) {
                blocksToRemoveCache.add(blockLocation);
            }
        }
        // remove cached removal blocks from being removed next time
        for (BlockLocation blockLocation : blocksToRemoveCache) {
            plantChunkStorage.removeBlockFromRemoval(blockLocation);
        }
        blocksToRemoveCache.clear();
        // add/update all blocks for each chunk
        for (PlantChunk plantChunk : plantChunkStorage.getPlantChunks().values()) {
            // for each block in chunk, insert into db
            for (Map.Entry<BlockLocation, PlantBlock> entry : plantChunk.getPlantBlocks().entrySet()) {
                insertPlantBlockIntoTablePlantBlocks(conn, entry.getValue(), plantChunk);
                // if block has parent, add it to parents table
                if (entry.getValue().hasParent()) {
                    insertPlantBlockIntoTableParents(conn, entry.getValue());
                }
                // if block has guardian, add it to guardians table
                if (entry.getValue().hasGuardian()) {
                    insertPlantBlockIntoTableGuardians(conn, entry.getValue());
                }
            }
        }

        return true;
    }

    //endregion

    //region PlantBlocks Table

    boolean createTablePlantBlocks(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS plantblocks (\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    cx INTEGER,\n"
                + "    cz INTEGER,\n"
                + "    plant TEXT NOT NULL,\n"
                + "    grows BOOLEAN,\n"
                + "    stage INTEGER,\n"
                + "    block_id TEXT,\n"
                + "    duration INTEGER,\n"
                + "    playerUUID TEXT,\n"
                + "    PRIMARY KEY (x,y,z)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            main.getLogger().severe(
                    "Exception occurred creating table 'plantblocks'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTablePlantBlocks(Connection conn, PlantBlock block, PlantChunk chunk) {
        String sql = "REPLACE INTO plantblocks(x, y, z, cx, cz, plant, grows, stage, block_id, duration, playerUUID)\n"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(    1,block.getLocation().getX());
            pstmt.setInt(    2,block.getLocation().getY());
            pstmt.setInt(    3,block.getLocation().getZ());
            pstmt.setInt(    4,chunk.getLocation().getX());
            pstmt.setInt(    5,chunk.getLocation().getZ());
            pstmt.setString( 6,block.getPlant().getId());
            pstmt.setBoolean(7,block.getGrows());
            pstmt.setInt(    8,block.getStage());
            pstmt.setString( 9,block.getStageBlockId());
            pstmt.setLong(  10,block.getDuration());
            pstmt.setString(11,block.getPlayerUUID().toString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert PlantBlock into plantblocks: " + block.toString() + "; " + e.toString());
            return false;
        }
        main.getLogger().info("Stored PlantBlock in plantblocks in db: " + block.toString());
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
                    "Could not remove BlockLocation from plantblocks: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        main.getLogger().info("Removed BlockLocation from plantblocks: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region Parents Table

    boolean createTableParents(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS parents (\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    px INTEGER,\n"
                + "    py INTEGER,\n"
                + "    pz INTEGER,\n"
                + "    PRIMARY KEY (x,y,z)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            main.getLogger().severe(
                    "Exception occurred creating table 'parents'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTableParents(Connection conn, PlantBlock block) {
        String sql = "REPLACE INTO parents(x, y, z, px, py, pz)\n"
                + "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(   1,block.getLocation().getX());
            pstmt.setInt(   2,block.getLocation().getY());
            pstmt.setInt(   3,block.getLocation().getZ());
            pstmt.setInt(   4,block.getParentLocation().getX());
            pstmt.setInt(   5,block.getParentLocation().getY());
            pstmt.setInt(   6,block.getParentLocation().getZ());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert PlantBlock into parents: " + block.toString() + "; " + e.toString());
            return false;
        }
        main.getLogger().info("Stored PlantBlock in parents in db: " + block.toString());
        return true;
    }

    boolean removeBlockLocationFromTableParents(Connection conn, BlockLocation blockLocation) {
        String sql = "DELETE FROM parents\n"
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
                    "Could not remove BlockLocation from parents: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        main.getLogger().info("Removed BlockLocation from parents: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region Guardians Table

    boolean createTableGuardians(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS guardians (\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    gx INTEGER,\n"
                + "    gy INTEGER,\n"
                + "    gz INTEGER,\n"
                + "    PRIMARY KEY (x,y,z)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            main.getLogger().severe(
                    "Exception occurred creating table 'guardians'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTableGuardians(Connection conn, PlantBlock block) {
        String sql = "REPLACE INTO guardians(x, y, z, gx, gy, gz)\n"
                + "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(   1,block.getLocation().getX());
            pstmt.setInt(   2,block.getLocation().getY());
            pstmt.setInt(   3,block.getLocation().getZ());
            pstmt.setInt(   4,block.getGuardianLocation().getX());
            pstmt.setInt(   5,block.getGuardianLocation().getY());
            pstmt.setInt(   6,block.getGuardianLocation().getZ());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert PlantBlock into guardians: " + block.toString() + "; " + e.toString());
            return false;
        }
        main.getLogger().info("Stored PlantBlock in guardians in db: " + block.toString());
        return true;
    }

    boolean removeBlockLocationFromTableGuardians(Connection conn, BlockLocation blockLocation) {
        String sql = "DELETE FROM guardians\n"
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
                    "Could not remove BlockLocation from guardians: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        main.getLogger().info("Removed BlockLocation from guardians: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region Add Plant Blocks

    boolean addPlantBlocksFromDatabase(Connection conn, String worldName, String url) {
        // Create table if doesn't already exist
        createTablePlantBlocks(conn);
        // Get and load all plant blocks stored in db
        String sql = "SELECT x, y, z, cx, cz, plant, grows, stage, block_id, duration, playerUUID FROM plantblocks;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create PlantBlock from database row
                addPlantBlockFromResultSet(rs, worldName);
                main.getLogger().info(String.format("Found plant in db with data: %d,%d,%d,%d,%d,%s,%b,%d,%s,%d,%s",
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("cx"),
                        rs.getInt("cz"),
                        rs.getString("plant"),
                        rs.getBoolean("grows"),
                        rs.getInt("stage"),
                        rs.getString("block_id"),
                        rs.getInt("duration"),
                        rs.getString("playerUUID")));
            }
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred loading plants from url: " + url + "; " + e.toString());
            return false;
        }
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
            PlantBlock plantBlock;
            String uuidString = rs.getString("playerUUID");
            boolean grows = rs.getBoolean("grows");
            if (uuidString != null && uuidString.length() > 0) {
                plantBlock = new PlantBlock(blockLocation, plant, UUID.fromString(uuidString), grows);
            }
            else {
                plantBlock = new PlantBlock(blockLocation, plant, grows);
            }
            // add plantBlock to plantManager
            main.getPlantManager().addPlantBlock(plantBlock);
        } catch (SQLException e) {
            main.getLogger().warning("SQLException occurred trying to load plant; " + e.toString());
        }
    }

    //endregion

    //region Add Parents

    boolean addParentsFromDatabase(Connection conn, String worldName, String url) {
        // Create table if doesn't already exist
        createTableParents(conn);
        // Get and load all parent/child blocks stored in db
        String sql = "SELECT x, y, z, px, py, pz FROM parents;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create parent from database row
                addParentFromResultSet(rs, worldName);
                main.getLogger().info(String.format("Found block's parent in db with data: %d,%d,%d,%d,%d,%d",
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("px"),
                        rs.getInt("py"),
                        rs.getInt("pz")));
            }
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred loading parents from url: " + url + "; " + e.toString());
            return false;
        }
        return true;
    }

    void addParentFromResultSet(ResultSet rs, String worldName) {
        try {
            // create blockLocation from db entry
            BlockLocation childLocation = new BlockLocation(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    worldName);
            // create parentLocation from db entry
            BlockLocation parentLocation = new BlockLocation(
                    rs.getInt("px"),
                    rs.getInt("py"),
                    rs.getInt("pz"),
                    worldName);
            // add parent to child block
            PlantBlock childPlantBlock = main.getPlantManager().getPlantBlock(childLocation);
            if (childPlantBlock != null) {
                childPlantBlock.setParentLocation(parentLocation);
            }
            // add child to parent block
            PlantBlock parentPlantBlock = main.getPlantManager().getPlantBlock(parentLocation);
            if (parentPlantBlock != null) {
                parentPlantBlock.addChildLocation(childLocation);
            }
        } catch (SQLException e) {
            main.getLogger().warning("SQLException occurred trying to load parent; " + e.toString());
        }
    }

    //endregion

    //region Add Guardians

    boolean addGuardiansFromDatabase(Connection conn, String worldName, String url) {
        // Create table if doesn't already exist
        createTableGuardians(conn);
        // Get and load all parent/child blocks stored in db
        String sql = "SELECT x, y, z, gx, gy, gz FROM guardians;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create parent from database row
                addGuardiansFromResultSet(rs, worldName);
                main.getLogger().info(String.format("Found block's guardian in db with data: %d,%d,%d,%d,%d,%d",
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("gx"),
                        rs.getInt("gy"),
                        rs.getInt("gz")));
            }
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred loading guardians from url: " + url + "; " + e.toString());
            return false;
        }
        return true;
    }

    void addGuardiansFromResultSet(ResultSet rs, String worldName) {
        try {
            // create blockLocation from db entry
            BlockLocation childLocation = new BlockLocation(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    worldName);
            // create guardianLocation from db entry
            BlockLocation guardianLocation = new BlockLocation(
                    rs.getInt("gx"),
                    rs.getInt("gy"),
                    rs.getInt("gz"),
                    worldName);
            // add guardian to child block
            PlantBlock childPlantBlock = main.getPlantManager().getPlantBlock(childLocation);
            if (childPlantBlock != null) {
                childPlantBlock.setGuardianLocation(guardianLocation);
            }
            // add child to guardian block
            PlantBlock guardianPlantBlock = main.getPlantManager().getPlantBlock(guardianLocation);
            if (guardianPlantBlock != null) {
                guardianPlantBlock.addChildLocation(childLocation);
            }
        } catch (SQLException e) {
            main.getLogger().warning("SQLException occurred trying to load guardian; " + e.toString());
        }
    }

    //endregion

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

    String getUrlFromFile(File file) {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    public void cancelTask() {
        saveTask.cancel();
    }

}
