package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public class ScriptColor {

    ScriptBlock red = ScriptResult.ZERO;
    ScriptBlock green = ScriptResult.ZERO;
    ScriptBlock blue = ScriptResult.ZERO;

    public ScriptColor() {}

    public ScriptBlock getRed() {
        return red;
    }

    public void setRed(ScriptBlock red) {
        this.red = red;
    }

    public ScriptBlock getGreen() {
        return green;
    }

    public void setGreen(ScriptBlock green) {
        this.green = green;
    }

    public ScriptBlock getBlue() {
        return blue;
    }

    public void setBlue(ScriptBlock blue) {
        this.blue = blue;
    }

    public Color getColor(Player player, PlantBlock plantBlock) {
        return Color.fromRGB(
                Math.min(255, Math.max(0, red.loadValue(plantBlock, player).getIntegerValue())),
                Math.min(255, Math.max(0, green.loadValue(plantBlock, player).getIntegerValue())),
                Math.min(255, Math.max(0, blue.loadValue(plantBlock, player).getIntegerValue()))
        );
    }

}
