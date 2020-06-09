package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.ItemHelper;
import org.bukkit.entity.Player;

public class PlantDurabilityEffect extends PlantEffect {

    private ScriptBlock amount = ScriptResult.ZERO;

    public PlantDurabilityEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        ItemHelper.updateDamage(player.getInventory().getItemInMainHand(), getAmountValue(player, plantBlock));
    }

    public ScriptBlock getAmount() {
        return amount;
    }

    public int getAmountValue(Player player, PlantBlock plantBlock) {
        return amount.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setAmount(ScriptBlock amount) {
        this.amount = amount;
    }

}
