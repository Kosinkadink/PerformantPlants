package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.plants.PlantItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantBuyCommand extends PPCommand {

    private Main main;

    public PlantBuyCommand(Main mainClass) {
        super(new String[] { "buy" },
                "Buy plant item for player.",
                "/pp buy <player> <plant-id> <amount>",
                "performantplants.buy",
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
        if (!main.getEconomy().has(player, totalWorth)) {
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
        main.getEconomy().withdrawPlayer(player, totalWorth);
        // TODO: show message to buyer
        if (amount != 0 && givenAmount == 0) {
            commandSender.sendMessage(playerName + " bought 0 of " + plantId + " due to full inventory");
        } else {
            commandSender.sendMessage(String.format("%s bought %d of %s at price of %.2f",
                    playerName, givenAmount, plantId, totalWorth));
        }
    }
}
