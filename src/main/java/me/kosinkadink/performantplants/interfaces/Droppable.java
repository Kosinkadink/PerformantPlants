package me.kosinkadink.performantplants.interfaces;

import me.kosinkadink.performantplants.plants.Drop;

import java.util.ArrayList;

public interface Droppable {

    int getDropLimit();
    void setDropLimit(int limit);

    ArrayList<Drop> getDrops();
    void addDrop(Drop drop);

}
