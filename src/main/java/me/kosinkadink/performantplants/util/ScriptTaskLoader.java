package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;

import java.util.*;

public class ScriptTaskLoader {
    PerformantPlants performantPlants;

    private final HashSet<String> taskNames = new HashSet<>();
    private final HashSet<String> scriptNames = new HashSet<>();

    private final HashMap<String, List<String>> taskTaskDependents = new HashMap<>();
    private final HashMap<String, List<String>> scriptScriptDependents = new HashMap<>();
    private final HashMap<String, List<String>> scriptTaskDependents = new HashMap<>();
    private final HashMap<String, List<String>> taskScriptDependents = new HashMap<>();

    private final HashSet<String> failedTasks = new HashSet<>();
    private final HashSet<String> failedScripts = new HashSet<>();

    private String currentTask = "";
    private String currentScript = "";

    public ScriptTaskLoader(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public void addTaskName(String name) {
        taskNames.add(name);
    }

    public void addScriptName(String name) {
        scriptNames.add(name);
    }

    public boolean isTask(String name) {
        return taskNames.contains(name);
    }

    public void addTaskTaskDependent(String task, String dependent) {
        if (taskTaskDependents.containsKey(task)) {
            taskTaskDependents.get(task).add(dependent);
        } else {
            ArrayList<String> dependents = new ArrayList<>();
            dependents.add(dependent);
            taskTaskDependents.put(task, dependents);
        }
    }

    public void addScriptScriptDependent(String script, String dependent) {
        if (scriptScriptDependents.containsKey(script)) {
            scriptScriptDependents.get(script).add(dependent);
        } else {
            ArrayList<String> dependents = new ArrayList<>();
            dependents.add(dependent);
            scriptScriptDependents.put(script, dependents);
        }
    }

    public void addScriptTaskDependent(String script, String task) {
        if (scriptTaskDependents.containsKey(script)) {
            scriptTaskDependents.get(script).add(task);
        } else {
            ArrayList<String> dependents = new ArrayList<>();
            dependents.add(task);
            scriptTaskDependents.put(script, dependents);
        }
    }

    public void addTaskScriptDependent(String task, String script) {
        if (taskScriptDependents.containsKey(task)) {
            taskScriptDependents.get(task).add(script);
        } else {
            ArrayList<String> dependents = new ArrayList<>();
            dependents.add(script);
            taskScriptDependents.put(task, dependents);
        }
    }

    public void addFailedTask(String name) {
        failedTasks.add(name);
        // remove from task names
        taskNames.remove(name);
    }

    public HashSet<String> getFailedTasks() {
        return failedTasks;
    }

    public HashSet<String> getFailedScripts() {
        return failedScripts;
    }

    public void addFailedScript(String name) {
        failedScripts.add(name);
        // remove from script names
        scriptNames.remove(name);
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public boolean isTaskSet() {
        return !currentTask.isEmpty();
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public boolean isScriptSet() {
        return !currentScript.isEmpty();
    }

    public void resetCurrentScript() {
        currentScript = "";
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentScript(String currentScript) {
        this.currentScript = currentScript;
    }

    public void processTasksAndScripts() {
        ArrayList<String> initialFailedTasks = new ArrayList<>(failedTasks);
        for (String failedTask : initialFailedTasks) {
            taskFailDependentsInitial(failedTask, String.format("error in Task %s", failedTask));
        }
    }

    private void taskFailDependentsInitial(String taskName, String reason) {
        // fail self, just in case
        addFailedTask(taskName);
        // fail each dependent task
        for (String taskToFail : taskTaskDependents.getOrDefault(taskName, Collections.emptyList())) {
            taskFailDependents(taskToFail, reason);
        }
        // fail each dependent script
        for (String scriptToFail : taskScriptDependents.getOrDefault(taskName, Collections.emptyList())) {
            scriptFailDependents(scriptToFail, reason);
        }
    }

    private void taskFailDependents(String taskName, String reason) {
        if (failedTasks.contains(taskName)) {
            return;
        }
        // fail self
        addFailedTask(taskName);
        // explain
        performantPlants.getLogger().warning(String.format("Task %s won't be loaded due to %s",
                taskName, reason));
        // fail each dependent task
        for (String taskToFail : taskTaskDependents.getOrDefault(taskName, Collections.emptyList())) {
            taskFailDependents(taskToFail, reason);
        }
        // fail each dependent script
        for (String scriptToFail : taskScriptDependents.getOrDefault(taskName, Collections.emptyList())) {
            scriptFailDependents(scriptToFail, reason);
        }
    }

    private void scriptFailDependents(String scriptName, String reason) {
        if (failedScripts.contains(scriptName)) {
            return;
        }
        // fail self
        addFailedScript(scriptName);
        // explain
        performantPlants.getLogger().warning(String.format("Stored script block %s won't be loaded due to %s",
                scriptName, reason));
        // fail each dependent task
        for (String taskToFail : scriptTaskDependents.getOrDefault(scriptName, Collections.emptyList())) {
            taskFailDependents(taskToFail, reason);
        }
        // fail each dependent script
        for (String scriptToFail : scriptScriptDependents.getOrDefault(scriptName, Collections.emptyList())) {
            scriptFailDependents(scriptToFail, reason);
        }
    }

}
