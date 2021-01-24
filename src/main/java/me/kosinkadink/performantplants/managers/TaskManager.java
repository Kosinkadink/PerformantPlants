package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.hooks.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.tasks.PlantTask;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {

    private final PerformantPlants performantPlants;

    private final ConcurrentHashMap<String, PlantTask> taskMap = new ConcurrentHashMap<>();
    private final HashSet<UUID> taskIdsToDelete = new HashSet<>();

    // hook mapping
    // player hooks; map PlayerUUID to a set of PlantHooks
    private final ConcurrentHashMap<String, HashSet<PlantHook>> hookPlayerOnlineMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, HashSet<PlantHook>> hookPlayerOfflineMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, HashSet<PlantHook>> hookPlayerAliveMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, HashSet<PlantHook>> hookPlayerDeadMap = new ConcurrentHashMap<>();
    // block hooks; map block location to a set of PlantHooks
    private final ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> hookPlantBrokenMap = new ConcurrentHashMap<>();
    // chunk hooks; map chunk location to a set of PlantHooks
    private final ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> hookPlantChunkLoadedMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> hookPlantChunkUnloadedMap = new ConcurrentHashMap<>();

    public TaskManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public ConcurrentHashMap<String, PlantTask> getTaskMap() {
        return taskMap;
    }

    public boolean scheduleTask(PlantTask task) {
        if (task.isStartable()) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) {
                performantPlants.getLogger().info("Scheduling task with id: " + task.getTaskId());
            }
            addTaskToMap(task);
            // if autostart, then start task
            if (task.isAutostart()) {
                boolean startedTask = task.startTask(performantPlants);
                // if failed to start, cancel task and return false
                if (!startedTask) {
                    cancelTask(task.getTaskId().toString(), null);
                    return false;
                }
            } else {
                // otherwise pause it
                task.pauseTask();
            }
            // check hooks
            for (PlantHook hook : task.getHooks()) {
                // if performStartup results in a cancel action (returned false), return false since it got cancelled
                if (!hook.performStartup()) {
                    return false;
                }
                // register hook
                registerHook(hook);
            }
            return true;
        }
        return false;
    }

    public boolean scheduleFrozenTask(PlantTask task) {
        if (task.isStartable()) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) {
                performantPlants.getLogger().info("Scheduling FROZEN task with id: " + task.getTaskId());
            }
            addTaskToMap(task);
            // if not paused, then start to unfreeze
            if (!task.isPaused()) {
                boolean startedTask = task.startTask(performantPlants);
                // if failed to start, cancel task and return false
                if (!startedTask) {
                    cancelTask(task.getTaskId().toString(), null);
                    return false;
                }
            }
            // check hooks
            for (PlantHook hook : task.getHooks()) {
                // if performStartup results in a cancel action (returned false), return false since it got cancelled
                if (!hook.performStartup()) {
                    return false;
                }
                // register hook
                registerHook(hook);
            }
            return true;
        }
        return false;
    }

    public boolean resumeTask(String taskId, PlantHook hook) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) {
                performantPlants.getLogger().info("Resuming task with id: " + taskId);
            }
            boolean started = plantTask.startTask(performantPlants);
            if (started) {
                // perform hook script block
                performHookScriptBlock(hook, plantTask);
            }
            return started;
        }
        return false;
    }

    public void pauseAllTasks() {
        for (PlantTask plantTask : taskMap.values()) {
            plantTask.pauseTask();
        }
    }

    public boolean pauseTask(String taskId, PlantHook hook) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) {
                performantPlants.getLogger().info("Pausing task with id: " + taskId);
            }
            boolean paused = plantTask.pauseTask();
            if (paused) {
                // perform hook script block
                performHookScriptBlock(hook, plantTask);
            }
            return true;
        }
        return false;
    }

    public boolean cancelTask(String taskId, PlantHook hook) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) {
                performantPlants.getLogger().info("Cancelling task with id: " + taskId);
            }
            boolean cancelled = plantTask.cancelTask();
            if (cancelled) {
                // perform hook script block
                performHookScriptBlock(hook, plantTask);
            }
            // remove task
            removeTaskId(taskId);
            return true;
        }
        return false;
    }

    public void freezeAllTasks() {
        for (PlantTask plantTask : taskMap.values()) {
            plantTask.freezeTask();
        }
    }

    public boolean freezeTask(String taskId) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            plantTask.freezeTask();
            return true;
        }
        return false;
    }

    public boolean unfreezeTask(String taskId) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            plantTask.unfreezeTask(performantPlants);
            return true;
        }
        return false;
    }

    public void removeTaskIdFromRemoval(UUID taskId) {
        taskIdsToDelete.remove(taskId);
    }

    public HashSet<UUID> getTaskIdsToDelete() {
        return taskIdsToDelete;
    }

    //region Hook Triggers
    public boolean triggerPlayerAliveHooks(Player player) {
        HashSet<PlantHook> hookIdSet = hookPlayerAliveMap.get(player.getUniqueId().toString());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlayerDeadHooks(Player player) {
        HashSet<PlantHook> hookIdSet = hookPlayerDeadMap.get(player.getUniqueId().toString());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlayerOnlineHooks(Player player) {
        HashSet<PlantHook> hookIdSet = hookPlayerOnlineMap.get(player.getUniqueId().toString());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlayerOfflineHooks(Player player) {
        HashSet<PlantHook> hookIdSet = hookPlayerOfflineMap.get(player.getUniqueId().toString());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlantBrokenHooks(PlantBlock plantBlock) {
        HashSet<PlantHook> hookIdSet = hookPlantBrokenMap.get(plantBlock.getLocation());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlantChunkLoadedHooks(PlantChunk chunk) {
        HashSet<PlantHook> hookIdSet = hookPlantChunkLoadedMap.get(chunk.getLocation());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    public boolean triggerPlantChunkUnloadedHooks(PlantChunk chunk) {
        HashSet<PlantHook> hookIdSet = hookPlantChunkUnloadedMap.get(chunk.getLocation());
        if (hookIdSet == null || hookIdSet.isEmpty()) {
            return false;
        }
        return performHooks(hookIdSet);
    }

    private boolean performHooks(HashSet<PlantHook> hookIdSet) {
        for (PlantHook hook : hookIdSet) {
            switch(hook.getAction()) {
                case START:
                    return resumeTask(hook.getTaskId().toString(), hook);
                case PAUSE:
                    return pauseTask(hook.getTaskId().toString(), hook);
                case CANCEL:
                    return cancelTask(hook.getTaskId().toString(), hook);
            }
        }
        return true;
    }

    private boolean performHookScriptBlock(PlantHook hook, PlantTask task) {
        if (hook != null) {
            return hook.performScriptBlock(task);
        }
        return false;
    }
    //endregion

    //region Hook Register/Unregister
    public boolean registerHook(PlantHook hook) {
        if (hook != null) {
            // player hooks
            if (hook instanceof PlantHookPlayer) {
                PlantHookPlayer hookPlayer = (PlantHookPlayer) hook;
                ConcurrentHashMap<String, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlayerHookMap(hookPlayer);
                if (plantHookMap != null) {
                    return addPlayerHookToMap(hookPlayer, plantHookMap);
                }
            }
            else if (hook instanceof PlantHookPlantBlock) {
                PlantHookPlantBlock hookPlantBlock = (PlantHookPlantBlock) hook;
                ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlantBlockHookMap(hookPlantBlock);
                if (plantHookMap != null) {
                    return addPlantBlockHookToMap(hookPlantBlock, plantHookMap);
                }
            }
            else if (hook instanceof PlantHookPlantChunk) {
                PlantHookPlantChunk hookPlantChunk = (PlantHookPlantChunk) hook;
                ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlantChunkHookMap(hookPlantChunk);
                if (plantHookMap != null) {
                    return addPlantChunkHookToMap(hookPlantChunk, plantHookMap);
                }
            }
        }
        return false;
    }

    public boolean unregisterHook(PlantHook hook) {
        if (hook != null) {
            // player hooks
            if (hook instanceof PlantHookPlayer) {
                PlantHookPlayer hookPlayer = (PlantHookPlayer) hook;
                ConcurrentHashMap<String, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlayerHookMap(hookPlayer);
                // if recognized, get HashSet and remove hook
                if (plantHookMap != null) {
                    HashSet<PlantHook> hookHashSet = plantHookMap.get(hookPlayer.getOfflinePlayer().getUniqueId().toString());
                    if (hookHashSet != null) {
                        return hookHashSet.remove(hook);
                    }
                }
            }
            else if (hook instanceof PlantHookPlantBlock) {
                PlantHookPlantBlock hookPlantBlock = (PlantHookPlantBlock) hook;
                ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlantBlockHookMap(hookPlantBlock);
                // if recognized, get HashSet and remove hook
                if (plantHookMap != null) {
                    HashSet<PlantHook> hookHashSet = plantHookMap.get(hookPlantBlock.getBlockLocation());
                    if (hookHashSet != null) {
                        return hookHashSet.remove(hook);
                    }
                }
            }
            else if (hook instanceof PlantHookPlantChunk) {
                PlantHookPlantChunk hookPlantChunk = (PlantHookPlantChunk) hook;
                ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> plantHookMap =
                        getMatchingPlantChunkHookMap(hookPlantChunk);
                // if recognized, get HashSet and remove hook
                if (plantHookMap != null) {
                    HashSet<PlantHook> hookHashSet = plantHookMap.get(hookPlantChunk.getChunkLocation());
                    if (hookHashSet != null) {
                        return hookHashSet.remove(hook);
                    }
                }
            }
        }
        return false;
    }

    private boolean addPlayerHookToMap(PlantHookPlayer hook, ConcurrentHashMap<String, HashSet<PlantHook>> plantHookMap) {
        if (hook == null || plantHookMap == null) {
            return false;
        }
        // get hook input corresponding to hook type
        String hookInput = hook.getOfflinePlayer().getUniqueId().toString();
        // see if HashSet bound to hook input already exists
        HashSet<PlantHook> hookHashSet = plantHookMap.get(hookInput);
        if (hookHashSet != null) {
            hookHashSet.add(hook);
        } else {
            hookHashSet = new HashSet<>();
            hookHashSet.add(hook);
            plantHookMap.put(hookInput, hookHashSet);
        }
        return true;
    }

    private boolean addPlantBlockHookToMap(PlantHookPlantBlock hook, ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> plantHookMap) {
        if (hook == null || plantHookMap == null) {
            return false;
        }
        // get hook input corresponding to hook type
        BlockLocation hookInput = hook.getBlockLocation();
        if (hookInput == null) {
            return false;
        }
        // see if HashSet bound to hook input already exists
        HashSet<PlantHook> hookHashSet = plantHookMap.get(hookInput);
        if (hookHashSet != null) {
            hookHashSet.add(hook);
        } else {
            hookHashSet = new HashSet<>();
            hookHashSet.add(hook);
            plantHookMap.put(hookInput, hookHashSet);
        }
        return true;
    }

    private boolean addPlantChunkHookToMap(PlantHookPlantChunk hook, ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> plantHookMap) {
        if (hook == null || plantHookMap == null) {
            return false;
        }
        // get hook input corresponding to hook type
        ChunkLocation hookInput = hook.getChunkLocation();
        if (hookInput == null) {
            return false;
        }
        // see if HashSet bound to hook input already exists
        HashSet<PlantHook> hookHashSet = plantHookMap.get(hookInput);
        if (hookHashSet != null) {
            hookHashSet.add(hook);
        } else {
            hookHashSet = new HashSet<>();
            hookHashSet.add(hook);
            plantHookMap.put(hookInput, hookHashSet);
        }
        return true;
    }

    private ConcurrentHashMap<String, HashSet<PlantHook>> getMatchingPlayerHookMap(PlantHookPlayer hook) {
        ConcurrentHashMap<String, HashSet<PlantHook>> plantHookMap = null;
        // figure out which type of player hook it is
        if (hook instanceof PlantHookPlayerOnline) {
            plantHookMap = hookPlayerOnlineMap;
        } else if (hook instanceof PlantHookPlayerOffline) {
            plantHookMap = hookPlayerOfflineMap;
        } else if (hook instanceof PlantHookPlayerAlive) {
            plantHookMap = hookPlayerAliveMap;
        } else if (hook instanceof PlantHookPlayerDead) {
            plantHookMap = hookPlayerDeadMap;
        }
        return plantHookMap;
    }

    private ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> getMatchingPlantBlockHookMap(PlantHookPlantBlock hook) {
        ConcurrentHashMap<BlockLocation, HashSet<PlantHook>> plantHookMap = null;
        if (hook instanceof PlantHookPlantBlockBroken) {
            plantHookMap = hookPlantBrokenMap;
        }
        return plantHookMap;
    }

    private ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> getMatchingPlantChunkHookMap(PlantHookPlantChunk hook) {
        ConcurrentHashMap<ChunkLocation, HashSet<PlantHook>> plantHookMap = null;
        if (hook instanceof PlantHookPlantChunkLoaded) {
            plantHookMap = hookPlantChunkLoadedMap;
        } else if (hook instanceof PlantHookPlantChunkUnloaded) {
            plantHookMap = hookPlantChunkUnloadedMap;
        }
        return plantHookMap;
    }
    //endregion

    private PlantTask getTask(String taskId) {
        return taskMap.get(taskId);
    }

    private void addTaskToMap(PlantTask task) {
        taskMap.put(task.getTaskId().toString(), task);
        removeTaskIdFromRemoval(task.getTaskId());
    }

    private boolean removeTaskId(String taskId) {
        PlantTask plantTask = taskMap.remove(taskId);
        if (plantTask == null) {
            return false;
        }
        // unregister hooks
        for (PlantHook hook : plantTask.getHooks()) {
            unregisterHook(hook);
        }
        taskIdsToDelete.add(plantTask.getTaskId());
        return true;
    }

}
