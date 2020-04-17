package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private Player player;
    private PlantBlock plantBlock;
    private Block block;
    private EquipmentSlot hand;

    public PlantInteractEvent(Player player, PlantBlock plantBlock, Block block, EquipmentSlot hand) {
        this.player = player;
        this.plantBlock = plantBlock;
        this.block = block;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public PlantBlock getPlantBlock() {
        return plantBlock;
    }

    public Block getBlock() {
        return block;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
