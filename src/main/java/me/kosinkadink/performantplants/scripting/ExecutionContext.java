package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ExecutionContext {

    private ExecutionWrapper wrapper = new ExecutionWrapper();

    private Player player = null;
    private PlantBlock plantBlock = null;
    private PlantData plantData = null;
    private ItemStack itemStack = null;
    private EquipmentSlot equipmentSlot = null;
    private Location location = null;

    public ExecutionContext() {

    }

    public ExecutionContext copy() {
        return new ExecutionContext()
                .set(wrapper)
                .set(player)
                .set(plantBlock)
                .set(itemStack)
                .set(equipmentSlot)
                .set(location);
    }

    //region Wrapper
    public ExecutionWrapper getWrapper() {
        return wrapper;
    }

    public ExecutionContext setWrapper(ExecutionWrapper wrapper) {
        if (wrapper != null) {
            this.wrapper = wrapper;
        }
        return this;
    }

    public ExecutionContext set(ExecutionWrapper wrapper) {
        return setWrapper(wrapper);
    }
    //endregion

    //region Player
    public Player getPlayer() {
        return player;
    }

    public boolean isPlayerSet() {
        return player != null;
    }

    public ExecutionContext setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public ExecutionContext set(Player player) {
        return setPlayer(player);
    }
    //endregion

    //region PlantBlock
    public PlantBlock getPlantBlock() {
        return plantBlock;
    }

    public PlantBlock getEffectivePlantBlock() {
        if (isPlantBlockSet()) {
            return plantBlock.getEffectivePlantBlock();
        }
        return plantBlock;
    }

    public boolean isPlantBlockSet() {
        return plantBlock != null;
    }

    public ExecutionContext setPlantBlock(PlantBlock plantBlock) {
        this.plantBlock = plantBlock;
        return this;
    }

    public ExecutionContext set(PlantBlock plantBlock) {
        return setPlantBlock(plantBlock);
    }
    //endregion

    //region PlantData
    public PlantData getPlantData() {
        // if plant data set directly, return that
        if (plantData != null) {
            return plantData;
        }
        // otherwise return effective plant data from block
        else if (plantBlock != null) {
            return plantBlock.getEffectivePlantData();
        }
        return null;
    }

    public boolean isPlantDataSet() {
        return plantData != null;
    }

    public boolean isPlantDataPossible() {
        return plantData != null || plantBlock != null;
    }

    public ExecutionContext setPlantData(PlantData data) {
        plantData = data;
        return this;
    }

    public ExecutionContext set(PlantData data) {
        return setPlantData(data);
    }
    //endregion

    //region ItemStack
    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isItemStackSet() {
        return itemStack != null;
    }

    public ExecutionContext setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public ExecutionContext set(ItemStack itemStack) {
        return setItemStack(itemStack);
    }
    //endregion

    //region EquipmentSlot
    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public boolean isEquipmentSlotSet() {
        return equipmentSlot != null;
    }

    public ExecutionContext setEquipmentSlot(EquipmentSlot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
        return this;
    }

    public ExecutionContext set(EquipmentSlot equipmentSlot) {
        return setEquipmentSlot(equipmentSlot);
    }
    //endregion

    //region Location
    public Location getLocation() {
        if (location != null) {
            return location;
        }
        if (isPlayerSet()) {
            return player.getLocation();
        }
        if (isPlantBlockSet()) {
            return plantBlock.getBlock().getLocation();
        }
        return null;
    }

    public boolean isLocationSet() {
        return location != null;
    }

    public boolean isLocationPossible() {
        return getLocation() != null;
    }

    public ExecutionContext setLocation(Location location) {
        this.location = location;
        return this;
    }

    public ExecutionContext set(Location location) {
        return setLocation(location);
    }
    //endregion
}
