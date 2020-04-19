package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantFeedEffect implements PlantEffect {

    private int foodAmount = 0;
    private float saturationAmount = 0;

    public PlantFeedEffect(int foodAmount, float saturationAmount) {
        this.foodAmount = foodAmount;
        this.saturationAmount = saturationAmount;
    }

    @Override
    public void performEffect(Player player, Location location) {
        int newFoodLevel = Math.min(20, player.getFoodLevel() + foodAmount);
        float newSaturationLevel = Math.min(newFoodLevel, player.getSaturation() + saturationAmount);
        player.setFoodLevel(newFoodLevel);
        player.setSaturation(newSaturationLevel);
    }

    @Override
    public void performEffect(Block block) { }

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
