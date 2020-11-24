package me.kosinkadink.performantplants.scripting;

import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class PlantData {

    JSONObject data;
    Plant plant;

    public PlantData(JSONObject data) {
        this.data = data;
    }

    public PlantData(String jsonString) {
        this((JSONObject) JSONValue.parse(jsonString));
    }

    public String createJsonString() {
        return data.toString();
    }

    public boolean updateData(PlantData plantData) {
        // updates values of data
        JSONObject newData = plantData.getData();
        boolean updatedAnyVariable = false;
        for (Object key : newData.keySet()) {
            updatedAnyVariable = updateVariable(key, newData.get(key)) || updatedAnyVariable;
        }
        return updatedAnyVariable;
    }

    public JSONObject getData() {
        return data;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public PlantData clone() {
        JSONObject clone = (JSONObject) JSONValue.parse(createJsonString());
        PlantData newPlantData = new PlantData(clone);
        newPlantData.setPlant(plant);
        return newPlantData;
    }

    public boolean dataEquals(PlantData otherPlantData) {
        // compare lengths
        if (data.size() != otherPlantData.data.size()) {
            return false;
        }
        // compare keys and values
        for (Object key : otherPlantData.data.keySet()) {
            if (!data.containsKey(key)) {
                return false;
            }
            if (!data.get(key).equals(otherPlantData.data.get(key))) {
                return false;
            }
        }
        return true;
    }

    public boolean updateVariable(Object variableName, Object value) {
        // if current data contains same key, check if same type
        if (data.containsKey(variableName)) {
            // update value if same type
            if (ScriptHelper.getType(data.get(variableName)) == ScriptHelper.getType(value)) {
                Object previousValue = data.put(variableName, value);
                return previousValue != value;
            }
        }
        return false;
    }

    public Object getVariable(String variableName) {
        return data.get(variableName);
    }
}
