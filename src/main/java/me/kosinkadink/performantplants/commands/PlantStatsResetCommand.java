package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlantStatsResetCommand extends PPCommand {

    private PerformantPlants performantPlants;

    private HashMap<String, Method> statMethodMap = new HashMap<>();

    public PlantStatsResetCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "stats", "reset" },
                "Resets specific stat for specific players for specific plant-id.",
                "/pp stats reset <stat> <player> <plant-id>",
                "performantplants.stats.reset",
                3,
                3);
        performantPlants = performantPlantsClass;
        fillInStatMethodMap();
    }

    void fillInStatMethodMap() {
        try {
            statMethodMap.put("sold",
                    performantPlants.getStatisticsManager().getClass().getMethod("resetPlantItemsSoldForPlayer", UUID.class, String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // get stat
        String stat = argList.get(0);
        // get player string
        String playerString = argList.get(1);
        UUID playerUUID = null;
        // get plantItemId
        String plantItemId = argList.get(2);
        // check if player is online
        Player player = performantPlants.getServer().getPlayer(playerString);
        if (player == null) {
            //get list of offline players, see if name matches
            boolean found = false;
            for (OfflinePlayer offlinePlayer : performantPlants.getServer().getOfflinePlayers()) {
                if (playerString.equalsIgnoreCase(offlinePlayer.getName())
                        || playerString.equalsIgnoreCase(offlinePlayer.getUniqueId().toString())) {
                    playerUUID = offlinePlayer.getUniqueId();
                    found = true;
                    break;
                }
            }
            if (!found) {
                commandSender.sendMessage(String.format("Player '%s' not found", playerString));
                return;
            }
        } else {
            playerUUID = player.getUniqueId();
        }
        // check if valid stat
        Method method = statMethodMap.get(stat);
        if (method != null) {
            try {
                if ((boolean)method.invoke(performantPlants.getStatisticsManager(), playerUUID, plantItemId)) {
                    commandSender.sendMessage(String.format("Stat '%s' reset for player %s for plant item '%s'", stat, playerString, plantItemId));
                } else {
                    commandSender.sendMessage(String.format("No stats found to reset for plant item %s", plantItemId));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            commandSender.sendMessage(String.format("Stat '%s' not recognized", stat));
        }
    }
}
