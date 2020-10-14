package me.kosinkadink.performantplants.scripting;

import java.util.concurrent.ConcurrentHashMap;

public class ScopedPlantData {

    private final String scope;

    private final PlantData defaultPlantData;
    private final ConcurrentHashMap<String, PlantData> plantDataMap = new ConcurrentHashMap<>();

    public ScopedPlantData(String scope, PlantData defaultPlantData) {
        this.scope = scope;
        this.defaultPlantData = defaultPlantData;
    }

    public ConcurrentHashMap<String, PlantData> getPlantDataMap() {
        return plantDataMap;
    }

    public String getScope() {
        return scope;
    }

    // default plant data
    public PlantData getDefaultPlantData() {
        return defaultPlantData;
    }

    // scoped plant data
    public PlantData initializePlantData(String parameter) {
        PlantData newPlantData = defaultPlantData.clone();
        plantDataMap.put(parameter, newPlantData);
        return newPlantData;
    }

    public void removePlantData(String parameter) {
        plantDataMap.remove(parameter);
    }

    public PlantData getPlantData(String parameter) {
        return plantDataMap.get(parameter);
    }

    public Object getVariable(String parameter, String variableName) {
        // try to get from parameterized plant data
        PlantData plantData = getPlantData(parameter);
        if (plantData != null) {
            return plantData.getVariable(variableName);
        }
        // try to get from default plant data
        return defaultPlantData.getVariable(variableName);
    }

    public PlantData updateVariable(String parameter, String variableName, Object value) {
        // try to update existing parameterized plant data
        PlantData plantData = getPlantData(parameter);
        if (plantData == null) {
            // assign default value to parameter
            plantData = initializePlantData(parameter);
        }
        boolean updated = plantData.updateVariable(variableName, value);
        // if value was updated, return plant data
        if (updated) {
            return plantData;
        }
        return null;
    }

}
