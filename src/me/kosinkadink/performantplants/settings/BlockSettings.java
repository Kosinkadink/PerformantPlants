package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class BlockSettings {

    private int xRel;
    private int yRel;
    private int zRel;
    private Material material;
    private ArrayList<String> blockDataStrings = new ArrayList<>();
    private String skullTexture;
    private boolean required = false;


    public BlockSettings(int xRel, int yRel, int zRel, Material material) {
        this.xRel = xRel;
        this.yRel = yRel;
        this.zRel = zRel;
        this.material = material;
    }

    public BlockSettings(int xRel, int yRel, int zRel, Material material, boolean required) {
        this(xRel, yRel, zRel, material);
        this.required = required;
    }

    public int getXRel() {
        return xRel;
    }

    public int getYRel() {
        return yRel;
    }

    public int getZRel() {
        return zRel;
    }

    public Material getMaterial() {
        return material;
    }

    public ArrayList<String> getBlockDataStrings() {
        return blockDataStrings;
    }

    public void setBlockDataStrings(ArrayList<String> blockDataStrings) {
        if (blockDataStrings != null) {
            this.blockDataStrings = blockDataStrings;
        } else {
            this.blockDataStrings = new ArrayList<>();
        }

    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        this.skullTexture = skullTexture;
    }

    public boolean isRequired() {
        return required;
    }
}
