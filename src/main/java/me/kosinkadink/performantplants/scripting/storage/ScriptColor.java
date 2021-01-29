package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Color;

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

    public Color getColor(ExecutionContext context) {
        return Color.fromRGB(
                Math.min(255, Math.max(0, red.loadValue(context).getIntegerValue())),
                Math.min(255, Math.max(0, green.loadValue(context).getIntegerValue())),
                Math.min(255, Math.max(0, blue.loadValue(context).getIntegerValue()))
        );
    }

}
