package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.plants.PlantConsumable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantConsumeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private Player player;
    private PlantConsumable consumable;
    private EquipmentSlot hand;

    public PlantConsumeEvent(Player player, PlantConsumable consumable, EquipmentSlot hand) {
        this.player = player;
        this.consumable = consumable;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public PlantConsumable getConsumable() {
        return consumable;
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
