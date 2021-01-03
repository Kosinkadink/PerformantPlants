package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ScriptTaskLoader {
    PerformantPlants performantPlants;

    private final HashSet<String> taskNames = new HashSet<>();
    private final HashMap<String, ArrayList<String>> taskDependencies = new HashMap<>();
    private final ArrayList<String> failedTasks = new ArrayList<>();

    private String currentTask = "";

    private final HashMap<String, ScriptTask> taskHashMap = new HashMap<>();

    public ScriptTaskLoader(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public void addTaskName(String name) {
        taskNames.add(name);
    }

    public boolean isTask(String name) {
        return taskNames.contains(name);
    }

    public void addTask(String taskId, ScriptTask scriptTask) {
        taskHashMap.put(taskId, scriptTask);
    }

    public HashMap<String, ScriptTask> processTasksAndReturnMap(ConfigurationSection section) {
        if (!failedTasks.isEmpty()) {
            for (String failedTask : failedTasks) {
                ArrayList<String> dependencies = taskDependencies.get(failedTask);
                if (dependencies != null && !dependencies.isEmpty()) {
                    performantPlants.getLogger().warning(String.format("Task '%s' failed to load, so all tasks depending on it (%s) " +
                            "will be unloaded in section %s",
                            failedTask, dependencies.toString(), section.getCurrentPath()));
                    // remove dependent tasks from map
                    for (String dependency : dependencies) {
                        taskHashMap.remove(dependency);
                    }
                }
                taskHashMap.remove(failedTask);
            }
        }
        return taskHashMap;
    }

    public void addTaskDependency(String name, String dependency) {
        if (taskDependencies.containsKey(name)) {
            ArrayList<String> dependencies = taskDependencies.get(name);
            dependencies.add(dependency);
        } else {
            ArrayList<String> dependencies = new ArrayList<>();
            dependencies.add(dependency);
            taskDependencies.put(name, dependencies);
        }
    }

    public void addFailedTask(String name) {
        failedTasks.add(name);
        // remove from task names
        taskNames.remove(name);
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }
}
