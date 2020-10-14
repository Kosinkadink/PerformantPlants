package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.entity.Player;

public class PlantChatEffect extends PlantEffect {

    private ScriptBlock toPlayer = ScriptResult.EMPTY;
    private ScriptBlock fromPlayer = ScriptResult.EMPTY;

    public PlantChatEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        ScriptResult fromPlayerResult = fromPlayer.loadValue(plantBlock, player);
        String fromPlayerString = fromPlayerResult.getStringValue();
        if (!fromPlayerString.isEmpty()) {
            player.chat(TextHelper.translateAlternateColorCodes(fromPlayerString));
        }
        ScriptResult toPlayerResult = toPlayer.loadValue(plantBlock, player);
        String toPlayerString = toPlayerResult.getStringValue();
        if (!toPlayerString.isEmpty()) {
            player.sendMessage(TextHelper.translateAlternateColorCodes(toPlayerString));
        }
    }

    public ScriptBlock getToPlayer() {
        return toPlayer;
    }

    public String getToPlayerValue(Player player, PlantBlock plantBlock) {
        return toPlayer.loadValue(plantBlock, player).getStringValue();
    }

    public void setToPlayer(ScriptBlock message) {
        toPlayer = message;
    }

    public ScriptBlock getFromPlayer() {
        return fromPlayer;
    }

    public String getFromPlayerValue(Player player, PlantBlock plantBlock) {
        return fromPlayer.loadValue(plantBlock, player).getStringValue();
    }

    public void setFromPlayer(ScriptBlock message) {
        fromPlayer = message;
    }

}
