package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptRelativeLocation {

    ScriptBlock x = ScriptResult.ZERO;
    ScriptBlock y = ScriptResult.ZERO;
    ScriptBlock z = ScriptResult.ZERO;

    public ScriptRelativeLocation() {}

    public ScriptBlock getX() {
        return x;
    }

    public void setX(ScriptBlock x) {
        this.x = x;
    }

    public ScriptBlock getY() {
        return y;
    }

    public void setY(ScriptBlock y) {
        this.y = y;
    }

    public ScriptBlock getZ() {
        return z;
    }

    public void setZ(ScriptBlock z) {
        this.z = z;
    }

    public RelativeLocation getRelativeLocation(Player player, PlantBlock plantBlock) {
        return new RelativeLocation(
                x.loadValue(plantBlock, player).getIntegerValue(),
                y.loadValue(plantBlock, player).getIntegerValue(),
                z.loadValue(plantBlock, player).getIntegerValue()
        );
    }


}
