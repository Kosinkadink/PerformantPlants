package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScopeParameterPair;
import me.kosinkadink.performantplants.scripting.ScopedPlantData;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PlantDataStorage {

    private final String unscopedScope = "";
    private final String unscopedParameter = "";

    private final String plantId;
    private final ConcurrentHashMap<String, ScopedPlantData> scopeMap = new ConcurrentHashMap<>();
    private final HashSet<ScopeParameterPair> pairsToDelete = new HashSet<>();

    public PlantDataStorage(String plantId) {
        this.plantId = plantId;
    }

    public String getPlantId() {
        return plantId;
    }

    // scoped data
    public Object getVariable(String scope, String parameter, String variableName) {
        if (scope.equals(unscopedScope) && !scope.equals(parameter)) {
            return null;
        }
        ScopedPlantData scopedPlantData = getScopedPlantData(scope);
        if (scopedPlantData != null) {
            return scopedPlantData.getVariable(parameter, variableName);
        }
        return null;
    }

    public boolean updateVariable(String scope, String parameter, String variableName, Object value) {
        ScopedPlantData scopedPlantData = getScopedPlantData(scope);
        if (scopedPlantData != null) {
            // update plant data
            PlantData updatedPlantData = scopedPlantData.updateVariable(parameter, variableName, value);
            // if null, no update occurred
            if (updatedPlantData == null) {
                return false;
            }
            // check if data is now equal to default, and if so, delete and mark for db deletion
            if (updatedPlantData.dataEquals(scopedPlantData.getDefaultPlantData())) {
                scopedPlantData.removePlantData(parameter);
                addScopeForRemoval(new ScopeParameterPair(scope, parameter));
            }
            else {
                removeScopeFromRemoval(new ScopeParameterPair(scope, parameter));
            }
            return true;
        }
        return false;
    }

    ScopedPlantData getScopedPlantData(String scope) {
        return scopeMap.get(scope);
    }

    public boolean addUnscopedPlantData(PlantData plantData) {
        if (plantData != null) {
            return addScopedPlantData(unscopedScope, plantData);
        }
        return false;
    }

    public boolean addScopedPlantData(String scope, PlantData plantData) {
        if (plantData != null) {
            ScopedPlantData scopedPlantData = new ScopedPlantData(scope, plantData);
            scopeMap.put(scopedPlantData.getScope(), scopedPlantData);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return scopeMap.isEmpty();
    }

    // removal
    public void addScopeForRemoval(ScopeParameterPair pair) {
        pairsToDelete.add(pair);
    }

    public void removeScopeFromRemoval(ScopeParameterPair pair) {
        pairsToDelete.remove(pair);
    }

    public HashSet<ScopeParameterPair> getScopesToDelete() {
        return pairsToDelete;
    }

}
