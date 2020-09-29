package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class PlantInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Player player;
    private final PlantBlock plantBlock;
    private final Block block;
    private final BlockFace blockFace;
    private final EquipmentSlot hand;
    private boolean useOnClick = false;

    public PlantInteractEvent(Player player, PlantBlock plantBlock, Block block, BlockFace blockFace, EquipmentSlot hand) {
        this.player = player;
        this.plantBlock = plantBlock;
        this.block = block;
        this.blockFace = blockFace;
        this.hand = hand;
    }

    public PlantInteractEvent(Player player, PlantBlock plantBlock, Block block, BlockFace blockFace, EquipmentSlot hand, boolean useOnClick) {
        this(player, plantBlock, block, blockFace, hand);
        this.useOnClick = useOnClick;
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

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public boolean isUseOnClick() {
        return useOnClick;
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
