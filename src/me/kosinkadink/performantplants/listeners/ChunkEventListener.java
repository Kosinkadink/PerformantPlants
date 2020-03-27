package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkEventListener implements Listener {

    private Main main;

    public ChunkEventListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if player logged in at plant chunk
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            PlantChunk plantChunk = main.getPlantManager().getPlantChunk(event.getPlayer().getLocation().getChunk());
            if (plantChunk != null && !plantChunk.isLoaded()) {
                // load plantChunk
                plantChunk.load(main);
            }
        });
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // check if not new chunk
        if (!event.isNewChunk()) {
            main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
                // check if plant chunk
                PlantChunk plantChunk = main.getPlantManager().getPlantChunk(event.getChunk());
                if (plantChunk != null) {
                    // load plantChunk
                    plantChunk.load(main);
                }
            });
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            // get plantChunk
            PlantChunk plantChunk = main.getPlantManager().getPlantChunk(event.getChunk());
            if (plantChunk != null) {
                // unload plantChunk
                plantChunk.unload(main);
            }
        });
    }

}
