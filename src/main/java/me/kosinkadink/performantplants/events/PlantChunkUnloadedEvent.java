package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.chunks.PlantChunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantChunkUnloadedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlantChunk chunk;

    public PlantChunkUnloadedEvent(PlantChunk chunk) {
        this.chunk = chunk;
    }

    public PlantChunk getChunk() {
        return chunk;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
