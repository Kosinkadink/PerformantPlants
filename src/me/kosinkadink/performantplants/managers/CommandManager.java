package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.commands.PPCommand;
import me.kosinkadink.performantplants.executors.PPCommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private Main main;

    private PPCommandExecutor PPCommandExecutor;

    private List<PPCommand> registeredCommands = new ArrayList<>();

    private String commandRoot = "pp";

    public CommandManager(Main mainClass) {
        main = mainClass;
        PPCommandExecutor = new PPCommandExecutor(mainClass);
    }

    public List<PPCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    public void registerCommand(PPCommand ppCommand) {
        main.getCommand(commandRoot).setExecutor(PPCommandExecutor);
        registeredCommands.add(ppCommand);
    }

    public String getCommandRoot() {
        return commandRoot;
    }

}
