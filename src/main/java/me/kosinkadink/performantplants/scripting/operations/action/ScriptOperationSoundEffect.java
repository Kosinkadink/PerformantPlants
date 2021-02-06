package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.EnumHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.Location;
import org.bukkit.Sound;

import javax.annotation.Nonnull;

public class ScriptOperationSoundEffect extends ScriptOperationAction {

    public ScriptOperationSoundEffect(ScriptBlock sound, ScriptBlock volume, ScriptBlock pitch,
                                      ScriptBlock offsetX, ScriptBlock offsetY, ScriptBlock offsetZ,
                                      ScriptBlock multiplier, ScriptBlock ignoreDirectionY, ScriptBlock clientside) {
        super(sound, volume, pitch, offsetX, offsetY, offsetZ, multiplier, ignoreDirectionY, clientside);
    }

    public ScriptBlock getSound() {
        return inputs[0];
    }

    public ScriptBlock getVolume() {
        return inputs[1];
    }

    public ScriptBlock getPitch() {
        return inputs[2];
    }

    public ScriptBlock getOffsetX() {
        return inputs[3];
    }

    public ScriptBlock getOffsetY() {
        return inputs[4];
    }

    public ScriptBlock getOffsetZ() {
        return inputs[5];
    }

    public ScriptBlock getMultiplier() {
        return inputs[6];
    }

    public ScriptBlock getIgnoreDirectionY() {
        return inputs[7];
    }

    public ScriptBlock getClientside() {
        return inputs[8];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isLocationPossible()) {
            // set sound
            Sound sound = EnumHelper.getSound(getSound().loadValue(context).getStringValue());
            if (sound == null) {
                return ScriptResult.FALSE;
            }
            Location location = context.getLocation();
            // add offsets
            location.add(
                    getOffsetX().loadValue(context).getDoubleValue(),
                    getOffsetY().loadValue(context).getDoubleValue(),
                    getOffsetZ().loadValue(context).getDoubleValue()
            );
            // worry about multiplier stuff if player is set
            if (context.isPlayerSet()) {
                // ignore direction y with multiplier, if set
                double multiplier = getMultiplier().loadValue(context).getDoubleValue();
                boolean ignoreDirectionY = getIgnoreDirectionY().loadValue(context).getBooleanValue();
                if (ignoreDirectionY) {
                    location.add(location.getDirection().setY(0).normalize().multiply(multiplier));
                } else {
                    location.add(location.getDirection().normalize().multiply(multiplier));
                }
            }
            // only worry about clientside if player is set
            if (context.isPlayerSet() && getClientside().loadValue(context).getBooleanValue()) {
                context.getPlayer().playSound(location, sound, getVolume().loadValue(context).getFloatValue(),
                        getPitch().loadValue(context).getFloatValue());
                return ScriptResult.TRUE;
            }
            // otherwise try to play sound in world
            try {
                if (location.getWorld() != null) {
                    location.getWorld().playSound(location, sound, getVolume().loadValue(context).getFloatValue(),
                            getPitch().loadValue(context).getFloatValue());
                    return ScriptResult.TRUE;
                }
            } catch (IllegalArgumentException e) {
                // do nothing, just make sure no runtime exception is thrown
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isString(getSound())) {
            throw new IllegalArgumentException(String.format("sound must be ScriptType STRING, not %s",
                    getSound().getType()));
        }
        if (!ScriptHelper.isNumeric(getVolume())) {
            throw new IllegalArgumentException(String.format("volume must be ScriptType LONG or DOUBLE, not %s",
                    getVolume().getType()));
        }
        if (!ScriptHelper.isNumeric(getPitch())) {
            throw new IllegalArgumentException(String.format("pitch must be ScriptType LONG or DOUBLE, not %s",
                    getPitch().getType()));
        }
        if (!ScriptHelper.isNumeric(getOffsetX())) {
            throw new IllegalArgumentException(String.format("offset-x must be ScriptType LONG or DOUBLE, not %s",
                    getOffsetX().getType()));
        }
        if (!ScriptHelper.isNumeric(getOffsetY())) {
            throw new IllegalArgumentException(String.format("offset-y must be ScriptType LONG or DOUBLE, not %s",
                    getOffsetY().getType()));
        }
        if (!ScriptHelper.isNumeric(getOffsetZ())) {
            throw new IllegalArgumentException(String.format("offset-z must be ScriptType LONG or DOUBLE, not %s",
                    getOffsetZ().getType()));
        }
        if (!ScriptHelper.isNumeric(getMultiplier())) {
            throw new IllegalArgumentException(String.format("multiplier must be ScriptType LONG or DOUBLE, not %s",
                    getMultiplier().getType()));
        }
        if (!ScriptHelper.isBoolean(getIgnoreDirectionY())) {
            throw new IllegalArgumentException(String.format("ignore-direction-y must be ScriptType BOOLEAN, not %s",
                    getIgnoreDirectionY().getType()));
        }
        if (!ScriptHelper.isBoolean(getClientside())) {
            throw new IllegalArgumentException(String.format("clientside must be ScriptType BOOLEAN, not %s",
                    getClientside().getType()));
        }
    }
}
