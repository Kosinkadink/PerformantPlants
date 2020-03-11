package me.kosinkadink.performantplants.locations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class BlockLocation {

    private final int x;
    private final int y;
    private final int z;
    private final String world;

    public BlockLocation(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    /**
     * @param location
     * Create BlockLocation from Location object
     */
    public BlockLocation(Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        world = Objects.requireNonNull(location.getWorld()).getName();
    }

    /**
     * @param block
     * Create BlockLocation from block
     */
    public BlockLocation(Block block) {
        this(block.getLocation());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

    public Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        BlockLocation fromO = (BlockLocation)o;
        // true if all components match, false otherwise
        return x == fromO.x && y == fromO.y && z == fromO.z && world.equals(fromO.world);
    }

    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z + "," + world;
    }
}
