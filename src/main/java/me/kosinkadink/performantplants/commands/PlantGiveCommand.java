package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantGiveCommand extends PPCommand {

    private Main main;

    public PlantGiveCommand(Main mainClass) {
        super(new String[] { "give" },
                "Give plant item to player.",
                "/pp give <player> <plant-id> <amount>",
                "performantplants.give",
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
        ItemStack requestedItem = main.getPlantTypeManager().getPlantItemStackById(plantId);
        // if item not found, inform sender and stop
        if (requestedItem == null) {
            commandSender.sendMessage(String.format("Plant item '%s' not recognized", plantId));
            return;
        }
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
