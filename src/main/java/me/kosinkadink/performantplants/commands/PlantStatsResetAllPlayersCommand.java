package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class PlantStatsResetAllPlayersCommand extends PPCommand {

    private Main main;

    private HashMap<String, Method> statMethodMap = new HashMap<>();

    public PlantStatsResetAllPlayersCommand(Main mainClass) {
        super(new String[] { "stats", "resetallplayers" },
                "Resets specific stat for all players.",
                "/pp stats resetallplayers <stat>",
                "performantplants.stats.resetallplayers",
                1,
                1);
        main = mainClass;
        fillInStatMethodMap();
    }

    void fillInStatMethodMap() {
        try {
            statMethodMap.put("sold",
                    main.getStatisticsManager().getClass().getMethod("resetAllPlantItemsSoldForAllPlayers"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // get stat
        String stat = argList.get(0);
        // check if valid stat
        Method method = statMethodMap.get(stat);
        if (method != null) {
            try {
                method.invoke(main.getStatisticsManager());
                commandSender.sendMessage(String.format("Stat '%s' reset for all players", stat));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            commandSender.sendMessage(String.format("Stat '%s' not recognized", stat));
        }
    }

}
