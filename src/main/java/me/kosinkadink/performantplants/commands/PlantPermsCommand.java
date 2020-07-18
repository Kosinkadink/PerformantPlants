package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.util.PermissionHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlantPermsCommand extends PPCommand {

    private Main main;

    public PlantPermsCommand(Main mainClass) {
        super(new String[] { "perms" },
                "List usage and permissions required for registered commands.",
                "/pp perms",
                "performantplants.perms",
                0,
                0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        commandSender.sendMessage(String.format("%s ----%s PerformantPlants Perms%s ----",
                ChatColor.AQUA, ChatColor.GREEN, ChatColor.AQUA));
        commandSender.sendMessage(String.format("%sEnvironmental:", ChatColor.AQUA));
        commandSender.sendMessage(new String[] {
                String.format("%splace: %s%s", ChatColor.GREEN, ChatColor.LIGHT_PURPLE, PermissionHelper.Place),
                String.format("%sbreak: %s%s", ChatColor.GREEN, ChatColor.LIGHT_PURPLE, PermissionHelper.Break),
                String.format("%sinteract: %s%s", ChatColor.GREEN, ChatColor.LIGHT_PURPLE, PermissionHelper.Interact),
                String.format("%sconsume: %s%s", ChatColor.GREEN, ChatColor.LIGHT_PURPLE, PermissionHelper.Consume),
        });
        commandSender.sendMessage(String.format("%sCommands:", ChatColor.AQUA));
        for (PPCommand command : main.getCommandManager().getRegisteredCommands()) {
            commandSender.sendMessage(String.format("%s%s %s- %s%s",
                    ChatColor.GREEN, command.getUsage(), ChatColor.AQUA,
                    ChatColor.LIGHT_PURPLE, command.getPermission()));
        }
    }
}
