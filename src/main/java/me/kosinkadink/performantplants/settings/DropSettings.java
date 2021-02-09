package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class DropSettings {

    private ScriptBlock amount = new ScriptResult(1);
    private ScriptBlock condition = ScriptResult.TRUE;
    private ItemSettings itemSettings;

    public DropSettings() {}


    public ScriptBlock getAmount() {
        return amount;
    }

    public void setAmount(ScriptBlock amount) {
        this.amount = amount;
    }

    public ScriptBlock getCondition() {
        return condition;
    }

    public void setCondition(ScriptBlock condition) {
        this.condition = condition;
    }

    public ItemSettings getItemSettings() {
        return itemSettings;
    }

    public void setItemSettings(ItemSettings itemSettings) {
        this.itemSettings = itemSettings;
    }
}
