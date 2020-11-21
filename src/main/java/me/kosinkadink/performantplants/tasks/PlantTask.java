package me.kosinkadink.performantplants.tasks;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.util.PlayerHelper;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.UUID;

public class PlantTask {

    private BukkitTask bukkitTask;
    private long taskStartTime;
    private boolean cancelled = false;
    private boolean paused = false;

    private final UUID taskId;
    private final String plantId;
    private final String taskConfigId;

    private ScriptTask scriptTask = null;

    private OfflinePlayer offlinePlayer;
    private BlockLocation blockLocation;
    private long delay;
    private boolean autostart = true;

    private final ArrayList<PlantHook> hooks = new ArrayList<>();

    public PlantTask(UUID taskId, String plantId, String taskConfigId) {
        this.taskId = taskId;
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
        setupScriptTask();
    }

    public PlantTask(String plantId, String taskConfigId) {
        taskId = UUID.randomUUID();
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
        setupScriptTask();
    }

    private void setupScriptTask() {
        // get task based on plant and task config id
        Plant plant = Main.getInstance().getPlantTypeManager().getPlantById(plantId);
        if (plant != null) {
            ScriptTask scriptTask = plant.getScriptTask(taskConfigId);
            if (scriptTask != null) {
                this.scriptTask = scriptTask;
            }
        }
    }

    public ScriptTask getScriptTask() {
        return scriptTask;
    }

    public boolean startTask(Main main) {
        // if task already exists, do nothing
        if (!isStartable()) {
            return false;
        }
        // set new start time
        taskStartTime = System.currentTimeMillis();
        // start script task
        bukkitTask = Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(),
                () -> {
                    try {
                        scriptTask.getTaskScriptBlock().loadValue(getPlantBlock(), PlayerHelper.getFreshPlayer(offlinePlayer));
                    } catch (Exception e) {
                        // do nothing
                    }
                    Main.getInstance().getTaskManager().cancelTask(taskId.toString(), null);
                }, delay);
        // mark not paused
        paused = false;
        return true;
    }

    public boolean pauseTask() {
        // set paused to true
        paused = true;
        // try to cancel task
        if (isRunning()) {
            stopRunning();
            // figure out remaining time
            long millisPassed = System.currentTimeMillis()-taskStartTime;
            // subtract ticks passed from delay
            delay -= TimeHelper.millisToTicks(millisPassed);
            if (delay < 0) {
                delay = 0;
            }
            return true;
        }
        return false;
    }

    public boolean cancelTask() {
        if (!isCancelled()) {
            stopRunning();
            cancelled = true;
            return true;
        }
        return false;
    }

    public void freezeTask() {
        // pause task but keep initial paused state
        boolean initialPause = paused;
        pauseTask();
        paused = initialPause;
    }

    public void unfreezeTask(Main main) {
        if (!isPaused()) {
            startTask(main);
        }
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getPlantId() {
        return plantId;
    }

    public String getTaskConfigId() {
        return taskConfigId;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public void setOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    public void setBlockLocation(BlockLocation blockLocation) {
        this.blockLocation = blockLocation;
    }

    public PlantBlock getPlantBlock() {
        PlantBlock plantBlock = null;
        if (blockLocation != null) {
            plantBlock = Main.getInstance().getPlantManager().getPlantBlock(blockLocation);
        }
        return plantBlock;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public boolean isRunning() {
        return bukkitTask != null;
    }

    private void stopRunning() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isStartable() {
        return !isCancelled() && !isRunning() && isValid();
    }

    public boolean isValid() {
        return scriptTask != null;
    }

    public void addHook(PlantHook hook) {
        hooks.add(hook);
    }

    public ArrayList<PlantHook> getHooks() {
        return hooks;
    }

}
