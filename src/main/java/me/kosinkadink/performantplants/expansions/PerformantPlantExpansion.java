package me.kosinkadink.performantplants.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kosinkadink.performantplants.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PerformantPlantExpansion extends PlaceholderExpansion {

    private Main main;

    public PerformantPlantExpansion(Main main) {
        this.main = main;
    }

    /**
     * Expansion class is internal, so have to override persist() to not unregister expansion on PlaceholderAPI reload
     * @return true to persist thorugh reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Expansion class is internal, so this check is not needed and always returns true
     * @return true since it's an internal class
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "performantplants";
    }

    @Override
    public String getAuthor() {
        return main.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }

        // %performantplants_buyprice_<plantitemid>% -> int
        // %performantplants_sellprice_<plantitemid>% -> int
        // %performantplants_hasseed_<plantitemid>% -> boolean

        // %performantplants_sold_<player>_<plantitemid>% -> int

        // %performantplants_planted_<player>_<plantid>% -> int


        // return null if invalid placeholder provided
        return null;
    }

//    @Override
//    public String onPlaceholderRequest(Player player, String identifier) {
//        if (player == null) {
//            return "";
//        }
//
//        // %performantplants_sold_<plantitemid>%
//
//
//        // return null if invalid placeholder provided
//        return null;
//    }

}
