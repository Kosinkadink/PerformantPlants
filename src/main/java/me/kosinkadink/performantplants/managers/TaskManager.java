package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.tasks.PlantTask;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {

    private final Main main;

    private final ConcurrentHashMap<String, PlantTask> taskMap = new ConcurrentHashMap<>();
    private final HashSet<UUID> taskIdsToDelete = new HashSet<>();

    public TaskManager(Main main) {
        this.main = main;
    }

    public boolean scheduleTask(PlantTask task) {
        boolean startedTask = task.startTask(main);
        if (startedTask) {
            addTaskToMap(task);
            return true;
        }
        return false;
    }

    public void pauseAllTasks() {
        for (PlantTask plantTask : taskMap.values()) {
            plantTask.pauseTask();
        }
    }

    public boolean pauseTask(String taskId) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            plantTask.pauseTask();
            return true;
        }
        return false;
    }

    public boolean cancelTask(String taskId) {
        PlantTask plantTask = getTask(taskId);
        if (plantTask != null) {
            plantTask.cancelTask();
            removeTaskId(taskId);
            return true;
        }
        return false;
    }

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
        taskIdsToDelete.add(plantTask.getTaskId());
        return true;
    }

    public void removeTaskIdFromRemoval(UUID taskId) {
        taskIdsToDelete.remove(taskId);
    }

    public HashSet<UUID> getTaskIdsToDelete() {
        return taskIdsToDelete;
    }

}
