package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.plants.Drop;

import java.util.ArrayList;

public class DropStorage {

    private int dropLimit = -1;
    private final ArrayList<Drop> drops = new ArrayList<>();

    public DropStorage() {}

    public int getDropLimit() {
        return dropLimit;
    }

    public void setDropLimit(int limit) {
        if (limit >= 0) {
            dropLimit = limit;
        }
    }

    public boolean isDropLimitSet() {
        return dropLimit >= 0;
    }

    public ArrayList<Drop> getDrops() {
        return drops;
    }

    public void addDrop(Drop drop) {
        drops.add(drop);
    }

}
