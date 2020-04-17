package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantFarmlandTrampleEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private Player player;
    private PlantBlock plantBlock;
    private Block block;
    private Block trampledBlock;

    public PlantFarmlandTrampleEvent(Player player, PlantBlock plantBlock, Block block, Block trampledBlock) {
        this.player = player;
        this.plantBlock = plantBlock;
        this.block = block;
        this.trampledBlock = trampledBlock;
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

    public Block getTrampledBlock() {
        return trampledBlock;
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
