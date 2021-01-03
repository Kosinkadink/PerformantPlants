package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PPCommand {

    protected static final ArrayList<String> emptyList = new ArrayList<>();

    protected String[] commandNameWords;
    private String description;
    private String usage;
    private String permission;
    private int minArgs;
    private int maxArgs;

    PPCommand(String[] commandNameWords, String description, String usage, String permission, int minArgs, int maxArgs) {
        this.commandNameWords = commandNameWords;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public abstract void executeCommand(CommandSender commandSender, List<String> argList);

    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        return emptyList;
    }

    public String[] getCommandNameWords() {
        return commandNameWords;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }

    protected List<String> getTabCompletionPlantIds(String arg, PerformantPlants performantPlants) {
        return getTabCompletionPlantIds(arg, performantPlants, true);
    }

    protected List<String> getTabCompletionPlantIds(String arg, PerformantPlants performantPlants, boolean includeSubItems) {
        // if '.' present, see if valid plant
        if (includeSubItems && (arg.contains(".") || performantPlants.getPlantTypeManager().getPlantById(arg) != null)) {
            String[] names = arg.split("\\.");
            Plant plant = performantPlants.getPlantTypeManager().getPlantById(names[0]);
            if (plant != null) {
                return plant.getItemIds().stream().filter(id -> id.startsWith(arg)).collect(Collectors.toList());
            }
        }
        return performantPlants.getPlantTypeManager().getPlantIds()
                .stream().filter(id -> id.startsWith(arg)).collect(Collectors.toList());
    }

    protected List<String> getTabCompletionListOfPlantIds(String fullPlantIdString, PerformantPlants performantPlants) {
        // separate by commas
        String plantId;
        String existingPlantIds = "";
        int index = fullPlantIdString.lastIndexOf(",");
        if (index >= 0) {
            if (index < fullPlantIdString.length()-1) {
                plantId = fullPlantIdString.substring(index+1);
            } else {
                plantId = "";
            }
            existingPlantIds = fullPlantIdString.substring(0, index+1);
        } else {
            plantId = fullPlantIdString;
        }
        List<String> possibleOptions = getTabCompletionPlantIds(plantId, performantPlants);
        // finalize for lambda function
        String finalExistingPlantIds = existingPlantIds;
        possibleOptions.replaceAll(plant -> finalExistingPlantIds + plant);
        return possibleOptions;
    }

}
