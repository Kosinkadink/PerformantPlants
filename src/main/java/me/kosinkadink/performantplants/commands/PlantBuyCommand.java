package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlantBuyCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantBuyCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "buy" },
                "Buy plant item for player.",
                "/pp buy <player> <plant-id> <amount>",
                "performantplants.buy",
                2,
                3);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        // if on first argument, return default list of players
        if (args.length == commandNameWords.length+1) {
            return null;
        }
        // if on second argument, return list of plant-ids
        if (args.length == commandNameWords.length+2) {
            return getTabCompletionPlantIds(args[commandNameWords.length+1], performantPlants);
        }
        // if on third argument, return list of amounts
        if (args.length == commandNameWords.length+3) {
            String amountString = args[commandNameWords.length + 2];
            ItemStack itemStack = performantPlants.getPlantTypeManager().getPlantItemStackById(args[commandNameWords.length + 1]);
            if (itemStack != null) {
                // create basic list
                return new ArrayList<String>() {
                    {
                        add("1");
                        add(Integer.toString(itemStack.getMaxStackSize()));
                    }
                }.stream().filter(id -> id.startsWith(amountString)).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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
        PlantItem requestedItem = performantPlants.getPlantTypeManager().getPlantItemById(plantId);
        // if item not found, inform sender and stop
        if (requestedItem == null) {
            commandSender.sendMessage(String.format("Plant item '%s' not recognized", plantId));
            return;
        }
        // determine if plant item is sellable
        if (requestedItem.getBuyPrice() < 0) {
            commandSender.sendMessage(String.format("Plant item '%s' is not buyable", plantId));
            return;
        }
        ItemStack requestedItemStack = requestedItem.getItemStack();
        // get amount; default to 1 if not provided
        int amount = 1;
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
        // check if player has necessary funds
        double totalWorth = requestedItem.getBuyPrice()*amount;
        if (!performantPlants.getEconomy().has(player, totalWorth)) {
            commandSender.sendMessage(String.format("Player %s does not have the required %.2f to purchase %d of %s",
                    playerName, totalWorth, amount, plantId));
            return;
        }
        // set item stack to appropriate amount
        requestedItemStack.setAmount(amount);
        // give item stack to player
        HashMap<Integer,ItemStack> remainingMap = player.getInventory().addItem(requestedItemStack);
        int givenAmount = amount;
        // determine what was actually given
        if (!remainingMap.isEmpty()) {
            givenAmount -= remainingMap.get(0).getAmount();
        }
        // take proper amount of money
        totalWorth = requestedItem.getBuyPrice()*givenAmount;
        performantPlants.getEconomy().withdrawPlayer(player, totalWorth);
        // show message to buyer
        if (amount != 0 && givenAmount == 0) {
            player.sendMessage("Bought 0 of " + plantId + " due to full inventory");
            if (!(commandSender instanceof Player) || ((Player) commandSender).getUniqueId() != player.getUniqueId()) {
                commandSender.sendMessage(playerName + " bought 0 of " + plantId + " due to full inventory");
            }
        } else {
            player.sendMessage(String.format("Bought %d of %s at price of %.2f",
                    givenAmount, plantId, totalWorth));
            if (!(commandSender instanceof Player) || ((Player) commandSender).getUniqueId() != player.getUniqueId()) {
                commandSender.sendMessage(String.format("%s bought %d of %s at price of %.2f",
                        playerName, givenAmount, plantId, totalWorth));
            }
        }
    }
}
