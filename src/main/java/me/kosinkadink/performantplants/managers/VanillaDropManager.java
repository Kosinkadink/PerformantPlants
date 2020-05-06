package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ConcurrentHashMap;

public class VanillaDropManager {

    private Main main;

    private ConcurrentHashMap<Material, PlantInteractStorage> blockDropInteractMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<EntityType, PlantInteractStorage> entityDropInteractMap = new ConcurrentHashMap<>();

    public VanillaDropManager(Main main) {
        this.main = main;
    }

    public PlantInteractStorage getInteract(Block block) {
        return blockDropInteractMap.get(block.getType());
    }

    public PlantInteractStorage getInteract(EntityType type) {
        return entityDropInteractMap.get(type);
    }

    public void addInteract(Material material, PlantInteractStorage storage) {
        blockDropInteractMap.put(material, storage);
    }

    public void addInteract(EntityType type, PlantInteractStorage storage) {
        entityDropInteractMap.put(type, storage);
    }

    public void removeInteract(Material material) {
        blockDropInteractMap.remove(material);
    }

    public void removeInteract(EntityType type) {
        entityDropInteractMap.remove(type);
    }

}
