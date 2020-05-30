package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.entity.Player;

public class PlantChatEffect extends PlantEffect {

    private String toPlayer = "";
    private String fromPlayer = "";

    public PlantChatEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        if (!fromPlayer.isEmpty()) {
            String formatted = PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, player, fromPlayer);
            player.chat(TextHelper.translateAlternateColorCodes(formatted));
        }
        if (!toPlayer.isEmpty()) {
            String formatted = PlaceholderHelper.setVariablesAndPlaceholders(plantBlock, player, toPlayer);
            player.sendMessage(TextHelper.translateAlternateColorCodes(formatted));
        }
    }

    public String getToPlayer() {
        return toPlayer;
    }

    public void setToPlayer(String message) {
        toPlayer = message;
    }

    public String getFromPlayer() {
        return fromPlayer;
    }

    public void setFromPlayer(String message) {
        fromPlayer = message;
    }

}
