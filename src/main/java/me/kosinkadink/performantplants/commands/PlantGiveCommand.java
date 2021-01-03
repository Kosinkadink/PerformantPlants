package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlantGiveCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantGiveCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "give" },
                "Give plant item to player.",
                "/pp give <player> <plant-id> <amount>",
                "performantplants.give",
                2,
                3);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        // if on first argument, then return default player list
        if (args.length == commandNameWords.length+1) {
            return null;
        }
        // if on second argument, then return list of plant-ids
        else if (args.length == commandNameWords.length+2) {
            return getTabCompletionPlantIds(args[commandNameWords.length+1], performantPlants);
        }
        // if on third argument, then return list of 1 and max stack size of item
        else if (args.length == commandNameWords.length+3) {
            String amountString = args[commandNameWords.length + 2];
            ItemStack itemStack = performantPlants.getPlantTypeManager().getPlantItemStackById(args[commandNameWords.length+1]);
            if (itemStack != null) {
                return new ArrayList<String>() {
                    {
                        add("1");
                        add(Integer.toString(itemStack.getMaxStackSize()));
                    }
                }.stream().filter(id -> id.startsWith(amountString)).collect(Collectors.toList());
            }
        }
        return emptyList;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // get player
        String playerName = argList.get(0);
        Player player = performantPlants.getServer().getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage("Player " + playerName + " not found");
            return;
        }
        String plantId = argList.get(1);
        // get plant item
        ItemStack requestedItem = performantPlants.getPlantTypeManager().getPlantItemStackById(plantId);
        // if item not found, inform sender and stop
        if (requestedItem == null) {
            commandSender.sendMessage(String.format("Plant item '%s' not recognized", plantId));
            return;
        }
        // get amount; default to max stack size if not provided
        int amount = requestedItem.getMaxStackSize();
        if (argList.size() == 3) {
            try {
                amount = Integer.parseInt(argList.get(2));
                if (amount < 0) {
                    commandSender.sendMessage("Amount cannot be less than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                commandSender.sendMessage(getUsage());
                return;
            }
        }
        // set item stack to appropriate amount
        requestedItem.setAmount(amount);
        // give item stack to player
        HashMap<Integer,ItemStack> remainingMap = player.getInventory().addItem(requestedItem);
        int givenAmount = amount;
        // determine what was actually given
        if (!remainingMap.isEmpty()) {
            givenAmount -= remainingMap.get(0).getAmount();
        }
        if (amount != 0 && givenAmount == 0) {
            commandSender.sendMessage("Gave " + playerName + " 0 of " + plantId + " due to full inventory");
        } else {
            commandSender.sendMessage("Gave " + playerName + " " + givenAmount + " of " + plantId);
        }
    }
}
