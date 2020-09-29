package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Player player;
    private final PlantBlock plantBlock;
    private final Block block;
    private boolean blockBroken = false;

    public PlantBreakEvent(Player player, PlantBlock plantBlock, Block block) {
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

    public boolean isBlockBroken() {
        return blockBroken;
    }

    public void setBlockBroken(boolean blockBroken) {
        this.blockBroken = blockBroken;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
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
