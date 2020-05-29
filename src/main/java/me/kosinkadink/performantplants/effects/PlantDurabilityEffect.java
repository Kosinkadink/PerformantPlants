package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.ItemHelper;
import org.bukkit.entity.Player;

public class PlantDurabilityEffect extends PlantEffect {

    private int amount = 0;

    public PlantDurabilityEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        ItemHelper.updateDamage(player.getInventory().getItemInMainHand(), amount);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
