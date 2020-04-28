package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.DropHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantDropEffect extends PlantEffect {

    private DropStorage dropStorage = new DropStorage();

    public PlantDropEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        DropHelper.performDrops(dropStorage, player.getLocation());
    }

    @Override
    void performEffectAction(Block block) {
        DropHelper.performDrops(dropStorage, block);
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }
}
