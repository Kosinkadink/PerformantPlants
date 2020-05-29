package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.entity.Player;

public class PlantChatEffect extends PlantEffect {

    private String toPlayer = "";
    private String fromPlayer = "";

    public PlantChatEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        if (!fromPlayer.isEmpty()) {
            player.chat(TextHelper.translateAlternateColorCodes(fromPlayer));
        }
        if (!toPlayer.isEmpty()) {
            player.sendMessage(TextHelper.translateAlternateColorCodes(toPlayer));
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
