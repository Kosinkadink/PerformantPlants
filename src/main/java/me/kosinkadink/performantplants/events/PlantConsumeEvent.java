package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantConsumeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Player player;
    private final ScriptBlock consumable;
    private final EquipmentSlot hand;
    private boolean eaten = false;

    public PlantConsumeEvent(Player player, ScriptBlock consumable, EquipmentSlot hand) {
        this.player = player;
        this.consumable = consumable;
        this.hand = hand;
    }

    public PlantConsumeEvent(Player player, ScriptBlock consumable, EquipmentSlot hand, boolean eaten) {
        this(player, consumable, hand);
        this.eaten = eaten;
    }

    public Player getPlayer() {
        return player;
    }

    public ScriptBlock getConsumable() {
        return consumable;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public boolean isEaten() {
        return eaten;
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
