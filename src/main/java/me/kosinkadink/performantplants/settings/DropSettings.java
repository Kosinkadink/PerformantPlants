package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class DropSettings {

    private ScriptBlock amount = new ScriptResult(1);
    private ScriptBlock doIf = ScriptResult.TRUE;
    private ItemSettings itemSettings;

    public DropSettings() {}


    public ScriptBlock getAmount() {
        return amount;
    }

    public void setAmount(ScriptBlock amount) {
        this.amount = amount;
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
