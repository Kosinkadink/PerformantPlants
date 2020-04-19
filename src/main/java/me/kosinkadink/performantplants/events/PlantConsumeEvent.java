package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.plants.PlantItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantConsumeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private Player player;
    private PlantItem plantItem;
    private EquipmentSlot hand;

    public PlantConsumeEvent(Player player, PlantItem plantItem, EquipmentSlot hand) {
        this.player = player;
        this.plantItem = plantItem;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public PlantItem getPlantItem() {
        return plantItem;
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
