package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.DropHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantDropEffect extends PlantEffect {

    private DropStorage dropStorage = new DropStorage();

    public PlantDropEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        DropHelper.performDrops(dropStorage, player.getLocation(), player, plantBlock);
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        DropHelper.performDrops(dropStorage, block, null, plantBlock);
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }
}
