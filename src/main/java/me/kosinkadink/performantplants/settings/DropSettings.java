package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class DropSettings {

    private ScriptBlock minAmount = new ScriptResult(1);
    private ScriptBlock maxAmount = new ScriptResult(1);
    private ScriptBlock doIf = ScriptResult.TRUE;
    private ItemSettings itemSettings;

    public DropSettings() {}


    public ScriptBlock getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(ScriptBlock minAmount) {
        this.minAmount = minAmount;
    }

    public ScriptBlock getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(ScriptBlock maxAmount) {
        this.maxAmount = maxAmount;
    }

    public ScriptBlock getDoIf() {
        return doIf;
    }

    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public ItemSettings getItemSettings() {
        return itemSettings;
    }

    public void setItemSettings(ItemSettings itemSettings) {
        this.itemSettings = itemSettings;
    }
}
