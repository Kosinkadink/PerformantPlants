package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
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
    void performEffectAction(Player player, PlantBlock plantBlock) {
        Location spawnLocation;
        // use eye location, if set
        if (isEyeLocation(player, plantBlock)) {
            spawnLocation = player.getEyeLocation();
        } else {
            spawnLocation = player.getLocation();
        }
        spawnLocation.add(
                getOffsetXValue(player, plantBlock),
                getOffsetYValue(player, plantBlock),
                getOffsetZValue(player, plantBlock)
        );
        // ignore direction y with multiplier, if set
        if (isIgnoreDirectionY(player, plantBlock)) {
            spawnLocation.add(player.getLocation().getDirection().setY(0).normalize().multiply(
                    getMultiplierValue(player, plantBlock)
            ));
        } else {
            spawnLocation.add(player.getLocation().getDirection().normalize().multiply(
                    getMultiplierValue(player, plantBlock)
            ));
        }
        // get particle value
        Particle particle = getParticleValue(null, plantBlock);
        if (particle == null) {
            return;
        }
        // do client-side, if set
        if (isClientSide(player, plantBlock)) {
            player.spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    getCountValue(player, plantBlock),
                    getDataOffsetXValue(player, plantBlock),
                    getDataOffsetYValue(player, plantBlock),
                    getDataOffsetZValue(player, plantBlock),
                    getExtraValue(player, plantBlock)
            );
        } else {
            player.getWorld().spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    getCountValue(player, plantBlock),
                    getDataOffsetXValue(player, plantBlock),
                    getDataOffsetYValue(player, plantBlock),
                    getDataOffsetZValue(player, plantBlock),
                    getExtraValue(player, plantBlock)
            );
        }
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        Location spawnLocation = BlockHelper.getCenter(block);
        // add offset
        spawnLocation.add(
                getOffsetXValue(null, plantBlock),
                getOffsetYValue(null, plantBlock),
                getOffsetZValue(null, plantBlock)
        );
        Particle particle = getParticleValue(null, plantBlock);
        if (particle == null) {
            return;
        }
        block.getWorld().spawnParticle(particle,
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                getCountValue(null, plantBlock),
                getDataOffsetXValue(null, plantBlock),
                getDataOffsetYValue(null, plantBlock),
                getDataOffsetZValue(null, plantBlock),
                getExtraValue(null, plantBlock)
        );
    }

    public ScriptBlock getParticleName() {
        return particleName;
    }

    public Particle getParticleValue(Player player, PlantBlock plantBlock) {
        if (particleName == null) {
            return null;
        }
        try {
            return Particle.valueOf(particleName.loadValue(plantBlock, player).getStringValue().toUpperCase());
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

    public int getCountValue(Player player, PlantBlock plantBlock) {
        return Math.max(1, count.loadValue(plantBlock, player).getIntegerValue());
    }

    public void setCount(ScriptBlock count) {
        this.count = count;
    }

    public ScriptBlock getOffsetX() {
        return offsetX;
    }

    public double getOffsetXValue(Player player, PlantBlock plantBlock) {
        return offsetX.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetX(ScriptBlock offsetX) {
        this.offsetX = offsetX;
    }

    public ScriptBlock getOffsetY() {
        return offsetY;
    }

    public double getOffsetYValue(Player player, PlantBlock plantBlock) {
        return offsetY.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetY(ScriptBlock offsetY) {
        this.offsetY = offsetY;
    }

    public ScriptBlock getOffsetZ() {
        return offsetZ;
    }

    public double getOffsetZValue(Player player, PlantBlock plantBlock) {
        return offsetZ.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetZ(ScriptBlock offsetZ) {
        this.offsetZ = offsetZ;
    }

    public ScriptBlock getDataOffsetX() {
        return dataOffsetX;
    }

    public double getDataOffsetXValue(Player player, PlantBlock plantBlock) {
        return dataOffsetX.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setDataOffsetX(ScriptBlock dataOffsetX) {
        this.dataOffsetX = dataOffsetX;
    }

    public ScriptBlock getDataOffsetY() {
        return dataOffsetY;
    }

    public double getDataOffsetYValue(Player player, PlantBlock plantBlock) {
        return dataOffsetY.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setDataOffsetY(ScriptBlock dataOffsetY) {
        this.dataOffsetY = dataOffsetY;
    }

    public ScriptBlock getDataOffsetZ() {
        return dataOffsetZ;
    }

    public double getDataOffsetZValue(Player player, PlantBlock plantBlock) {
        return dataOffsetZ.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setDataOffsetZ(ScriptBlock dataOffsetZ) {
        this.dataOffsetZ = dataOffsetZ;
    }

    public ScriptBlock getExtra() {
        return extra;
    }

    public double getExtraValue(Player player, PlantBlock plantBlock) {
        return extra.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setExtra(ScriptBlock extra) {
        this.extra = extra;
    }

    public ScriptBlock getMultiplier() {
        return multiplier;
    }

    public double getMultiplierValue(Player player, PlantBlock plantBlock) {
        return multiplier.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setMultiplier(ScriptBlock multiplier) {
        this.multiplier = multiplier;
    }

    public ScriptBlock getEyeLocation() {
        return eyeLocation;
    }

    public boolean isEyeLocation(Player player, PlantBlock plantBlock) {
        return eyeLocation.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setEyeLocation(ScriptBlock eyeLocation) {
        this.eyeLocation = eyeLocation;
    }

    public ScriptBlock getIgnoreDirectionY() {
        return ignoreDirectionY;
    }

    public boolean isIgnoreDirectionY(Player player, PlantBlock plantBlock) {
        return ignoreDirectionY.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setIgnoreDirectionY(ScriptBlock ignoreDirectionY) {
        this.ignoreDirectionY = ignoreDirectionY;
    }

    public ScriptBlock getClientSide() {
        return clientSide;
    }

    public boolean isClientSide(Player player, PlantBlock plantBlock) {
        return clientSide.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setClientSide(ScriptBlock clientSide) {
        this.clientSide = clientSide;
    }

}
