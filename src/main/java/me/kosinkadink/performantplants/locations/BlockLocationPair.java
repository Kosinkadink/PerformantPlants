package me.kosinkadink.performantplants.locations;

import java.util.Objects;

public class BlockLocationPair {

    private final BlockLocation location1;
    private final BlockLocation location2;

    public BlockLocationPair(BlockLocation location1, BlockLocation location2) {
        this.location1 = location1;
        this.location2 = location2;
    }


    public BlockLocation getLocation1() {
        return location1;
    }

    public BlockLocation getLocation2() {
        return location2;
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        BlockLocationPair fromO = (BlockLocationPair)o;
        // true if all components match, false otherwise
        return location1.equals(fromO.location1) && location2.equals(fromO.location2);
    }

    public int hashCode() {
        return Objects.hash(location1, location2);
    }

    @Override
    public String toString() {
        return location1.toString() + "+" + location2.toString();
    }
}
