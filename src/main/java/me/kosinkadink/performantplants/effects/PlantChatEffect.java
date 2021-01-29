package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.TextHelper;

public class PlantChatEffect extends PlantEffect {

    private ScriptBlock toPlayer = ScriptResult.EMPTY;
    private ScriptBlock fromPlayer = ScriptResult.EMPTY;

    public PlantChatEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        ScriptResult fromPlayerResult = fromPlayer.loadValue(context);
        String fromPlayerString = fromPlayerResult.getStringValue();
        if (!fromPlayerString.isEmpty()) {
            context.getPlayer().chat(TextHelper.translateAlternateColorCodes(fromPlayerString));
        }
        ScriptResult toPlayerResult = toPlayer.loadValue(context);
        String toPlayerString = toPlayerResult.getStringValue();
        if (!toPlayerString.isEmpty()) {
            context.getPlayer().sendMessage(TextHelper.translateAlternateColorCodes(toPlayerString));
        }
    }

    public ScriptBlock getToPlayer() {
        return toPlayer;
    }

    public String getToPlayerValue(ExecutionContext context) {
        return toPlayer.loadValue(context).getStringValue();
    }

    public void setToPlayer(ScriptBlock message) {
        toPlayer = message;
    }

    public ScriptBlock getFromPlayer() {
        return fromPlayer;
    }

    public String getFromPlayerValue(ExecutionContext context) {
        return fromPlayer.loadValue(context).getStringValue();
    }

    public void setFromPlayer(ScriptBlock message) {
        fromPlayer = message;
    }

}
