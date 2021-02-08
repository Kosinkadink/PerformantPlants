package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ConcurrentHashMap;

public class VanillaDropManager {

    private PerformantPlants performantPlants;

    private ConcurrentHashMap<Material, ScriptBlock> blockDropInteractMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<EntityType, ScriptBlock> entityDropInteractMap = new ConcurrentHashMap<>();

    public VanillaDropManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public ScriptBlock getInteract(Block block) {
        return blockDropInteractMap.get(block.getType());
    }

    public ScriptBlock getInteract(EntityType type) {
        return entityDropInteractMap.get(type);
    }

    public void addInteract(Material material, ScriptBlock storage) {
        blockDropInteractMap.put(material, storage);
    }

    public void addInteract(EntityType type, ScriptBlock storage) {
        entityDropInteractMap.put(type, storage);
    }

    public void removeInteract(Material material) {
        blockDropInteractMap.remove(material);
    }

    public void removeInteract(EntityType type) {
        entityDropInteractMap.remove(type);
    }

}
