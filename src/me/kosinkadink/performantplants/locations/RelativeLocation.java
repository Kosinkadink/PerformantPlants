package me.kosinkadink.performantplants.locations;

public class RelativeLocation {

    private final int x;
    private final int y;
    private final int z;

    public RelativeLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        RelativeLocation fromO = (RelativeLocation)o;
        // true if all components match, false otherwise
        return x == fromO.x && y == fromO.y && z == fromO.z;
    }

}
