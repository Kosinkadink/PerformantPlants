package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScopeParameterIdentifier;
import me.kosinkadink.performantplants.scripting.ScopedPlantData;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PlantDataStorage {

    private final String unscopedScope = "";
    private final String unscopedParameter = "";

    private final String plantId;
    private final ConcurrentHashMap<String, ScopedPlantData> scopeMap = new ConcurrentHashMap<>();
    private final HashSet<ScopeParameterIdentifier> identifiersToDelete = new HashSet<>();

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

    public boolean containsScopeParameter(String scope, String parameter) {
        ScopedPlantData scopedPlantData = getScopedPlantData(scope);
        if (scopedPlantData != null) {
            return scopedPlantData.getPlantDataMap().containsKey(parameter);
        }
        return false;
    }

    public boolean removeScopeParameter(String scope, String parameter) {
        ScopedPlantData scopedPlantData = getScopedPlantData(scope);
        if (scopedPlantData != null) {
            // remove data for parameter and add to removal
            scopedPlantData.removePlantData(parameter);
            addScopeForRemoval(new ScopeParameterIdentifier(plantId, scope, parameter));
            return true;
        }
        return false;
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
                addScopeForRemoval(new ScopeParameterIdentifier(plantId, scope, parameter));
            }
            else {
                removeScopeFromRemoval(new ScopeParameterIdentifier(plantId, scope, parameter));
            }
            return true;
        }
        return false;
    }

    public boolean updateData(String scope, String parameter, PlantData plantData) {
        ScopedPlantData scopedPlantData = getScopedPlantData(scope);
        if (scopedPlantData != null) {
            // update plant data
            PlantData savedPlantData = scopedPlantData.getPlantData(parameter);
            // initialize data if parameter not currently present
            if (savedPlantData == null) {
                savedPlantData = scopedPlantData.initializePlantData(parameter);
            }
            return savedPlantData.updateData(plantData);
        }
        return false;
    }

    ScopedPlantData getScopedPlantData(String scope) {
        return scopeMap.get(scope);
    }

    public ConcurrentHashMap<String, ScopedPlantData> getScopeMap() {
        return scopeMap;
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
    public void addScopeForRemoval(ScopeParameterIdentifier pair) {
        identifiersToDelete.add(pair);
    }

    public void removeScopeFromRemoval(ScopeParameterIdentifier pair) {
        identifiersToDelete.remove(pair);
    }

    public HashSet<ScopeParameterIdentifier> getScopesToDelete() {
        return identifiersToDelete;
    }

}
