package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ScriptOperationIsPlayerSprinting extends ScriptOperationPlayer {

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        // also check for sneaking, as player.isSprinting() would be true if player starts sneaking while sprinting
        Player player = context.getPlayer();
        return new ScriptResult(context.isPlayerSet() && player.isSprinting() && !player.isSneaking());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
