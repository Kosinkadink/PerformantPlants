package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.statistics.StatisticsAmount;
import me.kosinkadink.performantplants.statistics.StatisticsTagItem;
import me.kosinkadink.performantplants.storage.StatisticsAmountStorage;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {

    private PerformantPlants performantPlants;

    private ConcurrentHashMap<UUID, StatisticsAmountStorage> plantItemsSoldStorageMap = new ConcurrentHashMap<>();
    private HashSet<StatisticsAmount> statisticsAmountsToDelete = new HashSet<>();

    private ConcurrentHashMap<String, StatisticsTagStorage> plantTagStorageMap = new ConcurrentHashMap<>();
    private HashSet<StatisticsTagItem> statisticsTagsToDeleteItem = new HashSet<>();

    public StatisticsManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    // region PlantItemsSold

    public void resetAllPlantItemsSoldForAllPlayers() {
        for (StatisticsAmountStorage storage : plantItemsSoldStorageMap.values()) {
            statisticsAmountsToDelete.addAll(storage.getStatisticsAmountMap().values());
            storage.removeAllStatisticsAmount();
        }
        plantItemsSoldStorageMap.clear();
    }

    public void resetAllPlantItemsSoldForPlayer(UUID playerUUID) {
        StatisticsAmountStorage storage = plantItemsSoldStorageMap.remove(playerUUID);
        if (storage != null) {
            statisticsAmountsToDelete.addAll(storage.getStatisticsAmountMap().values());
            storage.removeAllStatisticsAmount();
        }
    }

    public boolean resetPlantItemsSoldForPlayer(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            StatisticsAmount statisticsAmount = statisticsAmountStorage.removeStatisticsAmount(plantItemId);
            if (statisticsAmount != null) {
                statisticsAmountsToDelete.add(statisticsAmount);
                return true;
            }
        }
        return false;
    }

    public void addPlantItemsSold(UUID playerUUID, String plantItemId, int addAmount) {
        StatisticsAmount plantItemsSold = getPlantItemsSold(playerUUID, plantItemId);
        if (plantItemsSold != null) {
            plantItemsSold.setAmount(plantItemsSold.getAmount() + addAmount);
            return;
        }
        plantItemsSold = new StatisticsAmount(playerUUID, plantItemId, addAmount);
        addPlantItemsSold(plantItemsSold);
    }

    public void addPlantItemsSold(StatisticsAmount plantItemsSold) {
        addStatisticsAmount(plantItemsSold, plantItemsSoldStorageMap);
        removeStatisticsAmountFromRemoval(plantItemsSold);
    }

    public StatisticsAmount getPlantItemsSold(UUID playerUUID, String plantItemId) {
        StatisticsAmountStorage statisticsAmountStorage = plantItemsSoldStorageMap.get(playerUUID);
        if (statisticsAmountStorage != null) {
            return statisticsAmountStorage.getStatisticsAmount(plantItemId);
        }
        return null;
    }

    public HashSet<StatisticsAmount> getStatisticsAmountsToDelete() {
        return statisticsAmountsToDelete;
    }

    void removeStatisticsAmountFromRemoval(StatisticsAmount statisticsAmount) {
        statisticsAmountsToDelete.remove(statisticsAmount);
    }

    void addStatisticsAmount(StatisticsAmount statisticsAmount, ConcurrentHashMap<UUID, StatisticsAmountStorage> map) {
        StatisticsAmountStorage statisticsAmountStorage = map.get(statisticsAmount.getPlayerUUID());
        if (statisticsAmountStorage != null) {
            statisticsAmountStorage.addStatisticsAmount(statisticsAmount);
        } else {
            statisticsAmountStorage = new StatisticsAmountStorage();
            statisticsAmountStorage.addStatisticsAmount(statisticsAmount);
            map.put(statisticsAmount.getPlayerUUID(), statisticsAmountStorage);
        }
    }

    // endregion

    // region PlantTags

    public void registerPlantTag(StatisticsTagItem tag) {
        StatisticsTagStorage storage = plantTagStorageMap.get(tag.getId());
        if (storage != null) {
            storage.addStatisticsTag(tag);
        } else {
            storage = new StatisticsTagStorage();
            storage.addStatisticsTag(tag);
            plantTagStorageMap.put(tag.getId(), storage);
        }
        removeStatisticTagFromRemoval(tag);
    }

    public boolean unregisterPlantTag(String tagId) {
        StatisticsTagStorage storage = plantTagStorageMap.remove(tagId);
        if (storage != null) {
            statisticsTagsToDeleteItem.addAll(storage.getStatisticsTagMap().values());
            storage.removeAllStatisticsTag();
            return true;
        }
        return false;
    }

    public boolean addPlantId(String tagId, String plantId) {
        return addPlantId(tagId, plantId, false);
    }

    public boolean addPlantId(String tagId, String plantId, boolean register) {
        if (tagId == null || tagId.isEmpty()) {
            return false;
        }
        StatisticsTagItem tag = new StatisticsTagItem(tagId, plantId);
        if (register || plantTagStorageMap.containsKey(tagId)) {
            registerPlantTag(tag);
            return true;
        }
        return false;
    }

    public StatisticsTagItem removePlantId(String tagId, String plantId) {
        if (tagId == null || tagId.isEmpty() || plantId == null || plantId.isEmpty()) {
            return null;
        }
        StatisticsTagStorage storage = plantTagStorageMap.get(tagId);
        if (storage != null) {
            StatisticsTagItem item = storage.removeStatisticsTag(plantId);
            if (storage.getStatisticsTagMap().isEmpty()) {
                unregisterPlantTag(tagId);
            }
            return item;
        }
        return null;
    }

    public StatisticsTagStorage getPlantTag(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return plantTagStorageMap.get(id);
    }

    public ArrayList<String> getAllPlantTags() {
        ArrayList<String> tags = new ArrayList<>();
        for (Map.Entry<String, StatisticsTagStorage> entry : plantTagStorageMap.entrySet()) {
            tags.add(entry.getKey());
        }
        return tags;
    }

    public HashSet<StatisticsTagItem> getStatisticsTagToDeleteItem() {
        return statisticsTagsToDeleteItem;
    }

    void removeStatisticTagFromRemoval(String tagId, String plantId) {
        removeStatisticTagFromRemoval(new StatisticsTagItem(tagId, plantId));
    }

    void removeStatisticTagFromRemoval(StatisticsTagItem tag) {
        statisticsTagsToDeleteItem.remove(tag);
    }

    // endregion

    ConcurrentHashMap<UUID, StatisticsAmountStorage> getPlantItemsSoldStorageMap() {
        return plantItemsSoldStorageMap;
    }

    ConcurrentHashMap<String, StatisticsTagStorage> getPlantTagStorageMap() {
        return plantTagStorageMap;
    }

}
