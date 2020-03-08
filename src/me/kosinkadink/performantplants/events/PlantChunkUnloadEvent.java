package me.kosinkadink.performantplants.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantChunkUnloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
