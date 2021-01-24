package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantBrokenEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlantBlock plantBlock;
    private final Block block;

    public PlantBrokenEvent(Player player, PlantBlock plantBlock, Block block) {
        this.player = player;
        this.plantBlock = plantBlock;
        this.block = block;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
