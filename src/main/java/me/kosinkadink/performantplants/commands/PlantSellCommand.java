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

public class PlantSellCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantSellCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "sell" },
                "Sell plant item for player.",
                "/pp sell <player> <plant-id> <amount>",
                "performantplants.sell",
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
            String amountString = args[commandNameWords.length+2];
            ItemStack itemStack = performantPlants.getPlantTypeManager().getPlantItemStackById(args[commandNameWords.length+1]);
            if (itemStack != null) {
                // create basic list
                ArrayList<String> possibleOptions = new ArrayList<String>() {
                    {
                        add("1");
                        add(Integer.toString(itemStack.getMaxStackSize()));
                    }
                };
                // see how much of the item player has in inventory (but do not check other player's inventory)
                String playerName = args[commandNameWords.length];
                if (commandSender instanceof Player && commandSender.getName().equals(playerName)) {
                    int sum = 0;
                    for (ItemStack inventoryStack : ((Player)commandSender).getInventory().getStorageContents()) {
                        if (inventoryStack != null && inventoryStack.isSimilar(itemStack)) {
                            sum += inventoryStack.getAmount();
                        }
                    }
                    if (sum > 0) {
                        possibleOptions.add(Integer.toString(sum));
                    }
                }
                return possibleOptions.stream().filter(id -> id.startsWith(amountString)).collect(Collectors.toList());
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
        if (requestedItem.getSellPrice() < 0) {
            commandSender.sendMessage(String.format("Plant item '%s' is not sellable", plantId));
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
        // set item stack to appropriate amount
        requestedItemStack.setAmount(amount);
        // give item stack to player
        HashMap<Integer,ItemStack> remainingMap = player.getInventory().removeItem(requestedItemStack);
        int takenAmount = amount;
        // determine what was actually taken
        if (!remainingMap.isEmpty()) {
            takenAmount -= remainingMap.get(0).getAmount();
        }
        // take proper amount of money
        double totalWorth = requestedItem.getSellPrice()*takenAmount;
        performantPlants.getEconomy().depositPlayer(player, totalWorth);
        // show message to seller
        if (amount != 0 && takenAmount == 0) {
            player.sendMessage("Sold 0 of " + plantId + " due to none found in inventory");
            if (!(commandSender instanceof Player) || ((Player) commandSender).getUniqueId() != player.getUniqueId()) {
                commandSender.sendMessage(playerName + " sold 0 of " + plantId + " due to none found in inventory");
            }
        } else {
            player.sendMessage(String.format("Sold %d of %s for price of %.2f",
                    takenAmount, plantId, totalWorth));
            if (!(commandSender instanceof Player) || ((Player) commandSender).getUniqueId() != player.getUniqueId()) {
                commandSender.sendMessage(String.format("%s sold %d of %s for price of %.2f",
                        playerName, takenAmount, plantId, totalWorth));
            }
            // add sale to StatisticsManager
            performantPlants.getStatisticsManager().addPlantItemsSold(player.getUniqueId(), plantId, takenAmount);
        }
    }
}
