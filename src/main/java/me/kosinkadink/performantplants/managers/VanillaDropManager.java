package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.DropStorage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ConcurrentHashMap;

public class VanillaDropManager {

    private Main main;

    private ConcurrentHashMap<Material, DropStorage> blockDropMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<EntityType, DropStorage> entityDropMap = new ConcurrentHashMap<>();

    public VanillaDropManager(Main main) {
        this.main = main;
    }

    public DropStorage getDropStorage(Block block) {
        return blockDropMap.get(block.getType());
    }

    public DropStorage getDropStorage(EntityType type) {
        return entityDropMap.get(type);
    }

    public void addDropStorage(Material material, DropStorage storage) {
        blockDropMap.put(material, storage);
    }

    public void addDropStorage(EntityType type, DropStorage storage) {
        entityDropMap.put(type, storage);
    }

    public void removeDropStorage(Material material) {
        blockDropMap.remove(material);
    }

    public void removeDropStorage(EntityType type) {
        entityDropMap.remove(type);
    }

}
