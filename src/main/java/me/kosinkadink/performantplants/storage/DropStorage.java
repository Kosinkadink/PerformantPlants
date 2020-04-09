package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.plants.Drop;

import java.util.ArrayList;

public class DropStorage implements Droppable {

    private int dropLimit = -1;
    private ArrayList<Drop> drops = new ArrayList<>();

    public DropStorage() {}

    @Override
    public int getDropLimit() {
        return dropLimit;
    }

    @Override
    public void setDropLimit(int limit) {
        if (limit >= 0) {
            dropLimit = limit;
        }
    }

    @Override
    public ArrayList<Drop> getDrops() {
        return drops;
    }

    @Override
    public void addDrop(Drop drop) {
        drops.add(drop);
    }

}
