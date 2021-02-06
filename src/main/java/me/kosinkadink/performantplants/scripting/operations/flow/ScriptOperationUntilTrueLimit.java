package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ScriptOperationUntilTrueLimit extends ScriptOperationFlow {

    private final ArrayList<ScriptBlock> scriptBlocks;

    public ScriptOperationUntilTrueLimit(ScriptBlock limit, ArrayList<ScriptBlock> scriptBlocks) {
        super(limit);
        this.scriptBlocks = scriptBlocks;
    }

    public ScriptBlock getLimit() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        int limit = getLimit().loadValue(context).getIntegerValue();
        boolean hasLimit = limit > 0;
        int count = 0;
        for (ScriptBlock scriptBlock : scriptBlocks) {
            boolean isTrue = scriptBlock.loadValue(context).getBooleanValue();
            if (isTrue) {
                count++;
            }
            if (count >= limit) {
                return ScriptResult.TRUE;
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
        if (ScriptHelper.isLong(getLimit())) {
            throw new IllegalArgumentException("limit should be ScriptType LONG, not " + getLimit().getType());
        }
    }
}
