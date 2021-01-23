package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.effects.*;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.*;
import me.kosinkadink.performantplants.recipes.*;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.ItemStackRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.action.*;
import me.kosinkadink.performantplants.scripting.operations.block.ScriptOperationIsBlockNull;
import me.kosinkadink.performantplants.scripting.operations.block.ScriptOperationPassOnlyBlock;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToBoolean;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToDouble;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToLong;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToString;
import me.kosinkadink.performantplants.scripting.operations.compare.*;
import me.kosinkadink.performantplants.scripting.operations.flow.ScriptOperationFunction;
import me.kosinkadink.performantplants.scripting.operations.flow.ScriptOperationIf;
import me.kosinkadink.performantplants.scripting.operations.flow.ScriptOperationSwitch;
import me.kosinkadink.performantplants.scripting.operations.function.*;
import me.kosinkadink.performantplants.scripting.operations.inventory.ScriptOperationGetMainHandEnchantmentLevel;
import me.kosinkadink.performantplants.scripting.operations.inventory.ScriptOperationHasMainHandEnchantment;
import me.kosinkadink.performantplants.scripting.operations.logic.*;
import me.kosinkadink.performantplants.scripting.operations.math.*;
import me.kosinkadink.performantplants.scripting.operations.player.*;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationChance;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationChoice;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationRandomDouble;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationRandomLong;
import me.kosinkadink.performantplants.scripting.operations.world.ScriptOperationGetWorld;
import me.kosinkadink.performantplants.scripting.storage.ScriptColor;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.scripting.storage.hooks.*;
import me.kosinkadink.performantplants.settings.*;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.*;
import me.kosinkadink.performantplants.util.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class ConfigurationManager {

    private final PerformantPlants performantPlants;

    private final YamlConfiguration config = new YamlConfiguration();
    private final YamlConfiguration vanillaDropsConfig = new YamlConfiguration();
    private final HashMap<String, YamlConfiguration> plantConfigMap = new HashMap<>();

    private final ConfigSettings configSettings = new ConfigSettings();

    private ScriptTaskLoader currentTaskLoader = null;

    public ConfigurationManager(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
        loadMainConfig();
        loadPlantConfigs();
        loadVanillaDropConfig();
    }

    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    void loadMainConfig() {
        String fileName = "config.yml";
        // main config
        File configFile = new File(performantPlants.getDataFolder(), fileName);
        // create file if doesn't exist
        if (!configFile.exists()) {
            performantPlants.saveResource(fileName, false);
        }
        try {
            config.load(configFile);
        } catch (Exception e) {
            performantPlants.getLogger().severe("Error occurred trying to load " + fileName);
            e.printStackTrace();
        }
        // get parameters from config.yml
        // get if debug
        if (config.isBoolean("debug")) {
            configSettings.setDebug(config.getBoolean("debug"));
        }
        // get database save delay time
        if (config.isInt("save-delay-minutes")) {
            int saveDelayMinutes = config.getInt("save-delay-minutes");
            if (saveDelayMinutes > 0) {
                configSettings.setSaveDelayMinutes(saveDelayMinutes);
            }
        }
    }

    void loadPlantConfigs() {
        String plantsPath = Paths.get(performantPlants.getDataFolder().getPath(), "plants").toString();
        // create plants directory if doesn't exist
        File plantsDir = new File(plantsPath);
        if (!plantsDir.exists()) {
            // if doesn't exist, create it
            plantsDir.mkdir();
        }
        // get all plant files from plantsPath
        Collection<File> plantFiles = FileUtils.listFiles(plantsDir, new String[] {"yml"}, true);
        if (plantFiles.isEmpty()) {
            // if plantFiles is null or of length zero, add default plants?
            // TODO: add default plants
            return;
        }
        // otherwise read all of files
        for (File file : plantFiles) {
            performantPlants.getLogger().info("Loading plant config from file: " + file.getPath());
            loadPlantConfig(file);
        }
        // now that all configs are loaded in, create plants from loaded configs
        loadPlantsFromConfigs();
    }

    void loadPlantConfig(File file) {
        String plantId = getFileNameWithoutExtension(file);
        YamlConfiguration plantConfig = new YamlConfiguration();
        try {
            plantConfig.load(file);
        }
        catch (Exception e) {
            performantPlants.getLogger().severe("Error occurred trying to load plant config: " + plantId);
            e.printStackTrace();
            return;
        }
        if (plantId.startsWith("_")) {
            performantPlants.getLogger().warning(String.format("Plant config '%s' cannot start with '_' character; will not be " +
                    "loaded until this is fixed", plantId));
            return;
        }
        // put plant yaml config in config map for future reference
        plantConfigMap.put(plantId, plantConfig);
    }

    void loadPlantsFromConfigs() {
        // now that all configs are loaded in, create plants from loaded configs
        // first, load in global plant data from each config so it can be referenced later
        performantPlants.getLogger().info("Starting to load global data for all plants...");
        for (Map.Entry<String, YamlConfiguration> entry : plantConfigMap.entrySet()) {
            String plantId = entry.getKey();
            YamlConfiguration plantConfig = entry.getValue();
            loadGlobalPlantDataFromConfig(plantId, plantConfig);
        }
        performantPlants.getLogger().info("Done loading global data for all plants");
        // now load in the actual plants from the configs
        performantPlants.getLogger().info("Starting to load configs for all plants...");
        for (Map.Entry<String, YamlConfiguration> entry : plantConfigMap.entrySet()) {
            String plantId = entry.getKey();
            performantPlants.getLogger().info("Attempting to load plant: " + plantId);
            YamlConfiguration plantConfig = entry.getValue();
            loadPlantFromConfig(plantId, plantConfig);
        }
        performantPlants.getLogger().info("Done loading configs for all plants");
    }

    void loadGlobalPlantDataFromConfig(String plantId, YamlConfiguration plantConfig) {
        // get global-data section
        ConfigurationSection globalDataSection = plantConfig.getConfigurationSection("global-data");
        if (globalDataSection != null) {
            performantPlants.getLogger().info("Attempting to add global variables for plant: " + plantId);
            PlantDataStorage plantDataStorage = new PlantDataStorage(plantId);
            // attempt to add unscoped plant data
            PlantData unscopedData = createPlantData(globalDataSection, null);
            if (unscopedData != null) {
                plantDataStorage.addUnscopedPlantData(unscopedData);
                performantPlants.getLogger().info(String.format("Added unscoped global variables for plant: %s", plantId));
            }
            // add scoped plant data
            ConfigurationSection scopedDataSection = globalDataSection.getConfigurationSection("scoped");
            if (scopedDataSection != null) {
                // parse through each scope
                for (String scope : scopedDataSection.getKeys(false)) {
                    // attempt to get scoped plant data
                    PlantData scopedData = createPlantData(scopedDataSection, scope, null);
                    if (scopedData != null) {
                        plantDataStorage.addScopedPlantData(scope, scopedData);
                        performantPlants.getLogger().info(String.format("Added '%s' scoped global variables for plant: %s",
                                scope, plantId));
                    }
                }
            }
            if (!plantDataStorage.isEmpty()) {
                performantPlants.getPlantTypeManager().addPlantDataStorage(plantDataStorage);
                performantPlants.getLogger().info("Successfully added global data for plant: " + plantId);
            } else {
                performantPlants.getLogger().info("No global data added for plant: " + plantId);
            }
        }
    }

    void loadPlantFromConfig(String plantId, YamlConfiguration plantConfig) {
        // get item info; if section not present, log error and return
        ItemSettings itemSettings;
        ConfigurationSection itemConfig = plantConfig.getConfigurationSection("item");
        if (itemConfig != null) {
            itemSettings = loadItemConfig(itemConfig,false);
            if (itemSettings == null) {
                performantPlants.getLogger().warning("Item could not be queried for plant: " + plantId);
                return;
            }
        } else {
            performantPlants.getLogger().warning("Could not load plant " + plantId + "; item section not found");
            return;
        }
        // create plant ItemStack and add it to plant type manager
        PlantItem plantItem = new PlantItem(itemSettings.generatePlantItemStack(plantId));
        // set buy/sell prices
        addPropertiesToPlantItem(itemConfig, plantItem);
        // Plant to be saved
        Plant plant = new Plant(plantId, plantItem);
        // get growing section
        ConfigurationSection growingConfig = plantConfig.getConfigurationSection("growing");
        // if growing section is present, set plant data
        if (growingConfig != null) {
            // load plant data, if present
            PlantData plantData = createPlantData(growingConfig, plant);
            if (plantData != null) {
                plant.setPlantData(plantData);
            }
        }
        // load plant tasks and stored plant script blocks, if present
        boolean loadedScriptBlocksAndTasks = loadPlantScriptTasksAndScriptBlocks(
                plantConfig,
                "stored-script-blocks",
                "tasks",
                plant.getPlantData()
        );
        if (!loadedScriptBlocksAndTasks) {
            performantPlants.getLogger().warning("Plant tasks and stored-script-blocks could not be registered for plant:" + plantId);
            return;
        }
        // if growing section is present, get seed item + stages
        if (growingConfig != null) {
            ConfigurationSection seedConfig = growingConfig.getConfigurationSection("seed-item");
            if (seedConfig != null) {
                // let main plant item also act as seed
                boolean usePlantAsSeed = seedConfig.isSet("use-plant-as-seed")
                        && seedConfig.getBoolean("use-plant-as-seed");
                if (usePlantAsSeed) {
                    plant.setSeedItem(plantItem);
                } else {
                    ItemSettings seedItemSettings = loadItemConfig(seedConfig,false);
                    if (seedItemSettings != null) {
                        // set seed buy/sell price
                        PlantItem seedItem = new PlantItem(seedItemSettings.generatePlantItemStack(plantId, true));
                        addPropertiesToPlantItem(seedConfig, seedItem);
                        plant.setSeedItem(seedItem);
                    } else {
                        performantPlants.getLogger().info("seedItemSettings were null for plant: " + plantId);
                        return;
                    }
                }
                // if seed item has been set, get growth requirements + stages
                if (plant.getSeedItem() != null) {
                    // set growth bounds for general growth (overridable by stage-specific min/max growth time)
                    if (!growingConfig.isInt("min-growth-time") || !growingConfig.isInt("max-growth-time")) {
                        performantPlants.getLogger().warning("Growth time bounds not set/integer for growing for plant: " + plantId);
                        return;
                    }
                    plant.setMinGrowthTime(growingConfig.getLong("min-growth-time"));
                    plant.setMaxGrowthTime(growingConfig.getLong("max-growth-time"));
                    // set plant requirements, if present
                    if (growingConfig.isConfigurationSection("plant-requirements")) {
                        if (!addRequirementsToStorage(growingConfig.getConfigurationSection("plant-requirements"),
                                plant.getPlantRequirementStorage())) {
                            performantPlants.getLogger().warning("plant-requirements could not be loaded for plant: " + plantId);
                            return;
                        }
                    }
                    // set growth requirements, if present
                    if (growingConfig.isConfigurationSection("growth-requirements")) {
                        if (!addRequirementsToStorage(growingConfig.getConfigurationSection("growth-requirements"),
                                plant.getGrowthRequirementStorage())) {
                            performantPlants.getLogger().warning("growth-requirements could not be loaded for plant: " + plantId);
                            return;
                        }
                    }
                    // load stored plant growths stage blocks, if present
                    List<GrowthStageBlock> plantStageBlocks = loadGrowthStageBlocks(
                            growingConfig,
                            "stored-stage-blocks",
                            null,
                            plant.getPlantData()
                    );
                    if (plantStageBlocks == null) {
                        performantPlants.getLogger().warning("Plant stored-stage-blocks could not be registered for plant: " + plantId);
                        return;
                    }
                    for (GrowthStageBlock block : plantStageBlocks) {
                        plant.addGrowthStageBlock(block.getId(), block);
                    }
                    // get growth stages; since seed is present, growth stages are REQUIRED
                    if (!growingConfig.isSet("stages") && growingConfig.isConfigurationSection("stages")) {
                        performantPlants.getLogger().warning("No stages are configured for plant while seed is present: " + plantId);
                        return;
                    } else {
                        ConfigurationSection stagesConfig = growingConfig.getConfigurationSection("stages");
                        for (String stageId : stagesConfig.getKeys(false)) {
                            ConfigurationSection stageConfig = stagesConfig.getConfigurationSection(stageId);
                            if (stageConfig == null) {
                                performantPlants.getLogger().warning("Could not load stageConfig for plant: " + plantId);
                                return;
                            }
                            GrowthStage growthStage = new GrowthStage(stageId);
                            // set min and max growth times, if present for stage
                            if (stageConfig.isInt("min-growth-time") && stageConfig.isInt("max-growth-time")) {
                                long stageMinGrowthTime = stageConfig.getLong("min-growth-time");
                                long stageMaxGrowthTime = stageConfig.getLong("max-growth-time");
                                growthStage.setMinGrowthTime(stageMinGrowthTime);
                                growthStage.setMaxGrowthTime(stageMaxGrowthTime);
                            }
                            // set drops and/or drop limit
                            if (!addDropsToDropStorage(stageConfig, growthStage.getDropStorage(), plant.getPlantData())) {
                                return;
                            }
                            // set growth checkpoint, if present
                            if (stageConfig.isBoolean("growth-checkpoint")) {
                                growthStage.setGrowthCheckpoint(stageConfig.getBoolean("growth-checkpoint"));
                            }
                            // set on-execute, if present
                            if (stageConfig.isConfigurationSection("on-execute")) {
                                ConfigurationSection onExecuteSection = stageConfig.getConfigurationSection("on-execute");
                                // load interaction from default path
                                PlantInteract onExecute = loadPlantInteract(onExecuteSection, plant.getPlantData());
                                if (onExecute == null) {
                                    performantPlants.getLogger().warning(String.format("Stage %s's on-execute could not be loaded from section: %s", stageId, onExecuteSection.getCurrentPath()));
                                    return;
                                }
                                growthStage.setOnExecute(onExecute);
                            }
                            // set on-fail, if present
                            if (stageConfig.isConfigurationSection("on-fail")) {
                                ConfigurationSection onFailSection = stageConfig.getConfigurationSection("on-fail");
                                // load interaction from default path
                                PlantInteract onFail = loadPlantInteract(onFailSection, plant.getPlantData());
                                if (onFail == null) {
                                    performantPlants.getLogger().warning(String.format("Stage %s's on-fail could not be loaded from section: %s", stageId, onFailSection.getCurrentPath()));
                                    return;
                                }
                                growthStage.setOnFail(onFail);
                            }
                            // set growth requirements, if present
                            if (stageConfig.isConfigurationSection("growth-requirements")) {
                                if (!addRequirementsToStorage(stageConfig.getConfigurationSection("growth-requirements"),
                                        growthStage.getRequirementStorage())) {
                                    performantPlants.getLogger().warning(String.format("Stage growth-requirements could not be loaded for stage %s of plant: %s", stageId, plantId));
                                    return;
                                }
                            }
                            // set blocks for growth
                            if (!stageConfig.isConfigurationSection("blocks")) {
                                performantPlants.getLogger().warning(String.format("No blocks provided for growth stage %s in plant %s: ", stageId, plantId));
                                return;
                            }
                            List<GrowthStageBlock> blocks = loadGrowthStageBlocks(stageConfig, "blocks", growthStage, plant.getPlantData());
                            if (blocks == null || blocks.isEmpty()) {
                                performantPlants.getLogger().warning(String.format("Something went wrong loading blocks for growth stage %s in plant %s: ", stageId, plantId));
                                return;
                            }
                            // add growth stage blocks to stage
                            for (GrowthStageBlock block : blocks) {
                                growthStage.addGrowthStageBlock(block);
                            }
                            // add growth stage to plant
                            plant.addGrowthStage(growthStage);
                        }
                    }
                    // validate stages
                    if (!plant.validateStages()) {
                        return;
                    }
                }
            }
            else {
                performantPlants.getLogger().info("seedConfig was null for plant: " + plantId);
            }
        }
        // add consumable behavior
        if (itemConfig.isConfigurationSection("consumable")) {
            PlantConsumableStorage consumable = loadPlantConsumableStorage(itemConfig.getConfigurationSection("consumable"), plant.getPlantData());
            if (consumable != null) {
                plantItem.setConsumableStorage(consumable);
            }
        }
        if (itemConfig.isConfigurationSection("clickable")) {
            PlantConsumableStorage clickable = loadPlantConsumableStorage(itemConfig.getConfigurationSection("clickable"), plant.getPlantData());
            if (clickable != null) {
                plantItem.setClickableStorage(clickable);
            }
        }
        // load plant goods; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("goods")) {
            ConfigurationSection goodsSection = plantConfig.getConfigurationSection("goods");
            for (String goodId : goodsSection.getKeys(false)) {
                if (goodId.equals("seed")) {
                    performantPlants.getLogger().info(String.format("Good %s not added; uses reserved name 'seed'; in section: %s", goodId, goodsSection.getCurrentPath()));
                    continue;
                }
                ConfigurationSection goodSection = goodsSection.getConfigurationSection(goodId);
                ItemSettings goodSettings = loadItemConfig(goodSection, false);
                if (goodSettings != null) {
                    PlantItem goodItem = new PlantItem(goodSettings.generatePlantItemStack(goodId));
                    addPropertiesToPlantItem(goodSection, goodItem);
                    if (goodSection.isConfigurationSection("consumable")) {
                        PlantConsumableStorage goodConsumable = loadPlantConsumableStorage(goodSection.getConfigurationSection("consumable"), plant.getPlantData());
                        if (goodConsumable != null) {
                            goodItem.setConsumableStorage(goodConsumable);
                        }
                    }
                    if (goodSection.isConfigurationSection("clickable")) {
                        PlantConsumableStorage goodClickable = loadPlantConsumableStorage(goodSection.getConfigurationSection("clickable"), plant.getPlantData());
                        if (goodClickable != null) {
                            goodItem.setClickableStorage(goodClickable);
                        }
                    }
                    plant.addGoodItem(goodId, goodItem);
                    performantPlants.getLogger().info(String.format("Added good item '%s' to plant: %s", goodId, plantId));
                }
            }
        }
        // load crafting recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("crafting-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("crafting-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addCraftingRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load furnace recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("furnace-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("furnace-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addFurnaceRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load blast furnace recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("blast-furnace-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("blast-furnace-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addBlastingRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load smoker recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("smoker-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("smoker-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addSmokingRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load campfire recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("campfire-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("campfire-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addCampfireRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load smithing recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("smithing-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("smithing-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addSmithingRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load anvil recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("anvil-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("anvil-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addAnvilRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }
        // load potion recipes; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("potion-recipes")) {
            ConfigurationSection recipesSection = plantConfig.getConfigurationSection("potion-recipes");
            for (String recipeName : recipesSection.getKeys(false)) {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                addPotionRecipe(recipeSection, String.format("%s_%s",plantId,recipeName));
            }
        }

        // add plant to plant type manager
        performantPlants.getPlantTypeManager().addPlantType(plant);
        performantPlants.getLogger().info("Successfully loaded plant: " + plantId);
    }

    void loadVanillaDropConfig() {
        String fileName = "vanilla_drops.yml";
        // vanilla drops config
        File vanillaDropsFile = new File(performantPlants.getDataFolder(), fileName);
        // create file if doesn't exist
        if (!vanillaDropsFile.exists()) {
            performantPlants.saveResource(fileName, false);
        }
        try {
            vanillaDropsConfig.load(vanillaDropsFile);
        } catch (Exception e) {
            performantPlants.getLogger().severe("Error occurred trying to load " + fileName);
            e.printStackTrace();
        }
        if (!vanillaDropsConfig.isConfigurationSection("vanilla-drops")) {
            return;
        }
        ConfigurationSection vanillaDropsSection = vanillaDropsConfig.getConfigurationSection("vanilla-drops");
        for (String placeholder : vanillaDropsSection.getKeys(false)) {
            ConfigurationSection dropSection = vanillaDropsSection.getConfigurationSection(placeholder);
            if (dropSection == null) {
                performantPlants.getLogger().warning("Vanilla drop section was null in section: " + dropSection.getCurrentPath());
                return;
            }
            if (!dropSection.isString("type")) {
                performantPlants.getLogger().warning("Vanilla drop section does not include type in section: " + dropSection.getCurrentPath());
                return;
            }
            String type = dropSection.getString("type");
            if (type != null) {
                if (type.equalsIgnoreCase("block")) {
                    addVanillaBlockDrop(dropSection);
                } else if (type.equalsIgnoreCase("entity")) {
                    addVanillaEntityDrop(dropSection);
                }
            }
        }
    }

    void addVanillaBlockDrop(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        if (!section.isString("material")) {
            performantPlants.getLogger().warning("Vanilla block drop not added; no entity-type provided");
            return;
        }
        String name = section.getString("material");
        Material material = Material.getMaterial(name.toUpperCase());
        if (material == null) {
            performantPlants.getLogger().warning(String.format("Vanilla block drop not added; material '%s' not recognized", name));
            return;
        }
        // set interact
        ConfigurationSection onDropSection = section.getConfigurationSection("on-drop");
        if (onDropSection == null) {
            performantPlants.getLogger().warning("Vanilla block drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
        }
        PlantInteractStorage storage = loadPlantInteractStorage(onDropSection);
        if (storage == null) {
            performantPlants.getLogger().warning("Vanilla block drop not added; issue reading contents of on-drop section");
        }
        // add to vanilla drop manager
        performantPlants.getVanillaDropManager().addInteract(material, storage);
        performantPlants.getLogger().info("Added vanilla drop behavior for block: " + material.toString());
    }

    void addVanillaEntityDrop(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        if (!section.isString("entity-type")) {
            performantPlants.getLogger().warning("Vanilla entity drop not added; no entity provided");
            return;
        }
        String name = section.getString("entity-type");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            performantPlants.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' not recognized", name));
            return;
        }
        if (!entityType.isAlive()) {
            performantPlants.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' is not alive", name));
            return;
        }
        // set interact
        ConfigurationSection onDropSection = section.getConfigurationSection("on-drop");
        if (onDropSection == null) {
            performantPlants.getLogger().warning("Vanilla entity drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
        }
        PlantInteractStorage storage = loadPlantInteractStorage(onDropSection);
        if (storage == null) {
            performantPlants.getLogger().warning("Vanilla entity drop not added; issue reading contents of on-drop section");
        }
        // add to vanilla drop manager
        performantPlants.getVanillaDropManager().addInteract(entityType, storage);
        performantPlants.getLogger().info("Added vanilla drop behavior for entity: " + entityType.getKey().getKey().toUpperCase());
    }

    ItemSettings loadItemConfig(ConfigurationSection section, boolean allowLink) {
        // check if section exists
        if (section != null) {
            ItemSettings linkedItemSettings = null;
            String plantId = null;
            // if link to another item is allowed, check for it
            if (allowLink) {
                if (section.isSet("link")) {
                    String link = section.getString("link");
                    if (link == null) {
                        performantPlants.getLogger().warning("link string could not be read in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if set, need to load from appropriate config section
                    String[] plantInfo = link.split("\\.", 2);
                    plantId = plantInfo[0];
                    String itemString = "";
                    if (plantInfo.length > 1) {
                        itemString = plantInfo[1];
                    }
                    // load plant item from config file
                    YamlConfiguration linkedPlantConfig = plantConfigMap.get(plantId);
                    if (linkedPlantConfig == null) {
                        performantPlants.getLogger().warning("PlantId '" + plantId + "' does not match any plant config in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if item, get item
                    if (itemString.equals("")) {
                        if (!linkedPlantConfig.isConfigurationSection("item")) {
                            performantPlants.getLogger().warning(String.format("Linked plant %s does not contain item section",
                                    plantId));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection("item");
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else if (itemString.equalsIgnoreCase("seed")) {
                        // if seed, get growing.seed-item
                        if (!linkedPlantConfig.isConfigurationSection("growing.seed-item")) {
                            performantPlants.getLogger().warning(String.format("Linked plant %s does not contain item section",
                                    plantId));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection("growing.seed-item");
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else if (!itemString.endsWith("goods")) {
                        String goodId = itemString;
                        itemString = "goods." + itemString;
                        if (!linkedPlantConfig.isConfigurationSection(itemString)) {
                            performantPlants.getLogger().warning(String.format("Linked plant %s does not contain good %s", plantId, goodId));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection(itemString);
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else {
                        performantPlants.getLogger().warning("Linked item was neither item, seed, or good in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if linkedItem settings null, return null
                    if (linkedItemSettings == null) {
                        performantPlants.getLogger().warning("Linked item could not be loaded from item section: " + section.getCurrentPath());
                        return null;
                    }
                }
            }
            // get item-related details from section
            String materialName = section.getString("material");
            if (materialName == null && linkedItemSettings == null) {
                performantPlants.getLogger().warning("Material not provided in item section: " + section.getCurrentPath());
                return null;
            }
            Material material = Material.getMaterial(materialName);
            if (material == null && linkedItemSettings == null) {
                performantPlants.getLogger().warning("Material '" + materialName + "' not recognized in item section: " + section.getCurrentPath());
                return null;
            }
            int amount = -1;
            if (section.isInt("amount")) {
                int readAmount = section.getInt("amount");
                if (readAmount > 0) {
                    amount = readAmount;
                }
            }
            String skullTexture = section.getString("skull-texture");
            String displayName = TextHelper.translateAlternateColorCodes(section.getString("display-name"));
            List<String> lore = TextHelper.translateAlternateColorCodes(section.getStringList("lore"));

            // return linked item settings, if present
            if (linkedItemSettings != null) {
                linkedItemSettings.setAmount(amount);
                return new ItemSettings(linkedItemSettings.generatePlantItemStack(plantId));
            }

            ItemSettings finalItemSettings = new ItemSettings(material, displayName, lore, skullTexture, amount);
            if (section.isInt("damage")) {
                finalItemSettings.setDamage(Math.max(0, section.getInt("damage")));
            }
            if (section.isBoolean("unbreakable")) {
                finalItemSettings.setUnbreakable(section.getBoolean("unbreakable"));
            }
            // get item flags, if present
            List<String> itemFlagStrings = section.getStringList("flags");
            for (String itemFlagString : itemFlagStrings) {
                ItemFlag flag;
                try {
                    flag = ItemFlag.valueOf(itemFlagString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("ItemFlag '%s' not recognized in item section: %s", itemFlagString, section.getCurrentPath()));
                    return null;
                }
                finalItemSettings.addItemFlag(flag);
            }
            // get enchantments -> Enchantment:Level, if present
            List<String> enchantmentStrings = section.getStringList("enchantments");
            for (String enchantmentString : enchantmentStrings) {
                String[] splitString = enchantmentString.split(":");
                if (splitString.length > 2 || splitString.length == 0) {
                    performantPlants.getLogger().warning("Enchantment string was invalid in item section: " + section.getCurrentPath());
                    continue;
                }
                String enchantmentName = splitString[0].toLowerCase();
                int level = 1;
                // set custom level, if present
                if (splitString.length == 2) {
                    try {
                        level = Math.max(1, Integer.parseInt(splitString[1]));
                    } catch (NumberFormatException e) {
                        performantPlants.getLogger().warning("Enchantment level was not an integer in item section: " + section.getCurrentPath());
                        continue;
                    }
                }
                // get enchantment
                Enchantment enchantment;
                try {
                    enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentName));
                } catch (IllegalArgumentException e) {
                    enchantment = null;
                }
                if (enchantment == null) {
                    performantPlants.getLogger().warning(String.format("Enchantment '%s' not recognized in item section: %s",
                            enchantmentName, section.getCurrentPath()));
                    continue;
                }
                finalItemSettings.addEnchantmentLevel(new EnchantmentLevel(enchantment, level));
            }
            // get potion effects, if present
            List<PotionEffect> potionEffects = loadPotionEffects(section);
            for (PotionEffect potionEffect : potionEffects) {
                finalItemSettings.addPotionEffect(potionEffect);
            }
            // get potion color, if present
            if (section.isConfigurationSection("potion-color")) {
                ConfigurationSection colorSection = section.getConfigurationSection("potion-color");
                if (colorSection != null) {
                    Color potionColor = createColor(colorSection, null).getColor(null, null);
                    if (potionColor != null) {
                        finalItemSettings.setPotionColor(potionColor);
                    }
                }
            }
            // get base potion data, if present
            if (section.isConfigurationSection("potion-data")) {
                ConfigurationSection potionDataSection = section.getConfigurationSection("potion-data");
                if (potionDataSection != null) {
                    PotionData potionData = createPotionData(potionDataSection);
                    if (potionData != null) {
                        finalItemSettings.setPotionData(potionData);
                    }
                }
            }
            // get custom model data, if present
            if (section.isInt("custom-model-data")) {
                int customModelData = section.getInt("custom-model-data");
                if (customModelData < 0) {
                    performantPlants.getLogger().warning(String.format("CustomModelData '%d' cannot be less than 0 in section: %s",
                            customModelData, section.getCurrentPath()));
                    return null;
                }
                finalItemSettings.setCustomModelData(customModelData);
            }
            return finalItemSettings;
        }
        return null;
    }

    BlockSettings loadBlockConfig(ConfigurationSection section) {
        if (section != null) {
            // get block-related details from section
            String materialName = "STONE";
            if (section.isString("material")) {
                materialName = section.getString("material");
                if (materialName == null) {
                    performantPlants.getLogger().warning("Material not provided in block section: " + section.getCurrentPath());
                    return null;
                }
            }
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                performantPlants.getLogger().warning("Material '" + materialName + "' not recognized in block section: " + section.getCurrentPath());
                return null;
            }

            // get skull texture
            String skullTexture = section.getString("skull-texture");
            ArrayList<String> blockDataStrings = new ArrayList<>(section.getStringList("data"));
            // get offset
            int xRel = 0;
            int yRel = 0;
            int zRel = 0;
            if (section.isSet("offset")) {
                if (section.isInt("offset.x")) {
                    xRel = section.getInt("offset.x");
                }
                if (section.isInt("offset.y")) {
                    yRel = section.getInt("offset.y");
                }
                if (section.isInt("offset.z")) {
                    zRel = section.getInt("offset.z");
                }
            }
            // create BlockSettings from values
            BlockSettings blockSettings = new BlockSettings(xRel, yRel, zRel, material);
            if (skullTexture != null) {
                blockSettings.setSkullTexture(skullTexture);
            }
            if (!blockDataStrings.isEmpty()) {
                blockSettings.setBlockDataStrings(blockDataStrings);
            }
            return blockSettings;
        }
        return null;
    }

    DropSettings loadDropConfig(ConfigurationSection section, PlantData data) {
        if (section != null) {
            DropSettings dropSettings = new DropSettings();
            // set min and max amounts, if present
            if (section.isSet("max-amount")) {
                ScriptBlock value = createPlantScript(section, "max-amount", data);
                if (value == null || !ScriptHelper.isLong(value)) {
                    performantPlants.getLogger().warning(String.format("max-amount value could not be read or was not ScriptType LONG in drop section: %s",
                            section.getCurrentPath()));
                    return null;
                } else {
                    dropSettings.setMaxAmount(value);
                }
            }
            if (section.isSet("min-amount")) {
                ScriptBlock value = createPlantScript(section, "min-amount", data);
                if (value == null || !ScriptHelper.isLong(value)) {
                    performantPlants.getLogger().warning(String.format("min-amount value could not be read or was not ScriptType LONG in drop section: %s",
                            section.getCurrentPath()));
                    return null;
                } else {
                    dropSettings.setMinAmount(value);
                }
            }
            // set drop do-if, if present
            if (section.isSet("do-if")) {
                ScriptBlock value = createPlantScript(section, "do-if", data);
                if (value == null || !ScriptHelper.isBoolean(value)) {
                    performantPlants.getLogger().warning(String.format("do-if value could not be read or was not ScriptType BOOLEAN in drop section: %s",
                            section.getCurrentPath()));
                    return null;
                } else {
                    dropSettings.setDoIf(value);
                }
            }
            // set ItemSettings
            if (!section.isSet("item")) {
                performantPlants.getLogger().warning("item not set for drop section: " + section.getCurrentPath());
                return null;
            }
            ItemSettings itemSettings = loadItemConfig(section.getConfigurationSection("item"),true);
            if (itemSettings == null) {
                performantPlants.getLogger().warning("itemSettings could not be created for drop section: " + section.getCurrentPath());
            }
            dropSettings.setItemSettings(itemSettings);
            return dropSettings;
        }
        return null;
    }

    List<PotionEffect> loadPotionEffects(ConfigurationSection section) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        // get potion effects -> PotionEffectType:Duration:Amplifier, if present
        List<String> potionStrings = section.getStringList("potion-effects");
        for (String potionString : potionStrings) {
            String[] splitString = potionString.split(":");
            if (splitString.length > 3 || splitString.length < 2) {
                performantPlants.getLogger().warning("Potion string was invalid in section: " + section.getCurrentPath());
                continue;
            }
            String potionName = splitString[0].toUpperCase();
            // get potion name
            PotionEffectType potionEffectType = PotionEffectType.getByName(potionName);
            if (potionEffectType == null) {
                performantPlants.getLogger().warning(String.format("Potion '%s' not recognized in section: %s",
                        potionName, section.getCurrentPath()));
                continue;
            }
            // get duration
            int duration;
            try {
                duration = Math.max(1, Integer.parseInt(splitString[1]));
            } catch (NumberFormatException e) {
                performantPlants.getLogger().warning("Potion duration was not an integer in section: " + section.getCurrentPath());
                continue;
            }
            int amplifier = 0;
            if (splitString.length == 3) {
                try {
                    amplifier = Math.max(0, Integer.parseInt(splitString[2])-1);
                } catch (NumberFormatException e) {
                    performantPlants.getLogger().warning("Potion amplifier was not an integer in section: " + section.getCurrentPath());
                    continue;
                }
            }
            potionEffects.add(new PotionEffect(potionEffectType, duration, amplifier));
        }
        return potionEffects;
    }

    List<GrowthStageBlock> loadGrowthStageBlocks(ConfigurationSection section, String sectionName, GrowthStage growthStage, PlantData data) {
        if (section == null) {
            return null;
        }
        ArrayList<GrowthStageBlock> blocks = new ArrayList<>();
        ConfigurationSection blocksConfig = section.getConfigurationSection(sectionName);
        if (blocksConfig == null) {
            return blocks;
        }
        for (String blockName : blocksConfig.getKeys(false)) {
            ConfigurationSection blockConfig = blocksConfig.getConfigurationSection(blockName);
            if (blockConfig == null) {
                performantPlants.getLogger().warning("Could not load stage's blockConfig in section: " + blocksConfig.getCurrentPath());
                return null;
            }
            BlockSettings blockSettings = loadBlockConfig(blockConfig.getConfigurationSection("block-data"));
            if (blockSettings == null) {
                performantPlants.getLogger().warning("blockSettings for growth block returned null in section: " + blockConfig.getCurrentPath());
                return null;
            }
            GrowthStageBlock growthStageBlock;
            try {
                growthStageBlock = new GrowthStageBlock(
                        blockName,
                        blockSettings.getXRel(),
                        blockSettings.getYRel(),
                        blockSettings.getZRel(),
                        blockSettings.getMaterial(),
                        blockSettings.getBlockDataStrings(),
                        blockSettings.getSkullTexture()
                );
            } catch (IllegalArgumentException e) {
                performantPlants.getLogger().warning(String.format("Could not create growth stage block in section %s due to: %s",
                        blockConfig.getCurrentPath(), e.getMessage()));
                return null;
            }
            // set ignore space, if present
            if (blockConfig.isBoolean("ignore-space")) {
                growthStageBlock.setIgnoreSpace(blockConfig.getBoolean("ignore-space"));
            }
            // set break children, if present
            if (blockConfig.isBoolean("break-children")) {
                growthStageBlock.setBreakChildren(blockConfig.getBoolean("break-children"));
            }
            // set break parent, if present
            if (blockConfig.isBoolean("break-parent")) {
                growthStageBlock.setBreakParent(blockConfig.getBoolean("break-parent"));
            }
            // set update stage on break, if present
            if (blockConfig.isBoolean("regrow")) {
                growthStageBlock.setUpdateStageOnBreak(blockConfig.getBoolean("regrow"));
            }
            // set random rotation, if present
            if (blockConfig.isBoolean("random-rotation")) {
                growthStageBlock.setRandomOrientation(blockConfig.getBoolean("random-rotation"));
            }
            // set placed orientation, if present
            if (blockConfig.isBoolean("placed-rotation")) {
                growthStageBlock.setPlacedOrientation(blockConfig.getBoolean("placed-rotation"));
            }
            // set replace plant block, if present
            if (blockConfig.isBoolean("replace-plant-block")) {
                growthStageBlock.setReplacePlantBlock(blockConfig.getBoolean("replace-plant-block"));
            }
            // set replace vanilla block, if present
            if (blockConfig.isBoolean("replace-vanilla-block")) {
                growthStageBlock.setReplaceVanillaBlock(blockConfig.getBoolean("replace-vanilla-block"));
            }
            // set replace vanilla block, if present
            if (blockConfig.isBoolean("vanilla-block")) {
                growthStageBlock.setVanillaBlock(blockConfig.getBoolean("vanilla-block"));
            }
            // set childOf, if present
            if (blockConfig.isSet("child-of")) {
                if (!blockConfig.isConfigurationSection("child-of")
                        || !blockConfig.isInt("child-of.x")
                        || !blockConfig.isInt("child-of.y")
                        || !blockConfig.isInt("child-of.z")) {
                    performantPlants.getLogger().warning("child-of is not configured properly in section: " + section.getCurrentPath());
                    return null;
                }
                growthStageBlock.setChildOf(new RelativeLocation(
                        blockConfig.getInt("child-of.x"),
                        blockConfig.getInt("child-of.y"),
                        blockConfig.getInt("child-of.z")
                ));
            }
            // set drops, if present
            if (blockConfig.isSet("drops")) {
                // add drops
                boolean valid = addDropsToDropStorage(blockConfig, growthStageBlock.getDropStorage(), data);
                if (!valid) {
                    return null;
                }
                // if no limit defined for growthStageBlock but is defined for growth stage, apply it
                if (growthStage != null) {
                    if (!growthStageBlock.getDropStorage().isDropLimitSet() && growthStage.getDropStorage().isDropLimitSet()) {
                        growthStageBlock.getDropStorage().setDropLimit(growthStage.getDropStorage().getDropLimit());
                    }
                }
            }
            // set interact behavior, if present
            if (blockConfig.isConfigurationSection("on-interact")) {
                ConfigurationSection onInteractSection = blockConfig.getConfigurationSection("on-interact");
                PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onInteractSection, data);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-interact section: " + onInteractSection.getCurrentPath());
                    return null;
                }
                // add interactions to growth stage block
                growthStageBlock.setOnInteract(plantInteractStorage);
            }
            // set click behavior, if present
            if (blockConfig.isConfigurationSection("on-click")) {
                ConfigurationSection onClickSection = blockConfig.getConfigurationSection("on-click");
                PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onClickSection, data);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-click section: " + onClickSection.getCurrentPath());
                    return null;
                }
                // add interactions to growth stage block
                growthStageBlock.setOnClick(plantInteractStorage);
            }
            // set break behavior, if present
            if (blockConfig.isConfigurationSection("on-break")) {
                ConfigurationSection onBreakSection = blockConfig.getConfigurationSection("on-break");
                PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onBreakSection, data);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-break section: " + onBreakSection.getCurrentPath());
                    return null;
                }
                // add interactions to growth stage block
                growthStageBlock.setOnBreak(plantInteractStorage);
            }
            // add growth stage block to stage
            blocks.add(growthStageBlock);
        }
        return blocks;
    }

    PlantInteractStorage loadPlantInteractStorage(ConfigurationSection section) {
        return loadPlantInteractStorage(section, null);
    }

    PlantInteractStorage loadPlantInteractStorage(ConfigurationSection section, PlantData data) {
        PlantInteractStorage plantInteractStorage = new PlantInteractStorage();
        // add default interaction, if present
        if (section.isConfigurationSection("default")) {
            ConfigurationSection defaultInteractSection = section.getConfigurationSection("default");
            // load interaction from default path
            PlantInteract plantInteract = loadPlantInteract(defaultInteractSection, data);
            if (plantInteract == null) {
                performantPlants.getLogger().warning("Default PlantInteract could not be loaded from section: " + defaultInteractSection.getCurrentPath());
                return null;
            }
            plantInteractStorage.setDefaultInteract(plantInteract);
        }
        // add item interactions, if present
        if (section.isConfigurationSection("items")) {
            ConfigurationSection itemsInteractSection = section.getConfigurationSection("items");
            for (String placeholder : itemsInteractSection.getKeys(false)) {
                ConfigurationSection itemInteractSection = itemsInteractSection.getConfigurationSection(placeholder);
                if (itemInteractSection == null) {
                    performantPlants.getLogger().warning("Item PlantInteract section was null from section: " + itemsInteractSection.getCurrentPath());
                    return null;
                }
                ConfigurationSection itemSection = itemInteractSection.getConfigurationSection("item");
                if (itemSection == null) {
                    performantPlants.getLogger().warning("Item section not present in PlantInteract section from section: " + itemInteractSection.getCurrentPath());
                    return null;
                }
                ItemSettings itemInteractSettings = loadItemConfig(itemSection, true);
                if (itemInteractSettings == null) {
                    return null;
                }
                PlantInteract plantInteract = loadPlantInteract(itemInteractSection, data);
                if (plantInteract == null) {
                    performantPlants.getLogger().warning("Item PlantInteract could not be loaded from section: " + itemInteractSection.getCurrentPath());
                    return null;
                }
                // set interact's item stack
                plantInteract.setItemStack(itemInteractSettings.generateItemStack());
                plantInteractStorage.addPlantInteract(plantInteract);
            }
        }
        return plantInteractStorage;
    }

    PlantInteract loadPlantInteract(ConfigurationSection section, PlantData data) {
        if (section == null) {
            return null;
        }
        PlantInteract plantInteract = new PlantInteract();
        // set if block should break on interact, if present
        if (section.isSet("break-block")) {
            ScriptBlock value = createPlantScript(section, "break-block", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen break-block value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isBreakBlock(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setBreakBlock(value);
            }
        }
        if (section.isSet("only-break-block-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-break-block-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen only-break-block-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyBreakBlockOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyBreakBlockOnDo(value);
            }
        }

        // set if interaction should give block drops, if present
        if (section.isSet("give-block-drops")) {
            ScriptBlock value = createPlantScript(section, "give-block-drops", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen give-block-drops value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isGiveBlockDrops(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setGiveBlockDrops(value);
            }
        }

        // set if should take item, if present
        if (section.isSet("take-item")) {
            ScriptBlock value = createPlantScript(section, "take-item", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen take-item value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isTakeItem(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setTakeItem(value);
            }
        }
        if (section.isSet("only-take-item-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-take-item-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen only-take-item-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyTakeItemOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyTakeItemOnDo(value);
            }
        }

        // set match type only, if present
        if (section.isBoolean("match-material")) {
            plantInteract.setMatchMaterial(section.getBoolean("match-material"));
        }
        // set match enchantments only, if present
        if (section.isBoolean("match-enchantments")) {
            plantInteract.setMatchEnchantments(section.getBoolean("match-enchantments"));
        }
        // set match enchantment level, if present
        if (section.isBoolean("match-enchantment-level")) {
            plantInteract.setMatchEnchantmentLevel(section.getBoolean("match-enchantment-level"));
        }
        // set required block faces, if present
        if (section.isList("required-block-faces")) {
            for (String name : section.getStringList("required-block-faces")) {
                BlockFace blockFace;
                try {
                    blockFace = BlockFace.valueOf(name);
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("BlockFace '%s' not recognized in item section: %s",
                            name, section.getCurrentPath()));
                    return null;
                }
                if (!BlockHelper.isOmnidirectionalBlockFace(blockFace)) {
                    performantPlants.getLogger().warning(String.format("BlockFace '%s' is not a valid omnidirectional block face " +
                                    "(UP,DOWN,EAST,WEST,NORTH,SOUTH) in item section: %s",
                            name, section.getCurrentPath()));
                    return null;
                }
                plantInteract.addRequiredBlockFace(blockFace);
            }
        }

        // set condition for match, if present
        if (section.isSet("condition")) {
            ScriptBlock value = createPlantScript(section, "condition", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen condition value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isConditionMet(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setCondition(value);
            }
        }

        // set condition for doing actions, if present
        if (section.isSet("do-if")) {
            ScriptBlock value = createPlantScript(section, "do-if", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen do-if value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.generateDoIf(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setDoIf(value);
            }
        }

        // set if effects should only happen on successful chance, if present
        if (section.isSet("only-effects-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-effects-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen only-effects-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyEffectsOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyEffectsOnDo(value);
            }
        }
        if (section.isSet("only-consumable-effects-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-consumable-effects-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen only-consumable-effects-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyConsumableEffectsOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyConsumableEffectsOnDo(value);
            }
        }

        // add drops, if present
        if (section.isConfigurationSection("drops")) {
            boolean valid = addDropsToDropStorage(section, plantInteract.getDropStorage(), data);
            if (!valid) {
                return null;
            }
        }
        if (section.isSet("only-drop-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-drop-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Interact will not have chosen only-drop-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyDropOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyDropOnDo(value);
            }
        }

        // add effects, if present
        addEffectsToEffectStorage(section, plantInteract.getEffectStorage(), data);
        // add consumable, if present
        if (section.isConfigurationSection("consumable")) {
            PlantConsumableStorage consumable = loadPlantConsumableStorage(section.getConfigurationSection("consumable"), data);
            if (consumable != null) {
                plantInteract.setConsumableStorage(consumable);
            }
        }
        // add script blocks, if present
        ScriptBlock scriptBlock = createPlantScript(section, "plant-script", data);
        if (scriptBlock != null) {
            plantInteract.setScriptBlock(scriptBlock);
        }
        scriptBlock = createPlantScript(section, "plant-script-on-do", data);
        if (scriptBlock != null) {
            plantInteract.setScriptBlockOnDo(scriptBlock);
        }
        scriptBlock = createPlantScript(section, "plant-script-on-not-do", data);
        if (scriptBlock != null) {
            plantInteract.setScriptBlockOnNotDo(scriptBlock);
        }

        return plantInteract;
    }

    PlantConsumableStorage loadPlantConsumableStorage(ConfigurationSection section, PlantData data) {
        if (section == null) {
            return null;
        }
        PlantConsumableStorage consumableStorage = new PlantConsumableStorage();
        for (String placeholder : section.getKeys(false)) {
            if (section.isConfigurationSection(placeholder)) {
                PlantConsumable consumable = loadPlantConsumable(section.getConfigurationSection(placeholder), data);
                if (consumable != null) {
                    consumableStorage.addConsumable(consumable);
                }
            }
        }
        if (consumableStorage.getConsumableList().isEmpty()) {
            return null;
        }
        return consumableStorage;
    }

    PlantConsumable loadPlantConsumable(ConfigurationSection section, PlantData data) {
        if (section == null) {
            return null;
        }
        PlantConsumable consumable = new PlantConsumable();
        // set take item, if present
        if (section.isSet("take-item")) {
            ScriptBlock value = createPlantScript(section, "take-item", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen take-item value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isTakeItem(null, null), section.getCurrentPath()));
            } else {
                consumable.setTakeItem(value);
            }
        }
        if (section.isSet("only-take-item-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-take-item-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen only-take-item-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isOnlyTakeItemOnDo(null, null), section.getCurrentPath()));
            } else {
                consumable.setOnlyTakeItemOnDo(value);
            }
        }
        // set missing food, if present
        if (section.isSet("missing-food")) {
            ScriptBlock value = createPlantScript(section, "missing-food", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen missing-food value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isMissingFood(null, null), section.getCurrentPath()));
            } else {
                consumable.setMissingFood(value);
            }
        }
        // set normal eat, if present
        if (section.isSet("normal-eat")) {
            ScriptBlock value = createPlantScript(section, "normal-eat", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen normal-eat value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isNormalEat(null, null), section.getCurrentPath()));
            } else {
                consumable.setNormalEat(value);
            }
        }
        // set damage to add to item, if present
        if (section.isSet("add-damage")) {
            ScriptBlock value = createPlantScript(section, "add-damage", data);
            if (value == null || !ScriptHelper.isLong(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen add-damage value and instead will be" +
                                " %n; must be ScriptType LONG in section: %s",
                        consumable.getAddDamage(null, null), section.getCurrentPath()));
            } else {
                consumable.setAddDamage(value);
            }
        }
        if (section.isSet("only-add-damage-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-add-damage-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen only-add-damage-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isOnlyAddDamageOnDo(null, null), section.getCurrentPath()));
            } else {
                consumable.setOnlyAddDamageOnDo(value);
            }
        }
        // set give item, if present
        if (section.isConfigurationSection("give-items")) {
            ConfigurationSection itemsSection = section.getConfigurationSection("give-items");
            for (String placeholder : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(placeholder);
                ItemSettings itemSettings = loadItemConfig(itemSection, true);
                if (itemSettings == null) {
                    performantPlants.getLogger().warning(String.format("Problem getting give-items in consumable section %s;" +
                                    "will continue to load, but this consumable method will not be available (fix config)",
                            itemSection.getCurrentPath()));
                    return null;
                }
                consumable.addItemToGive(itemSettings.generateItemStack());
            }
        }
        if (section.isSet("only-give-items-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-give-items-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen only-give-items-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isOnlyGiveItemsOnDo(null, null), section.getCurrentPath()));
            } else {
                consumable.setOnlyGiveItemsOnDo(value);
            }
        }
        // set condition for match, if present
        if (section.isSet("condition")) {
            ScriptBlock value = createPlantScript(section, "condition", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen condition value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isConditionMet(null, null), section.getCurrentPath()));
            } else {
                consumable.setCondition(value);
            }
        }
        // set required items, if present
        if (section.isConfigurationSection("required-items")) {
            ConfigurationSection requiredItemsSection = section.getConfigurationSection("required-items");
            for (String placeholder : requiredItemsSection.getKeys(false)) {
                ConfigurationSection requiredItemSection = requiredItemsSection.getConfigurationSection(placeholder);
                // if section can't be found, don't load consumable and return null
                if (requiredItemSection == null) {
                    performantPlants.getLogger().warning(String.format("Section not found for required item '%s' in section: %s;" +
                                    "will continue to load, but item won't be consumable (fix config)",
                            placeholder, requiredItemsSection));
                    return null;
                }
                RequiredItem requiredItem = loadRequiredItem(requiredItemSection, placeholder, data);
                // if null, don't load consumable and return null
                if (requiredItem == null) {
                    return null;
                }
                consumable.addRequiredItem(requiredItem);
            }
        }
        if (section.isSet("only-take-required-items-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-take-required-items-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen only-take-required-items-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isOnlyTakeRequiredItemsOnDo(null, null), section.getCurrentPath()));
            } else {
                consumable.setOnlyTakeRequiredItemsOnDo(value);
            }
        }

        // add effects, if present
        addEffectsToEffectStorage(section, consumable.getEffectStorage(), data);

        // set if effects should only happen on successful chance, if present
        if (section.isSet("only-effects-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-effects-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen only-effects-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.isOnlyEffectsOnDo(null, null), section.getCurrentPath()));
            } else {
                consumable.setOnlyEffectsOnDo(value);
            }
        }

        // set condition for doing actions, if present
        if (section.isSet("do-if")) {
            ScriptBlock value = createPlantScript(section, "do-if", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("Consumable will not have chosen do-if value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        consumable.generateDoIf(null, null), section.getCurrentPath()));
            } else {
                consumable.setDoIf(value);
            }
        }

        // add script blocks, if present
        ScriptBlock scriptBlock = createPlantScript(section, "plant-script", data);
        if (scriptBlock != null) {
            consumable.setScriptBlock(scriptBlock);
        }
        scriptBlock = createPlantScript(section, "plant-script-on-do", data);
        if (scriptBlock != null) {
            consumable.setScriptBlockOnDo(scriptBlock);
        }
        scriptBlock = createPlantScript(section, "plant-script-on-not-do", data);
        if (scriptBlock != null) {
            consumable.setScriptBlockOnNotDo(scriptBlock);
        }
        // return consumable
        return consumable;
    }

    RequiredItem loadRequiredItem(ConfigurationSection section, String sectionName, PlantData data) {
        // set item
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection == null) {
            performantPlants.getLogger().warning(String.format("No item section found for required item '%s' in section: %s;" +
                            "will continue to load, but item won't be consumable (fix config)",
                    sectionName, section));
            return null;
        }
        ItemSettings itemSettings = loadItemConfig(itemSection, true);
        if (itemSettings == null) {
            performantPlants.getLogger().warning(String.format("Problem getting required item in section %s;" +
                            "will continue to load, but this item will not be consumable (fix config)",
                    itemSection.getCurrentPath()));
            return null;
        }
        // create required item from item
        RequiredItem requiredItem = new RequiredItem(itemSettings.generateItemStack());
        // set take item, if set
        if (section.isSet("take-item")) {
            ScriptBlock value = createPlantScript(section, "take-item", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("RequiredItem will not have chosen take-item value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        requiredItem.isTakeItem(null, null), section.getCurrentPath()));
            } else {
                requiredItem.setTakeItem(value);
            }
        }
        // set if required item should be in hand (offhand or main hand), if set
        if (section.isSet("in-hand")) {
            ScriptBlock value = createPlantScript(section, "in-hand", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("RequiredItem will not have chosen in-hand value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        requiredItem.isInHand(null, null), section.getCurrentPath()));
            } else {
                requiredItem.setInHand(value);
            }
        }
        // set damage to add to item, if present
        if (section.isSet("add-damage")) {
            ScriptBlock value = createPlantScript(section, "add-damage", data);
            if (value == null || !ScriptHelper.isLong(value)) {
                performantPlants.getLogger().warning(String.format("RequiredItem will not have chosen add-damage value and instead will be" +
                                " %n; must be ScriptType LONG in section: %s",
                        requiredItem.getAddDamage(null, null), section.getCurrentPath()));
            } else {
                requiredItem.setAddDamage(value);
            }
        }
        // set condition for match, if present
        if (section.isSet("condition")) {
            ScriptBlock value = createPlantScript(section, "condition", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                performantPlants.getLogger().warning(String.format("RequiredItem will not have chosen condition value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        requiredItem.isConditionMet(null, null), section.getCurrentPath()));
            } else {
                requiredItem.setCondition(value);
            }
        }
        return requiredItem;
    }

    ScriptColor createColor(ConfigurationSection section, PlantData data) {
        ScriptColor color = new ScriptColor();
        if (section.isSet("r")) {
            ScriptBlock red = createPlantScript(section, "r", data);
            if (red == null || !ScriptHelper.isLong(red)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen red value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getRed().loadValue(null, null).getIntegerValue(), section.getCurrentPath()));
            }
            color.setRed(red);
        }
        if (section.isSet("g")) {
            ScriptBlock green = createPlantScript(section, "g", data);
            if (green == null || !ScriptHelper.isLong(green)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen green value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getGreen().loadValue(null, null).getIntegerValue(), section.getCurrentPath()));
            }
            color.setGreen(green);
        }
        if (section.isSet("b")) {
            ScriptBlock blue = createPlantScript(section, "b", data);
            if (blue == null || !ScriptHelper.isLong(blue)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen blue value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getBlue().loadValue(null, null).getIntegerValue(), section.getCurrentPath()));
            }
            color.setBlue(blue);
        }
        return color;
    }

    PotionData createPotionData(ConfigurationSection section) {
        PotionType potionType;
        boolean extended = false;
        boolean upgraded = false;
        if (section.isString("type")) {
            String name = section.getString("type");
            try {
                potionType = PotionType.valueOf(name);
            } catch (IllegalArgumentException e) {
                performantPlants.getLogger().warning(String.format("PotionType '%s' not recognized in item section: %s",
                        name, section.getCurrentPath()));
                return null;
            }
        } else {
            performantPlants.getLogger().warning("PotionType not set in item section: " + section.getCurrentPath());
            return null;
        }
        // get extended, if present
        if (section.isBoolean("extended")) {
            extended = section.getBoolean("extended");
        }
        // get upgraded, if present
        if (section.isBoolean("upgraded")) {
            upgraded = section.getBoolean("upgraded");
        }
        return new PotionData(potionType, extended, upgraded);
    }

    boolean addRequirementsToStorage(ConfigurationSection section, RequirementStorage requirementStorage) {
        if (section == null) {
            return false;
        }
        if (requirementStorage == null) {
            performantPlants.getLogger().warning("No RequirementStorage provided in section: " + section.getCurrentPath());
            return false;
        }
        // set water requirement, if present
        if (section.isBoolean("water-required")) {
            requirementStorage.setWaterRequired(section.getBoolean("water-required"));
        }
        // set lava requirement, if present
        if (section.isBoolean("lava-required")) {
            requirementStorage.setLavaRequired(section.getBoolean("lava-required"));
        }
        // set light requirement, if present
        if (section.isInt("light-minimum")) {
            requirementStorage.setLightLevelMinimum(section.getInt("light-minimum"));
        }
        if (section.isInt("light-maximum")) {
            requirementStorage.setLightLevelMaximum(section.getInt("light-maximum"));
        }
        // set time requirements, if present
        if (section.isInt("time-minimum")) {
            requirementStorage.setTimeMinimum(section.getLong("time-minimum"));
        }
        if (section.isInt("time-maximum")) {
            requirementStorage.setTimeMaximum(section.getLong("time-maximum"));
        }
        // set temperature requirements, if present
        if (section.isInt("temperature-minimum") || section.isDouble("temperature-minimum")) {
            requirementStorage.setTemperatureMinimum(section.getDouble("temperature-minimum"));
        }
        if (section.isInt("temperature-maximum") || section.isDouble("temperature-maximum")) {
            requirementStorage.setTemperatureMaximum(section.getDouble("temperature-maximum"));
        }
        // set world requirements, if present
        if (section.isList("world-whitelist")) {
            for (String world : section.getStringList("world-whitelist")) {
                requirementStorage.addToWorldWhitelist(world);
            }
        }
        if (section.isList("world-blacklist")) {
            for (String world : section.getStringList("world-blacklist")) {
                requirementStorage.addToWorldBlacklist(world);
            }
        }
        // set biome requirements, if present
        if (section.isList("biome-whitelist")) {
            for (String biomeName : section.getStringList("biome-whitelist")) {
                try {
                    requirementStorage.addToBiomeWhitelist(Biome.valueOf(biomeName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("Biome '%s' not recognized for biome-whitelist in section: %s",
                            biomeName, section.getCurrentPath()));
                    return false;
                }
            }
        }
        if (section.isList("biome-blacklist")) {
            for (String biomeName : section.getStringList("biome-blacklist")) {
                try {
                    requirementStorage.addToBiomeBlacklist(Biome.valueOf(biomeName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("Biome '%s' not recognized for biome-blacklist in section: %s",
                            biomeName, section.getCurrentPath()));
                    return false;
                }
            }
        }
        // set environment requirements, if present
        if (section.isList("environment-whitelist")) {
            for (String environmentName : section.getStringList("environment-whitelist")) {
                try {
                    requirementStorage.addToEnvironmentWhitelist(World.Environment.valueOf(environmentName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("Environment '%s' not recognized for environment-whitelist in section: %s",
                            environmentName, section.getCurrentPath()));
                    return false;
                }
            }
        }
        if (section.isList("environment-blacklist")) {
            for (String environmentName : section.getStringList("environment-blacklist")) {
                try {
                    requirementStorage.addToEnvironmentBlacklist(World.Environment.valueOf(environmentName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("Environment '%s' not recognized for environment-blacklist in section: %s",
                            environmentName, section.getCurrentPath()));
                    return false;
                }
            }
        }
        // set block requirements, if present
        if (section.isSet("required-blocks") && section.isConfigurationSection("required-blocks")) {
            ConfigurationSection requiredBlocksSection = section.getConfigurationSection("required-blocks");
            for (String blockName : requiredBlocksSection.getKeys(false)) {
                ConfigurationSection requiredBlockSection = requiredBlocksSection.getConfigurationSection(blockName);
                if (requiredBlockSection == null) {
                    performantPlants.getLogger().warning("config section was null for required block in section: " + requiredBlocksSection.getCurrentPath());
                    return false;
                }
                BlockSettings blockSettings = loadBlockConfig(requiredBlockSection);
                if (blockSettings == null) {
                    performantPlants.getLogger().warning("blockSettings for required block returned null in section: " + requiredBlockSection.getCurrentPath());
                    return false;
                }
                RequiredBlock requiredBlock;
                try {
                    requiredBlock = new RequiredBlock(
                            blockSettings.getXRel(),
                            blockSettings.getYRel(),
                            blockSettings.getZRel(),
                            blockSettings.getMaterial(),
                            blockSettings.getBlockDataStrings());
                } catch (IllegalArgumentException e) {
                    performantPlants.getLogger().warning(String.format("Could not create required block in section due to: %s",
                            requiredBlockSection.getCurrentPath(), e.getMessage()));
                    return false;
                }
                // set required, if set
                if (requiredBlockSection.isBoolean("required")) {
                    requiredBlock.setRequired(requiredBlockSection.getBoolean("required"));
                }
                // set blacklisted, if set
                if (requiredBlockSection.isBoolean("blacklisted")) {
                    requiredBlock.setBlacklisted(requiredBlockSection.getBoolean("blacklisted"));
                }
                // set not air, if set
                if (requiredBlockSection.isBoolean("not-air")) {
                    requiredBlock.setNotAir(requiredBlockSection.getBoolean("not-air"));
                }
                requirementStorage.addRequiredBlock(requiredBlock);
            }
        }
        return true;
    }

    boolean addDropsToDropStorage(ConfigurationSection section, DropStorage dropStorage, PlantData data) {
        // add drop limit, if present
        if (section.isInt("drop-limit")) {
            dropStorage.setDropLimit(section.getInt("drop-limit"));
        }
        ConfigurationSection dropsConfig = section.getConfigurationSection("drops");
        if (dropsConfig != null) {
            // iterate through drops
            for (String dropName : dropsConfig.getKeys(false)) {
                ConfigurationSection dropConfig = dropsConfig.getConfigurationSection(dropName);
                if (dropConfig == null) {
                    performantPlants.getLogger().warning("dropConfig was null in section: " + section.getCurrentPath());
                    return false;
                }
                // get drop settings
                DropSettings dropSettings = loadDropConfig(dropConfig, data);
                if (dropSettings == null) {
                    performantPlants.getLogger().warning("dropSettings were null in section: " + section.getCurrentPath());
                    return false;
                }
                ItemSettings dropItemSettings = dropSettings.getItemSettings();
                if (dropItemSettings == null) {
                    performantPlants.getLogger().warning("dropItemSettings were null in section: " + section.getCurrentPath());
                    return false;
                }
                Drop drop = new Drop(
                        dropItemSettings.generateItemStack(),
                        dropSettings.getMinAmount(),
                        dropSettings.getMaxAmount(),
                        dropSettings.getDoIf()
                );
                dropStorage.addDrop(drop);
            }
        }
        return true;
    }

    void addPropertiesToPlantItem(ConfigurationSection section, PlantItem plantItem) {
        if (section == null) {
            return;
        }
        // set buy price
        if (section.isDouble("buy-price") || section.isInt("buy-price")) {
            double price = section.getDouble("buy-price");
            if (price >= 0.0) {
                plantItem.setBuyPrice(price);
            }
        }
        // set sell price
        if (section.isDouble("sell-price") || section.isInt("sell-price")) {
            double price = section.getDouble("sell-price");
            if (price >= 0.0) {
                plantItem.setSellPrice(price);
            }
        }
        // set burn time
        if (section.isInt("burn-time")) {
            int burnTime = section.getInt("burn-time");
            plantItem.setBurnTime(burnTime);
        }
        // set allow anvil
        if (section.isBoolean("allow-anvil")) {
            plantItem.setAllowAnvil(section.getBoolean("allow-anvil"));
        }
        // set allow anvil rename
        if (section.isBoolean("allow-rename")) {
            plantItem.setAllowAnvilRename(section.getBoolean("allow-rename"));
        }
        // set allow smithing
        if (section.isBoolean("allow-smithing")) {
            plantItem.setAllowSmithing(section.getBoolean("allow-smithing"));
        }
        // set allow grindstone
        if (section.isBoolean("allow-grindstone")) {
            plantItem.setAllowGrindstone(section.getBoolean("allow-grindstone"));
        }
        // set allow stonecutter
        if (section.isBoolean("allow-stonecutter")) {
            plantItem.setAllowStonecutter(section.getBoolean("allow-stonecutter"));
        }
        // set allow enchanting
        if (section.isBoolean("allow-enchanting")) {
            plantItem.setAllowEnchanting(section.getBoolean("allow-enchanting"));
        }
        // set allow beacon
        if (section.isBoolean("allow-beacon")) {
            plantItem.setAllowBeacon(section.getBoolean("allow-beacon"));
        }
        // set allow loom
        if (section.isBoolean("allow-loom")) {
            plantItem.setAllowLoom(section.getBoolean("allow-loom"));
        }
        // set allow cartographer
        if (section.isBoolean("allow-cartographer")) {
            plantItem.setAllowCartography(section.getBoolean("allow-cartographer"));
        }
        // set allow crafting
        if (section.isBoolean("allow-crafting")) {
            plantItem.setAllowCrafting(section.getBoolean("allow-crafting"));
        }
        // set allow fuel
        if (section.isBoolean("allow-fuel")) {
            plantItem.setAllowFuel(section.getBoolean("allow-fuel"));
        }
        // set allow smelting
        if (section.isBoolean("allow-smelting")) {
            plantItem.setAllowSmelting(section.getBoolean("allow-smelting"));
        }
        // set allow brewing
        if (section.isBoolean("allow-brewing")) {
            plantItem.setAllowBrewing(section.getBoolean("allow-brewing"));
        }
        // set allow ingredient
        if (section.isBoolean("allow-ingredient")) {
            plantItem.setAllowIngredient(section.getBoolean("allow-ingredient"));
        }
        // set allow consume
        if (section.isBoolean("allow-consume")) {
            plantItem.setAllowConsume(section.getBoolean("allow-consume"));
        }
        // set allow consume
        if (section.isBoolean("allow-entity-interact")) {
            plantItem.setAllowEntityInteract(section.getBoolean("allow-entity-interact"));
        }
        // set allow wear
        if (section.isBoolean("allow-wear")) {
            plantItem.setAllowWear(section.getBoolean("allow-wear"));
        }

    }

    void addEffectsToEffectStorage(ConfigurationSection section, PlantEffectStorage effectStorage, PlantData data) {
        addEffectsToEffectStorage(section, effectStorage, data, false);
    }

    void addEffectsToEffectStorage(ConfigurationSection section, PlantEffectStorage effectStorage, PlantData data, boolean useSectionDirectly) {
        if (section == null || effectStorage == null) {
            return;
        }
        ConfigurationSection effectsSection = section;
        if (!useSectionDirectly) {
            if (!section.isConfigurationSection("effects")) {
                return;
            }
            effectsSection = section.getConfigurationSection("effects");
        }
        for (String placeholder : effectsSection.getKeys(false)) {
            ConfigurationSection effectSection = effectsSection.getConfigurationSection(placeholder);
            if (effectSection != null) {
                addEffect(effectSection, effectStorage, data);
            }
        }
        // set effect limit, if present
        if (section.isInt("effect-limit")) {
            effectStorage.setEffectLimit(section.getInt("effect-limit"));
        }
    }

    //region Add Effects

    boolean addEffect(ConfigurationSection section, PlantEffectStorage effectStorage, PlantData data) {
        String type = section.getString("type");
        if (type == null) {
            performantPlants.getLogger().warning("Type not set for effect at: " + section.getCurrentPath());
            return false;
        }
        PlantEffect effect;
        switch (type.toLowerCase()) {
            case "feed":
                effect = createFeedEffect(section, data);
                break;
            case "heal":
                effect = createHealEffect(section, data);
                break;
            case "sound":
                effect = createSoundEffect(section, data);
                break;
            case "particle":
                effect = createParticleEffect(section, data);
                break;
            case "potion":
                effect = createPotionEffect(section, data);
                break;
            case "drop":
                effect = createDropEffect(section, data);
                break;
            case "air":
                effect = createAirEffect(section, data);
                break;
            case "area":
                effect = createAreaEffect(section, data);
                break;
            case "durability":
                effect = createDurabilityEffect(section, data);
                break;
            case "chat":
                effect = createChatEffect(section, data);
                break;
            case "explosion":
                effect = createExplosionEffect(section, data);
                break;
            case "command":
                effect = createCommandEffect(section, data);
                break;
            case "script":
                effect = createScriptEffect(section, data);
                break;
            default:
                performantPlants.getLogger().warning(String.format("Effect %s not recognized; not added to effect storage for section: %s",
                        type, section.getCurrentPath()));
                return false;
        }
        if (effect != null) {
            addDoIfAndDelayToEffect(section, effect, data);
            effectStorage.addEffect(effect);
            return true;
        }
        return false;
    }

    PlantFeedEffect createFeedEffect(ConfigurationSection section, PlantData data) {
        PlantFeedEffect effect = new PlantFeedEffect();
        // set food amount, if present
        if (section.isSet("food-amount")) {
            ScriptBlock foodAmount = createPlantScript(section, "food-amount", data);
            if (foodAmount == null || !ScriptHelper.isLong(foodAmount)) {
                performantPlants.getLogger().warning(String.format("Feed effect will not have chosen food-amount and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getFoodAmountValue(null, null), section.getCurrentPath()));
            } else {
                effect.setFoodAmount(foodAmount);
            }
        }
        // set saturation amount, if present
        if (section.isSet("saturate-amount")) {
            ScriptBlock saturateAmount = createPlantScript(section, "saturate-amount", data);
            if (saturateAmount == null || !ScriptHelper.isNumeric(saturateAmount)) {
                performantPlants.getLogger().warning(String.format("Feed effect will not have chosen saturate-amount and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getSaturationAmountValue(null, null), section.getCurrentPath()));
            } else {
                effect.setSaturationAmount(saturateAmount);
            }
        }
        return effect;
    }

    PlantHealEffect createHealEffect(ConfigurationSection section, PlantData data) {
        PlantHealEffect effect = new PlantHealEffect();
        // set heal amount, if set
        if (section.isSet("heal-amount")) {
            ScriptBlock healAmount = createPlantScript(section, "heal-amount", data);
            if (healAmount == null || !ScriptHelper.isNumeric(healAmount)) {
                performantPlants.getLogger().warning(String.format("Heal effect will not have chosen heal-amount and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getHealAmountValue(null, null), section.getCurrentPath()));
            } else {
                effect.setHealAmount(healAmount);
            }
        }
        return effect;
    }

    PlantSoundEffect createSoundEffect(ConfigurationSection section, PlantData data) {
        // set sound
        PlantSoundEffect effect = new PlantSoundEffect();
        if (!section.isSet("sound")) {
            performantPlants.getLogger().warning("Sound effect not added; sound field not found in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock sound = createPlantScript(section, "sound", data);
        if (sound == null || !ScriptHelper.isString(sound)) {
            performantPlants.getLogger().warning(String.format("Sound effect not added; sound field must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setSoundName(sound);
        // TODO: check that sound exists if does not include variables
        // set volume, if present
        if (section.isSet("volume")) {
            ScriptBlock volume = createPlantScript(section, "volume", data);
            if (volume == null || !ScriptHelper.isNumeric(volume)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen volume and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getVolumeValue(null, null), section.getCurrentPath()));
            } else {
                effect.setVolume(volume);
            }
        }
        // set pitch, if present
        if (section.isSet("pitch")) {
            ScriptBlock pitch = createPlantScript(section, "pitch", data);
            if (pitch == null || !ScriptHelper.isNumeric(pitch)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen pitch and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getPitchValue(null, null), section.getCurrentPath()));
            } else {
                effect.setPitch(pitch);
            }
        }
        // set offsets, if present
        // set offsets, if present
        if (section.isSet("offset-x")) {
            ScriptBlock offsetX = createPlantScript(section, "offset-x", data);
            if (offsetX == null || !ScriptHelper.isNumeric(offsetX)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetX(offsetX);
            }
        }
        if (section.isSet("offset-y")) {
            ScriptBlock offsetY = createPlantScript(section, "offset-y", data);
            if (offsetY == null || !ScriptHelper.isNumeric(offsetY)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetY(offsetY);
            }
        }
        if (section.isSet("offset-z")) {
            ScriptBlock offsetZ = createPlantScript(section, "offset-z", data);
            if (offsetZ == null || !ScriptHelper.isNumeric(offsetZ)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen offset-z and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetZValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetZ(offsetZ);
            }
        }
        // set multiplier, if present
        if (section.isSet("multiplier")) {
            ScriptBlock multiplier = createPlantScript(section, "multiplier", data);
            if (multiplier == null || !ScriptHelper.isNumeric(multiplier)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen multiplier and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getMultiplierValue(null, null), section.getCurrentPath()));
            } else {
                effect.setMultiplier(multiplier);
            }
        }
        // set if should be eye location, if present
        if (section.isSet("eye-location")) {
            ScriptBlock eyeLocation = createPlantScript(section, "eye-location", data);
            if (eyeLocation == null || !ScriptHelper.isBoolean(eyeLocation)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen eye-location value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isEyeLocation(null, null), section.getCurrentPath()));
            } else {
                effect.setEyeLocation(eyeLocation);
            }
        }
        // set if should ignore y component of facing direction
        if (section.isSet("ignore-direction-y")) {
            ScriptBlock ignoreDirectionY = createPlantScript(section, "ignore-direction-y", data);
            if (ignoreDirectionY == null || !ScriptHelper.isBoolean(ignoreDirectionY)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen ignore-direction-y value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isIgnoreDirectionY(null, null), section.getCurrentPath()));
            } else {
                effect.setIgnoreDirectionY(ignoreDirectionY);
            }
        }
        // set client-side, if present
        if (section.isSet("client-side")) {
            ScriptBlock clientSide = createPlantScript(section, "client-side", data);
            if (clientSide == null || !ScriptHelper.isBoolean(clientSide)) {
                performantPlants.getLogger().warning(String.format("Sound effect will not have chosen client-side value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isClientSide(null, null), section.getCurrentPath()));
            } else {
                effect.setClientSide(clientSide);
            }
        }
        return effect;
    }

    PlantParticleEffect createParticleEffect(ConfigurationSection section, PlantData data) {
        // set particle
        PlantParticleEffect effect = new PlantParticleEffect();
        if (!section.isSet("particle")) {
            performantPlants.getLogger().warning("Particle effect not added; particle was null in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock particle = createPlantScript(section, "particle", data);
        if (particle == null || !ScriptHelper.isString(particle)) {
            performantPlants.getLogger().warning(String.format("Particle effect not added; particle must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setParticleName(particle);
        // set count, if present
        if (section.isSet("count")) {
            ScriptBlock count = createPlantScript(section, "count", data);
            if (count == null || !ScriptHelper.isLong(count)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen count and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getCountValue(null, null), section.getCurrentPath()));
            } else {
                effect.setCount(count);
            }
        }
        // set offsets, if present
        if (section.isSet("offset-x")) {
            ScriptBlock offsetX = createPlantScript(section, "offset-x", data);
            if (offsetX == null || !ScriptHelper.isNumeric(offsetX)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetX(offsetX);
            }
        }
        if (section.isSet("offset-y")) {
            ScriptBlock offsetY = createPlantScript(section, "offset-y", data);
            if (offsetY == null || !ScriptHelper.isNumeric(offsetY)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetY(offsetY);
            }
        }
        if (section.isSet("offset-z")) {
            ScriptBlock offsetZ = createPlantScript(section, "offset-z", data);
            if (offsetZ == null || !ScriptHelper.isNumeric(offsetZ)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen offset-z and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetZValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetZ(offsetZ);
            }
        }
        // set data offsets, if present
        if (section.isSet("data-offset-x")) {
            ScriptBlock dataOffsetX = createPlantScript(section, "data-offset-x", data);
            if (dataOffsetX == null || !ScriptHelper.isNumeric(dataOffsetX)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen data-offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getDataOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDataOffsetX(dataOffsetX);
            }
        }
        if (section.isSet("data-offset-y")) {
            ScriptBlock dataOffsetY = createPlantScript(section, "data-offset-y", data);
            if (dataOffsetY == null || !ScriptHelper.isNumeric(dataOffsetY)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen data-offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getDataOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDataOffsetY(dataOffsetY);
            }
        }
        if (section.isSet("data-offset-z")) {
            ScriptBlock dataOffsetZ = createPlantScript(section, "data-offset-z", data);
            if (dataOffsetZ == null || !ScriptHelper.isNumeric(dataOffsetZ)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen data-offset-z and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getDataOffsetZValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDataOffsetZ(dataOffsetZ);
            }
        }
        // set multiplier, if present
        if (section.isSet("multiplier")) {
            ScriptBlock multiplier = createPlantScript(section, "multiplier", data);
            if (multiplier == null || !ScriptHelper.isNumeric(multiplier)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen multiplier and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getMultiplierValue(null, null), section.getCurrentPath()));
            } else {
                effect.setMultiplier(multiplier);
            }
        }
        // set extra, if present
        if (section.isSet("extra")) {
            ScriptBlock extra = createPlantScript(section, "extra", data);
            if (extra == null || !ScriptHelper.isNumeric(extra)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen extra value and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getExtraValue(null, null), section.getCurrentPath()));
            } else {
                effect.setExtra(extra);
            }
        }
        // set if should be eye location, if present
        if (section.isSet("eye-location")) {
            ScriptBlock eyeLocation = createPlantScript(section, "eye-location", data);
            if (eyeLocation == null || !ScriptHelper.isBoolean(eyeLocation)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen eye-location value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isEyeLocation(null, null), section.getCurrentPath()));
            } else {
                effect.setEyeLocation(eyeLocation);
            }
        }
        // set if should ignore y component of facing direction
        if (section.isSet("ignore-direction-y")) {
            ScriptBlock ignoreDirectionY = createPlantScript(section, "ignore-direction-y", data);
            if (ignoreDirectionY == null || !ScriptHelper.isBoolean(ignoreDirectionY)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen ignore-direction-y value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isIgnoreDirectionY(null, null), section.getCurrentPath()));
            } else {
                effect.setIgnoreDirectionY(ignoreDirectionY);
            }
        }
        // set client-side, if present
        if (section.isSet("client-side")) {
            ScriptBlock clientSide = createPlantScript(section, "client-side", data);
            if (clientSide == null || !ScriptHelper.isBoolean(clientSide)) {
                performantPlants.getLogger().warning(String.format("Particle effect will not have chosen client-side value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isClientSide(null, null), section.getCurrentPath()));
            } else {
                effect.setClientSide(clientSide);
            }
        }
        return effect;
    }

    PlantPotionEffect createPotionEffect(ConfigurationSection section, PlantData data) {
        // set potion effect type
        PlantPotionEffect effect = new PlantPotionEffect();
        if (!section.isSet("potion")) {
            performantPlants.getLogger().warning("Potion effect not added; potion not found in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock potionName = createPlantScript(section, "potion", data);
        if (potionName == null || !ScriptHelper.isString(potionName)) {
            performantPlants.getLogger().warning(String.format("Potion effect not added; potion must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setPotionEffectTypeName(potionName);
        // if no variables, then check if potion is recognized
        if (!potionName.containsVariable()) {
            PotionEffectType potionEffectType = effect.getPotionEffectType(null, null);
            if (potionEffectType == null) {
                String potionNameString = potionName.loadValue(null, null).getStringValue();
                performantPlants.getLogger().warning(String.format("Potion effect not added; potion '%s' not recognized", potionNameString));
                return null;
            }
        }
        // set duration, if present
        if (section.isSet("duration")) {
            ScriptBlock duration = createPlantScript(section, "duration", data);
            if (duration == null || !ScriptHelper.isLong(duration)) {
                performantPlants.getLogger().warning(String.format("Potion effect will not have chosen duration and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getDurationValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDuration(duration);
            }
        }
        // set amplifier, if present
        if (section.isSet("amplifier")) {
            ScriptBlock amplifier = createPlantScript(section, "amplifier", data);
            if (amplifier == null || !ScriptHelper.isLong(amplifier)) {
                performantPlants.getLogger().warning(String.format("Potion effect will not have chosen amplifier and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getAmplifierValue(null, null), section.getCurrentPath()));
            } else {
                effect.setAmplifier(amplifier);
            }
        }
        // set ambient, if present
        if (section.isSet("ambient")) {
            ScriptBlock ambient = createPlantScript(section, "ambient", data);
            if (ambient == null || !ScriptHelper.isBoolean(ambient)) {
                performantPlants.getLogger().warning(String.format("Potion effect will not have chosen ambient value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isAmbient(null, null), section.getCurrentPath()));
            } else {
                effect.setAmbient(ambient);
            }
        }
        // set particles, if present
        if (section.isSet("particles")) {
            ScriptBlock particles = createPlantScript(section, "particles", data);
            if (particles == null || !ScriptHelper.isBoolean(particles)) {
                performantPlants.getLogger().warning(String.format("Potion effect will not have chosen particles value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isParticles(null, null), section.getCurrentPath()));
            } else {
                effect.setParticles(particles);
            }
        }
        // set icon
        if (section.isSet("icon")) {
            ScriptBlock icon = createPlantScript(section, "icon", data);
            if (icon == null || !ScriptHelper.isBoolean(icon)) {
                performantPlants.getLogger().warning(String.format("Potion effect will not have chosen icon value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isIcon(null, null), section.getCurrentPath()));
            } else {
                effect.setIcon(icon);
            }
        }
        return effect;
    }

    PlantDropEffect createDropEffect(ConfigurationSection section, PlantData data) {
        if (!section.isConfigurationSection("drops")) {
            performantPlants.getLogger().warning("Drop effect not added; no drops section provided in section: " + section.getCurrentPath());
            return null;
        }
        PlantDropEffect effect = new PlantDropEffect();
        boolean added = addDropsToDropStorage(section, effect.getDropStorage(), data);
        if (!added) {
            performantPlants.getLogger().warning("Drop effect not added; issue getting drops");
            return null;
        }
        return effect;
    }

    PlantAirEffect createAirEffect(ConfigurationSection section, PlantData data) {
        PlantAirEffect effect = new PlantAirEffect();
        if (!section.isSet("amount")) {
            performantPlants.getLogger().warning("Air effect not added; no amount provided in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock amount = createPlantScript(section, "amount", data);
        if (amount == null || !ScriptHelper.isLong(amount)) {
            performantPlants.getLogger().warning("Air effect not added; amount must be ScriptType Long in section: " + section.getCurrentPath());
            return null;
        }
        effect.setAmount(amount);
        return effect;
    }

    PlantAreaEffect createAreaEffect(ConfigurationSection section, PlantData data) {
        PlantAreaEffect effect = new PlantAreaEffect();
        // get potion effects, if present
        List<PotionEffect> potionEffects = loadPotionEffects(section);
        for (PotionEffect potionEffect : potionEffects) {
            effect.addPotionEffect(potionEffect);
        }
        if (section.isConfigurationSection("color")) {
            ConfigurationSection colorSection = section.getConfigurationSection("color");
            if (colorSection != null) {
                effect.setColor(createColor(colorSection, data));
            }
        }
        if (section.isSet("duration")) {
            ScriptBlock duration = createPlantScript(section, "duration", data);
            if (duration == null || !ScriptHelper.isLong(duration)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen duration and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getDurationValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDuration(duration);
            }
        }
        if (section.isSet("duration-on-use")) {
            ScriptBlock durationOnUse = createPlantScript(section, "duration-on-use", data);
            if (durationOnUse == null || !ScriptHelper.isLong(durationOnUse)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen duration-on-use and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getDurationValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDurationOnUse(durationOnUse);
            }
        }
        if (section.isSet("particle")) {
            ScriptBlock particle = createPlantScript(section, "particle", data);
            if (particle == null || !ScriptHelper.isString(particle)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen particle; must be ScriptType STRING in section %s",
                        section.getCurrentPath()));
            } else {
                effect.setParticleName(particle);
            }
        }
        if (section.isSet("radius")) {
            ScriptBlock radius = createPlantScript(section, "radius", data);
            if (radius == null || !ScriptHelper.isNumeric(radius)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen radius and instead will be" +
                        " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadius(radius);
            }
        }
        if (section.isSet("radius-on-use")) {
            ScriptBlock radiusOnUse = createPlantScript(section, "radius-on-use", data);
            if (radiusOnUse == null || !ScriptHelper.isNumeric(radiusOnUse)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen radius-on-use and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusOnUseValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadiusOnUse(radiusOnUse);
            }
        }
        if (section.isSet("radius-per-tick")) {
            ScriptBlock radiusPerTick = createPlantScript(section, "radius-per-tick", data);
            if (radiusPerTick == null || !ScriptHelper.isNumeric(radiusPerTick)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen radius-per-tick and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusPerTickValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadiusPerTick(radiusPerTick);
            }
        }
        if (section.isSet("reapplication-delay")) {
            ScriptBlock reapplicationDelay = createPlantScript(section, "reapplication-delay", data);
            if (reapplicationDelay == null || !ScriptHelper.isLong(reapplicationDelay)) {
                performantPlants.getLogger().warning(String.format("Area effect will not have chosen reapplication-delay and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getReapplicationDelayValue(null, null), section.getCurrentPath()));
            } else {
                effect.setReapplicationDelay(reapplicationDelay);
            }
        }
        return effect;
    }

    PlantDurabilityEffect createDurabilityEffect(ConfigurationSection section, PlantData data) {
        PlantDurabilityEffect effect = new PlantDurabilityEffect();
        if (!section.isSet("damage-amount")) {
            performantPlants.getLogger().warning("Durability effect not added; no damage-amount provided in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock amount = createPlantScript(section, "damage-amount", data);
        if (amount == null || !ScriptHelper.isLong(amount)) {
            performantPlants.getLogger().warning(String.format("Durability effect not added; must be ScriptType LONG in section: %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setAmount(amount);
        return effect;
    }

    PlantChatEffect createChatEffect(ConfigurationSection section, PlantData data) {
        PlantChatEffect effect = new PlantChatEffect();
        if (section.isSet("from-player")) {
            ScriptBlock fromPlayer = createPlantScript(section, "from-player", data);
            if (fromPlayer == null || ScriptHelper.isNull(fromPlayer)) {
                performantPlants.getLogger().warning(String.format("Chat effect will not have chosen from-player message and instead will be" +
                        " %s; must be neither null nor ScriptType NULL in section: %s",
                    effect.getFromPlayerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setFromPlayer(fromPlayer);
            }
        }
        if (section.isSet("to-player")) {
            ScriptBlock toPlayer = createPlantScript(section, "to-player", data);
            if (toPlayer == null || ScriptHelper.isNull(toPlayer)) {
                performantPlants.getLogger().warning(String.format("Chat effect will not have chosen to-player message and instead will be" +
                                " %s; must be neither null nor ScriptType NULL in section: %s",
                        effect.getToPlayerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setToPlayer(toPlayer);
            }
        }
        if (effect.getFromPlayer() == ScriptResult.EMPTY && effect.getToPlayer() == ScriptResult.EMPTY) {
            performantPlants.getLogger().warning("Chat effect not added; from-player and to-player messages both not set in section: " + section.getCurrentPath());
            return null;
        }
        return effect;
    }

    PlantExplosionEffect createExplosionEffect(ConfigurationSection section, PlantData data) {
        PlantExplosionEffect effect = new PlantExplosionEffect();
        if (section.isSet("power")) {
            ScriptBlock power = createPlantScript(section, "power", data);
            if (power == null || !ScriptHelper.isNumeric(power)) {
                performantPlants.getLogger().warning(String.format("Explosion effect will not have chosen power and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getPowerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setPower(power);
            }
        }
        if (section.isSet("fire")) {
            ScriptBlock fire = createPlantScript(section, "fire", data);
            if (fire == null || !ScriptHelper.isBoolean(fire)) {
                performantPlants.getLogger().warning(String.format("Explosion effect will not have chosen fire value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isFire(null, null), section.getCurrentPath()));
            } else {
                effect.setFire(fire);
            }
        }
        if (section.isSet("break-blocks")) {
            ScriptBlock breakBlocks = createPlantScript(section, "break-blocks", data);
            if (breakBlocks == null || !ScriptHelper.isBoolean(breakBlocks)) {
                performantPlants.getLogger().warning(String.format("Command effect will not have chosen break-blocks value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isBreakBlocks(null, null), section.getCurrentPath()));
            } else {
                effect.setBreakBlocks(breakBlocks);
            }
        }
        return effect;
    }

    PlantCommandEffect createCommandEffect(ConfigurationSection section, PlantData data) {
        PlantCommandEffect effect = new PlantCommandEffect();
        if (!section.isSet("command")) {
            performantPlants.getLogger().warning("Command effect not added; command value not present in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock command = createPlantScript(section, "command", data);
        if (command == null || !ScriptHelper.isString(command)) {
            performantPlants.getLogger().warning("Command effect not added; command value must be ScriptType STRING in section: " + section.getCurrentPath());
            return null;
        }
        effect.setCommand(command);
        if (section.isSet("console")) {
            ScriptBlock console = createPlantScript(section, "console", data);
            if (console == null || !ScriptHelper.isBoolean(console)) {
                performantPlants.getLogger().warning(String.format("Command effect will not have chosen console value and instead will be" +
                        " %b; must be ScriptType BOOLEAN in section: %s",
                    effect.isConsole(null, null), section.getCurrentPath()));
            } else {
                effect.setConsole(console);
            }
        }
        return effect;
    }

    PlantScriptEffect createScriptEffect(ConfigurationSection section, PlantData data) {
        PlantScriptEffect effect = new PlantScriptEffect();
        if (!section.isSet("plant-script")) {
            performantPlants.getLogger().warning("Script effect not added; plant-script section not present in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scriptBlock = createPlantScript(section, "plant-script", data);
        if (scriptBlock == null) {
            performantPlants.getLogger().warning("Script effect not added; plant-script must be a script block in section: " + section.getCurrentPath());
            return null;
        }
        effect.setScriptBlock(scriptBlock);
        return effect;
    }

    void addDoIfAndDelayToEffect(ConfigurationSection section, PlantEffect effect, PlantData data) {
        // set do-if, if present
        if (section.isSet("do-if")) {
            ScriptBlock doIf = createPlantScript(section, "do-if", data);
            effect.setDoIf(doIf);
        }
        // set delay, if present
        if (section.isSet("delay")) {
            ScriptBlock delay = createPlantScript(section, "delay", data);
            effect.setDelay(delay);
        }
    }

    //endregion

    //region Add Recipes

    void preparePlantRecipe(ConfigurationSection section, PlantRecipe plantRecipe) {
        // load PlantInteractStorage, if present
        PlantInteractStorage storage = null;
        ConfigurationSection storageSection = section.getConfigurationSection("on-craft");
        if (storageSection != null) {
            storage = loadPlantInteractStorage(storageSection);
            plantRecipe.setStorage(storage);
        }
        boolean ignoreResultPresent = false;
        boolean ignoreResult = false;
        if (section.isBoolean("ignore-result")) {
            ignoreResultPresent = true;
            ignoreResult = section.getBoolean("ignore-result");
            plantRecipe.setIgnoreResult(ignoreResult);
        }
    }

    void addCraftingRecipe(ConfigurationSection section, String recipeName) {
        String type = section.getString("type");
        if (type == null) {
            performantPlants.getLogger().warning("Type not set for crafting recipe at: " + section.getCurrentPath());
            return;
        }
        // get shaped recipe
        if (type.equalsIgnoreCase("shaped")) {
            // get result
            if (!section.isConfigurationSection("result")) {
                performantPlants.getLogger().warning("No result section for shapeless crafting recipe at " + section.getCurrentPath());
                return;
            }
            ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
            // if no item settings, return (something went wrong or linked item did not exist)
            if (resultSettings == null) {
                return;
            }
            try {
                // initialize recipe - get result
                ItemStack resultStack = resultSettings.generateItemStack();
                ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(performantPlants, "shaped_" + recipeName), resultStack);
                // get shape
                if (!section.isList("shape")) {
                    performantPlants.getLogger().warning("No shape section (must be list) for shaped crafting recipe at " + section.getCurrentPath());
                    return;
                }
                recipe.shape(section.getStringList("shape").toArray(new String[0]));
                // get ingredients to understand shape
                if (!section.isConfigurationSection("ingredients")) {
                    performantPlants.getLogger().warning("No ingredients section for shaped crafting recipe at " + section.getCurrentPath());
                    return;
                }
                ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
                for (String ingredientString : ingredientsSection.getKeys(false)) {
                    // check that ingredientString was only a single char
                    if (ingredientString.length() != 1) {
                        performantPlants.getLogger().warning(String.format("Ingredient designation must be a single character; was %s instead at %s", ingredientString, section.getCurrentPath()));
                        return;
                    }
                    // get char
                    char ingredientChar = ingredientString.charAt(0);
                    // get ingredient item
                    ConfigurationSection ingredientSection = ingredientsSection.getConfigurationSection(ingredientString);
                    ItemSettings itemSettings = loadItemConfig(ingredientSection, true);
                    if (itemSettings == null) {
                        return;
                    }
                    // add ingredient to recipe
                    ItemStack ingredientStack = itemSettings.generateItemStack();
                    recipe.setIngredient(ingredientChar, new RecipeChoice.ExactChoice(ingredientStack));
                }
                // otherwise add recipe to server
                performantPlants.getServer().addRecipe(recipe);
                // add recipe to recipe manager
                PlantRecipe plantRecipe = new PlantRecipe(recipe);
                preparePlantRecipe(section, plantRecipe);
                performantPlants.getRecipeManager().addShapedRecipe(plantRecipe);
                performantPlants.getLogger().info("Registered shaped crafting recipe: " + recipeName);
            } catch (Exception e) {
                performantPlants.getLogger().warning(String.format("Failed to add shaped crafting recipe at %s due to exception: %s",
                        section.getCurrentPath(), e.getMessage()));
            }
        }
        // get shapeless recipe
        else if (type.equalsIgnoreCase("shapeless")) {
            // get result
            if (!section.isConfigurationSection("result")) {
                performantPlants.getLogger().warning("No result section for shapeless crafting recipe at " + section.getCurrentPath());
                return;
            }
            ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
            // if no item settings, return (something went wrong or linked item did not exist)
            if (resultSettings == null) {
                return;
            }
            try {
                // initialize recipe - get result
                ItemStack resultStack = resultSettings.generateItemStack();
                ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(performantPlants, "shapeless_" + recipeName), resultStack);
                // get ingredients
                if (!section.isConfigurationSection("ingredients")) {
                    performantPlants.getLogger().warning("No ingredients section for shapeless crafting recipe at " + section.getCurrentPath());
                    return;
                }
                ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
                for (String placeholder : ingredientsSection.getKeys(false)) {
                    ConfigurationSection ingredientSection = ingredientsSection.getConfigurationSection(placeholder);
                    ItemSettings itemSettings = loadItemConfig(ingredientSection, true);
                    if (itemSettings == null) {
                        return;
                    }
                    // add ingredient to recipe
                    ItemStack ingredientStack = itemSettings.generateItemStack();
                    recipe.addIngredient(new RecipeChoice.ExactChoice(ingredientStack));
                }
                // if no ingredients added, return without adding recipe
                if (recipe.getIngredientList().isEmpty()) {
                    performantPlants.getLogger().warning("No ingredients were found for shapeless crafting recipe at " + section.getCurrentPath());
                    return;
                }
                // otherwise add recipe to server
                performantPlants.getServer().addRecipe(recipe);
                // add recipe to recipe manager
                PlantRecipe plantRecipe = new PlantRecipe(recipe);
                preparePlantRecipe(section, plantRecipe);
                performantPlants.getRecipeManager().addShapelessRecipe(plantRecipe);
                performantPlants.getLogger().info("Registered shapeless crafting recipe: " + recipeName);
            } catch (Exception e) {
                performantPlants.getLogger().warning(String.format("Failed to add shapeless crafting recipe at %s due to exception: %s",
                        section.getCurrentPath(), e.getMessage()));
            }
        } else {
            performantPlants.getLogger().warning(String.format("Type '%s' not a recognized crafting recipe at %s",
                    type, section.getCurrentPath()));
        }
    }

    CookingRecipeSettings loadCookingRecipeConfig(ConfigurationSection section) {
        // get result
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for cooking recipe at " + section.getCurrentPath());
            return null;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return null;
        }
        // get input
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for cooking recipe at " + section.getCurrentPath());
            return null;
        }
        ItemSettings inputSettings = loadItemConfig(section.getConfigurationSection("input"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (inputSettings == null) {
            return null;
        }
        // get experience
        float experience = 0;
        if (section.isDouble("experience") || section.isInt("experience")) {
            experience = (float) section.getDouble("experience");
        }
        int cookingTime = 200;
        // get cooking time
        if (section.isInt("cooking-time")) {
            cookingTime = section.getInt("cooking-time");
        }
        // create item stacks
        ItemStack resultStack = resultSettings.generateItemStack();
        ItemStack inputStack = inputSettings.generateItemStack();
        return new CookingRecipeSettings(resultStack, inputStack, experience, cookingTime);
    }

    void addFurnaceRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            // add recipe to server
            NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "furnace_" + recipeName);
            FurnaceRecipe recipe = new FurnaceRecipe(namespacedKey,
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            performantPlants.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(recipeSettings.getInput());
            PlantItemStackRecipe itemStackRecipe = new PlantItemStackRecipe(recipeKey, recipeSettings.getResult(), namespacedKey);
            performantPlants.getRecipeManager().addFurnaceRecipe(itemStackRecipe);
            performantPlants.getLogger().info("Registered furnace recipe: " + recipeName);
        } catch (Exception e) {
            performantPlants.getLogger().warning(String.format("Failed to add furnace recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addBlastingRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            // add recipe to server
            NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "blasting_" + recipeName);
            BlastingRecipe recipe = new BlastingRecipe(namespacedKey,
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            performantPlants.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(recipeSettings.getInput());
            PlantItemStackRecipe itemStackRecipe = new PlantItemStackRecipe(recipeKey, recipeSettings.getResult(), namespacedKey);
            performantPlants.getRecipeManager().addBlastingRecipe(itemStackRecipe);
            performantPlants.getLogger().info("Registered blast furnace recipe: " + recipeName);
        } catch (Exception e) {
            performantPlants.getLogger().warning(String.format("Failed to add blast furnace recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addSmokingRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            // add recipe to server
            NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "smoking_" + recipeName);
            SmokingRecipe recipe = new SmokingRecipe(namespacedKey,
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            performantPlants.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(recipeSettings.getInput());
            PlantItemStackRecipe itemStackRecipe = new PlantItemStackRecipe(recipeKey, recipeSettings.getResult(), namespacedKey);
            performantPlants.getRecipeManager().addSmokingRecipe(itemStackRecipe);
            performantPlants.getLogger().info("Registered smoker recipe: " + recipeName);
        } catch (Exception e) {
            performantPlants.getLogger().warning(String.format("Failed to add smoker recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addCampfireRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            // add recipe to server
            NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "campfire_" + recipeName);
            CampfireRecipe recipe = new CampfireRecipe(namespacedKey,
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            performantPlants.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(recipeSettings.getInput());
            PlantItemStackRecipe itemStackRecipe = new PlantItemStackRecipe(recipeKey, recipeSettings.getResult(), namespacedKey);
            performantPlants.getRecipeManager().addCampfireRecipe(itemStackRecipe);
            performantPlants.getLogger().info("Registered campfire recipe: " + recipeName);
        } catch (Exception e) {
            performantPlants.getLogger().warning(String.format("Failed to add campfire recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addStonecuttingRecipe(ConfigurationSection section, String recipeName) {
        // get result
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for stonecutting recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return;
        }
        // get input
        if (!section.isConfigurationSection("input")) {
            performantPlants.getLogger().warning("No input section for stonecutting recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings inputSettings = loadItemConfig(section.getConfigurationSection("input"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (inputSettings == null) {
            return;
        }
        // create item stacks
        ItemStack resultStack = resultSettings.generateItemStack();
        ItemStack inputStack = inputSettings.generateItemStack();
        StonecuttingRecipe recipe = new StonecuttingRecipe(new NamespacedKey(performantPlants, "stonecutting_" + recipeName),
                resultStack,
                new RecipeChoice.ExactChoice(inputStack));
        // add recipe to server
        performantPlants.getServer().addRecipe(recipe);
        // add recipe to recipe manager
        performantPlants.getRecipeManager().addStonecuttingRecipe(recipe);
        performantPlants.getLogger().info("Registered stonecutting recipe: " + recipeName);
    }

    void addSmithingRecipe(ConfigurationSection section, String recipeName) {
        // get result
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for smithing recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return;
        }
        // get base
        if (!section.isConfigurationSection("base")) {
            performantPlants.getLogger().warning("No base section for smithing recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings baseSettings = loadItemConfig(section.getConfigurationSection("base"), true);
        // if no base settings, return (something went wrong or linked item did not exist)
        if (baseSettings == null) {
            return;
        }
        // get addition
        if (!section.isConfigurationSection("addition")) {
            performantPlants.getLogger().warning("No addition section for smithing recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings additionSettings = loadItemConfig(section.getConfigurationSection("addition"), true);
        // if no base settings, return (something went wrong or linked item did not exist)
        if (additionSettings == null) {
            return;
        }
        // create item stacks
        ItemStack resultStack = resultSettings.generateItemStack();
        ItemStack baseStack = baseSettings.generateItemStack();
        ItemStack additionStack = additionSettings.generateItemStack();
        // add recipe to server
        NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "smithing_" + recipeName);
        SmithingRecipe smithingRecipe = new SmithingRecipe(namespacedKey,
                resultStack,
                new RecipeChoice.ExactChoice(baseStack),
                new RecipeChoice.ExactChoice(additionStack));
        performantPlants.getServer().addRecipe(smithingRecipe);
        // add recipe to recipe manager
        SmithingRecipeKey recipeKey = new SmithingRecipeKey(baseStack, additionStack);
        PlantRecipe plantRecipe = new PlantRecipe(new PlantSmithingRecipe(recipeKey, resultStack, namespacedKey));
        preparePlantRecipe(section, plantRecipe);
        performantPlants.getRecipeManager().addSmithingRecipe(plantRecipe);
        performantPlants.getLogger().info("Registered smithing recipe: " + recipeName);
    }

    void addAnvilRecipe(ConfigurationSection section, String recipeName) {
        // get result
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for anvil recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return;
        }
        // get level cost
        int levelCost = 1;
        if (section.isInt("level-cost")) {
            levelCost = Math.max(1, section.getInt("level-cost"));
        }
        // get base
        if (!section.isConfigurationSection("base")) {
            performantPlants.getLogger().warning("No base section for anvil recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings baseSettings = loadItemConfig(section.getConfigurationSection("base"), true);
        // if no base settings, return (something went wrong or linked item did not exist)
        if (baseSettings == null) {
            return;
        }
        // get addition
        if (!section.isConfigurationSection("addition")) {
            performantPlants.getLogger().warning("No addition section for anvil recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings additionSettings = loadItemConfig(section.getConfigurationSection("addition"), true);
        // if no base settings, return (something went wrong or linked item did not exist)
        if (additionSettings == null) {
            return;
        }
        // get name, if present
        String name = "";
        if (section.isString("name")) {
            name = section.getString("name");
            if (name == null) {
                name = "";
            }
        }
        // create item stacks
        ItemStack resultStack = resultSettings.generateItemStack();
        ItemStack baseStack = baseSettings.generateItemStack();
        ItemStack additionStack = additionSettings.generateItemStack();
        // add recipe to recipe manager
        NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "anvil_" + recipeName);
        AnvilRecipeKey recipeKey = new AnvilRecipeKey(baseStack, additionStack, name);
        PlantRecipe plantRecipe = new PlantRecipe(new PlantAnvilRecipe(recipeKey, resultStack, levelCost, namespacedKey));
        preparePlantRecipe(section, plantRecipe);
        performantPlants.getRecipeManager().addAnvilRecipe(plantRecipe);
        performantPlants.getLogger().info("Registered anvil recipe: " + recipeName);
    }

    void addPotionRecipe(ConfigurationSection section, String recipeName) {
        // get result
        if (!section.isConfigurationSection("result")) {
            performantPlants.getLogger().warning("No result section for potion recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return;
        }
        // get ingredient
        if (!section.isConfigurationSection("ingredient")) {
            performantPlants.getLogger().warning("No ingredient section for potion recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings ingredientSettings = loadItemConfig(section.getConfigurationSection("ingredient"), true);
        // if no ingredient settings, return (something went wrong or linked item did not exist)
        if (ingredientSettings == null) {
            return;
        }
        // get potion
        if (!section.isConfigurationSection("potion")) {
            performantPlants.getLogger().warning("No potion section for potion recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings potionSettings = loadItemConfig(section.getConfigurationSection("potion"), true);
        // if no base settings, return (something went wrong or linked item did not exist)
        if (potionSettings == null) {
            return;
        }
        // create item stacks
        ItemStack resultStack = resultSettings.generateItemStack();
        ItemStack ingredientStack = ingredientSettings.generateItemStack();
        ItemStack potionStack = potionSettings.generateItemStack();
        // add recipe to recipe manager
        NamespacedKey namespacedKey = new NamespacedKey(performantPlants, "potion_" + recipeName);
        PotionRecipeKey recipeKey = new PotionRecipeKey(ingredientStack, potionStack);
        PlantPotionRecipe potionRecipe = new PlantPotionRecipe(recipeKey, resultStack, namespacedKey);
        performantPlants.getRecipeManager().addPotionRecipe(potionRecipe);
        performantPlants.getLogger().info("Registered potion recipe: " + recipeName);
    }

    //endregion

    //region Plant Script

    PlantData createPlantData(ConfigurationSection section, Plant plant) {
        return createPlantData(section, "plant-data", plant);
    }

    PlantData createPlantData(ConfigurationSection section, String sectionName, Plant plant) {
        if (section == null) {
            return null;
        }
        JSONObject data = new JSONObject();
        if (section.isConfigurationSection(sectionName)) {
            ConfigurationSection plantDataSection = section.getConfigurationSection(sectionName);
            for (String variableName : plantDataSection.getKeys(false)) {
                // check that variable names don't use any reserved or restricted names
                if (variableName.isEmpty()) {
                    performantPlants.getLogger().warning("Variables names cannot be empty; no variables will be initialized " +
                            "until this is fixed in section: " + plantDataSection.getCurrentPath());
                    return null;
                }
                if (variableName.startsWith("_")) {
                    performantPlants.getLogger().warning(String.format("Variable name '%s' cannot begin with '_'; reserved for " +
                            "general block values. No variables will be initialized until this is fixed in section: %s",
                            variableName, plantDataSection.getCurrentPath()));
                    return null;
                }
                if (Character.isDigit(variableName.charAt(0))) {
                    performantPlants.getLogger().warning(String.format("Variable name '%s' cannot begin with a digit; no " +
                            "variables will be initialized until this is fixed in section: %s",
                            variableName, plantDataSection.getCurrentPath()));
                    return null;
                }
                if (variableName.contains(".")) {
                    performantPlants.getLogger().warning(String.format("Variable name '%s' cannot contain a period; no variables" +
                            "will be initialized until this is fixed in section: %s",
                            variableName, plantDataSection.getCurrentPath()));
                    return null;
                }
                // get variable info
                ConfigurationSection variableSection = plantDataSection.getConfigurationSection(variableName);
                if (variableSection == null) {
                    continue;
                }
                ScriptType type;
                // get variable type
                if (variableSection.isString("type")) {
                    type = ScriptType.fromString(variableSection.getString("type"));
                    if (type == null) {
                        performantPlants.getLogger().warning(String.format("Variable type '%s' not recognized; no variables will " +
                                "be initialized until this is fixed in section: %s",
                                variableSection.getString("type"),
                                variableSection.getCurrentPath()));
                        return null;
                    }
                    switch(type) {
                        case BOOLEAN:
                            data.put(variableName, variableSection.getBoolean("value", false));
                            break;
                        case LONG:
                            data.put(variableName, variableSection.getLong("value", 0L));
                            break;
                        case DOUBLE:
                            data.put(variableName, variableSection.getDouble("value",
                                    (double) variableSection.getLong("value", 0L)));
                            break;
                        case STRING:
                            data.put(variableName, variableSection.getString("value", ""));
                            break;
                        default:
                            performantPlants.getLogger().warning(String.format("Variable type %s is not supported right now;" +
                                            "no variables will be initialized until this is fixed in section: %s",
                                    type,
                                    variableSection.getCurrentPath()));
                            return null;
                    }
                    // plantData.addVariableType(variableName, type);
                } else {
                    performantPlants.getLogger().warning(String.format("No type defined for variable '%s'; no variables will be " +
                            "initialized until this is fixed in section: %s",
                            variableName,
                            variableSection.getCurrentPath()));
                    return null;
                }
            }
            if (data.isEmpty()) {
                performantPlants.getLogger().warning("No variables were loaded, so no plant data will be created in section " +
                        section.getConfigurationSection(sectionName).getCurrentPath());
                return null;
            }
            PlantData plantData = new PlantData(data);
            plantData.setPlant(plant);
            return plantData;
        }
        return null;
    }

    //region Plant Script Tasks and Stored Script Blocks
    boolean loadPlantScriptTasksAndScriptBlocks(ConfigurationSection section, String scriptSectionName, String taskSectionName, PlantData data) {
        if (section == null || data == null) {
            return false;
        }

        HashMap<String, ScriptTask> taskHashMap = new HashMap<>();

        // create currentTaskLoader
        currentTaskLoader = new ScriptTaskLoader(performantPlants);

        // load all task names
        if (section.isConfigurationSection(taskSectionName)) {
            ConfigurationSection scriptTasksSection = section.getConfigurationSection(taskSectionName);
            for (String taskId : scriptTasksSection.getKeys(false)) {
                currentTaskLoader.addTaskName(taskId);
            }
        }

        // load all script blocks
        if (section.isConfigurationSection(scriptSectionName)) {
            ConfigurationSection scriptBlocksSection = section.getConfigurationSection(scriptSectionName);
            for (String scriptBlockName : scriptBlocksSection.getKeys(false)) {
                // set current script name
                currentTaskLoader.setCurrentScript(scriptBlockName);
                // try to get script block
                ScriptBlock scriptBlock = createPlantScript(scriptBlocksSection, scriptBlockName, data);
                if (scriptBlock == null) {
                    performantPlants.getLogger().warning(String.format("Could not create stored script block '%s' in section: %s",
                            scriptBlockName, scriptBlocksSection.getCurrentPath()));
                    continue;
                }
                // put script block in map and add script name
                currentTaskLoader.addScriptName(scriptBlockName);
                data.getPlant().addScriptBlock(scriptBlockName, scriptBlock);
            }
        }
        // reset script name
        currentTaskLoader.resetCurrentScript();

        // load all tasks
        if (section.isConfigurationSection(taskSectionName)) {
            ConfigurationSection scriptTasksSection = section.getConfigurationSection(taskSectionName);
            // parse each task section
            for (String taskId : scriptTasksSection.getKeys(false)) {
                ScriptTask scriptTask = createPlantScriptTask(scriptTasksSection, taskId, data);
                if (scriptTask == null) {
                    performantPlants.getLogger().warning(String.format("Could not create script task '%s' in section: %s",
                            taskId, scriptTasksSection.getCurrentPath()));
                    currentTaskLoader.addFailedTask(taskId);
                    continue;
                }
                // put task in map and add task name
                taskHashMap.put(taskId, scriptTask);
                currentTaskLoader.addTaskName(taskId);
            }
        }
        // process tasks and script blocks
        currentTaskLoader.processTasksAndScripts();
        // remove failed scripts and failed tasks
        for (String failedScriptName : currentTaskLoader.getFailedScripts()) {
            data.getPlant().getScriptBlockMap().remove(failedScriptName);
        }
        for (String failedTaskName : currentTaskLoader.getFailedTasks()) {
            taskHashMap.remove(failedTaskName);
        }
        // add tasks
        for (Map.Entry<String, ScriptTask> entry : taskHashMap.entrySet()) {
            data.getPlant().addScriptTask(entry.getKey(), entry.getValue());
        }
        // reset currentTaskLoader to null
        currentTaskLoader = null;
        return true;
    }

    ScriptTask createPlantScriptTask(ConfigurationSection section, String subsectionName, PlantData data) {
        ScriptTask scriptTask = new ScriptTask(data.getPlant().getId(), subsectionName);
        ConfigurationSection taskSection = section.getConfigurationSection(subsectionName);
        // check that task section exists
        if (taskSection == null) {
            return null;
        }
        // set script block
        currentTaskLoader.setCurrentTask(subsectionName);
        ScriptBlock scriptBlock = createPlantScript(taskSection, "plant-script", data);
        if (scriptBlock == null) {
            return null;
        }
        scriptTask.setTaskScriptBlock(scriptBlock);
        // set delay, if present
        if (taskSection.isSet("delay")) {
            ScriptBlock delay = createPlantScript(taskSection, "delay", data);
            if (delay == null || !ScriptHelper.isLong(delay)) {
                performantPlants.getLogger().warning(String.format("Task's delay must be ScriptType LONG in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setDelay(delay);
        }
        // set specific player id, if present
        if (taskSection.isSet("player-id")) {
            ScriptBlock playerId = createPlantScript(taskSection, "player-id", data);
            if (playerId == null || !ScriptHelper.isString(playerId)) {
                performantPlants.getLogger().warning(String.format("Task's player-id must be ScriptType STRING in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setPlayerId(playerId);
        }
        // set current player, if present
        if (taskSection.isSet("current-player")) {
            ScriptBlock currentPlayer = createPlantScript(taskSection, "current-player", data);
            if (currentPlayer == null || !ScriptHelper.isBoolean(currentPlayer)) {
                performantPlants.getLogger().warning(String.format("Task's current-player must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setCurrentPlayer(currentPlayer);
        }
        // set current block, if present
        if (taskSection.isSet("current-block")) {
            ScriptBlock currentBlock = createPlantScript(taskSection, "current-block", data);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning(String.format("Task's current-block must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setCurrentBlock(currentBlock);
        }
        // set autostart, if present
        if (taskSection.isSet("autostart")) {
            ScriptBlock currentBlock = createPlantScript(taskSection, "autostart", data);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning(String.format("Task's autostart must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setAutostart(currentBlock);
        }
        // add hooks
        ArrayList<ScriptHook> hooks = createPlantScriptHooks(scriptTask, taskSection, "hooks", data);
        if (hooks == null) {
            performantPlants.getLogger().warning(String.format("Task's hooks had issue loading in section: %s",
                    section.getCurrentPath()));
            return null;
        }
        for (ScriptHook hook : hooks) {
            scriptTask.addHook(hook);
        }
        // return created script task
        return scriptTask;
    }
    //endregion

    //region Plant Script Hooks
    ArrayList<ScriptHook> createPlantScriptHooks(ScriptTask scriptTask, ConfigurationSection section, String subsectionName, PlantData data) {
        // if section exists
        ArrayList<ScriptHook> hooks = new ArrayList<>();
        if (section.isSet(subsectionName)) {
            ConfigurationSection hooksSection = section.getConfigurationSection(subsectionName);
            // create hooks with START action
            if (hooksSection.isSet("start")) {
                ConfigurationSection typeSection = hooksSection.getConfigurationSection("start");
                for (String hookName : typeSection.getKeys(false)) {
                    ScriptHook hook = createPlantScriptHook(HookAction.START, scriptTask, typeSection, hookName, data);
                    if (hook == null) {
                        performantPlants.getLogger().warning(String.format("Hook '%s' with action START not added in section: %s",
                                hookName, typeSection.getCurrentPath()));
                        return null;
                    }
                    hooks.add(hook);
                }
            }
            // create hooks with PAUSE action
            if (hooksSection.isSet("pause")) {
                ConfigurationSection typeSection = hooksSection.getConfigurationSection("pause");
                for (String hookName : typeSection.getKeys(false)) {
                    ScriptHook hook = createPlantScriptHook(HookAction.PAUSE, scriptTask, typeSection, hookName, data);
                    if (hook == null) {
                        performantPlants.getLogger().warning(String.format("Hook '%s' with action PAUSE not added in section: %s",
                                hookName, typeSection.getCurrentPath()));
                        return null;
                    }
                    hooks.add(hook);
                }
            }
            // create hooks with CANCEL action
            if (hooksSection.isSet("cancel")) {
                ConfigurationSection typeSection = hooksSection.getConfigurationSection("cancel");
                for (String hookName : typeSection.getKeys(false)) {
                    ScriptHook hook = createPlantScriptHook(HookAction.CANCEL, scriptTask, typeSection, hookName, data);
                    if (hook == null) {
                        performantPlants.getLogger().warning(String.format("Hook '%s' with action CANCEL not added in section: %s",
                                hookName, typeSection.getCurrentPath()));
                        return null;
                    }
                    hooks.add(hook);
                }
            }
        }
        // return list
        return hooks;
    }

    ScriptHook createPlantScriptHook(HookAction action, ScriptTask task, ConfigurationSection section, String subsectionName, PlantData data) {
        ConfigurationSection hookSection = section.getConfigurationSection(subsectionName);
        if (hookSection == null) {
            return null;
        }
        String type = hookSection.getString("type");
        if (type == null) {
            performantPlants.getLogger().warning(String.format("Type not set for hook in section: %s", hookSection.getCurrentPath()));
            return null;
        }
        // create hook from matching type
        ScriptHook scriptHook = null;
        switch (type) {
            case "player_alive":
                scriptHook = createPlantScriptHookPlayerAlive(action, task, hookSection, data); break;
            case "player_dead":
                scriptHook = createPlantScriptHookPlayerDead(action, task, hookSection, data); break;
            case "player_online":
                scriptHook = createPlantScriptHookPlayerOnline(action, task, hookSection, data); break;
            case "player_offline":
                scriptHook = createPlantScriptHookPlayerOffline(action, task, hookSection, data); break;
            default:
                performantPlants.getLogger().warning(String.format("Type '%s' not recognized as a hook in section: %s",
                        type, hookSection.getCurrentPath()));
                return null;
        }
        // set script block + config id, if applicable
        scriptHook = createPlantScriptHookScriptBlockAndConfigId(scriptHook, action, hookSection, data);
        return scriptHook;
    }

    ArrayList<ScriptBlock> createPlantScriptHookPlayerInputs(ScriptTask task, ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> playerHookInputs = new ArrayList<>();
        // get current-player block, if set
        if (section.isSet("current-player")) {
            ScriptBlock currentPlayer = createPlantScript(section, "current-player", data);
            if (currentPlayer == null || !ScriptHelper.isBoolean(currentPlayer)) {
                performantPlants.getLogger().warning(String.format("Hook's current-player must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            playerHookInputs.add(currentPlayer);
        } else {
            // otherwise set to scriptTask's default value
            playerHookInputs.add(task.getCurrentPlayer());
        }
        // get player-id block, if set
        if (section.isSet("player-id")) {
            ScriptBlock playerId = createPlantScript(section, "player-id", data);
            if (playerId == null || !ScriptHelper.isString(playerId)) {
                performantPlants.getLogger().warning(String.format("Hook's player-id must be ScriptType STRING in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            playerHookInputs.add(playerId);
        } else {
            // otherwise set to scriptTask's default value
            playerHookInputs.add(task.getPlayerId());
        }
        // verify list of length 2 as expected
        if (playerHookInputs.size() != 2) {
            return null;
        }
        return playerHookInputs;
    }

    ScriptHook createPlantScriptHookScriptBlockAndConfigId(ScriptHook hook, HookAction action, ConfigurationSection section, PlantData data) {
        if (hook == null) {
            return null;
        }
        if (section.isSet("plant-script")) {
            ScriptBlock scriptBlock = createPlantScript(section, "plant-script", data);
            if (scriptBlock == null) {
                performantPlants.getLogger().warning("Plant-script section is not valid plant script block in section: " + section.getCurrentPath());
                return null;
            }
            hook.setHookScriptBlock(scriptBlock);
        }
        hook.setHookConfigId(action.toString() + "." + section.getName());
        return hook;
    }

    ScriptHookPlayerAlive createPlantScriptHookPlayerAlive(HookAction action, ScriptTask task, ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, data);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerAlive hook = new ScriptHookPlayerAlive(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerDead createPlantScriptHookPlayerDead(HookAction action, ScriptTask task, ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, data);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerDead hook = new ScriptHookPlayerDead(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerOnline createPlantScriptHookPlayerOnline(HookAction action, ScriptTask task, ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, data);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerOnline hook = new ScriptHookPlayerOnline(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerOffline createPlantScriptHookPlayerOffline(HookAction action, ScriptTask task, ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, data);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerOffline hook = new ScriptHookPlayerOffline(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }
    //endregion

    ScriptBlock createPlantScript(ConfigurationSection section, String subsectionName, PlantData data) {
        try {
            if (section == null) {
                return null;
            }
            else if (subsectionName != null) {
                if (section.isBoolean(subsectionName)) {
                    return new ScriptResult(section.getBoolean(subsectionName));
                } else if (section.isInt(subsectionName)) {
                    return new ScriptResult(section.getInt(subsectionName));
                } else if (section.isLong(subsectionName)) {
                    return new ScriptResult(section.getLong(subsectionName));
                } else if (section.isDouble(subsectionName)) {
                    return new ScriptResult(section.getDouble(subsectionName));
                } else if (section.isString(subsectionName)) {
                    return new ScriptResult(section.getString(subsectionName));
                } else {
                    return createPlantScriptSpecific(section.getConfigurationSection(subsectionName), data);
                }
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            performantPlants.getLogger().warning(String.format("IllegalArgumentException loading PlantScript in section '%s' for " +
                    "subsection '%s': '%s'", section.getCurrentPath(), subsectionName, e.toString()));
            return null;
        }
    }


    ScriptBlock createPlantScriptSpecific(ConfigurationSection section, PlantData data) {
        // get plant script
        if (section == null) {
            return null;
        }
        for (String blockName : section.getKeys(false)) {
            boolean directValue = false;
            ConfigurationSection blockSection = section.getConfigurationSection(blockName);
            if (blockSection == null) {
                blockSection = section;
                directValue = true;
            }
            ScriptBlock returned;
            switch (blockName.toLowerCase()) {
                // constant/variable
                case "value":
                case "variable":
                case "result":
                    returned = createPlantScriptResult(blockSection, data); break;
                // reference to defined stored-script-block in plant
                case "stored":
                    returned = getStoredScriptBlock(blockSection, directValue, blockName, data); break;
                // math
                case "+":
                case "add":
                    returned = createScriptOperationAdd(blockSection, directValue, data); break;
                case "+=":
                case "addto":
                    returned = createScriptOperationAddTo(blockSection, directValue, data); break;
                case "-":
                case "subtract":
                    returned = createScriptOperationSubtract(blockSection, directValue, data); break;
                case "-=":
                case "subtractfrom":
                    returned = createScriptOperationSubtractFrom(blockSection, directValue, data); break;
                case "*":
                case "multiply":
                    returned = createScriptOperationMultiply(blockSection, directValue, data); break;
                case "*=":
                case "multiplyby":
                    returned = createScriptOperationMultiplyBy(blockSection, directValue, data); break;
                case "/":
                case "divide":
                    returned = createScriptOperationDivide(blockSection, directValue, data); break;
                case "/=":
                case "divideby":
                    returned = createScriptOperationDivideBy(blockSection, directValue, data); break;
                case "%":
                case "modulus":
                    returned = createScriptOperationModulus(blockSection, directValue, data); break;
                case "%=":
                case "modulusof":
                    returned = createScriptOperationModulusOf(blockSection, directValue, data); break;
                case "**":
                case "power":
                    returned = createScriptOperationPower(blockSection, directValue, data); break;
                case "**=":
                case "powerof":
                    returned = createScriptOperationPowerOf(blockSection, directValue, data); break;
                // logic
                case "&&":
                case "and":
                    returned = createScriptOperationAnd(blockSection, directValue, data); break;
                case "!&&":
                case "nand":
                    returned = createScriptOperationNand(blockSection, directValue, data); break;
                case "||":
                case "or":
                    returned = createScriptOperationOr(blockSection, directValue, data); break;
                case "^":
                case "xor":
                    returned = createScriptOperationXor(blockSection, directValue, data); break;
                case "!||":
                case "nor":
                    returned = createScriptOperationNor(blockSection, directValue, data);  break;
                case "!":
                case "not":
                    returned = createScriptOperationNot(blockSection, directValue, blockName, data); break;
                // compare
                case "==":
                case "equal":
                case "equals":
                    returned = createScriptOperationEqual(blockSection, directValue, data); break;
                case "!=":
                case "notequal":
                case "notequals":
                    returned = createScriptOperationNotEqual(blockSection, directValue, data); break;
                case ">":
                case "greaterthan":
                    returned = createScriptOperationGreaterThan(blockSection, directValue, data); break;
                case ">=":
                case "greaterthanorequalto":
                    returned = createScriptOperationGreaterThanOrEqualTo(blockSection, directValue, data); break;
                case "<":
                case "lessthan":
                    returned = createScriptOperationLessThan(blockSection, directValue, data); break;
                case "<=":
                case "lessthanorequalto":
                    returned = createScriptOperationLessThanOrEqualTo(blockSection, directValue, data); break;
                // cast
                case "(boolean)":
                case "toboolean":
                    returned = createScriptOperationToBoolean(blockSection, directValue, blockName, data); break;
                case "(double)":
                case "todouble":
                    returned = createScriptOperationToDouble(blockSection, directValue, blockName, data); break;
                case "(long)":
                case "tolong":
                    returned = createScriptOperationToLong(blockSection, directValue, blockName, data); break;
                case "(string)":
                case "tostring":
                    returned = createScriptOperationToString(blockSection, directValue, blockName, data); break;
                // functions
                case "contains":
                    returned = createScriptOperationContains(blockSection, directValue, data); break;
                case "length":
                    returned = createScriptOperationLength(blockSection, directValue, blockName, data); break;
                case "=":
                case "setvalue":
                    returned = createScriptOperationSetValue(blockSection, directValue, data); break;
                case "setvaluescope":
                case "setvaluescopeparameter":
                    returned = createScriptOperationSetValueScopeParameter(blockSection, directValue, data); break;
                case "getvaluescope":
                case "getvaluescopeparameter":
                    returned = createScriptOperationGetValueScopeParameter(blockSection, directValue, data); break;
                case "removescope":
                case "removescopeparameter":
                    returned = createScriptOperationRemoveScopeParameter(blockSection, directValue, data); break;
                case "containsscope":
                case "containsscopeparameter":
                    returned = createScriptOperationContainsScopeParameter(blockSection, directValue, data); break;
                // flow
                case "if":
                    returned = createScriptOperationIf(blockSection, directValue, data); break;
                case "func":
                case "function":
                    returned = createScriptOperationFunction(blockSection, directValue, data); break;
                case "switch":
                    returned = createScriptOperationSwitch(blockSection, directValue, data); break;
                // action
                case "changestage":
                    returned = createScriptOperationChangeStage(blockSection, directValue, data); break;
                case "interact":
                    returned = createScriptOperationInteract(blockSection, directValue, data); break;
                case "consumable":
                    returned = createScriptOperationConsumable(blockSection, directValue, data); break;
                case "effects":
                    returned = createScriptOperationEffects(blockSection, directValue, data); break;
                case "createblocks":
                    returned = createScriptOperationCreatePlantBlocks(blockSection, directValue, data); break;
                case "scheduletask":
                    returned = createScriptOperationScheduleTask(blockSection, directValue, data); break;
                case "canceltask":
                    returned = createScriptOperationCancelTask(blockSection, directValue, data); break;
                // player
                case "isplayernull":
                    returned = new ScriptOperationIsPlayerNull(); break;
                case "isplayerdead":
                    returned = new ScriptOperationIsPlayerDead(); break;
                case "isplayersneaking":
                    returned = new ScriptOperationIsPlayerSneaking(); break;
                case "isplayersprinting":
                    returned = new ScriptOperationIsPlayerSprinting(); break;
                case "passonlyplayer":
                    returned = createScriptOperationPassOnlyPlayer(blockSection, directValue, blockName, data); break;
                // block
                case "isblocknull":
                    returned = new ScriptOperationIsBlockNull(); break;
                case "passonlyblock":
                    returned = createScriptOperationPassOnlyBlock(blockSection, directValue, blockName, data); break;
                // world
                case "getworld":
                    returned = new ScriptOperationGetWorld(); break;
                // inventory
                case "hasmainhandenchantment":
                    returned =  createScriptOperationHasMainHandEnchantment(blockSection, directValue, blockName, data); break;
                case "getmainhandenchantmentlevel":
                    returned =  createScriptOperationGetMainHandEnchantmentLevel(blockSection, directValue, blockName, data); break;
                // random
                case "chance":
                    returned = createScriptOperationChance(blockSection, directValue, blockName, data); break;
                case "choice":
                    returned = createScriptOperationChoice(blockSection, directValue, data); break;
                case "randomdouble":
                    returned = createScriptOperationRandomDouble(blockSection, directValue, data); break;
                case "randomlong":
                    returned = createScriptOperationRandomLong(blockSection, directValue, data); break;
                // not recognized
                default:
                    performantPlants.getLogger().warning(String.format("PlantScript block of type '%s' not recognized; this " +
                            "PlantScript will not be loaded until this is fixed in blockSection: %s",
                            blockName, blockSection.getCurrentPath()));
                    return null;
            }
            if (returned != null) {
                return returned.optimizeSelf();
            }
            return null;
        }
        performantPlants.getLogger().warning(String.format("No PlantScript block defined in section: %s",
                section.getCurrentPath()));
        return null;
    }

    ScriptResult createPlantScriptResult(ConfigurationSection section, PlantData data) {
        ScriptResult scriptResult = null;
        if (section.isString("variable")) {
            // get variable from data
            String variableName = section.getString("variable");
            // if variableName is null, don't go any further
            if (variableName == null) {
                performantPlants.getLogger().warning(String.format("Variable name could not be parsed, so this PlantScript will " +
                        "not be loaded until this is fixed in section: %s", section.getCurrentPath()));
                return null;
            }
            // if no plant data present or data does not contain variable name, return null
            Object variableValue = ScriptHelper.getGlobalPlantDataVariableValue(data, variableName);
            if (variableValue == null) {
                performantPlants.getLogger().warning(String.format("Variable '%s' not found, so this PlantScript will not be " +
                        "loaded until that is fixed in section: %s", variableName, section.getCurrentPath()));
                return null;
            }
            // get type from returned object
            ScriptType type = ScriptHelper.getType(variableValue);
            if (type == null) {
                performantPlants.getLogger().warning(String.format("Variable '%s' is stored with unrecognized type; something " +
                        "really went wrong so this PlantScript will not be loaded until this is fixed in section: %s",
                        variableName, section.getCurrentPath()));
                return null;
            }
            // return ScriptResult containing variable name + type
            scriptResult = new ScriptResult(variableName, type);
        } else if (section.isSet("value")) {
            // try to return ScriptResult containing value
            try {
                scriptResult = new ScriptResult(section.get("value"));
            } catch (IllegalArgumentException e) {
                performantPlants.getLogger().warning(String.format("Value could not be parsed into recognized type; this " +
                        "PlantScript will not be loaded until this is fixed in section: %s", section.getCurrentPath()));
                return null;
            }
        }
        if (scriptResult != null) {
            if (section.isBoolean("is-placeholder")) {
                scriptResult.setHasPlaceholder(section.getBoolean("is-placeholder"));
            }
            return scriptResult;
        }
        performantPlants.getLogger().warning(String.format("No variable section or value provided; this PlantScript will not be " +
                "loaded until this is fixed in section: %s", section.getCurrentPath()));
        return null;
    }

    // types - helpful
    ScriptBlock createScriptOperationUnary(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        if (!directValue) {
            return createPlantScript(section.getParent(), sectionName, data);
        }
        return createPlantScript(section, sectionName, data);
    }

    ArrayList<ScriptBlock> createScriptOperationBinary(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationBinary in section: %s", section.getCurrentPath()));
            return null;
        }
        if (!section.isSet("left") || !section.isSet("right")) {
            performantPlants.getLogger().warning("Left or right operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock left = createPlantScript(section, "left", data);
        if (left == null) {
            return null;
        }
        ScriptBlock right = createPlantScript(section, "right", data);
        if (right == null) {
            return null;
        }
        ArrayList<ScriptBlock> arrayList = new ArrayList<>();
        arrayList.add(left);
        arrayList.add(right);
        return arrayList;
    }

    // stored script block
    ScriptBlock getStoredScriptBlock(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        if (data == null || data.getPlant() == null) {
            return null;
        }
        ScriptBlock scriptBlockName = createScriptOperationUnary(section, directValue, sectionName, data);
        if (scriptBlockName == null) {
            performantPlants.getLogger().warning("Name of script block name could not be parsed in section: " +
                    section.getCurrentPath());
            return null;
        }
        if (scriptBlockName.containsVariable() || !(scriptBlockName instanceof ScriptResult)) {
            performantPlants.getLogger().warning("Script block name of stored script block cannot contain variables or not " +
                    "be ScriptResult in section: " + section.getCurrentPath());
            return null;
        }
        String storedBlockName = scriptBlockName.loadValue(null).getStringValue();
        ScriptBlock storedBlock = data.getPlant().getScriptBlock(storedBlockName);
        if (storedBlock == null) {
            performantPlants.getLogger().warning(String.format("Stored script block '%s' not found in section: %s",
                    storedBlockName, section.getCurrentPath()));
            return null;
        }

        if (currentTaskLoader != null) {
            // set dependency of stored script, if this is being called from inside a stored script definition
            if (currentTaskLoader.isScriptSet()) {
                currentTaskLoader.addScriptScriptDependent(storedBlockName, currentTaskLoader.getCurrentScript());
            }
            // set dependency of task, if this is being called from inside a task definition
            else if (currentTaskLoader.isTaskSet()) {
                currentTaskLoader.addScriptTaskDependent(storedBlockName, currentTaskLoader.getCurrentTask());
            }
        }
        return storedBlock;
    }

    // math
    ScriptOperation createScriptOperationAdd(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAdd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationAddTo(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAddTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtract(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtract(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtractFrom(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtractFrom(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiply(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiply(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiplyBy(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiplyBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivide(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivide(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivideBy(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivideBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulus(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulus(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulusOf(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulusOf(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPower(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPower(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPowerOf(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPowerOf(operands.get(0), operands.get(1));
    }
    
    //logic
    ScriptOperation createScriptOperationAnd(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAnd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNand(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNand(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationOr(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationOr(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationXor(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationXor(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNor(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNor(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNot(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationNot(operand);
    }
    
    //compare
    ScriptOperation createScriptOperationEqual(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNotEqual(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNotEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThan(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThanOrEqualTo(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThanOrEqualTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThan(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThanOrEqualTo(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThanOrEqualTo(operands.get(0), operands.get(1));
    }

    // cast
    ScriptOperation createScriptOperationToBoolean(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToBoolean(operand);
    }
    ScriptOperation createScriptOperationToDouble(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToDouble(operand);
    }
    ScriptOperation createScriptOperationToLong(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToLong(operand);
    }
    ScriptOperation createScriptOperationToString(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToString(operand);
    }

    // functions
    ScriptOperation createScriptOperationContains(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationContains(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLength(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationLength(operand);
    }
    ScriptOperation createScriptOperationSetValue(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSetValue(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationRemoveScopeParameter(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationRemoveScopeParameter in section: %s", section.getCurrentPath()));
            return null;
        }
        String plantIdString = "plant-id";
        String scopeString = "scope";
        String parameterString = "parameter";
        // set plantId
        if (!section.isSet(plantIdString)) {
            performantPlants.getLogger().warning("plant-id operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock plantId = createPlantScript(section, plantIdString, data);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, data);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, data);
        if (parameter == null) {
            return null;
        }
        return new ScriptOperationRemoveScopeParameter(plantId, scope, parameter);
    }
    ScriptOperation createScriptOperationContainsScopeParameter(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationContainsScopeParameter in section: %s", section.getCurrentPath()));
            return null;
        }
        String plantIdString = "plant-id";
        String scopeString = "scope";
        String parameterString = "parameter";
        // set plantId
        if (!section.isSet(plantIdString)) {
            performantPlants.getLogger().warning("plant-id operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock plantId = createPlantScript(section, plantIdString, data);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, data);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, data);
        if (parameter == null) {
            return null;
        }
        return new ScriptOperationContainsScopeParameter(plantId, scope, parameter);
    }
    ScriptOperation createScriptOperationSetValueScopeParameter(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationSetValueScopeParameter in section: %s", section.getCurrentPath()));
            return null;
        }
        String plantIdString = "plant-id";
        String scopeString = "scope";
        String parameterString = "parameter";
        String variableNameString = "variable";
        String valueString = "value";
        // set plantId
        if (!section.isSet(plantIdString)) {
            performantPlants.getLogger().warning("plant-id operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock plantId = createPlantScript(section, plantIdString, data);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, data);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, data);
        if (parameter == null) {
            return null;
        }
        // set variableName
        if (!section.isSet(variableNameString)) {
            performantPlants.getLogger().warning("variable operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock variableName = createPlantScript(section, variableNameString, data);
        if (variableName == null) {
            return null;
        }
        // set value
        if (!section.isSet(valueString)) {
            performantPlants.getLogger().warning("value operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock value = createPlantScript(section, valueString, data);
        if (value == null) {
            return null;
        }
        return new ScriptOperationSetValueScopeParameter(plantId, scope, parameter, variableName, value);
    }
    ScriptOperation createScriptOperationGetValueScopeParameter(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationGetValueScopeParameter in section: %s", section.getCurrentPath()));
            return null;
        }
        String plantIdString = "plant-id";
        String scopeString = "scope";
        String parameterString = "parameter";
        String variableNameString = "variable";
        String typeString = "type";
        // set plantId
        if (!section.isSet(plantIdString)) {
            performantPlants.getLogger().warning("plant-id operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock plantId = createPlantScript(section, plantIdString, data);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, data);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, data);
        if (parameter == null) {
            return null;
        }
        // set variableName
        if (!section.isSet(variableNameString)) {
            performantPlants.getLogger().warning("variable operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock variableName = createPlantScript(section, variableNameString, data);
        if (variableName == null) {
            return null;
        }
        // set type
        ScriptType expectedType = ScriptType.STRING;
        if (section.isSet(typeString)) {
            expectedType = ScriptType.fromString(section.getString(typeString));
            if (expectedType == null) {
                performantPlants.getLogger().warning(String.format("type '%s' not recognized in section: %s",
                        section.getString(typeString),
                        section.getCurrentPath()));
                return null;
            }
        }
        return new ScriptOperationGetValueScopeParameter(plantId, scope, parameter, variableName, expectedType);
    }

    //flow
    ScriptOperation createScriptOperationIf(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationIf in section: %s", section.getCurrentPath()));
            return null;
        }
        String conditionString = "condition";
        String ifTrueString = "if-true";
        String ifFalseString = "if-false";
        if (!section.isSet(conditionString) || !section.isSet(ifTrueString)) {
            performantPlants.getLogger().warning("Condition or if-true operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock condition = createPlantScript(section, conditionString, data);
        if (condition == null) {
            return null;
        }
        ScriptBlock ifTrue = createPlantScript(section, ifTrueString, data);
        if (ifTrue == null) {
            return null;
        }
        if (!section.isSet("if-false")) {
            return new ScriptOperationIf(condition, ifTrue);
        }
        ScriptBlock ifFalse = createPlantScript(section, ifFalseString, data);
        if (ifFalse == null) {
            return null;
        }
        return new ScriptOperationIf(condition, ifTrue, ifFalse);
    }
    ScriptOperation createScriptOperationFunction(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationFunction in section: %s", section.getCurrentPath()));
            return null;
        }
        int index = 0;
        ScriptBlock[] scriptBlocks = new ScriptBlock[section.getKeys(false).size()];
        for (String placeholder : section.getKeys(false)) {
            if (!section.isSet(placeholder)) {
                performantPlants.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
                        "Function in section: %s", placeholder, section));
                return null;
            }
            ScriptBlock scriptBlock = createPlantScript(section, placeholder, data);
            if (scriptBlock == null) {
                return null;
            }
            scriptBlocks[index] = scriptBlock;
            index++;
        }
        if (scriptBlocks.length == 0) {
            performantPlants.getLogger().warning("No subsections found to generate PlantScript Function in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationFunction(scriptBlocks);
    }
    ScriptOperation createScriptOperationSwitch(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationSwitch in section: %s", section.getCurrentPath()));
            return null;
        }
        String conditionString = "condition";
        String casesString = "cases";
        String defaultString = "default";
        HashMap<ScriptResult, ScriptBlock> casesMap = new HashMap<>();
        ScriptBlock defaultCase = null;
        // get condition script block
        if (!section.isSet(conditionString)) {
            performantPlants.getLogger().warning("Condition operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock condition = createPlantScript(section, conditionString, data);
        if (condition == null) {
            return null;
        }
        // get cases, if present
        if (section.isSet(casesString)) {
            if (!section.isConfigurationSection(casesString)) {
                performantPlants.getLogger().warning("Cases section missing for ScriptOperationSwitch in section: " +
                        section.getCurrentPath());
                return null;
            }
            ConfigurationSection casesSection = section.getConfigurationSection(casesString);
            if (casesSection == null) {
                performantPlants.getLogger().warning("Cases section could not be parsed for ScriptOperationSwitch in section: " +
                        section.getCurrentPath());
                return null;
            }
            // iterate through cases
            for (String caseName : casesSection.getKeys(false)) {
                if (!casesSection.isSet(caseName)) {
                    performantPlants.getLogger().warning(String.format("No case subsection found to generate PlantScript for case '%s' in " +
                            "ScriptOperationSwitch in section: %s", caseName, section));
                    return null;
                }
                ScriptBlock scriptBlock = createPlantScript(casesSection, caseName, data);
                if (scriptBlock == null) {
                    return null;
                }
                ScriptResult scriptResult = new ScriptResult(caseName);
                switch(condition.getType()) {
                    case LONG:
                        scriptResult = new ScriptResult(scriptResult.getLongValue()); break;
                    case DOUBLE:
                        scriptResult = new ScriptResult(scriptResult.getDoubleValue()); break;
                    case BOOLEAN:
                        if (caseName.equalsIgnoreCase("true")) {
                            scriptResult = ScriptResult.TRUE;
                        } else if (caseName.equalsIgnoreCase("false")) {
                            scriptResult = ScriptResult.FALSE;
                        } else {
                            scriptResult = new ScriptResult(scriptResult.getBooleanValue());
                        } break;
                }
                casesMap.put(scriptResult, scriptBlock);
            }
        }
        // get default case, if set
        if (section.isSet(defaultString)) {
            defaultCase = createPlantScript(section, defaultString, data);
            if (defaultCase == null) {
                performantPlants.getLogger().warning(String.format("Default case could not be parsed for " +
                        "ScriptOperationSwitch in section: %s", section));
                return null;
            }
        }
        // check that any cases (including default) have been defined
        if (casesMap.size() == 0 && defaultCase == null) {
            performantPlants.getLogger().warning(String.format("No cases were defined for " +
                    "ScriptOperationSwitch in section: %s", section));
            return null;
        }
        int blockCount = 1 + casesMap.size();
        if (defaultCase != null) {
            blockCount++;
        }
        ScriptResult[] cases = new ScriptResult[casesMap.size()];
        ScriptBlock[] scriptBlocks = new ScriptBlock[blockCount];
        // set first script block to condition block
        scriptBlocks[0] = condition;
        int index = 0;
        // set cases and scriptBlocks array
        for (Map.Entry<ScriptResult, ScriptBlock> entry : casesMap.entrySet()) {
            cases[index] = entry.getKey();
            scriptBlocks[index+1] = entry.getValue();
            index++;
        }
        // add defaultCase at the end of scriptBlocks if exists
        if (defaultCase != null) {
            scriptBlocks[scriptBlocks.length-1] = defaultCase;
        }
        // create switch operation
        return new ScriptOperationSwitch(scriptBlocks, cases);
    }

    //action
    ScriptOperation createScriptOperationInteract(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationInteract in section: %s", section.getCurrentPath()));
            return null;
        }
        PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(section, data);
        if (plantInteractStorage == null) {
            performantPlants.getLogger().warning("Could not load interact section to generate PlantScript Interact in section: " +
                    section.getCurrentPath());
            return null;
        }
        String useMainHandString = "use-main-hand";
        ScriptBlock useMainHand = ScriptResult.TRUE;
        if (section.isSet(useMainHandString)) {
            useMainHand = createPlantScript(section, useMainHandString, data);
        }
        return new ScriptOperationInteract(plantInteractStorage, useMainHand);
    }
    ScriptOperation createScriptOperationConsumable(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationConsumable in section: %s", section.getCurrentPath()));
            return null;
        }
        PlantConsumableStorage plantConsumableStorage = loadPlantConsumableStorage(section, data);
        if (plantConsumableStorage == null) {
            performantPlants.getLogger().warning("Could not load consumable section to generate PlantScript Consumable in " +
                    "section: " + section.getCurrentPath());
            return null;
        }
        String useMainHandString = "use-main-hand";
        ScriptBlock useMainHand = ScriptResult.TRUE;
        if (section.isSet(useMainHandString)) {
            useMainHand = createPlantScript(section, useMainHandString, data);
        }
        return new ScriptOperationConsumable(plantConsumableStorage, useMainHand);
    }
    ScriptOperation createScriptOperationEffects(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationEffects in section: %s", section.getCurrentPath()));
            return null;
        }
        PlantEffectStorage plantEffectStorage = new PlantEffectStorage();
        addEffectsToEffectStorage(section, plantEffectStorage, data, true);
        return new ScriptOperationEffects(plantEffectStorage);
    }
    ScriptOperation createScriptOperationChangeStage(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationChangeStage in section: %s", section.getCurrentPath()));
            return null;
        }
        String stageString = "go-to-stage";
        String ifNextString = "go-to-next";
        if (!section.isSet(stageString) && !section.isSet(ifNextString)) {
            performantPlants.getLogger().warning("Go-to-stage and go-to-next both missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock stage = ScriptResult.EMPTY;
        ScriptBlock ifNext = ScriptResult.FALSE;
        if (section.isSet(stageString)) {
            stage = createPlantScript(section, stageString, data);
        }
        if (section.isSet(ifNextString)) {
            ifNext = createPlantScript(section, ifNextString, data);
        }
        return new ScriptOperationChangeStage(performantPlants, stage, ifNext);
    }
    ScriptOperation createScriptOperationCreatePlantBlocks(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationCreatePlantBlocks in section: %s", section.getCurrentPath()));
            return null;
        }
        // TODO: fill out
        return null;
    }
    ScriptOperation createScriptOperationScheduleTask(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationScheduleTask in section: %s", section.getCurrentPath()));
            return null;
        }
        // get task config id
        String taskConfigId = section.getString("task");
        if (taskConfigId == null || taskConfigId.isEmpty()) {
            performantPlants.getLogger().warning("Task missing or empty in section: " + section.getCurrentPath());
            return null;
        }
        // verify that stored plant task exists
        // use plant's loaded tasks if not loading tasks right now
        // otherwise use currentTaskLoader
        ScriptTask scriptTask;
        if (currentTaskLoader == null) {
            scriptTask = data.getPlant().getScriptTask(taskConfigId);
            if (scriptTask == null) {
                performantPlants.getLogger().warning(String.format("Task '%s' not recognized for plant '%s' in section: '%s'",
                        taskConfigId, data.getPlant().getId(), section.getCurrentPath()));
                return null;
            }
        } else {
            if (!currentTaskLoader.isTask(taskConfigId)) {
                performantPlants.getLogger().warning(String.format("Task '%s' not recognized as valid task for plant '%s' in section '%s'",
                        taskConfigId, data.getPlant().getId(), section.getCurrentPath()));
                return null;
            }
            // if this is called in a stored script block, add current script as dependent on task being scheduled
            if (currentTaskLoader.isScriptSet()) {
                currentTaskLoader.addTaskScriptDependent(taskConfigId, currentTaskLoader.getCurrentScript());
            }
            // if not a recursive call, add current task as dependent on task being scheduled
            else if (!currentTaskLoader.getCurrentTask().equals(taskConfigId)) {
                currentTaskLoader.addTaskTaskDependent(taskConfigId, currentTaskLoader.getCurrentTask());
            }
        }
        scriptTask = new ScriptTask(data.getPlant().getId(), taskConfigId);
        // get delay, if present
        ScriptBlock delay = scriptTask.getDelay();
        if (section.isSet("delay")) {
            delay = createPlantScript(section, "delay", data);
            if (delay == null || !ScriptHelper.isLong(delay)) {
                performantPlants.getLogger().warning("Delay must be ScriptType LONG in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get current player, if present
        ScriptBlock currentPlayer = scriptTask.getCurrentPlayer();
        if (section.isSet("current-player")) {
            currentPlayer = createPlantScript(section, "current-player", data);
            if (currentPlayer == null || !ScriptHelper.isBoolean(currentPlayer)) {
                performantPlants.getLogger().warning("Current-player must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get current block, if present
        ScriptBlock currentBlock = scriptTask.getCurrentBlock();
        if (section.isSet("current-block")) {
            currentBlock = createPlantScript(section, "current-block", data);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning("Current-block must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get player id, if present
        ScriptBlock playerId = scriptTask.getPlayerId();
        if (section.isSet("player-id")) {
            playerId = createPlantScript(section, "player-id", data);
            if (playerId == null || !ScriptHelper.isString(playerId)) {
                performantPlants.getLogger().warning("Player-id must be ScriptType STRING in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get autostart, if present
        ScriptBlock autostart = scriptTask.getAutostart();
        if (section.isSet("autostart")) {
            autostart = createPlantScript(section, "autostart", data);
            if (autostart == null || !ScriptHelper.isBoolean(autostart)) {
                performantPlants.getLogger().warning("Autostart must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // return operation
        return new ScriptOperationScheduleTask(data.getPlant().getId(), taskConfigId, delay,
                currentPlayer, currentBlock, playerId, autostart);
    }
    ScriptOperation createScriptOperationCancelTask(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationCancelTask in section: %s", section.getCurrentPath()));
            return null;
        }
        // get task id
        ScriptBlock taskId = createPlantScript(section, "task-id", data);
        if (taskId == null || !ScriptHelper.isString(taskId)) {
            performantPlants.getLogger().warning("Task-id must be ScriptType STRING in section: " + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationCancelTask(taskId);
    }

    //random
    ScriptOperation createScriptOperationChance(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationChance(operand);
    }
    ScriptOperation createScriptOperationChoice(ConfigurationSection section, boolean directValue, PlantData data) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationChoice in section: %s", section.getCurrentPath()));
            return null;
        }
        int index = 0;
        ScriptBlock[] scriptBlocks = new ScriptBlock[section.getKeys(false).size()];
        for (String placeholder : section.getKeys(false)) {
            if (!section.isSet(placeholder)) {
                performantPlants.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
                        "Function in section: %s", placeholder, section));
                return null;
            }
            ScriptBlock scriptBlock = createPlantScript(section, placeholder, data);
            if (scriptBlock == null) {
                return null;
            }
            scriptBlocks[index] = scriptBlock;
            index++;
        }
        if (scriptBlocks.length == 0) {
            performantPlants.getLogger().warning("No subsections found to generate PlantScript Choice in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationChoice(scriptBlocks);
    }
    ScriptOperation createScriptOperationRandomDouble(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomDouble(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationRandomLong(ConfigurationSection section, boolean directValue, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomLong(operands.get(0), operands.get(1));
    }

    //player
    private ScriptBlock createScriptOperationPassOnlyPlayer(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationPassOnlyPlayer(operand);
    }

    //block
    private ScriptBlock createScriptOperationPassOnlyBlock(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationPassOnlyBlock(operand);
    }

    //inventory
    private ScriptBlock createScriptOperationHasMainHandEnchantment(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationHasMainHandEnchantment(operand);
    }

    private ScriptBlock createScriptOperationGetMainHandEnchantmentLevel(ConfigurationSection section, boolean directValue, String sectionName, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationGetMainHandEnchantmentLevel(operand);
    }

    //endregion

    String getFileNameWithoutExtension(File file) {
        String fileName = "";
        if (file != null && file.exists()) {
            fileName = file.getName().replaceFirst("[.].+$", "");
        }
        return fileName;
    }

}
