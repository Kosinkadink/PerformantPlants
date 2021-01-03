package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.commands.PPCommand;
import me.kosinkadink.performantplants.executors.PPCommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {

    private PerformantPlants performantPlants;
    private PPCommandExecutor PPCommandExecutor;
    private List<PPCommand> registeredCommands = new ArrayList<>();
    private String commandRoot = "pp";

    public CommandManager(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
        PPCommandExecutor = new PPCommandExecutor(performantPlantsClass);
    }

    public List<PPCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    public void registerCommand(PPCommand ppCommand) {
        Objects.requireNonNull(performantPlants.getCommand(commandRoot)).setExecutor(PPCommandExecutor);
        registeredCommands.add(ppCommand);
    }

    public String getCommandRoot() {
        return commandRoot;
    }

}
