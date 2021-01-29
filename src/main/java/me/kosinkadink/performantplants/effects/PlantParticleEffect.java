package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantParticleEffect extends PlantEffect {

    private ScriptBlock particleName = new ScriptResult("SPELL");
    private ScriptBlock count = new ScriptResult(1);
    private ScriptBlock offsetX = ScriptResult.ZERO;
    private ScriptBlock offsetY = ScriptResult.ZERO;
    private ScriptBlock offsetZ = ScriptResult.ZERO;
    private ScriptBlock dataOffsetX = ScriptResult.ZERO;
    private ScriptBlock dataOffsetY = ScriptResult.ZERO;
    private ScriptBlock dataOffsetZ = ScriptResult.ZERO;
    private ScriptBlock extra = ScriptResult.ZERO;
    private ScriptBlock multiplier = ScriptResult.ZERO;
    private ScriptBlock eyeLocation = ScriptResult.TRUE;
    private ScriptBlock ignoreDirectionY = ScriptResult.FALSE;
    private ScriptBlock clientSide = ScriptResult.TRUE;

    public PlantParticleEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        Location spawnLocation;
        Player player = context.getPlayer();
        // use eye location, if set
        if (isEyeLocation(context)) {
            spawnLocation = player.getEyeLocation();
        } else {
            spawnLocation = player.getLocation();
        }
        spawnLocation.add(
                getOffsetXValue(context),
                getOffsetYValue(context),
                getOffsetZValue(context)
        );
        // ignore direction y with multiplier, if set
        if (isIgnoreDirectionY(context)) {
            spawnLocation.add(player.getLocation().getDirection().setY(0).normalize().multiply(
                    getMultiplierValue(context)
            ));
        } else {
            spawnLocation.add(player.getLocation().getDirection().normalize().multiply(
                    getMultiplierValue(context)
            ));
        }
        // get particle value
        Particle particle = getParticleValue(context);
        if (particle == null) {
            return;
        }
        // do client-side, if set
        if (isClientSide(context)) {
            player.spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    getCountValue(context),
                    getDataOffsetXValue(context),
                    getDataOffsetYValue(context),
                    getDataOffsetZValue(context),
                    getExtraValue(context)
            );
        } else {
            player.getWorld().spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    getCountValue(context),
                    getDataOffsetXValue(context),
                    getDataOffsetYValue(context),
                    getDataOffsetZValue(context),
                    getExtraValue(context)
            );
        }
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        Block block = context.getPlantBlock().getBlock();
        Location spawnLocation = BlockHelper.getCenter(block);
        // add offset
        spawnLocation.add(
                getOffsetXValue(context),
                getOffsetYValue(context),
                getOffsetZValue(context)
        );
        Particle particle = getParticleValue(context);
        if (particle == null) {
            return;
        }
        block.getWorld().spawnParticle(particle,
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                getCountValue(context),
                getDataOffsetXValue(context),
                getDataOffsetYValue(context),
                getDataOffsetZValue(context),
                getExtraValue(context)
        );
    }

    public ScriptBlock getParticleName() {
        return particleName;
    }

    public Particle getParticleValue(ExecutionContext context) {
        if (particleName == null) {
            return null;
        }
        try {
            return Particle.valueOf(particleName.loadValue(context).getStringValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setParticleName(ScriptBlock particleName) {
        this.particleName = particleName;
    }

    public ScriptBlock getCount() {
        return count;
    }

    public int getCountValue(ExecutionContext context) {
        return Math.max(1, count.loadValue(context).getIntegerValue());
    }

    public void setCount(ScriptBlock count) {
        this.count = count;
    }

    public ScriptBlock getOffsetX() {
        return offsetX;
    }

    public double getOffsetXValue(ExecutionContext context) {
        return offsetX.loadValue(context).getDoubleValue();
    }

    public void setOffsetX(ScriptBlock offsetX) {
        this.offsetX = offsetX;
    }

    public ScriptBlock getOffsetY() {
        return offsetY;
    }

    public double getOffsetYValue(ExecutionContext context) {
        return offsetY.loadValue(context).getDoubleValue();
    }

    public void setOffsetY(ScriptBlock offsetY) {
        this.offsetY = offsetY;
    }

    public ScriptBlock getOffsetZ() {
        return offsetZ;
    }

    public double getOffsetZValue(ExecutionContext context) {
        return offsetZ.loadValue(context).getDoubleValue();
    }

    public void setOffsetZ(ScriptBlock offsetZ) {
        this.offsetZ = offsetZ;
    }

    public ScriptBlock getDataOffsetX() {
        return dataOffsetX;
    }

    public double getDataOffsetXValue(ExecutionContext context) {
        return dataOffsetX.loadValue(context).getDoubleValue();
    }

    public void setDataOffsetX(ScriptBlock dataOffsetX) {
        this.dataOffsetX = dataOffsetX;
    }

    public ScriptBlock getDataOffsetY() {
        return dataOffsetY;
    }

    public double getDataOffsetYValue(ExecutionContext context) {
        return dataOffsetY.loadValue(context).getDoubleValue();
    }

    public void setDataOffsetY(ScriptBlock dataOffsetY) {
        this.dataOffsetY = dataOffsetY;
    }

    public ScriptBlock getDataOffsetZ() {
        return dataOffsetZ;
    }

    public double getDataOffsetZValue(ExecutionContext context) {
        return dataOffsetZ.loadValue(context).getDoubleValue();
    }

    public void setDataOffsetZ(ScriptBlock dataOffsetZ) {
        this.dataOffsetZ = dataOffsetZ;
    }

    public ScriptBlock getExtra() {
        return extra;
    }

    public double getExtraValue(ExecutionContext context) {
        return extra.loadValue(context).getDoubleValue();
    }

    public void setExtra(ScriptBlock extra) {
        this.extra = extra;
    }

    public ScriptBlock getMultiplier() {
        return multiplier;
    }

    public double getMultiplierValue(ExecutionContext context) {
        return multiplier.loadValue(context).getDoubleValue();
    }

    public void setMultiplier(ScriptBlock multiplier) {
        this.multiplier = multiplier;
    }

    public ScriptBlock getEyeLocation() {
        return eyeLocation;
    }

    public boolean isEyeLocation(ExecutionContext context) {
        return eyeLocation.loadValue(context).getBooleanValue();
    }

    public void setEyeLocation(ScriptBlock eyeLocation) {
        this.eyeLocation = eyeLocation;
    }

    public ScriptBlock getIgnoreDirectionY() {
        return ignoreDirectionY;
    }

    public boolean isIgnoreDirectionY(ExecutionContext context) {
        return ignoreDirectionY.loadValue(context).getBooleanValue();
    }

    public void setIgnoreDirectionY(ScriptBlock ignoreDirectionY) {
        this.ignoreDirectionY = ignoreDirectionY;
    }

    public ScriptBlock getClientSide() {
        return clientSide;
    }

    public boolean isClientSide(ExecutionContext context) {
        return clientSide.loadValue(context).getBooleanValue();
    }

    public void setClientSide(ScriptBlock clientSide) {
        this.clientSide = clientSide;
    }

}
