package me.kosinkadink.performantplants.statistics;

import java.util.UUID;

public class PlantItemsSold {

    private UUID playerUUID;
    private String plantItemId;
    private int amount;

    public PlantItemsSold(UUID playerUUID, String plantItemId, int amount) {
        this.playerUUID = playerUUID;
        this.plantItemId = plantItemId;
        setAmount(amount);
    }

    public PlantItemsSold(UUID playerUUID, String plantItemId) {
        this(playerUUID, plantItemId, 0);
    }

    public String getPlantItemId() {
        return plantItemId;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(amount, 0);
    }
}
