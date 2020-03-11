package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlantAddCommand extends PPCommand {

    private Main main;

    public PlantAddCommand(Main mainClass) {
        super(new String[] { "plant", "add" },
                "Creates plant block at player's location.",
                "/pp plant add",
                "performantplants.plant.add",
                0,
                0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Command can only be executed py a Player.");
        }
        else {
            Player player = (Player) commandSender;
            PlantBlock plantBlock = new PlantBlock(new BlockLocation(player.getLocation()),
                    main.getPlantTypeManager().getPlantById("test.seed"));
            main.getPlantManager().addPlantBlock(plantBlock);
        }
    }
}
