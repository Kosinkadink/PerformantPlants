package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkEventListener implements Listener {

    private PerformantPlants performantPlants;

    public ChunkEventListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if player logged in at plant chunk
        performantPlants.getServer().getScheduler().runTaskLaterAsynchronously(performantPlants, () -> {
            PlantChunk plantChunk = performantPlants.getPlantManager().getPlantChunk(event.getPlayer().getLocation().getChunk());
            if (plantChunk != null) {
                // load plantChunk
                plantChunk.load(performantPlants);
            }
        }, 5);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // check if not new chunk
        if (!event.isNewChunk()) {
            performantPlants.getServer().getScheduler().runTaskAsynchronously(performantPlants, () -> {
                // check if plant chunk
                PlantChunk plantChunk = performantPlants.getPlantManager().getPlantChunk(event.getChunk());
                if (plantChunk != null) {
                    // load plantChunk
                    plantChunk.load(performantPlants);
                }
            });
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        performantPlants.getServer().getScheduler().runTaskAsynchronously(performantPlants, () -> {
            // get plantChunk
            PlantChunk plantChunk = performantPlants.getPlantManager().getPlantChunk(event.getChunk());
            if (plantChunk != null) {
                // unload plantChunk
                plantChunk.unload(performantPlants);
            }
        });
    }

}
