package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.statistics.StatisticsAmount;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import me.kosinkadink.performantplants.storage.StatisticsAmountStorage;
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
    private File statisticsDatabaseFile;
    private BukkitTask saveTask;

    public DatabaseManager(Main mainClass) {
        main = mainClass;
        loadDatabases();
        // start up async background task to autosave blocks in db without the need to stop server
        startTask();
    }

    //region Load Data

    void loadDatabases() {
        main.getLogger().info("Loading plant databases...");
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
        main.getLogger().info("Loaded plant databases");
        // load statistics db; also created db file if doesn't already exist
        File file = new File(main.getDataFolder(), storageDir + "statistics");
        boolean loaded = loadStatisticsDatabase(file);
        if (loaded) {
            statisticsDatabaseFile = file;
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

    boolean loadStatisticsDatabase(File file) {
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Load plantsSold statistics from plantsSold
        boolean success = addPlantsSoldFromDatabase(conn, url);
        if (!success) {
            return false;
        }
        // all done now
        main.getLogger().info("Successfully loaded statistics db at url: " + url);
        return true;
    }

    //endregion

    //region Save Data

    public void saveDatabases() {
        main.getLogger().info("Saving plants into databases...");
        for (Map.Entry<String, File> entry : databaseFiles.entrySet()) {
            saveDatabase(entry.getValue(), entry.getKey());
        }
        main.getLogger().info("Saved plants into databases");
        // save statistics db
        if (statisticsDatabaseFile != null) {
            saveStatisticsDatabase(statisticsDatabaseFile);
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
        // Create tables if don't already exist
        createTablePlantBlocks(conn);
        createTableParents(conn);
        createTableGuardians(conn);
        // get plantChunkStorage for current world
        PlantChunkStorage plantChunkStorage = main.getPlantManager().getPlantChunkStorage(worldName);
        // remove any blocks set for removal
        main.getLogger().info("Removing blocks from db for world: " + worldName + "...");
        ArrayList<BlockLocation> blocksToRemoveCache = new ArrayList<>();
        for (BlockLocation blockLocation : new ArrayList<>(plantChunkStorage.getBlockLocationsToDelete())) {
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
        main.getLogger().info("Done removing blocks from db for world: " + worldName);
        // add/update all blocks for each chunk
        main.getLogger().info("Updating blocks in db for world: " + worldName + "...");
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
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
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        main.getLogger().info("Done updating blocks in db for world: " + worldName);
        return true;
    }

    boolean saveStatisticsDatabase(File file) {
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
        // Create tables if don't already exist
        createTablePlantsSold(conn);
        // remove any plantsSold set for removal
        main.getLogger().info("Removing plantsSold from db for world...");
        ArrayList<StatisticsAmount> plantsSoldToRemoveCache = new ArrayList<>();
        for (StatisticsAmount plantsSold : new ArrayList<>(main.getStatisticsManager().getStatisticsAmountsToDelete())) {
            boolean success = removeStatisticsAmountFromTablePlantsSold(conn, plantsSold);
            // if deleted from db, remove from StatisticsManager
            if (success) {
                plantsSoldToRemoveCache.add(plantsSold);
            }
        }
        // remove cached removal blocks from being removed next time
        for (StatisticsAmount plantsSold : plantsSoldToRemoveCache) {
            main.getStatisticsManager().removeStatisticsAmountFromRemoval(plantsSold);
        }
        main.getLogger().info("Done removing plantsSold from db");
        // add/update all plantsSold entries
        main.getLogger().info("Updating plantsSold in db...");
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (StatisticsAmountStorage storage : main.getStatisticsManager().getPlantItemsSoldStorageMap().values()) {
            for (StatisticsAmount statisticsAmount : storage.getStatisticsAmountMap().values()) {
                insertStatisticsAmountIntoTablePlantsSold(conn, statisticsAmount);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        main.getLogger().info("Done updating plantsSold in db");
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
                + "    drop_stage INTEGER,\n"
                + "    block_id TEXT,\n"
                + "    duration INTEGER,\n"
                + "    playerUUID TEXT,\n"
                + "    plantUUID TEXT NOT NULL,\n"
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
        String sql = "REPLACE INTO plantblocks(x, y, z, cx, cz, plant, grows, stage, drop_stage, block_id, duration, playerUUID, plantUUID)\n"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(    1,block.getLocation().getX());
            pstmt.setInt(    2,block.getLocation().getY());
            pstmt.setInt(    3,block.getLocation().getZ());
            pstmt.setInt(    4,chunk.getLocation().getX());
            pstmt.setInt(    5,chunk.getLocation().getZ());
            pstmt.setString( 6,block.getPlant().getId());
            pstmt.setBoolean(7,block.getGrows());
            pstmt.setInt(    8,block.getStageIndex());
            pstmt.setInt(    9,block.getDropStageIndex());
            pstmt.setString(10,block.getStageBlockId());
            pstmt.setLong(  11,block.getDuration());
            pstmt.setString(12,block.getPlayerUUID().toString());
            pstmt.setString(13,block.getPlantUUID().toString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert PlantBlock into plantblocks: " + block.toString() + "; " + e.toString());
            return false;
        }
        //main.getLogger().info("Stored PlantBlock in plantblocks in db: " + block.toString());
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
        //main.getLogger().info("Removed BlockLocation from plantblocks: " + blockLocation.toString());
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
        //main.getLogger().info("Stored PlantBlock in parents in db: " + block.toString());
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
        //main.getLogger().info("Removed BlockLocation from parents: " + blockLocation.toString());
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
        //main.getLogger().info("Stored PlantBlock in guardians in db: " + block.toString());
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
        //main.getLogger().info("Removed BlockLocation from guardians: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region PlantsSold Table

    boolean createTablePlantsSold(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS plantsSold (\n"
                + "    playerUUID TEXT,\n"
                + "    plantItemId TEXT,\n"
                + "    amount INTEGER,\n"
                + "    PRIMARY KEY (playerUUID, plantItemId)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            main.getLogger().severe(
                    "Exception occurred creating table 'plantsSold'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertStatisticsAmountIntoTablePlantsSold(Connection conn, StatisticsAmount sold) {
        String sql = "REPLACE INTO plantsSold(playerUUID, plantItemId, amount)\n"
                + "VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(   1,sold.getPlayerUUID().toString());
            pstmt.setString(   2,sold.getId());
            pstmt.setInt(   3,sold.getAmount());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning("Could not insert StatisticsAmount into plantsSold: " + sold.toString() + "; " + e.toString());
            return false;
        }
        return true;
    }

    boolean removeStatisticsAmountFromTablePlantsSold(Connection conn, StatisticsAmount sold) {
        String sql = "DELETE FROM plantsSold\n"
                + "    WHERE playerUUID = ?"
                + "    AND plantItemId = ?;";
        main.getLogger().info(String.format("Removing from plantsSold where playerUUID = %s and plantItemId = %s", sold.getPlayerUUID().toString(), sold.getId()));
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1,sold.getPlayerUUID().toString());
            pstmt.setString(2,sold.getId());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            main.getLogger().warning(
                    "Could not remove StatisticsAmount from plantsSold: " + sold.getPlayerUUID().toString() + ","
                            + sold.getId() + "; " + e.toString()
            );
            return false;
        }
        return true;
    }

    //endregion

    //region Add Plant Blocks

    boolean addPlantBlocksFromDatabase(Connection conn, String worldName, String url) {
        // Create table if doesn't already exist
        createTablePlantBlocks(conn);
        // Get and load all plant blocks stored in db
        String sql = "SELECT x, y, z, cx, cz, plant, grows, stage, drop_stage, block_id, duration, playerUUID, plantUUID FROM plantblocks;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create PlantBlock from database row
                addPlantBlockFromResultSet(rs, worldName);
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
            // TODO: check if plant type exists (returned not null)
            Plant plant = main.getPlantTypeManager().getPlantById(rs.getString("plant"));
            PlantBlock plantBlock;
            String uuidString = rs.getString("playerUUID");
            String plantUuidString = rs.getString("plantUUID");
            boolean grows = rs.getBoolean("grows");
            if (uuidString != null && uuidString.length() > 0) {
                plantBlock = new PlantBlock(blockLocation, plant, UUID.fromString(uuidString), grows,
                        UUID.fromString(plantUuidString));
            }
            else {
                plantBlock = new PlantBlock(blockLocation, plant, grows, UUID.fromString(plantUuidString));
            }
            // set stageIndex + dropStageIndex + stageBlockId
            plantBlock.setStageIndex(rs.getInt("stage"));
            plantBlock.setDropStageIndex(rs.getInt("drop_stage"));
            plantBlock.setStageBlockId(rs.getString("block_id"));
            // set duration
            plantBlock.setDuration(rs.getLong("duration"));
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

    //region Add PlantsSold

    boolean addPlantsSoldFromDatabase(Connection conn, String url) {
        // Create table if doesn't already exist
        createTablePlantsSold(conn);
        // Get and load all StatisticsAmounts stored in db
        String sql = "SELECT playerUUID, plantItemId, amount FROM plantsSold;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create parent from database row
                addPlantsSoldFromResultSet(rs);
            }
        } catch (SQLException e) {
            main.getLogger().severe("Exception occurred loading plantsSold from url: " + url + "; " + e.toString());
            return false;
        }
        return true;
    }

    void addPlantsSoldFromResultSet(ResultSet rs) {
        try {
            // create StatisticsAmount from dbEntry
            StatisticsAmount statisticsAmount = new StatisticsAmount(
                    UUID.fromString(rs.getString("playerUUID")),
                    rs.getString("plantItemId"),
                    rs.getInt("amount")
            );
            // add to StatisticsManager
            main.getStatisticsManager().addPlantItemsSold(statisticsAmount);
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

    public void startTask() {
        saveTask = main.getServer().getScheduler().runTaskTimerAsynchronously(main, this::saveDatabases,
                TimeHelper.minutesToTicks(main.getConfigManager().getConfigSettings().getSaveDelayMinutes()),
                TimeHelper.minutesToTicks(main.getConfigManager().getConfigSettings().getSaveDelayMinutes()));
    }

    public void cancelTask() {
        saveTask.cancel();
    }

}
