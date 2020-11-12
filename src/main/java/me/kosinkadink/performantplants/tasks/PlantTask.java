package me.kosinkadink.performantplants.tasks;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class PlantTask {

    private BukkitTask bukkitTask;
    private long taskStartTime;
    private boolean cancelled = false;
    private boolean running = false;

    private final UUID taskId;
    private final String plantId;
    private final String taskConfigId;

    private OfflinePlayer offlinePlayer;
    private BlockLocation blockLocation;
    private long delay;

    public PlantTask(String plantId, String taskConfigId) {
        taskId = UUID.randomUUID();
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
    }

    public boolean startTask(Main main) {
        // if task already exists, do nothing
        if (isCancelled() || bukkitTask != null) {
            return false;
        }
        // get task based on plant and task config id
        Plant plant = Main.getInstance().getPlantTypeManager().getPlantById(plantId);
        if (plant == null) {
            return false;
        }
        ScriptTask scriptTask = plant.getScriptTask(taskConfigId);
        if (scriptTask == null) {
            return false;
        }
        // set new start time
        taskStartTime = System.currentTimeMillis();
        // start script task
        bukkitTask = Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(),
                () -> {
                    PlantBlock plantBlock = null;
                    if (blockLocation != null) {
                        plantBlock = Main.getInstance().getPlantManager().getPlantBlock(blockLocation);
                    }
                    try {
                        scriptTask.getTaskScriptBlock().loadValue(plantBlock, offlinePlayer.getPlayer());
                    } catch (Exception e) {
                        // do nothing
                    }
                    running = false;
                    Main.getInstance().getTaskManager().cancelTask(taskId.toString());
                }, delay);
        running = true;
        return true;
    }

    public void pauseTask() {
        // try to cancel task
        if (bukkitTask != null && running) {
            bukkitTask.cancel();
            running = false;
            // figure out remaining time
            long millisPassed = System.currentTimeMillis()-taskStartTime;
            // subtract ticks passed from delay
            delay -= TimeHelper.millisToTicks(millisPassed);
            if (delay < 0) {
                delay = 0;
            }
            // set task to null
            bukkitTask = null;
        }
    }

    public void cancelTask() {
        pauseTask();
        cancelled = true;
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

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isRunning() {
        return running;
    }
}
