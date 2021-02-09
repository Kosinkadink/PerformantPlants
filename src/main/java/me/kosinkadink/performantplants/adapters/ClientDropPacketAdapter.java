package me.kosinkadink.performantplants.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ClientDropPacketAdapter extends PacketAdapter {

    PerformantPlants performantPlants;

    protected ClientDropPacketAdapter(PerformantPlants plugin) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        performantPlants = plugin;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer container = event.getPacket();
        // check if is drop item packet
        switch(container.getPlayerDigTypes().getValues().get(0)) {
            case DROP_ITEM:
                if (event.getPlayer() != null) {
                    Player player = event.getPlayer();
                    // get itemstack in main hand
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    // if not air, continue
                    if (!itemStack.getType().isAir()) {
                        // get plant item
                        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                        // if plant item not null and has on-drop, continue
                        if (plantItem != null && plantItem.hasOnDrop()) {
                            // create context
                            ExecutionContext context = new ExecutionContext()
                                    .set(player)
                                    .set(itemStack)
                                    .set(EquipmentSlot.HAND);
                            // try to perform script
                            boolean performed = plantItem.getOnDrop().loadValue(context).getBooleanValue();
                            // if returned true, cancel item drop
                            if (performed) {
                                event.setCancelled(true);
                                performantPlants.getPlayerInteractListener().setDropAction(player);
                                performantPlants.getServer().getScheduler().runTask(performantPlants, () -> {
                                    performantPlants.getPlayerInteractListener().resetDropAction(player);
                                });
                                // update appearance to look right client-side
                                player.getInventory().setItemInMainHand(player.getInventory().getItemInMainHand());
                            } else {
                                performantPlants.getPlayerInteractListener().resetDropAction(player);
                            }
                        }
                    }
                }
                break;
            case DROP_ALL_ITEMS:
                if (event.getPlayer() != null) {
                    Player player = event.getPlayer();
                    // get itemstack in main hand
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    // if not air, continue
                    if (!itemStack.getType().isAir()) {
                        // get plant item
                        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                        // if plant item not null and has on-drop, continue
                        if (plantItem != null && plantItem.hasOnDropAll()) {
                            // create context
                            ExecutionContext context = new ExecutionContext()
                                    .set(player)
                                    .set(itemStack)
                                    .set(EquipmentSlot.HAND);
                            // try to perform script
                            boolean performed = plantItem.getOnDropAll().loadValue(context).getBooleanValue();
                            // if returned true, cancel item drop
                            if (performed) {
                                event.setCancelled(true);
                                performantPlants.getPlayerInteractListener().setDropAction(player);
                                performantPlants.getServer().getScheduler().runTask(performantPlants, () -> {
                                    performantPlants.getPlayerInteractListener().resetDropAction(player);
                                });
                                // update appearance to look right client-side
                                player.getInventory().setItemInMainHand(player.getInventory().getItemInMainHand());
                            } else {
                                performantPlants.getPlayerInteractListener().resetDropAction(player);
                            }
                        }
                    }
                }
                break;
        }
    }

    public static void register(PerformantPlants plugin, ProtocolManager manager) {
        manager.addPacketListener(new ClientDropPacketAdapter(plugin));
    }
}
