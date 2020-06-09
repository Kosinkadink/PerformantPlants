package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class PlantFeedEffect extends PlantEffect {

    private ScriptBlock foodAmount = ScriptResult.ZERO;
    private ScriptBlock saturationAmount = ScriptResult.ZERO;

    public PlantFeedEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        int newFoodLevel = Math.max(0, Math.min(20, player.getFoodLevel() + getFoodAmountValue(player, plantBlock)));
        float newSaturationLevel = Math.max(0, Math.min(newFoodLevel, player.getSaturation() + getSaturationAmountValue(player, plantBlock)));
        player.setFoodLevel(newFoodLevel);
        player.setSaturation(newSaturationLevel);
    }

    public ScriptBlock getFoodAmount() {
        return foodAmount;
    }

    public int getFoodAmountValue(Player player, PlantBlock plantBlock) {
        return foodAmount.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setFoodAmount(ScriptBlock foodAmount) {
        this.foodAmount = foodAmount;
    }

    public ScriptBlock getSaturationAmount() {
        return saturationAmount;
    }

    public float getSaturationAmountValue(Player player, PlantBlock plantBlock) {
        return saturationAmount.loadValue(plantBlock, player).getFloatValue();
    }

    public void setSaturationAmount(ScriptBlock saturationAmount) {
        this.saturationAmount = saturationAmount;
    }

}
