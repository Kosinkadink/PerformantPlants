package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlantStatsResetPlayerCommand extends PPCommand {

    private PerformantPlants performantPlants;

    private HashMap<String, Method> statMethodMap = new HashMap<>();

    public PlantStatsResetPlayerCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "stats", "resetplayer" },
                "Resets specific stat for specific players.",
                "/pp stats resetplayer <stat> <player>",
                "performantplants.stats.resetplayer",
                2,
                2);
        performantPlants = performantPlantsClass;
        fillInStatMethodMap();
    }

    void fillInStatMethodMap() {
        try {
            statMethodMap.put("sold",
                    performantPlants.getStatisticsManager().getClass().getMethod("resetAllPlantItemsSoldForPlayer", UUID.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        // if on first argument, then return list of stats
        if (args.length == commandNameWords.length+1) {
            String stat = args[commandNameWords.length];
            return statMethodMap.keySet().stream().filter(id -> id.startsWith(stat)).collect(Collectors.toList());
        }
        // if on second argument, then return default list of players
        if (args.length == commandNameWords.length+2) {
            return null;
        }
        return Collections.emptyList();
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // get stat
        String stat = argList.get(0);
        // get player string
        String playerString = argList.get(1);
        UUID playerUUID = null;
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
                method.invoke(performantPlants.getStatisticsManager(), playerUUID);
                commandSender.sendMessage(String.format("Stat '%s' reset for player %s", stat, playerString));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            commandSender.sendMessage(String.format("Stat '%s' not recognized", stat));
        }
    }

}
