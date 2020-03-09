package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlantGiveCommand extends PPCommand {

    private Main main;

    public PlantGiveCommand(Main mainClass) {
        super(new String[] { "give" },
                "Give plant block to player.",
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
        if (player != null) {
            // get plant type
            String plantId = argList.get(1);
            Plant plant = main.getPlantTypeManager().getPlantById(plantId);
            if (plant != null) {
                // get amount; default to 1 if not provided
                int amount = 1;
                if (argList.size() == 3) {
                    try {
                        amount = Integer.parseInt(argList.get(2));
                    }
                    catch (NumberFormatException e) {
                        commandSender.sendMessage(getUsage());
                        return;
                    }
                }
                // get item stack with appropriate amount
                ItemStack requestedItem = new ItemBuilder(plant.getItem()).amount(amount).build();
                // check if player has room in inventory
                if (player.getInventory().firstEmpty() != -1) {
                    // give item stack to player
                    player.getInventory().addItem(requestedItem);
                    commandSender.sendMessage("Gave " + playerName + " " + amount + " of " + plantId);
                }
                else {
                    commandSender.sendMessage("Player's inventory is full, cannot give plant");
                }
            }
            else {
                commandSender.sendMessage("Plant of type " + plantId + " not recognized");
            }
        }
        else {
            commandSender.sendMessage("Player " + playerName + " not found");
        }
    }
}
