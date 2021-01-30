package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class ScriptOperationItemIsMaterial extends ScriptOperationItem {

    public ScriptOperationItemIsMaterial(ScriptBlock material) {
        super(material);
    }

    public ScriptBlock getMaterial() {
        return inputs[0];
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
}
