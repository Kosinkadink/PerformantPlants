package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.util.ScriptHelper;

import java.util.ArrayList;

public class DropStorage {

    private ScriptBlock dropLimit = null;
    private final ArrayList<Drop> drops = new ArrayList<>();

    public DropStorage() {}

    public ScriptBlock getDropLimit() {
        return dropLimit;
    }

    public int getDropLimitValue(ExecutionContext context) {
        return isDropLimitSet() ? dropLimit.loadValue(context).getIntegerValue() : -1;
    }

    public void setDropLimit(ScriptBlock dropLimit) {
        this.dropLimit = dropLimit;
    }

    public boolean isDropLimitSet() {
        return ScriptHelper.isNotNull(dropLimit);
    }

    public ArrayList<Drop> getDrops() {
        return drops;
    }

    public void addDrop(Drop drop) {
        drops.add(drop);
    }

}
