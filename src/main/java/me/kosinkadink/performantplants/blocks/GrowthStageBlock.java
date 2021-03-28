package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;

public class GrowthStageBlock {

    private final String id;
    private final RelativeLocation location;
    private final BlockData blockData;
    private String skullTexture;
    private RelativeLocation childOf;
    private boolean breakChildren = true;
    private boolean breakParent = false;
    private boolean updateStageOnBreak = false;
    private boolean ignoreSpace = false;
    private boolean stopGrowth = false;
    private boolean randomOrientation = false;
    private boolean placedOrientation = false;
    private ScriptBlock onRightClick = null;
    private ScriptBlock onLeftClick = null;
    private ScriptBlock onBreak = null;
    private ScriptBlock onExplode = null;
    private ScriptBlock onBurn = null;
    private ScriptBlock onPiston = null;
    private ScriptBlock onFade = ScriptResult.FALSE;
    private ScriptBlock onDecay = ScriptResult.FALSE;
    private ScriptBlock onDestroy = null; // default destroy behavior
    // block replacement variables
    private boolean replacePlantBlock = false;
    private boolean replaceVanillaBlock = false;
    private boolean isVanillaBlock = false;

    public GrowthStageBlock(String id, int xRel, int yRel, int zRel, Material material, ArrayList<String> blockDataStrings, String skullTexture) {
        this.id = id;
        location = new RelativeLocation(xRel, yRel, zRel);
        blockData = BlockHelper.createBlockData(material, blockDataStrings);
        this.skullTexture = skullTexture;
    }

    public String getId() {
        return id;
    }

    public RelativeLocation getLocation() {
        return location;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public RelativeLocation getChildOf() {
        return childOf;
    }

    public boolean hasChildOf() {
        return childOf != null;
    }

    public boolean isBreakChildren() {
        return breakChildren;
    }

    public void setBreakChildren(boolean bool) {
        breakChildren = bool;
    }

    public boolean isBreakParent() {
        return breakParent;
    }

    public void setBreakParent(boolean breakParent) {
        this.breakParent = breakParent;
    }

    public void setChildOf(RelativeLocation parentLocation) {
        childOf = parentLocation;
    }

    public boolean isIgnoreSpace() {
        return ignoreSpace;
    }

    public void setIgnoreSpace(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }

    public boolean isStopGrowth() {
        return stopGrowth;
    }

    public void setStopGrowth(boolean stopGrowth) {
        this.stopGrowth = stopGrowth;
    }

    public boolean isUpdateStageOnBreak() {
        return updateStageOnBreak;
    }

    public void setUpdateStageOnBreak(boolean updateStageOnBreak) {
        this.updateStageOnBreak = updateStageOnBreak;
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        this.skullTexture = skullTexture;
    }

    public boolean isRandomOrientation() {
        return randomOrientation;
    }

    public void setRandomOrientation(boolean randomOrientation) {
        this.randomOrientation = randomOrientation;
    }

    public boolean isPlacedOrientation() {
        return placedOrientation;
    }

    public void setPlacedOrientation(boolean placedOrientation) {
        this.placedOrientation = placedOrientation;
    }

    public ScriptBlock getOnRightClick() {
        return onRightClick;
    }

    public void setOnRightClick(ScriptBlock onRightClick) {
        this.onRightClick = onRightClick;
    }

    public ScriptBlock getOnLeftClick() {
        return onLeftClick;
    }

    public void setOnLeftClick(ScriptBlock onLeftClick) {
        this.onLeftClick = onLeftClick;
    }

    // destruction behavior
    public ScriptBlock getOnBreak() {
        return onBreak;
    }

    public void setOnBreak(ScriptBlock onBreak) {
        this.onBreak = onBreak;
    }

    public ScriptBlock getOnExplode() {
        return onExplode;
    }

    public void setOnExplode(ScriptBlock onExplode) {
        this.onExplode = onExplode;
    }

    public ScriptBlock getOnBurn() {
        return onBurn;
    }

    public void setOnBurn(ScriptBlock onBurn) {
        this.onBurn = onBurn;
    }

    public ScriptBlock getOnPiston() {
        return onPiston;
    }

    public void setOnPiston(ScriptBlock onPiston) {
        this.onPiston = onPiston;
    }

    public ScriptBlock getOnFade() {
        return onFade;
    }

    public void setOnFade(ScriptBlock onFade) {
        this.onFade = onFade;
    }

    public ScriptBlock getOnDecay() {
        return onDecay;
    }

    public void setOnDecay(ScriptBlock onDecay) {
        this.onDecay = onDecay;
    }

    public ScriptBlock getOnDestroy() {
        return onDestroy;
    }

    public void setOnDestroy(ScriptBlock onDestroy) {
        this.onDestroy = onDestroy;
    }

    // block replacement
    public boolean isReplacePlantBlock() {
        return replacePlantBlock;
    }

    public void setReplacePlantBlock(boolean replacePlantBlock) {
        this.replacePlantBlock = replacePlantBlock;
    }

    public boolean isReplaceVanillaBlock() {
        return replaceVanillaBlock;
    }

    public void setReplaceVanillaBlock(boolean replaceVanillaBlock) {
        this.replaceVanillaBlock = replaceVanillaBlock;
    }

    public boolean isVanillaBlock() {
        return isVanillaBlock;
    }

    public void setVanillaBlock(boolean vanillaBlock) {
        isVanillaBlock = vanillaBlock;
    }
}
