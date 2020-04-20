package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.util.RandomHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlantFeedEffect extends PlantEffect {

    private int foodAmount = 0;
    private float saturationAmount = 0;

    public PlantFeedEffect() { }

    public PlantFeedEffect(int foodAmount, float saturationAmount) {
        this.foodAmount = foodAmount;
        this.saturationAmount = saturationAmount;
    }

    @Override
    void performEffectAction(Player player, Location location) {
        int newFoodLevel = Math.min(20, player.getFoodLevel() + foodAmount);
        float newSaturationLevel = Math.min(newFoodLevel, player.getSaturation() + saturationAmount);
        player.setFoodLevel(newFoodLevel);
        player.setSaturation(newSaturationLevel);
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(int foodAmount) {
        this.foodAmount = Math.min(20, Math.max(0, foodAmount));
    }

    public float getSaturationAmount() {
        return saturationAmount;
    }

    public void setSaturationAmount(float saturationAmount) {
        this.saturationAmount = Math.min(20, Math.max(0, saturationAmount));
    }

}
