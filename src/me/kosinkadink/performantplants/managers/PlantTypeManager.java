package me.kosinkadink.performantplants.managers;

import com.sun.istack.internal.NotNull;
import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.stages.GrowthStage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlantTypeManager {

    private Main main;
    private ArrayList<Plant> plantTypes = new ArrayList<>();

    public PlantTypeManager(Main mainClass) {
        main = mainClass;
        // TODO: remove once done testing
        String testPlantName = "Test Plant";
        String testPlantId = "test";
        ItemStack testItemStack = new PlantItemBuilder(Material.OAK_LOG)
                .displayName(testPlantName)
                .lore(Collections.singletonList("Plant for testing purposes"))
                .build();
        ItemStack testSeedItemStack = new PlantItemBuilder(Material.PLAYER_HEAD)
                .displayName(testPlantName + " Seed")
                .lore(Collections.singletonList("Plant seed for testing purposes"))
                .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTliMGU5NjljZjNmY2NlZDM2YjcxMjM1MGZmYjQ2ZDhlZDc2MWZlNWVmYjEwZTNiNmE5Nzk1ZTY2NTZkYTk3In19fQ==")
                .build();
        Plant testPlant = new Plant(testPlantId, testItemStack, testSeedItemStack);
        testPlant.addRequiredBlockToGrow(new RequiredBlock(0,-1,0,Material.DIRT));
        testPlant.addRequiredBlockToGrow(new RequiredBlock(0,-1,0,Material.GRASS_BLOCK));
        testPlant.addRequiredBlockToGrow(new RequiredBlock(0,-1,0,Material.SAND));
        testPlant.setMinGrowthTime(15);
        testPlant.setMaxGrowthTime(30);
        testPlant.setWaterRequired(true);
        // first stage
        GrowthStage stage = new GrowthStage();
        GrowthStageBlock plantBaseGrowthBlock = new GrowthStageBlock("1",0,0,0, Material.PLAYER_HEAD);
        plantBaseGrowthBlock.setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTliMGU5NjljZjNmY2NlZDM2YjcxMjM1MGZmYjQ2ZDhlZDc2MWZlNWVmYjEwZTNiNmE5Nzk1ZTY2NTZkYTk3In19fQ==");
        stage.addGrowthStageBlock(plantBaseGrowthBlock);
        stage.addDrop(new Drop(testPlant.getSeedItem(), 1, 1, 100.0));
        testPlant.addGrowthStage(stage);
        // second stage
        stage = new GrowthStage();
        stage.addGrowthStageBlock(new GrowthStageBlock("1",0,0,0, Material.OAK_LOG));
        stage.addDrop(new Drop(testPlant.getItem(),1,1, 100.0));
        stage.addDrop(new Drop(testPlant.getSeedItem(), 1, 2, 100.0));
        GrowthStageBlock growthStageBlock = new GrowthStageBlock("leaf",0,1,0, Material.JUNGLE_LEAVES);
        growthStageBlock.setDropLimit(1);
        growthStageBlock.addDrop(new Drop(new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).build(),1,1,50.0));
        growthStageBlock.addDrop(new Drop(new ItemBuilder(Material.APPLE).build(),1,1,100.0));
        growthStageBlock.setUpdateStageOnBreak(true);
        stage.addGrowthStageBlock(growthStageBlock);
        testPlant.addGrowthStage(stage);
        // third stage
        stage = new GrowthStage();
        growthStageBlock = new GrowthStageBlock("leaf",0,2,0, Material.JUNGLE_LEAVES);
        growthStageBlock.setDropLimit(1);
        growthStageBlock.addDrop(new Drop(new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).build(),1,1,50.0));
        growthStageBlock.addDrop(new Drop(new ItemBuilder(Material.APPLE).build(),1,1,100.0));
        growthStageBlock.setUpdateStageOnBreak(true);
        growthStageBlock.setChildOf(new RelativeLocation(0,1,0));
        stage.addGrowthStageBlock(growthStageBlock);
        testPlant.addGrowthStage(stage);

        addPlantType(testPlant);
    }

    void addPlantType(Plant plantType) {
        plantTypes.add(plantType);
    }

    public Plant getPlantByDisplayName(String displayName) {
        for (Plant plantType : plantTypes) {
            if (plantType.getDisplayName().equalsIgnoreCase(displayName)) {
                return plantType;
            }
        }
        return null;
    }

    public Plant getPlantPlacedWith(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        String displayName = itemMeta.getDisplayName();
        for (Plant plantType : plantTypes) {
            //if (plantType.isPlaceable()) {
            if (plantType.getDisplayName().equalsIgnoreCase(displayName)) {
                return plantType;
            }
            //}
            if (plantType.hasSeed()) {
                if (plantType.getSeedDisplayName().equalsIgnoreCase(displayName)) {
                    return plantType;
                }
            }
        }
        return null;
    }

    public Plant getPlant(ItemStack itemStack) {
        return getPlantByDisplayName(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
    }

    public Plant getPlantById(String id) {
        for (Plant plantType : plantTypes) {
            if (plantType.getId().equalsIgnoreCase(id)) {
                return plantType;
            }
        }
        return null;
    }

    public ArrayList<Plant> getPlantTypes() {
        return plantTypes;
    }
}
