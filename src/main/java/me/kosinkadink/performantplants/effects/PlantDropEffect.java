package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.DropHelper;

public class PlantDropEffect extends PlantEffect {

    private DropStorage dropStorage = new DropStorage();

    public PlantDropEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        DropHelper.performDrops(dropStorage, context.getPlayer().getLocation(), context);
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        DropHelper.performDrops(dropStorage, context.getPlantBlock().getBlock().getLocation(), context);
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }
}
