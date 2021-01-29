package me.kosinkadink.performantplants.scripting;

import javax.annotation.Nonnull;

public abstract class ScriptBlock {

    protected ScriptType type = ScriptType.NULL;

    public abstract ScriptResult loadValue(@Nonnull ExecutionContext context);

    public abstract boolean containsVariable();

    public abstract boolean containsCategories(ScriptCategory... categories);

    public abstract ScriptCategory getCategory();

    public abstract ScriptBlock optimizeSelf();

    public abstract boolean shouldOptimize();

    public ScriptType getType() {
        return type;
    }

}
