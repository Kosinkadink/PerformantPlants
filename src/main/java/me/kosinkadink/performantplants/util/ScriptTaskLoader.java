package me.kosinkadink.performantplants.util;

import java.util.ArrayList;
import java.util.HashMap;

public class ScriptTaskLoader {
    private final ArrayList<String> taskNames = new ArrayList<>();
    private final HashMap<String, ArrayList<String>> taskDependencies = new HashMap<>();
    private final ArrayList<String> failedTasks = new ArrayList<>();

    public ScriptTaskLoader() {}

    public void addTaskName(String name) {
        taskNames.add(name);
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

}
