package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class PlantFeedEffect extends PlantEffect {

    private ScriptBlock foodAmount = ScriptResult.ZERO;
    private ScriptBlock saturationAmount = ScriptResult.ZERO;

    public PlantFeedEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        Player player = context.getPlayer();
        int newFoodLevel = Math.max(0, Math.min(20, player.getFoodLevel() + getFoodAmountValue(context)));
        float newSaturationLevel = Math.max(0, Math.min(newFoodLevel, player.getSaturation() + getSaturationAmountValue(context)));
        player.setFoodLevel(newFoodLevel);
        player.setSaturation(newSaturationLevel);
    }

    public ScriptBlock getFoodAmount() {
        return foodAmount;
    }

    public int getFoodAmountValue(ExecutionContext context) {
        return foodAmount.loadValue(context).getIntegerValue();
    }

    public void setFoodAmount(ScriptBlock foodAmount) {
        this.foodAmount = foodAmount;
    }

    public ScriptBlock getSaturationAmount() {
        return saturationAmount;
    }

    public float getSaturationAmountValue(ExecutionContext context) {
        return saturationAmount.loadValue(context).getFloatValue();
    }

    public void setSaturationAmount(ScriptBlock saturationAmount) {
        this.saturationAmount = saturationAmount;
    }

}
