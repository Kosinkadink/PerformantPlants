package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.hooks.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScopeParameterIdentifier;
import me.kosinkadink.performantplants.scripting.ScopedPlantData;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.scripting.storage.hooks.*;
import me.kosinkadink.performantplants.statistics.StatisticsAmount;
import me.kosinkadink.performantplants.statistics.StatisticsTagItem;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import me.kosinkadink.performantplants.storage.PlantDataStorage;
import me.kosinkadink.performantplants.storage.StatisticsAmountStorage;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import me.kosinkadink.performantplants.tasks.PlantTask;
import me.kosinkadink.performantplants.util.TaskAndHooksHolder;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final PerformantPlants performantPlants;
    private final String storageDir = "storage/";
    private final HashMap<String, File> databaseFiles = new HashMap<>();
    private File statisticsDatabaseFile;
    private File globalDataDatabaseFile;
    private File taskSchedulingDatabaseFile;
    private BukkitTask saveTask;

    public DatabaseManager(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
        loadDatabases();
        // start up async background task to autosave blocks in db without the need to stop server
        startTask();
    }

    //region Load Data

    void loadDatabases() {
        // load per-world plant dbs
        performantPlants.getLogger().info("Loading plant databases...");
        for (World world : Bukkit.getWorlds()) {
            // check if db for world exists
            String worldName = world.getName();
            File file = new File(performantPlants.getDataFolder(),storageDir + worldName);
            if (!file.exists()) {
                // if doesn't exist, make sure directories are created
                file.getParentFile().mkdirs();
            }
            // load db; also create db file if doesn't already exist
            boolean loaded = loadDatabase(file, worldName);
            if (loaded) {
                databaseFiles.put(worldName, file);
            }
        }
        performantPlants.getLogger().info("Loaded plant databases");
        // load statistics db; also create db file if doesn't already exist
        File file = new File(performantPlants.getDataFolder(), storageDir + "statistics");
        boolean loaded = loadStatisticsDatabase(file);
        if (loaded) {
            statisticsDatabaseFile = file;
        }
        // load global plant data db; also create db file if doesn't already exist
        file = new File(performantPlants.getDataFolder(), storageDir + "global_data");
        loaded = loadGlobalDataDatabase(file);
        if (loaded) {
            globalDataDatabaseFile = file;
        }
        // load task scheduling db; also create db file if doesn't already exist
        file = new File(performantPlants.getDataFolder(), storageDir + "task_scheduling");
        loaded = loadTaskSchedulingDatabase(file);
        if (loaded) {
            taskSchedulingDatabaseFile = file;
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
        // Load plant data from data table
        success = addDataFromDatabase(conn, worldName, url);
        if (!success) {
            return false;
        }

        // all done now
        performantPlants.getLogger().info("Successfully loaded plant db at url: " + url);
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
        // Load plantTags statistics from plantTags
        success = addPlantTagsFromDatabase(conn, url);
        if (!success) {
            return false;
        }
        // all done now
        performantPlants.getLogger().info("Successfully loaded statistics db at url: " + url);
        return true;
    }

    boolean loadGlobalDataDatabase(File file) {
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Load global plant data from globalPlantData
        boolean success = addGlobalPlantDataFromDatabase(conn, url);
        if (!success) {
            return false;
        }
        // add done now
        performantPlants.getLogger().info("Successfully loaded global data db at url: " + url);
        return true;
    }

    boolean loadTaskSchedulingDatabase(File file) {
        // Connect to db
        Connection conn = connect(file);
        if (conn == null) {
            // could not connect to db
            return false;
        }
        String url = getUrlFromFile(file);
        // Load tasks from tasks
        boolean success = addTasksAndHooksFromDatabase(conn, url);
        if (!success) {
            return false;
        }
        performantPlants.getLogger().info("Successfully loaded tasks db at url: " + url);
        return true;
    }

    //endregion

    //region Save Data

    public void saveDatabases() {
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Saving plants into databases...");
        for (Map.Entry<String, File> entry : databaseFiles.entrySet()) {
            saveDatabase(entry.getValue(), entry.getKey());
        }
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Saved plants into databases");
        // save statistics db
        if (statisticsDatabaseFile != null) {
            saveStatisticsDatabase(statisticsDatabaseFile);
        }
        // save global data db
        if (globalDataDatabaseFile != null) {
            saveGlobalPlantDataDatabase(globalDataDatabaseFile);
        }
        // save task scheduling db
        if (taskSchedulingDatabaseFile != null) {
            saveTaskSchedulingDatabase(taskSchedulingDatabaseFile);
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
        createTableData(conn);
        // get plantChunkStorage for current world
        PlantChunkStorage plantChunkStorage = performantPlants.getPlantManager().getPlantChunkStorage(worldName);
        // remove any blocks set for removal
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removing blocks from db for world: " + worldName + "...");
        ArrayList<BlockLocation> blocksToRemoveCache = new ArrayList<>();
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (BlockLocation blockLocation : new ArrayList<>(plantChunkStorage.getBlockLocationsToDelete())) {
            boolean success = removeBlockLocationFromTablePlantBlocks(conn, blockLocation);
            removeBlockLocationFromTableParents(conn, blockLocation);
            removeBlockLocationFromTableGuardians(conn, blockLocation);
            removeBlockLocationFromTableData(conn, blockLocation);
            // if deleted from db, remove from plantChunkStorage
            if (success) {
                blocksToRemoveCache.add(blockLocation);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        // remove cached removal blocks from being removed next time
        for (BlockLocation blockLocation : blocksToRemoveCache) {
            plantChunkStorage.removeBlockFromRemoval(blockLocation);
        }
        blocksToRemoveCache.clear();
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done removing blocks from db for world: " + worldName);
        // add/update all blocks for each chunk
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Updating blocks in db for world: " + worldName + "...");
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        int chunksSaved = 0;
        for (PlantChunk plantChunk : plantChunkStorage.getPlantChunks().values()) {
            // if chunk wasn't loaded since last save, don't update it in db
            if (!plantChunk.wasLoadedSinceSave()) {
                continue;
            }
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
                // if block has data, add it to data table
                if (entry.getValue().hasPlantData()) {
                    insertPlantBlockIntoTableData(conn, entry.getValue());
                }
            }
            // check if unloaded now, clear loaded since save
            if (!plantChunk.isLoaded()) {
                plantChunk.clearLoadedSinceSave();
            }
            chunksSaved++;
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info(String.format("Done updating blocks in %d chunks in db for world: %s", chunksSaved, worldName));
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
        createTablePlantTags(conn);

        //////////////////////////////////////////
        // PLANTS SOLD
        // remove any plantsSold set for removal
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removing plantsSold from db for world...");
        ArrayList<StatisticsAmount> plantsSoldToRemoveCache = new ArrayList<>();
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (StatisticsAmount plantsSold : new ArrayList<>(performantPlants.getStatisticsManager().getStatisticsAmountsToDelete())) {
            boolean success = removeStatisticsAmountFromTablePlantsSold(conn, plantsSold);
            // if deleted from db, remove from StatisticsManager
            if (success) {
                plantsSoldToRemoveCache.add(plantsSold);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        // remove cached removal blocks from being removed next time
        for (StatisticsAmount plantsSold : plantsSoldToRemoveCache) {
            performantPlants.getStatisticsManager().removeStatisticsAmountFromRemoval(plantsSold);
        }
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done removing plantsSold from db");
        // add/update all plantsSold entries
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Updating plantsSold in db...");
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (StatisticsAmountStorage storage : performantPlants.getStatisticsManager().getPlantItemsSoldStorageMap().values()) {
            for (StatisticsAmount statisticsAmount : storage.getStatisticsAmountMap().values()) {
                insertStatisticsAmountIntoTablePlantsSold(conn, statisticsAmount);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done updating plantsSold in db");
        //////////////////////////////////////////

        //////////////////////////////////////////
        // PLANT TAGS
        // remove any tag items set for removal
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removing plantTags from db for world...");
        ArrayList<StatisticsTagItem> plantTagsToRemoveCache = new ArrayList<>();
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (StatisticsTagItem tagItem : performantPlants.getStatisticsManager().getStatisticsTagToDeleteItem()) {
            boolean success = removeStatisticsTagItemFromTablePlantsSold(conn, tagItem);
            // if deleted from db, remove from StatisticsManager
            if (success) {
                plantTagsToRemoveCache.add(tagItem);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        // remove cached removal blocks from being removed next time
        for (StatisticsTagItem tagItem : plantTagsToRemoveCache) {
            performantPlants.getStatisticsManager().removeStatisticTagFromRemoval(tagItem);
        }
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done removing plantTags from db");
        // add/update all plantTags entries
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Updating plantTags in db...");
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (StatisticsTagStorage storage : performantPlants.getStatisticsManager().getPlantTagStorageMap().values()) {
            for (StatisticsTagItem statisticsTagItem : storage.getStatisticsTagMap().values()) {
                insertStatisticsTagItemIntoTablePlantTags(conn, statisticsTagItem);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done updating plantTags in db");
        //////////////////////////////////////////
        return true;
    }

    boolean saveGlobalPlantDataDatabase(File file) {
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
        createTableGlobalPlantData(conn);
        ////////////////////////////////////////////////////////
        // remove any ScopeParameterIdentifiers set for removal
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removing scopedPlantData from db...");
        ArrayList<ScopeParameterIdentifier> identifiersToRemoveCache = new ArrayList<>();
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (PlantDataStorage plantDataStorage : performantPlants.getPlantTypeManager().getPlantDataStorageMap().values()) {
            for (ScopeParameterIdentifier identifier : plantDataStorage.getScopesToDelete()) {
                boolean success = removeScopedPlantDataFromTableGlobalPlantData(conn, identifier);
                if (success) {
                    identifiersToRemoveCache.add(identifier);
                }
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        // remove cached removal identifiers from being removed next time
        for (ScopeParameterIdentifier identifier : identifiersToRemoveCache) {
            PlantDataStorage plantDataStorage = performantPlants.getPlantTypeManager().getPlantDataStorage(identifier.getPlantId());
            if (plantDataStorage != null) {
                plantDataStorage.removeScopeFromRemoval(identifier);
            }
        }
        identifiersToRemoveCache.clear();
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done removing scopedPlantData from db");
        /////////////////////////////////////////////////////////////

        /////////////////////////////////////
        // add/update all scopedPlantData entries
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Updating scopedPlantData in db...");
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (PlantDataStorage plantDataStorage : performantPlants.getPlantTypeManager().getPlantDataStorageMap().values()) {
            for (ScopedPlantData scopedPlantData : plantDataStorage.getScopeMap().values()) {
                for (Map.Entry<String, PlantData> entry : scopedPlantData.getPlantDataMap().entrySet()) {
                    String parameter = entry.getKey();
                    PlantData plantData = entry.getValue();
                    insertScopedPlantDataIntoTableGlobalPlantData(
                            conn,
                            plantDataStorage.getPlantId(),
                            scopedPlantData.getScope(),
                            parameter,
                            plantData
                    );
                }
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done updating scopedPlantData in db");
        //////////////////////////////////////////
        return true;
    }

    boolean saveTaskSchedulingDatabase(File file) {
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
        createTableTasks(conn);
        createTableHooks(conn);
        ////////////////////////////////////////////////////////
        // remove any tasks/hooks set for removal
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removing tasks and hooks from db...");
        ArrayList<UUID> tasksToRemoveCache = new ArrayList<>();
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        // remove tasks marked for deletion
        for (UUID taskToDelete : performantPlants.getTaskManager().getTaskIdsToDelete()) {
            boolean success = removePlantTaskFromTableTasks(conn, taskToDelete.toString());
            if (success) {
                tasksToRemoveCache.add(taskToDelete);
                // remove task's hooks
                removePlantHooksFromTableHooks(conn, taskToDelete.toString());
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        // remove cached removal identifiers from being removed next time
        for (UUID taskUUID : tasksToRemoveCache) {
            performantPlants.getTaskManager().removeTaskIdFromRemoval(taskUUID);
        }
        tasksToRemoveCache.clear();
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done removing tasks and hooks from db");
        ////////////////////////////////////////////////////////

        /////////////////////////////////////
        // add/update all task/hook entries
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Updating tasks and hooks in db...");
        // =========== TRANSACTION START
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("BEGIN;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
        }
        for (PlantTask task : performantPlants.getTaskManager().getTaskMap().values()) {
            boolean success = insertPlantTaskIntoTableTasks(conn, task);
            if (success) {
                for (PlantHook hook : task.getHooks()) {
                    insertPlantHookIntoTableHooks(conn, hook);
                }
            }
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
        }
        // =========== TRANSACTION END
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Done updating tasks and hooks in db");
        /////////////////////////////////////
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
                + "    executed_stage BOOLEAN,\n"
                + "    block_yaw FLOAT,\n"
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
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'plantblocks'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTablePlantBlocks(Connection conn, PlantBlock block, PlantChunk chunk) {
        String sql = "REPLACE INTO plantblocks(x, y, z, cx, cz, plant, grows, stage, drop_stage, executed_stage, block_yaw, block_id, duration, playerUUID, plantUUID)\n"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(     1,block.getLocation().getX());
            pstmt.setInt(     2,block.getLocation().getY());
            pstmt.setInt(     3,block.getLocation().getZ());
            pstmt.setInt(     4,chunk.getLocation().getX());
            pstmt.setInt(     5,chunk.getLocation().getZ());
            pstmt.setString(  6,block.getPlant().getId());
            pstmt.setBoolean( 7,block.isGrows());
            pstmt.setInt(     8,block.getStageIndex());
            pstmt.setInt(     9,block.getDropStageIndex());
            pstmt.setBoolean(10,block.isExecutedStage());
            pstmt.setFloat(  11,block.getBlockYaw());
            pstmt.setString( 12,block.getStageBlockId());
            pstmt.setLong(   13,block.getDuration());
            pstmt.setString( 14,block.getPlayerUUID().toString());
            pstmt.setString( 15,block.getPlantUUID().toString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning("Could not insert PlantBlock into plantblocks: " + block.toString() + "; " + e.toString());
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
            performantPlants.getLogger().warning(
                    "Could not remove BlockLocation from plantblocks: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        //main.getLogger().info("Removed BlockLocation from plantblocks: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region Data Table

    boolean createTableData(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS data (\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    json_data TEXT,\n"
                + "    PRIMARY KEY (x,y,z)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'data'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantBlockIntoTableData(Connection conn, PlantBlock block) {
        String sql = "REPLACE INTO data(x, y, z, json_data)\n"
                + "VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setInt(   1,block.getLocation().getX());
            pstmt.setInt(   2,block.getLocation().getY());
            pstmt.setInt(   3,block.getLocation().getZ());
            pstmt.setString(4,block.getPlantData().createJsonString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning("Could not insert PlantBlock into parents: " + block.toString() + "; " + e.toString());
            return false;
        }
        //main.getLogger().info("Stored PlantBlock in data in db: " + block.toString());
        return true;
    }

    boolean removeBlockLocationFromTableData(Connection conn, BlockLocation blockLocation) {
        String sql = "DELETE FROM data\n"
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
            performantPlants.getLogger().warning(
                    "Could not remove BlockLocation from data: " + blockLocation.toString() + "; " + e.toString()
            );
            return false;
        }
        //main.getLogger().info("Removed BlockLocation from data: " + blockLocation.toString());
        return true;
    }

    //endregion

    //region GlobalPlantData Table

    boolean createTableGlobalPlantData(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS globalPlantData (\n"
                + "    plant TEXT NOT NULL,\n"
                + "    scope TEXT NOT NULL,\n"
                + "    parameter TEXT NOT NULL,\n"
                + "    json_data TEXT,\n"
                + "    PRIMARY KEY (plant,scope,parameter)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'globalPlantData'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertScopedPlantDataIntoTableGlobalPlantData(Connection conn, String plantId, String scope,
                                                          String parameter, PlantData plantData) {
        String sql = "REPLACE INTO globalPlantData(plant, scope, parameter, json_data)\n"
                + "VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, plantId);
            pstmt.setString(2, scope);
            pstmt.setString(3, parameter);
            pstmt.setString(4, plantData.createJsonString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not insert ScopedPlantData into " +
                    "globalPlantData: %s,%s,%s; %s", plantId, scope, parameter, e.toString())
            );
            return false;
        }
        return true;
    }

    boolean removeScopedPlantDataFromTableGlobalPlantData(Connection conn, ScopeParameterIdentifier scopeParameterIdentifier) {
        String sql = "DELETE FROM globalPlantData\n"
                + "    WHERE plant = ?"
                + "    AND scope = ?"
                + "    AND parameter = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, scopeParameterIdentifier.getPlantId());
            pstmt.setString(2, scopeParameterIdentifier.getScope());
            pstmt.setString(3, scopeParameterIdentifier.getParameter());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not remove ScopedPlantData from " +
                    "globalPlantData: %s; %s",
                    scopeParameterIdentifier.toString(),
                    e.toString()
                    ));
            return false;
        }
        return true;
    }

    //endregion

    // region Task and Hooks Tables

    boolean createTableTasks(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS tasks (\n"
                + "    taskUUID TEXT NOT NULL,\n"
                + "    plant TEXT NOT NULL,\n"
                + "    taskConfigId TEXT NOT NULL,\n"
                + "    playerUUID text,\n"
                + "    x INTEGER,\n"
                + "    y INTEGER,\n"
                + "    z INTEGER,\n"
                + "    world TEXT,\n"
                + "    delay INTEGER,\n"
                + "    paused BOOLEAN,\n"
                + "    PRIMARY KEY (taskUUID)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'parents'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantTaskIntoTableTasks(Connection conn, PlantTask task) {
        String sql = "REPLACE INTO tasks(taskUUID, plant, taskConfigId, playerUUID, x, y, z, world, delay, paused)\n"
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // get player uuid
            String playerUUID = null;
            if (task.getOfflinePlayer() != null) {
                playerUUID = task.getOfflinePlayer().getUniqueId().toString();
            }
            // get block location
            int x = 0;
            int y = 0;
            int z = 0;
            String world = null;
            if (task.getBlockLocation() != null) {
                x = task.getBlockLocation().getX();
                y = task.getBlockLocation().getY();
                z = task.getBlockLocation().getZ();
                world = task.getBlockLocation().getWorldName();
            }
            // set values; index starts at 1
            pstmt.setString(  1, task.getTaskId().toString());
            pstmt.setString(  2, task.getPlantId());
            pstmt.setString(  3, task.getTaskConfigId());
            pstmt.setString(  4, playerUUID);
            pstmt.setInt(     5, x);
            pstmt.setInt(     6, y);
            pstmt.setInt(     7, z);
            pstmt.setString(  8, world);
            pstmt.setLong(    9, task.getDelay());
            pstmt.setBoolean(10, task.isPaused());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not insert PlantTask into " +
                    "tasks: %s,%s,%s; %s",
                    task.getTaskId(), task.getPlantId(), task.getTaskConfigId(), e.toString())
            );
            return false;
        }
        return true;
    }

    boolean removePlantTaskFromTableTasks(Connection conn, String taskUUID) {
        String sql = "DELETE FROM tasks\n"
                + "    WHERE taskUUID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, taskUUID);
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not remove PlantTask from " +
                            "tasks: %s; %s",
                    taskUUID,
                    e.toString()
            ));
            return false;
        }
        return true;
    }

    boolean createTableHooks(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS hooks (\n"
                + "    taskUUID TEXT NOT NULL,\n"
                + "    hookConfigId TEXT NOT NULL,\n"
                + "    action TEXT NOT NULL,\n"
                + "    json_data TEXT,\n"
                + "    PRIMARY KEY (taskUUID, hookConfigId)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'parents'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertPlantHookIntoTableHooks(Connection conn, PlantHook hook) {
        String sql = "REPLACE INTO hooks(taskUUID, hookConfigId, action, json_data)\n"
                + "VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, hook.getTaskId().toString());
            pstmt.setString(2, hook.getHookConfigId());
            pstmt.setString(3, hook.getAction().toString());
            pstmt.setString(4, hook.createJsonString());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not insert PlantHook into " +
                    "hooks: %s,%s,%s; %s",
                    hook.getHookConfigId(), hook.getTaskId(), hook.getAction().toString(), e.toString())
            );
            return false;
        }
        return true;
    }

    boolean removePlantHookFromTableHooks(Connection conn, HookIdentifier hookIdentifier) {
        String sql = "DELETE FROM hooks\n"
                + "    WHERE taskUUID = ?"
                + "    AND hookConfigId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, hookIdentifier.getTaskUUID());
            pstmt.setString(2, hookIdentifier.getHookConfigId());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not remove PlantHook from " +
                            "hooks: %s,%s; %s",
                    hookIdentifier.getTaskUUID(),
                    hookIdentifier.getHookConfigId(),
                    e.toString()
            ));
            return false;
        }
        return true;
    }

    boolean removePlantHooksFromTableHooks(Connection conn, String taskUUID) {
        String sql = "DELETE FROM hooks\n"
                + "    WHERE taskUUID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, taskUUID);
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(String.format("Could not remove PlantHook from " +
                            "hooks: %s; %s",
                    taskUUID,
                    e.toString()
            ));
            return false;
        }
        return true;
    }

    // endregion

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
            performantPlants.getLogger().severe(
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
            performantPlants.getLogger().warning("Could not insert PlantBlock into parents: " + block.toString() + "; " + e.toString());
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
            performantPlants.getLogger().warning(
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
            performantPlants.getLogger().severe(
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
            performantPlants.getLogger().warning("Could not insert PlantBlock into guardians: " + block.toString() + "; " + e.toString());
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
            performantPlants.getLogger().warning(
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
            performantPlants.getLogger().severe(
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
            performantPlants.getLogger().warning("Could not insert StatisticsAmount into plantsSold: " + sold.toString() + "; " + e.toString());
            return false;
        }
        return true;
    }

    boolean removeStatisticsAmountFromTablePlantsSold(Connection conn, StatisticsAmount sold) {
        String sql = "DELETE FROM plantsSold\n"
                + "    WHERE playerUUID = ?"
                + "    AND plantItemId = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1,sold.getPlayerUUID().toString());
            pstmt.setString(2,sold.getId());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(
                    "Could not remove StatisticsAmount from plantsSold: " + sold.getPlayerUUID().toString() + ","
                            + sold.getId() + "; " + e.toString()
            );
            return false;
        }
        return true;
    }

    //endregion

    //region PlantTags Table

    boolean createTablePlantTags(Connection conn) {
        // Create table if doesn't already exist
        String sql = "CREATE TABLE IF NOT EXISTS plantTags (\n"
                + "    tagId TEXT,\n"
                + "    plantItemId TEXT,\n"
                + "    PRIMARY KEY (tagId, plantItemId)"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            performantPlants.getLogger().severe(
                    "Exception occurred creating table 'plantTags'; " + e.toString()
            );
            return false;
        }
        return true;
    }

    boolean insertStatisticsTagItemIntoTablePlantTags(Connection conn, StatisticsTagItem item) {
        String sql = "REPLACE INTO plantTags(tagId, plantItemId)\n"
                + "VALUES(?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(   1,item.getId());
            pstmt.setString(   2,item.getPlantId());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning("Could not insert StatisticsAmount into plantsSold: " + item.toString() + "; " + e.toString());
            return false;
        }
        return true;
    }

    boolean removeStatisticsTagItemFromTablePlantsSold(Connection conn, StatisticsTagItem item) {
        String sql = "DELETE FROM plantTags\n"
                + "    WHERE tagId = ?"
                + "    AND plantItemId = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1,item.getId());
            pstmt.setString(2,item.getPlantId());
            // execute
            pstmt.executeUpdate();
        } catch (SQLException e) {
            performantPlants.getLogger().warning(
                    "Could not remove StatisticsAmount from plantsSold: " + item.getId() + ","
                            + item.getPlantId() + "; " + e.toString()
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
        String sql = "SELECT x, y, z, cx, cz, plant, grows, stage, drop_stage, executed_stage, block_yaw, block_id, duration, playerUUID, plantUUID FROM plantblocks;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create PlantBlock from database row
                addPlantBlockFromResultSet(rs, worldName);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred loading plants from url: " + url + "; " + e.toString());
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
            Plant plant = performantPlants.getPlantTypeManager().getPlantById(rs.getString("plant"));
            // if plant does not exist, do nothing
            if (plant == null) {
                return;
            }
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
            // set executedStage
            plantBlock.setExecutedStage(rs.getBoolean("executed_stage"));
            // set blockYaw
            plantBlock.setBlockYaw(rs.getFloat("block_yaw"));
            // set duration
            plantBlock.setDuration(rs.getLong("duration"));
            // add plantBlock to plantManager
            performantPlants.getPlantManager().addPlantBlock(plantBlock);
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load plant; " + e.toString());
        }
    }

    //endregion

    //region Add Data

    boolean addDataFromDatabase(Connection conn, String worldName, String url) {
        // Create table if doesn't already exsit
        createTableData(conn);
        // create list of blocks to remove, if data is no longer necessary to be kept in db
        ArrayList<BlockLocation> blockLocationsToRemove = new ArrayList<>();
        // Get and load all plant data stored in db
        String sql = "SELECT x, y, z, json_data FROM data;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create parent from database row
                BlockLocation toRemove = addDataFromResultSet(rs, worldName);
                if (toRemove != null) {
                    blockLocationsToRemove.add(toRemove);
                }
            }
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred loading parents from url: " + url + "; " + e.toString());
            return false;
        }
        // remove locations to be removed
        if (!blockLocationsToRemove.isEmpty()) {
            // =========== TRANSACTION START
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("BEGIN;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
            }
            for (BlockLocation blockLocation : blockLocationsToRemove) {
                removeBlockLocationFromTableData(conn, blockLocation);
            }
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("COMMIT;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
            }
            // =========== TRANSACTION END
        }
        return true;
    }

    BlockLocation addDataFromResultSet(ResultSet rs, String worldName) {
        try {
            // create blockLocation from db entry
            BlockLocation blockLocation = new BlockLocation(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    worldName);
            // get plant data
            PlantData plantData = new PlantData(rs.getString("json_data"));
            // get plant block at location
            PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(blockLocation);
            // forcefully initialize plant data if block has no parent (and therefore is a parent)
            if (!plantBlock.hasParent()) {
                plantBlock.forcefullyInitializePlantData();
            }
            // update data, if plantBlock has initial data
            if (plantBlock.hasPlantData()) {
                plantBlock.getPlantData().updateData(plantData);
                return null;
            }
            // otherwise, plant no longer uses data and it should be deleted from db
            return blockLocation;
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load data; " + e.toString());
        }
        return null;
    }

    //end region

    //region Add Global Plant Data

    boolean addGlobalPlantDataFromDatabase(Connection conn, String url) {
        // create table if doesn't already exist
        createTableGlobalPlantData(conn);
        // create list of identifiers to remove, if data is no longer necessary to be kept in db
        ArrayList<ScopeParameterIdentifier> identifiersToRemove = new ArrayList<>();
        // Get and load all plant data stored in db
        String sql = "SELECT plant, scope, parameter, json_data FROM globalPlantData;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                ScopeParameterIdentifier toRemove = addGlobalPlantDataFromResultSet(rs);
                if (toRemove != null) {
                    identifiersToRemove.add(toRemove);
                }
            }
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred loading globalPlantData from url: " + url + "; " + e.toString());
            return false;
        }
        // remove identifiers to be removed
        if (!identifiersToRemove.isEmpty()) {
            // =========== TRANSACTION START
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("BEGIN;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
            }
            for (ScopeParameterIdentifier identifier : identifiersToRemove) {
                removeScopedPlantDataFromTableGlobalPlantData(conn, identifier);
            }
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("COMMIT;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
            }
            // =========== TRANSACTION END
        }
        return true;
    }

    ScopeParameterIdentifier addGlobalPlantDataFromResultSet(ResultSet rs) {
        try {
            // create identifier from db entry
            ScopeParameterIdentifier identifier = new ScopeParameterIdentifier(
                    rs.getString("plant"),
                    rs.getString("scope"),
                    rs.getString("parameter")
            );
            // get plant data
            PlantData plantData = new PlantData(rs.getString("json_data"));
            // get plant data storage
            PlantDataStorage plantDataStorage = performantPlants.getPlantTypeManager().getPlantDataStorage(identifier.getPlantId());
            // if plant id shouldn't have any global data, then return identifier for removal
            if (plantDataStorage == null) {
                return identifier;
            }
            // attempt to update global scoped data
            boolean updated = plantDataStorage.updateData(identifier.getScope(), identifier.getParameter(), plantData);
            // if didn't update anything, was using default values and doesn't need to be included in the db;
            // mark for removal
            if (!updated) {
                return identifier;
            }
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load globalPlantData; " + e.toString());
        }
        return null;
    }

    //end region

    //region Add Tasks

    boolean addTasksAndHooksFromDatabase(Connection conn, String url) {
        // create tasks table if doesn't already exist
        createTableTasks(conn);
        // create hooks table if doesn't already exist
        createTableHooks(conn);
        // create list of tasks to remove, if tasks are no longer needed/valid
        ArrayList<String> tasksToRemove = new ArrayList<>();
        // create list of hooks to remove, if they are no longer needed for a task
        ArrayList<HookIdentifier> hooksToRemove = new ArrayList<>();
        // get and load all tasks stored in db
        String sql = "SELECT taskUUID, plant, taskConfigId, playerUUID, x, y, z, world, delay, paused FROM tasks;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                TaskAndHooksHolder toRemove = addTaskAndHooksFromResultSet(conn, rs);
                if (toRemove != null) {
                    if (toRemove.getTaskUUID() != null) {
                        tasksToRemove.add(toRemove.getTaskUUID());
                    }
                    if (toRemove.getHookIdentifiers() != null) {
                        hooksToRemove.addAll(toRemove.getHookIdentifiers());
                    }
                }
            }
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred loading tasks from url: " + url + "; " + e.toString());
            return false;
        }
        // remove tasks and hooks to be removed
        if (!tasksToRemove.isEmpty() || !hooksToRemove.isEmpty()) {
            // =========== TRANSACTION START
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("BEGIN;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred starting transaction; " + e.toString());
            }
            for (String taskUUID : tasksToRemove) {
                removePlantTaskFromTableTasks(conn, taskUUID);
            }
            for (HookIdentifier hookIdentifier : hooksToRemove) {
                removePlantHookFromTableHooks(conn, hookIdentifier);
            }
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("COMMIT;");
            } catch (SQLException e) {
                performantPlants.getLogger().severe("Exception occurred committing transaction; " + e.toString());
            }
            // =========== TRANSACTION END
        }
        return true;
    }

    TaskAndHooksHolder addTaskAndHooksFromResultSet(Connection conn, ResultSet rs) {
        try {
            // create task from result set
            PlantTask task = new PlantTask(
                    UUID.fromString(rs.getString("taskUUID")),
                    rs.getString("plant"),
                    rs.getString("taskConfigId")
            );
            // add OfflinePlayer, if playerUUID present
            String playerUUIDString = rs.getString("playerUUID");
            if (playerUUIDString != null && !playerUUIDString.isEmpty()) {
                UUID playerUUID = UUID.fromString(playerUUIDString);
                task.setOfflinePlayer(performantPlants.getServer().getOfflinePlayer(playerUUID));
            }
            // add BlockLocation, if world present
            String world = rs.getString("world");
            if (world != null && !world.isEmpty()) {
                task.setBlockLocation(new BlockLocation(
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        world
                ));
            }
            // add delay and initial paused value
            task.setDelay(rs.getLong("delay"));
            task.setInitialPausedValue(rs.getBoolean("paused"));
            // now that task is loaded, load/create corresponding hooks
            // first, get plant
            Plant plant = performantPlants.getPlantTypeManager().getPlantById(task.getPlantId());
            if (plant == null) {
                return new TaskAndHooksHolder(task.getTaskId().toString());
            }
            // get script corresponding script task
            ScriptTask scriptTask = plant.getScriptTask(task.getTaskConfigId());
            if (scriptTask == null) {
                return new TaskAndHooksHolder(task.getTaskId().toString());
            }
            // if there are no ScriptHooks defined for task, mark all saved hooks for deletion
            if (scriptTask.getHooks().isEmpty()) {
                return new TaskAndHooksHolder(
                        getHookIdentifiersForTaskFromTableHooks(conn, task.getTaskId().toString())
                );
            }
            // try to generate list of hooks for task
            ArrayList<HookIdentifier> hooksToRemove = new ArrayList<>();
            ArrayList<PlantHook> plantHooksToAdd = generatePlantHooksForTasksFromTableHooks(conn, task, scriptTask, hooksToRemove);
            // if null, mark task for deletion
            if (plantHooksToAdd == null) {
                return new TaskAndHooksHolder(task.getTaskId().toString());
            }
            // add hooks to task
            for (PlantHook hookToAdd : plantHooksToAdd) {
                task.addHook(hookToAdd);
            }
            // schedule task
            performantPlants.getTaskManager().scheduleFrozenTask(task);
            // return any hooks marked for removal
            return new TaskAndHooksHolder(hooksToRemove);
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load tasks and hooks; " + e.toString());
        }
        return null;
    }

    ArrayList<PlantHook> generatePlantHooksForTasksFromTableHooks(Connection conn, PlantTask task, ScriptTask scriptTask, ArrayList<HookIdentifier> hooksToRemove) {
        ArrayList<PlantHook> plantHooks = new ArrayList<>();
        if (scriptTask.getHooks().isEmpty()) {
            return plantHooks;
        }
        // compile set of hookConfigIds required for task
        HashSet<String> hookConfigIdsRequired = new HashSet<>();
        for (ScriptHook scriptHook : scriptTask.getHooks()) {
            hookConfigIdsRequired.add(scriptHook.getHookConfigId());
        }
        // make sql call to get all existing hook data for task
        String sql = "SELECT hookConfigId, action, json_data FROM hooks"
                + "    WHERE taskUUID = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, task.getTaskId().toString());
            // execute
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String hookConfigId = rs.getString("hookConfigId");
                // if hookConfigId not required, mark for removal and process next retrieved hook
                if (!hookConfigIdsRequired.contains(hookConfigId)) {
                    hooksToRemove.add(new HookIdentifier(task.getTaskId(), hookConfigId));
                    continue;
                }
                // if action not valid, mark for removal and process next retrieved hook
                HookAction action = HookAction.fromString(rs.getString("action"));
                if (action == null) {
                    hooksToRemove.add(new HookIdentifier(task.getTaskId(), hookConfigId));
                    continue;
                }
                ScriptHook scriptHook = scriptTask.getHook(hookConfigId);
                PlantHook plantHook = null;
                String jsonString = rs.getString("json_data");
                // try to load data into plantHook from db data
                try {
                    // create PlantHook of appropriate type
                    if (scriptHook instanceof ScriptHookPlayer) {
                        if (scriptHook instanceof ScriptHookPlayerAlive) {
                            plantHook = new PlantHookPlayerAlive(task.getTaskId(), action, hookConfigId, jsonString);
                        } else if (scriptHook instanceof ScriptHookPlayerDead) {
                            plantHook = new PlantHookPlayerDead(task.getTaskId(), action, hookConfigId, jsonString);
                        } else if (scriptHook instanceof ScriptHookPlayerOnline) {
                            plantHook = new PlantHookPlayerOnline(task.getTaskId(), action, hookConfigId, jsonString);
                        } else if (scriptHook instanceof ScriptHookPlayerOffline) {
                            plantHook = new PlantHookPlayerOffline(task.getTaskId(), action, hookConfigId, jsonString);
                        }
                    }
                    else if (scriptHook instanceof ScriptHookPlantBlock) {
                        if (scriptHook instanceof ScriptHookPlantBlockBroken) {
                            plantHook = new PlantHookPlantBlockBroken(task.getTaskId(), action, hookConfigId, jsonString);
                        }
                    }
                    else if (scriptHook instanceof ScriptHookPlantChunk) {
                        if (scriptHook instanceof ScriptHookPlantChunkLoaded) {
                            plantHook = new PlantHookPlantChunkLoaded(task.getTaskId(), action, hookConfigId, jsonString);
                        } else if (scriptHook instanceof ScriptHookPlantChunkUnloaded) {
                            plantHook = new PlantHookPlantChunkUnloaded(task.getTaskId(), action, hookConfigId, jsonString);
                        }
                    }
                } catch (PlantHookJsonParseException e) {
                    // invalid data, so mark hook for removal and process next hook
                    hooksToRemove.add(new HookIdentifier(task.getTaskId(), hookConfigId));
                    continue;
                }
                // mark hook for removal if hook type not recognized for some reason
                if (plantHook == null) {
                    performantPlants.getLogger().severe(String.format("BAD_CODE: generatePlantHooksForTasksFromTableHooks: hook %s is " +
                            "valid, but ScriptHook instance type has no matching statement; hook will be discarded.", hookConfigId));
                    hooksToRemove.add(new HookIdentifier(task.getTaskId(), hookConfigId));
                    continue;
                }
                // data load worked, so plantHook was properly loaded
                plantHooks.add(plantHook);
                hookConfigIdsRequired.remove(hookConfigId);
            }
            // for all remaining hooks, try to generate hooks from existing data in task
            for (String hookConfigId : hookConfigIdsRequired) {
                ScriptHook scriptHook = scriptTask.getHook(hookConfigId);
                PlantHook plantHook = null;
                // create PlantHook of matching type
                if (scriptHook instanceof ScriptHookPlayer) {
                    OfflinePlayer offlinePlayer = null;
                    ScriptHookPlayer thisScriptHook = (ScriptHookPlayer) scriptHook;
                    // if hook is meant to use specific player's id, try to get its value
                    if (thisScriptHook.getPlayerId() != null) {
                        String playerUUIDString;
                        // attempt to get value
                        if (task.getOfflinePlayer() != null) {
                            playerUUIDString = thisScriptHook.getPlayerIdValue(task.getOfflinePlayer().getPlayer(), task.getPlantBlock());
                        } else {
                            playerUUIDString = thisScriptHook.getPlayerIdValue(null, task.getPlantBlock());
                        }
                        UUID playerUUID;
                        // attempt to convert to UUID
                        try {
                            playerUUID = UUID.fromString(playerUUIDString);
                        } catch (IllegalArgumentException e) {
                            // do not have enough info to create hook; task in invalid
                            return null;
                        }
                        // set offline player
                        offlinePlayer = performantPlants.getServer().getOfflinePlayer(playerUUID);
                    }
                    // if offlinePlayer not generated yet, then use task to do it
                    if (offlinePlayer == null) {
                        // if no player included on task, then hook cannot be generated; task is invalid
                        if (task.getOfflinePlayer() == null) {
                            return null;
                        }
                        // otherwise use task's offline player
                        offlinePlayer = task.getOfflinePlayer();
                    }
                    if (thisScriptHook instanceof ScriptHookPlayerAlive) {
                        plantHook = new PlantHookPlayerAlive(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, offlinePlayer);
                    } else if (thisScriptHook instanceof ScriptHookPlayerDead) {
                        plantHook = new PlantHookPlayerDead(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, offlinePlayer);
                    } else if (thisScriptHook instanceof ScriptHookPlayerOnline) {
                        plantHook = new PlantHookPlayerOnline(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, offlinePlayer);
                    } else if (thisScriptHook instanceof ScriptHookPlayerOffline) {
                        plantHook = new PlantHookPlayerOffline(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, offlinePlayer);
                    }
                }
                else if (scriptHook instanceof ScriptHookPlantBlock) {
                    BlockLocation blockLocation = null;
                    ScriptHookPlantBlock thisScriptHook = (ScriptHookPlantBlock) scriptHook;
                    // use task to get block location; if not there, hook cannot be generated; task is invalid
                    if (task.getBlockLocation() == null) {
                        return null;
                    }
                    blockLocation = task.getBlockLocation();
                    if (thisScriptHook instanceof ScriptHookPlantBlockBroken) {
                        plantHook = new PlantHookPlantBlockBroken(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, blockLocation);
                    }
                }
                else if (scriptHook instanceof ScriptHookPlantChunk) {
                    ChunkLocation chunkLocation = null;
                    ScriptHookPlantChunk thisScriptHook = (ScriptHookPlantChunk) scriptHook;
                    // use task to get block location; if not there, hook cannot be generated; task is invalid
                    if (task.getBlockLocation() == null) {
                        return null;
                    }
                    chunkLocation = new ChunkLocation(task.getBlockLocation());
                    if (thisScriptHook instanceof ScriptHookPlantChunkLoaded) {
                        plantHook = new PlantHookPlantChunkLoaded(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, chunkLocation);
                    } else if (thisScriptHook instanceof ScriptHookPlantChunkUnloaded) {
                        plantHook = new PlantHookPlantChunkUnloaded(task.getTaskId(), thisScriptHook.getAction(), hookConfigId, chunkLocation);
                    }
                }
                else {
                    performantPlants.getLogger().severe(String.format("BAD_CODE: generatePlantHooksForTasksFromTableHooks: " +
                            "manually generating hook %s results in an unrecognized ScriptHook type; hook will not be included.", hookConfigId));
                    continue;
                }
                // if null, then there must be a subtype omitted in the code
                if (plantHook == null) {
                    performantPlants.getLogger().severe(String.format("BAD_CODE: generatePlantHooksForTasksFromTableHooks: " +
                            "ScriptHook superclass for hook %s recognized, but specific subclass is not; hook will not be included.", hookConfigId));
                    continue;
                }
                // add plant hook to list
                plantHooks.add(plantHook);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load PlantHooks for task; " + e.toString());
        }
        return plantHooks;
    }

    ArrayList<HookIdentifier> getHookIdentifiersForTaskFromTableHooks(Connection conn, String taskUUID) {
        ArrayList<HookIdentifier> hookIdentifiers = new ArrayList<>();
        String sql = "SELECT taskUUID, hookConfigId" +
                "    WHERE taskUUID = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set values; index starts at 1
            pstmt.setString(1, taskUUID);
            // execute
            ResultSet rs = pstmt.executeQuery();
            // add all return hook identifiers
            while (rs.next()) {
                HookIdentifier hookIdentifier = new HookIdentifier(
                        rs.getString("taskUUID"),
                        rs.getString("hookConfigId")
                );
                hookIdentifiers.add(hookIdentifier);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load hook identifiers; " + e.toString());
        }
        return hookIdentifiers;
    }

    //end region

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
            performantPlants.getLogger().severe("Exception occurred loading parents from url: " + url + "; " + e.toString());
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
            PlantBlock childPlantBlock = performantPlants.getPlantManager().getPlantBlock(childLocation);
            if (childPlantBlock != null) {
                childPlantBlock.setParentLocation(parentLocation);
            }
            // add child to parent block
            PlantBlock parentPlantBlock = performantPlants.getPlantManager().getPlantBlock(parentLocation);
            if (parentPlantBlock != null) {
                parentPlantBlock.addChildLocation(childLocation);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load parent; " + e.toString());
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
            performantPlants.getLogger().severe("Exception occurred loading guardians from url: " + url + "; " + e.toString());
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
            PlantBlock childPlantBlock = performantPlants.getPlantManager().getPlantBlock(childLocation);
            if (childPlantBlock != null) {
                childPlantBlock.setGuardianLocation(guardianLocation);
            }
            // add child to guardian block
            PlantBlock guardianPlantBlock = performantPlants.getPlantManager().getPlantBlock(guardianLocation);
            if (guardianPlantBlock != null) {
                guardianPlantBlock.addChildLocation(childLocation);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load guardian; " + e.toString());
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
            performantPlants.getLogger().severe("Exception occurred loading plantsSold from url: " + url + "; " + e.toString());
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
            performantPlants.getStatisticsManager().addPlantItemsSold(statisticsAmount);
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load plantsSold; " + e.toString());
        }
    }

    //endregion

    //region Add PlantTags

    boolean addPlantTagsFromDatabase(Connection conn, String url) {
        // Create table if doesn't already exist
        createTablePlantTags(conn);
        // Get and load all StatisticsAmounts stored in db
        String sql = "SELECT tagId, plantItemId FROM plantTags;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            // loop through result set
            while (rs.next()) {
                // create parent from database row
                addPlantTagsFromResultSet(rs);
            }
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Exception occurred loading plantTags from url: " + url + "; " + e.toString());
            return false;
        }
        return true;
    }

    void addPlantTagsFromResultSet(ResultSet rs) {
        try {
            // create StatisticsTagItem from dbEntry
            StatisticsTagItem statisticsTagItem = new StatisticsTagItem(
                    rs.getString("tagId"),
                    rs.getString("plantItemId")
            );
            // add to StatisticsManager
            performantPlants.getStatisticsManager().registerPlantTag(statisticsTagItem);
        } catch (SQLException e) {
            performantPlants.getLogger().warning("SQLException occurred trying to load plantTags; " + e.toString());
        }
    }

    //endregion

    Connection connect(File file) {
        String url = getUrlFromFile(file);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            performantPlants.getLogger().severe("Could not connect to url: " + url + "; " + e.toString());
        }
        return conn;
    }

    String getUrlFromFile(File file) {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    public void startTask() {
        saveTask = performantPlants.getServer().getScheduler().runTaskTimerAsynchronously(performantPlants, this::saveDatabases,
                TimeHelper.minutesToTicks(performantPlants.getConfigManager().getConfigSettings().getSaveDelayMinutes()),
                TimeHelper.minutesToTicks(performantPlants.getConfigManager().getConfigSettings().getSaveDelayMinutes()));
    }

    public void cancelTask() {
        saveTask.cancel();
    }

}
