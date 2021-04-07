package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Player player;
    private final Plant plant;
    private final Block block;
    private final Block blockAgainst;
    private final BlockState replacedState;
    private final EquipmentSlot hand;
    private final boolean grows;

    public PlantPlaceEvent(Player player, Plant plant, Block block, Block blockAgainst, EquipmentSlot hand, boolean grows) {
        this.player = player;
        this.plant = plant;
        this.block = block;
        this.replacedState = null;
        this.blockAgainst = blockAgainst;
        this.hand = hand;
        this.grows = grows;
    }

    public PlantPlaceEvent(Player player, Plant plant, Block block, BlockState replacedState, Block blockAgainst, EquipmentSlot hand, boolean grows) {
        this.player = player;
        this.plant = plant;
        this.block = block;
        this.replacedState = replacedState;
        this.blockAgainst = blockAgainst;
        this.hand = hand;
        this.grows = grows;
    }

    public Player getPlayer() {
        return player;
    }

    public Plant getPlant() {
        return plant;
    }

    public Block getBlock() {
        return block;
    }

    public BlockState getReplacedState() {
        return replacedState;
    }

    public Block getBlockAgainst() {
        return blockAgainst;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public boolean getGrows() {
        return grows;
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
