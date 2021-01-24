package me.kosinkadink.performantplants.locations;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class ChunkLocation {

    private final int x;
    private final int z;
    private final String world;

    /**
     * @param x: x chunk value
     * @param z: z chunk value
     * @param world
     * Create ChunkLocation from x, z, and world name
     */
    public ChunkLocation(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    /**
     * @param chunk
     * Create ChunkLocation from Chunk object
     */
    public ChunkLocation(Chunk chunk) {
        x = chunk.getX();
        z = chunk.getZ();
        world = chunk.getWorld().getName();
    }

    /**
     * @param location
     * Create ChunkLocation from Location object
     */
    public ChunkLocation(Location location) {
        // if getWorld not null, use word name, otherwise use empty string
        this(location.getBlockX() >> 4, location.getBlockZ() >> 4,
                location.getWorld() != null ? location.getWorld().getName() : "");
    }

    /**
     * @param block
     * Create ChunkLocation from Block object
     */
    public ChunkLocation(Block block) {
        this(block.getLocation());
    }

    /**
     * @param blockLocation
     * Create ChunkLocation from BlockLocation object
     */
    public ChunkLocation(BlockLocation blockLocation) {
        this(blockLocation.getX() >> 4, blockLocation.getZ() >> 4, blockLocation.getWorldName());
    }

    /**
     * @param plantBlock
     * Create ChunkLocation from PlantBlock object
     */
    public ChunkLocation(PlantBlock plantBlock) {
        this(plantBlock.getLocation());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getWorldName() {
        return world;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Chunk getChunk() {
        return getWorld().getChunkAt(x, z);
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        ChunkLocation fromO = (ChunkLocation)o;
        // true if all components match, false otherwise
        return x == fromO.x && z == fromO.z && world.equals(fromO.world);
    }

    public int hashCode() {
        return Objects.hash(x, z, world);
    }

    @Override
    public String toString() {
        return x + "," + z + "," + world;
    }
}
