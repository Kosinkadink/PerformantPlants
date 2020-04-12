package me.kosinkadink.performantplants.statistics;

import java.util.UUID;

public class StatisticsAmount {

    private UUID playerUUID;
    private String id;
    private int amount;

    public StatisticsAmount(UUID playerUUID, String id, int amount) {
        this.playerUUID = playerUUID;
        this.id = id;
        setAmount(amount);
    }

    public StatisticsAmount(UUID playerUUID, String id) {
        this(playerUUID, id, 0);
    }

    public String getId() {
        return id;
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
