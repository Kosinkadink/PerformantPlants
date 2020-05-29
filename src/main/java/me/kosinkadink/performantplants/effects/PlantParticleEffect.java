package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantParticleEffect extends PlantEffect {

    private Particle particle;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double dataOffsetX = 0;
    private double dataOffsetY = 0;
    private double dataOffsetZ = 0;
    private double extra = 0;
    private double multiplier = 0.0;
    private boolean eyeLocation = true;
    private boolean ignoreDirectionY = false;
    private boolean clientSide = true;

    public PlantParticleEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        Location spawnLocation;
        if (eyeLocation) {
            spawnLocation = player.getEyeLocation();
        } else {
            spawnLocation = player.getLocation();
        }
        spawnLocation.add(offsetX, offsetY, offsetZ);
        if (ignoreDirectionY) {
            spawnLocation.add(player.getLocation().getDirection().setY(0).normalize().multiply(multiplier));
        } else {
            spawnLocation.add(player.getLocation().getDirection().normalize().multiply(multiplier));
        }
        if (clientSide) {
            player.spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    count,
                    dataOffsetX,
                    dataOffsetY,
                    dataOffsetZ,
                    extra
            );
        } else {
            player.getWorld().spawnParticle(particle,
                    spawnLocation.getX(),
                    spawnLocation.getY(),
                    spawnLocation.getZ(),
                    count,
                    dataOffsetX,
                    dataOffsetY,
                    dataOffsetZ,
                    extra
            );
        }
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        Location spawnLocation = BlockHelper.getCenter(block);
        // add offset
        spawnLocation.add(offsetX, offsetY, offsetZ);
        block.getWorld().spawnParticle(particle,
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                count,
                dataOffsetX,
                dataOffsetY,
                dataOffsetZ,
                extra
        );
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(1, count);
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public double getDataOffsetX() {
        return dataOffsetX;
    }

    public void setDataOffsetX(double dataOffsetX) {
        this.dataOffsetX = dataOffsetX;
    }

    public double getDataOffsetY() {
        return dataOffsetY;
    }

    public void setDataOffsetY(double dataOffsetY) {
        this.dataOffsetY = dataOffsetY;
    }

    public double getDataOffsetZ() {
        return dataOffsetZ;
    }

    public void setDataOffsetZ(double dataOffsetZ) {
        this.dataOffsetZ = dataOffsetZ;
    }

    public double getExtra() {
        return extra;
    }

    public void setExtra(double extra) {
        this.extra = extra;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isEyeLocation() {
        return eyeLocation;
    }

    public void setEyeLocation(boolean eyeLocation) {
        this.eyeLocation = eyeLocation;
    }

    public boolean isIgnoreDirectionY() {
        return ignoreDirectionY;
    }

    public void setIgnoreDirectionY(boolean ignoreDirectionY) {
        this.ignoreDirectionY = ignoreDirectionY;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }

}
