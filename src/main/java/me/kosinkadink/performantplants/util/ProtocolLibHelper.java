package me.kosinkadink.performantplants.util;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.kosinkadink.performantplants.PerformantPlants;

public class ProtocolLibHelper {

    public static void printFilledStructureModifiers(PacketContainer container) {
        printValuesPresent(container.getModifier(), "getModifier");
        printValuesPresent(container.getBytes(), "getBytes");
        printValuesPresent(container.getBooleans(), "getBooleans");
        printValuesPresent(container.getShorts(), "getShorts");
        printValuesPresent(container.getIntegers(), "getIntegers");
        printValuesPresent(container.getLongs(), "getLongs");
        printValuesPresent(container.getFloat(), "getFloat");
        printValuesPresent(container.getDoubles(), "getDoubles");
        printValuesPresent(container.getStrings(), "getStrings");
        printValuesPresent(container.getUUIDs(), "getUUIDs");
        printValuesPresent(container.getStringArrays(), "getStringArrays");
        printValuesPresent(container.getByteArrays(), "getByteArrays");
        printValuesPresent(container.getIntegerArrays(), "getIntegerArrays");
        printValuesPresent(container.getItemModifier(), "getItemModifier");
        printValuesPresent(container.getItemArrayModifier(), "getItemArrayModifier");
        printValuesPresent(container.getItemListModifier(), "getItemListModifier");
        printValuesPresent(container.getStatisticMaps(), "getStatisticMaps");
        printValuesPresent(container.getWorldTypeModifier(), "getWorldTypeModifier");
        printValuesPresent(container.getDataWatcherModifier(), "getDataWatcherModifier");
        printValuesPresent(container.getEntityTypeModifier(), "getEntityTypeModifier");
        printValuesPresent(container.getPositionModifier(), "getPositionModifier");
        printValuesPresent(container.getBlockPositionModifier(), "getBlockPositionModifier");
        printValuesPresent(container.getChunkCoordIntPairs(), "getChunkCoordIntPairs");
        printValuesPresent(container.getNbtModifier(), "getNbtModifier");
        printValuesPresent(container.getListNbtModifier(), "getListNbtModifier");
        printValuesPresent(container.getVectors(), "getVectors");
        printValuesPresent(container.getAttributeCollectionModifier(), "getAttributeCollectionModifier");
        printValuesPresent(container.getPositionCollectionModifier(), "getPositionCollectionModifier");
        printValuesPresent(container.getBlockPositionCollectionModifier(), "getBlockPositionCollectionModifier");
        printValuesPresent(container.getWatchableCollectionModifier(), "getWatchableCollectionModifier");
        printValuesPresent(container.getBlocks(), "getBlocks");
        printValuesPresent(container.getGameProfiles(), "getGameProfiles");
        printValuesPresent(container.getBlockData(), "getBlockData");
        printValuesPresent(container.getMultiBlockChangeInfoArrays(), "getMultiBlockChangeInfoArrays");
        printValuesPresent(container.getChatComponents(), "getChatComponents");
        printValuesPresent(container.getChatComponentArrays(), "getChatComponentArrays");
        printValuesPresent(container.getServerPings(), "getServerPings");
        printValuesPresent(container.getPlayerInfoDataLists(), "getPlayerInfoDataLists");
        printValuesPresent(container.getProtocols(), "getProtocols");
        printValuesPresent(container.getClientCommands(), "getClientCommands");
        printValuesPresent(container.getChatVisibilities(), "getChatVisibilities");
        printValuesPresent(container.getDifficulties(), "getDifficulties");
        printValuesPresent(container.getEntityUseActions(), "getEntityUseActions");
        printValuesPresent(container.getGameModes(), "getGameModes");
        printValuesPresent(container.getResourcePackStatus(), "getResourcePackStatus");
        printValuesPresent(container.getPlayerInfoAction(), "getPlayerInfoAction");
        printValuesPresent(container.getTitleActions(), "getTitleActions");
        printValuesPresent(container.getWorldBorderActions(), "getWorldBorderActions");
        printValuesPresent(container.getCombatEvents(), "getCombatEvents");
        printValuesPresent(container.getPlayerDigTypes(), "getPlayerDigTypes");
        printValuesPresent(container.getPlayerActions(), "getPlayerActions");
        printValuesPresent(container.getScoreboardActions(), "getScoreboardActions");
        printValuesPresent(container.getParticles(), "getParticles");
        printValuesPresent(container.getNewParticles(), "getNewParticles");
        printValuesPresent(container.getEffectTypes(), "getEffectTypes");
        printValuesPresent(container.getSoundCategories(), "getSoundCategories");
        printValuesPresent(container.getSoundEffects(), "getSoundEffects");
        printValuesPresent(container.getItemSlots(), "getItemSlots");
        printValuesPresent(container.getHands(), "getHands");
        printValuesPresent(container.getDirections(), "getDirections");
        printValuesPresent(container.getChatTypes(), "getChatTypes");
        printValuesPresent(container.getMinecraftKeys(), "getMinecraftKeys");
        printValuesPresent(container.getDimensions(), "getDimensions");
    }

    private static void printValuesPresent(StructureModifier structureModifier, String name) {
        if (!structureModifier.getValues().isEmpty()) {
            PerformantPlants.getInstance().getServer().broadcastMessage(name);
        }
    }

}
