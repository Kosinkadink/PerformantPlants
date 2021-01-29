package me.kosinkadink.performantplants.scripting.storage;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

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

    public RelativeLocation getRelativeLocation(ExecutionContext context) {
        return new RelativeLocation(
                x.loadValue(context).getIntegerValue(),
                y.loadValue(context).getIntegerValue(),
                z.loadValue(context).getIntegerValue()
        );
    }


}
