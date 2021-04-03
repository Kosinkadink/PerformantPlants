package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import org.bukkit.block.Block;

import java.util.ArrayList;

public class RequiredBlock {

    private final RelativeLocation location;
    private final ArrayList<RequiredCondition> conditions = new ArrayList<>();
    private boolean critical = true;
    private boolean notAir = false;

    public RequiredBlock(int xRel, int yRel, int zRel) {
        location = new RelativeLocation(xRel, yRel, zRel);
    }

    public boolean checkIfMatches(Block block) {
        // match other conditions
        PlantBlock plantBlock = null;
        boolean checkedForPlantBlock = false;
        for (RequiredCondition condition : conditions) {
            //check if matches block/plant block conditions
            boolean matches;
            if (condition.isVanillaMatch()) {
                matches = condition.checkIfMatches(block);
            } else {
                if (!checkedForPlantBlock) {
                    plantBlock = PerformantPlants.getInstance().getPlantManager().getPlantBlock(block);
                    checkedForPlantBlock = true;
                }
                matches = condition.checkIfMatches(plantBlock);
            }
            // if matches and blacklisted, return false
            if (matches && condition.isBlacklisted()) {
                return false;
            }
            if (matches) {
                return true;
            }
        }
        // check if air
        if (isNotAir()) {
            return !block.getType().isAir();
        }
        // if found no match so far, then doesn't match
        return false;
    }

    public RelativeLocation getLocation() {
        return location;
    }

    public void addCondition(RequiredCondition condition) {
        if (condition != null) {
            conditions.add(condition);
        }
    }

    public boolean isConditionsEmpty() {
        return conditions.isEmpty();
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isNotAir() {
        return notAir;
    }

    public void setNotAir(boolean notAir) {
        this.notAir = notAir;
    }
}
