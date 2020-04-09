package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            String fullPlantId = plantId;
            // if plant id ends with '.seed', need to get seed item
            boolean isSeed = false;
            if (plantId.endsWith(".seed")) {
                isSeed = true;
                // remove .seed from end of id to get plant id
                plantId = plantId.split(".seed", 2)[0];
            }
            Plant plant = main.getPlantTypeManager().getPlantById(plantId);
            if (plant != null) {
                // if seed item requested and plant doesn't have one, warn sender and do nothing
                if (isSeed && !plant.hasSeed()) {
                    commandSender.sendMessage("Requested plant does not have an associated seed");
                    return;
                }
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
                ItemStack requestedItem;
                if (!isSeed) {
                    requestedItem = new ItemBuilder(plant.getItem()).amount(amount).build();
                }
                else {
                    requestedItem = new ItemBuilder(plant.getSeedItem()).amount(amount).build();
                }
                // check if player has room in inventory
                if (player.getInventory().firstEmpty() != -1) {
                    // give item stack to player
                    player.getInventory().addItem(requestedItem);
                    commandSender.sendMessage("Gave " + playerName + " " + amount + " of " + fullPlantId);
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
