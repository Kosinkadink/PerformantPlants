package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class ScriptOperationItemIsMaterial extends ScriptOperationItem {

    public ScriptOperationItemIsMaterial(ScriptBlock itemStack, ScriptBlock material) {
        super(itemStack, material);
    }

    public ScriptBlock getItemStack() {
        return inputs[0];
    }

    public ScriptBlock getMaterial() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (!context.isItemStackSet()) {
            return ScriptResult.FALSE;
        }
        // get material
        String materialName = getMaterial().loadValue(context).getStringValue();
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            return ScriptResult.FALSE;
        }
        // check if material matches
        return new ScriptResult(context.getItemStack().getType() == material);
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isItemStack(getItemStack())) {
            throw new IllegalArgumentException("Requires ScriptType ITEMSTACK for itemstack");
        }
        if (!ScriptHelper.isString(getMaterial())) {
            throw new IllegalArgumentException("Requires ScriptType STRING for material");
        }
    }
}