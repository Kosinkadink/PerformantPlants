package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.EnumHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.Location;
import org.bukkit.Particle;

import javax.annotation.Nonnull;

public class ScriptOperationParticleEffect extends ScriptOperationAction {

    public ScriptOperationParticleEffect(ScriptBlock particle, ScriptBlock count,
                                         ScriptBlock offsetX, ScriptBlock offsetY, ScriptBlock offsetZ,
                                         ScriptBlock dataOffsetX, ScriptBlock dataOffsetY, ScriptBlock dataOffsetZ,
                                         ScriptBlock extra, ScriptBlock multiplier,
                                         ScriptBlock ignoreDirectionY, ScriptBlock clientside) {
        super(particle, count, offsetX, offsetY, offsetZ,
                dataOffsetX, dataOffsetY, dataOffsetZ,
                extra, multiplier,
                ignoreDirectionY, clientside);
    }

    public ScriptBlock getParticle() {
        return inputs[0];
    }

    public ScriptBlock getCount() {
        return inputs[1];
    }

    public ScriptBlock getOffsetX() {
        return inputs[2];
    }

    public ScriptBlock getOffsetY() {
        return inputs[3];
    }

    public ScriptBlock getOffsetZ() {
        return inputs[4];
    }

    public ScriptBlock getDataOffsetX() {
        return inputs[5];
    }

    public ScriptBlock getDataOffsetY() {
        return inputs[6];
    }

    public ScriptBlock getDataOffsetZ() {
        return inputs[7];
    }

    public ScriptBlock getExtra() {
        return inputs[8];
    }

    public ScriptBlock getMultiplier() {
        return inputs[9];
    }

    public ScriptBlock getIgnoreDirectionY() {
        return inputs[10];
    }

    public ScriptBlock getClientside() {
        return inputs[11];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isLocationPossible()) {
            // set particle
            Particle particle = EnumHelper.getParticle(getParticle().loadValue(context).getStringValue());
            if (particle == null) {
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
            int count = getCount().loadValue(context).getIntegerValue();
            double dataOffsetX = getDataOffsetX().loadValue(context).getDoubleValue();
            double dataOffsetY = getDataOffsetY().loadValue(context).getDoubleValue();
            double dataOffsetZ = getDataOffsetZ().loadValue(context).getDoubleValue();
            double extra = getExtra().loadValue(context).getDoubleValue();
            boolean clientside = getClientside().loadValue(context).getBooleanValue();
            // only worry about clientside if player is set
            if (context.isPlayerSet() && clientside) {
                context.getPlayer().spawnParticle(
                        particle,
                        location,
                        count,
                        dataOffsetX,
                        dataOffsetY,
                        dataOffsetZ,
                        extra
                );
                return ScriptResult.TRUE;
            }
            // otherwise try to spawn particle in world
            try {
                if (location.getWorld() != null) {
                    location.getWorld().spawnParticle(
                            particle,
                            location,
                            count,
                            dataOffsetX,
                            dataOffsetY,
                            dataOffsetZ,
                            extra
                    );
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
        if (!ScriptHelper.isString(getParticle())) {
            throw new IllegalArgumentException(String.format("particle must be ScriptType STRING, not %s",
                    getParticle().getType()));
        }
        if (!ScriptHelper.isLong(getCount())) {
            throw new IllegalArgumentException(String.format("volume must be ScriptType LONG, not %s",
                    getCount().getType()));
        }
        // offsets
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
        // data offsets
        if (!ScriptHelper.isNumeric(getDataOffsetX())) {
            throw new IllegalArgumentException(String.format("data-offset-x must be ScriptType LONG or DOUBLE, not %s",
                    getDataOffsetX().getType()));
        }
        if (!ScriptHelper.isNumeric(getDataOffsetY())) {
            throw new IllegalArgumentException(String.format("data-offset-y must be ScriptType LONG or DOUBLE, not %s",
                    getDataOffsetY().getType()));
        }
        if (!ScriptHelper.isNumeric(getDataOffsetZ())) {
            throw new IllegalArgumentException(String.format("data-offset-z must be ScriptType LONG or DOUBLE, not %s",
                    getDataOffsetZ().getType()));
        }
        // other
        if (!ScriptHelper.isNumeric(getExtra())) {
            throw new IllegalArgumentException(String.format("extra must be ScriptType LONG or DOUBLE, not %s",
                    getExtra().getType()));
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
