package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.plants.PlantItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class PlantSellCommand extends PPCommand {

    private Main main;

    public PlantSellCommand(Main mainClass) {
        super(new String[] { "sell" },
                "Sell plant item for player.",
                "/pp sell <player> <plant-id> <amount>",
                "performantplants.sell",
                2,
                3);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // get player
        String playerName = argList.get(0);
        Player player = main.getServer().getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage("Player " + playerName + " not found");
            return;
        }
        String plantId = argList.get(1);
        // get plant item
        PlantItem requestedItem = main.getPlantTypeManager().getPlantItemById(plantId);
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
        main.getEconomy().depositPlayer(player, totalWorth);
        if (amount != 0 && takenAmount == 0) {
            commandSender.sendMessage(playerName + " sold 0 of " + plantId + " due to none found in inventory");
        } else {
            commandSender.sendMessage(String.format("%s sold %d of %s for price of %.2f",
                    playerName, takenAmount, plantId, totalWorth));
        }
    }
}
