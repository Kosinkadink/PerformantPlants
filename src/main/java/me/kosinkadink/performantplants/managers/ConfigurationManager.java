package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.*;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.ItemStackRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.action.*;
import me.kosinkadink.performantplants.scripting.operations.block.*;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToBoolean;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToDouble;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToLong;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToString;
import me.kosinkadink.performantplants.scripting.operations.compare.*;
import me.kosinkadink.performantplants.scripting.operations.flow.*;
import me.kosinkadink.performantplants.scripting.operations.function.*;
import me.kosinkadink.performantplants.scripting.operations.inventory.*;
import me.kosinkadink.performantplants.scripting.operations.item.*;
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
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantDataStorage;
import me.kosinkadink.performantplants.storage.RequirementStorage;
import me.kosinkadink.performantplants.util.*;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
        ExecutionContext context = new ExecutionContext().set(plant.getPlantData());
        // load plant tasks and stored plant script blocks, if present
        boolean loadedScriptBlocksAndTasks = loadPlantScriptTasksAndScriptBlocks(
                plantConfig,
                "stored-script-blocks",
                "tasks",
                context
        );
        if (!loadedScriptBlocksAndTasks) {
            performantPlants.getLogger().warning("Plant tasks and stored-script-blocks could not be registered for plant:" + plantId);
            return;
        }
        // add item interaction
        addPlantItemInteraction(itemConfig, plantItem, context);
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
                        // add seed item interaction
                        addPlantItemInteraction(seedConfig, seedItem, context);
                    } else {
                        performantPlants.getLogger().info("seedItemSettings were null for plant: " + plantId);
                        return;
                    }
                }
                // if seed item has been set, get growth requirements + stages
                if (plant.getSeedItem() != null) {
                    // set general plant growth time (overridable by stage-specific growth time)
                    if (growingConfig.isSet("growth-time")) {
                        ScriptBlock growthTime = createPlantScript(growingConfig, "growth-time", context);
                        if (growthTime == null || !ScriptHelper.isLong(growthTime)) {
                            performantPlants.getLogger().warning(String.format("Invalid growth-time; it is required and must be ScriptType LONG in section: %s",
                                    growingConfig.getCurrentPath()));
                            return;
                        }
                        plant.setGrowthTime(growthTime);
                    } else {
                        performantPlants.getLogger().warning("No growth-time found for growing for plant: " + plantId);
                        return;
                    }
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
                            context
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
                            // set stage growth time, if present for stage
                            if (stageConfig.isSet("growth-time")) {
                                ScriptBlock growthTime = createPlantScript(stageConfig, "growth-time", context);
                                if (growthTime == null || !ScriptHelper.isLong(growthTime)) {
                                    performantPlants.getLogger().warning(String.format("Invalid stage growth-time; " +
                                                    "for now this stage-specific growth-time will be ignored and " +
                                                    "plant growth-time will be used instead. Growth time must be " +
                                                    "ScriptType LONG in section: %s",
                                            stageConfig.getCurrentPath()));
                                } else {
                                    growthStage.setGrowthTime(growthTime);
                                }
                            }
                            // set drops and/or drop limit
                            if (!addDropsToDropStorage(stageConfig, growthStage.getDropStorage(), context)) {
                                return;
                            }
                            // set growth checkpoint, if present
                            if (stageConfig.isBoolean("growth-checkpoint")) {
                                growthStage.setGrowthCheckpoint(stageConfig.getBoolean("growth-checkpoint"));
                            }
                            // set on-execute, if present
                            if (stageConfig.isConfigurationSection("on-execute")) {
                                // load interaction from default path
                                ScriptBlock onExecute = createPlantScript(stageConfig, "on-execute", context);
                                if (onExecute == null) {
                                    performantPlants.getLogger().warning(String.format("Stage %s's on-execute could not be loaded from section: %s", stageId, stageConfig.getCurrentPath()));
                                    return;
                                }
                                growthStage.setOnExecute(onExecute);
                            }
                            // set on-fail, if present
                            if (stageConfig.isConfigurationSection("on-fail")) {
                                // load interaction from default path
                                ScriptBlock onFail = createPlantScript(stageConfig, "on-fail", context);
                                if (onFail == null) {
                                    performantPlants.getLogger().warning(String.format("Stage %s's on-fail could not be loaded from section: %s", stageId, stageConfig.getCurrentPath()));
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
                            List<GrowthStageBlock> blocks = loadGrowthStageBlocks(stageConfig, "blocks", growthStage, context);
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
                    addPlantItemInteraction(goodSection, goodItem, context);
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
        ScriptBlock storage;
        if (section.isConfigurationSection("on-drop")) {
            storage = createPlantScript(section, "on-drop", new ExecutionContext());
            if (storage == null) {
                performantPlants.getLogger().warning("Vanilla block drop not added; issue loading on-drop script in section: " + section.getCurrentPath());
                return;
            }
        } else {
            performantPlants.getLogger().warning("Vanilla block drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
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
        if (name == null) {
            performantPlants.getLogger().warning("Vanilla entity drop not added; no entity set");
            return;
        }
        EntityType entityType = EnumHelper.getEntityType(name);
        if (entityType == null) {
            performantPlants.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' not recognized", name));
            return;
        }
        if (!entityType.isAlive()) {
            performantPlants.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' is not alive", name));
            return;
        }
        // set interact
        ScriptBlock storage;
        if (section.isConfigurationSection("on-drop")) {
            storage = createPlantScript(section, "on-drop", new ExecutionContext());
            if (storage == null) {
                performantPlants.getLogger().warning("Vanilla entity drop not added; issue loading on-drop script in section: " + section.getCurrentPath());
                return;
            }
        } else {
            performantPlants.getLogger().warning("Vanilla entity drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
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
                    Color potionColor = createColor(colorSection, null).getColor(new ExecutionContext());
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

    DropSettings loadDropConfig(ConfigurationSection section, ExecutionContext context) {
        if (section != null) {
            DropSettings dropSettings = new DropSettings();
            // set amount, if present
            if (section.isSet("amount")) {
                ScriptBlock value = createPlantScript(section, "amount", context);
                if (value == null || !ScriptHelper.isLong(value)) {
                    performantPlants.getLogger().warning(String.format("amount value could not be read or was not ScriptType LONG in drop section: %s",
                            section.getCurrentPath()));
                    return null;
                } else {
                    dropSettings.setAmount(value);
                }
            }
            // set drop condition, if present
            if (section.isSet("condition")) {
                ScriptBlock value = createPlantScript(section, "condition", context);
                if (value == null || !ScriptHelper.isBoolean(value)) {
                    performantPlants.getLogger().warning(String.format("condition value could not be read or was not ScriptType BOOLEAN in drop section: %s",
                            section.getCurrentPath()));
                    return null;
                } else {
                    dropSettings.setCondition(value);
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

    List<GrowthStageBlock> loadGrowthStageBlocks(ConfigurationSection section, String sectionName, GrowthStage growthStage, ExecutionContext context) {
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
                boolean valid = addDropsToDropStorage(blockConfig, growthStageBlock.getDropStorage(), context);
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
            // set right click behavior, if present
            if (blockConfig.isConfigurationSection("on-right")) {
                ScriptBlock plantInteractStorage = createPlantScript(blockConfig, "on-right", context);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-right section: " + blockConfig.getCurrentPath());
                    return null;
                }
                // add interactions to growth stage block
                growthStageBlock.setOnRightClick(plantInteractStorage);
            }
            // set left click behavior, if present
            if (blockConfig.isConfigurationSection("on-left")) {
                ScriptBlock plantInteractStorage = createPlantScript(blockConfig, "on-left", context);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-left section: " + blockConfig.getCurrentPath());
                    return null;
                }
                // add interactions to growth stage block
                growthStageBlock.setOnLeftClick(plantInteractStorage);
            }
            // set break behavior, if present
            if (blockConfig.isConfigurationSection("on-break")) {
                ScriptBlock plantInteractStorage = createPlantScript(blockConfig, "on-break", context);
                if (plantInteractStorage == null) {
                    performantPlants.getLogger().warning("Could not load on-break section: " + blockConfig.getCurrentPath());
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

    ScriptColor createColor(ConfigurationSection section, ExecutionContext context) {
        ScriptColor color = new ScriptColor();
        if (section.isSet("r")) {
            ScriptBlock red = createPlantScript(section, "r", context);
            if (red == null || !ScriptHelper.isLong(red)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen red value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getRed().loadValue(new ExecutionContext()).getIntegerValue(), section.getCurrentPath()));
            }
            color.setRed(red);
        }
        if (section.isSet("g")) {
            ScriptBlock green = createPlantScript(section, "g", context);
            if (green == null || !ScriptHelper.isLong(green)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen green value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getGreen().loadValue(new ExecutionContext()).getIntegerValue(), section.getCurrentPath()));
            }
            color.setGreen(green);
        }
        if (section.isSet("b")) {
            ScriptBlock blue = createPlantScript(section, "b", context);
            if (blue == null || !ScriptHelper.isLong(blue)) {
                performantPlants.getLogger().warning(String.format("Color will not have chosen blue value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getBlue().loadValue(new ExecutionContext()).getIntegerValue(), section.getCurrentPath()));
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
                    performantPlants.getLogger().warning(String.format("Could not create required block in section %s due to: %s",
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

    boolean addDropsToDropStorage(ConfigurationSection section, DropStorage dropStorage, ExecutionContext context) {
        // add drop limit, if present
        if (section.isSet("drop-limit")) {
            ScriptBlock dropLimit = createPlantScript(section, "drop-limit", context);
            if (dropLimit == null || !ScriptHelper.isLong(dropLimit)) {
                performantPlants.getLogger().warning(String.format("Invalid drop-limit, no drop-limit will be set " +
                                "until fixed; must be ScriptType LONG in section: %s",
                        section.getCurrentPath()));
            }
            dropStorage.setDropLimit(dropLimit);
        }
        // add drops
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
                DropSettings dropSettings = loadDropConfig(dropConfig, context);
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
                        dropSettings.getAmount(),
                        dropSettings.getCondition()
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
        if (section.isBoolean("allow-cartography")) {
            plantItem.setAllowCartography(section.getBoolean("allow-cartography"));
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

    void addPlantItemInteraction(ConfigurationSection section, PlantItem plantItem, ExecutionContext context) {
        if (section == null) {
            return;
        }
        // add right click behavior
        if (section.isConfigurationSection("on-right")) {
            ScriptBlock onRightClick = createPlantScript(section, "on-right", context);
            if (onRightClick != null) {
                plantItem.setOnRightClick(onRightClick);
            }
        }
        // add left click behavior
        if (section.isConfigurationSection("on-left")) {
            ScriptBlock onLeftClick = createPlantScript(section, "on-left", context);
            if (onLeftClick != null) {
                plantItem.setOnLeftClick(onLeftClick);
            }
        }
        // add consume behavior
        if (section.isConfigurationSection("on-consume")) {
            ScriptBlock onConsume = createPlantScript(section, "on-consume", context);
            if (onConsume != null) {
                plantItem.setOnConsume(onConsume);
            }
        }
        // TODO: add on-drop
    }

    //region Add Recipes

    void preparePlantRecipe(ConfigurationSection section, PlantRecipe plantRecipe) {
        // load on-craft, if present
        if (section.isConfigurationSection("on-craft")) {
            ScriptBlock storage = createPlantScript(section, "on-craft", new ExecutionContext());
            plantRecipe.setInteract(storage);
        }
        if (section.isBoolean("ignore-result")) {
            boolean ignoreResult = section.getBoolean("ignore-result");
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
        return createPlantData(section, "data", plant);
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
                String invalidReason = ScriptHelper.checkIfValidVariableName(variableName);
                if (invalidReason != null) {
                    performantPlants.getLogger().warning("%s; no variables will in initialized until this is " +
                            "fixed in section: " + plantDataSection.getCurrentPath());
                    return null;
                }
                // get variable info
                ConfigurationSection variableSection = plantDataSection.getConfigurationSection(variableName);
                if (variableSection == null) {
                    performantPlants.getLogger().warning(
                            String.format("No section defined for variable %s; variable will be ignored in section: %s",
                                    variableName, plantDataSection));
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
                            data.put(variableName, variableSection.getBoolean("value", ScriptResult.getDefaultOfType(type).getBooleanValue()));
                            break;
                        case LONG:
                            data.put(variableName, variableSection.getLong("value", ScriptResult.getDefaultOfType(type).getLongValue()));
                            break;
                        case DOUBLE:
                            data.put(variableName, variableSection.getDouble("value",
                                    (double) variableSection.getLong("value", ScriptResult.getDefaultOfType(type).getLongValue())));
                            break;
                        case STRING:
                            data.put(variableName, variableSection.getString("value", ScriptResult.getDefaultOfType(type).getStringValue()));
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

    ScriptPlantData createScriptPlantData(ConfigurationSection section, String sectionName, ExecutionContext context) {
        if (section == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        if (section.isConfigurationSection(sectionName)) {
            ConfigurationSection plantDataSection = section.getConfigurationSection(sectionName);
            HashMap<String, ScriptBlock> scriptBlockMap = new HashMap<>();
            for (String variableName : plantDataSection.getKeys(false)) {
                // check that variable names don't use any reserved or restricted names
                String invalidReason = ScriptHelper.checkIfValidVariableName(variableName);
                if (invalidReason != null) {
                    performantPlants.getLogger().warning("%s; no variables will in initialized until this is " +
                            "fixed in section: " + plantDataSection.getCurrentPath());
                    return null;
                }
                // get variable info
                ConfigurationSection variableSection = plantDataSection.getConfigurationSection(variableName);
                if (variableSection == null) {
                    performantPlants.getLogger().warning(
                            String.format("No section defined for variable '%s'; variable will be ignored in section: %s",
                                    variableName, plantDataSection));
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
                    ScriptBlock value;
                    if (!variableSection.isSet("value")) {
                        value = ScriptResult.getDefaultOfType(type);
                    } else {
                        value = createPlantScript(variableSection, "value", context);
                        if (value == null) {
                            performantPlants.getLogger().warning(String.format("Value of variable '%s' could not be " +
                                    "parsed; no variables will be initialized until this is fixed in section: %s",
                                    variableName, variableSection.getCurrentPath()));
                            return null;
                        }
                        else if (value.getType() != type) {
                            performantPlants.getLogger().warning(
                                    String.format("Variable '%s' expected to be type %s, but was %s; no variables " +
                                            "will be initialized until this is fixed in section: %s",
                                            variableName, type.toString(), value.getType().toString(), variableSection.getCurrentPath()));
                            return null;
                        }
                    }
                    switch(type) {
                        case BOOLEAN:
                            if (value instanceof ScriptResult) {
                                ScriptResult resultValue = (ScriptResult) value;
                                if (!resultValue.containsVariable() && !resultValue.isHasPlaceholder()) {
                                    jsonObject.put(variableName, resultValue.getBooleanValue());
                                    break;
                                }
                            }
                            // put in default value, store script
                            jsonObject.put(variableName, ScriptResult.getDefaultOfType(type).getBooleanValue());
                            scriptBlockMap.put(variableName, value);
                            break;
                        case LONG:
                            if (value instanceof ScriptResult) {
                                ScriptResult resultValue = (ScriptResult) value;
                                if (!resultValue.containsVariable() && !resultValue.isHasPlaceholder()) {
                                    jsonObject.put(variableName, resultValue.getLongValue());
                                    break;
                                }
                            }
                            // put in default value, store script
                            jsonObject.put(variableName, ScriptResult.getDefaultOfType(type).getLongValue());
                            scriptBlockMap.put(variableName, value);
                            break;
                        case DOUBLE:
                            if (value instanceof ScriptResult) {
                                ScriptResult resultValue = (ScriptResult) value;
                                if (!resultValue.containsVariable() && !resultValue.isHasPlaceholder()) {
                                    jsonObject.put(variableName, resultValue.getDoubleValue());
                                    break;
                                }
                            }
                            // put in default value, store script
                            jsonObject.put(variableName, ScriptResult.getDefaultOfType(type).getDoubleValue());
                            scriptBlockMap.put(variableName, value);
                            break;
                        case STRING:
                            if (value instanceof ScriptResult) {
                                ScriptResult resultValue = (ScriptResult) value;
                                if (!resultValue.containsVariable() && !resultValue.isHasPlaceholder()) {
                                    jsonObject.put(variableName, resultValue.getStringValue());
                                    break;
                                }
                            }
                            // put in default value, store script
                            jsonObject.put(variableName, ScriptResult.getDefaultOfType(type).getStringValue());
                            scriptBlockMap.put(variableName, value);
                            break;
                        case ITEMSTACK:
                            if (value instanceof ScriptResult) {
                                ScriptResult resultValue = (ScriptResult) value;
                                if (!resultValue.containsVariable() && !resultValue.isHasPlaceholder()) {
                                    jsonObject.put(variableName, resultValue.getItemStackValue());
                                    break;
                                }
                            }
                            // put in default value, store script
                            jsonObject.put(variableName, ScriptResult.getDefaultOfType(type).getItemStackValue());
                            scriptBlockMap.put(variableName, value);
                            break;
                        default:
                            performantPlants.getLogger().warning(String.format("Variable type %s is not supported right now;" +
                                            "no variables will be initialized until this is fixed in section: %s",
                                    type,
                                    variableSection.getCurrentPath()));
                            return null;
                    }
                } else {
                    performantPlants.getLogger().warning(String.format("No type defined for variable '%s'; no variables will be " +
                                    "initialized until this is fixed in section: %s",
                            variableName,
                            variableSection.getCurrentPath()));
                    return null;
                }
            }
            if (jsonObject.isEmpty()) {
                performantPlants.getLogger().warning("No variables were loaded, so no plant data will be created in section " +
                        section.getConfigurationSection(sectionName).getCurrentPath());
                return null;
            }
            PlantData variableData = new PlantData(jsonObject);
            variableData.setPlant(context.getPlantData().getPlant());
            // create ScriptPlantData
            ScriptPlantData scriptData = new ScriptPlantData(variableData);
            // fill out any script block values to add
            for (Map.Entry<String, ScriptBlock> entry: scriptBlockMap.entrySet()) {
                scriptData.addValueScriptBlock(entry.getKey(), entry.getValue());
            }
            return scriptData;
        }
        return null;
    }

    //region Plant Script Tasks and Stored Script Blocks
    boolean loadPlantScriptTasksAndScriptBlocks(ConfigurationSection section, String scriptSectionName, String taskSectionName, ExecutionContext context) {
        if (section == null || context == null || !context.isPlantDataPossible()) {
            return false;
        }
        PlantData data = context.getPlantData();

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
                ScriptBlock scriptBlock = createPlantScript(scriptBlocksSection, scriptBlockName, context);
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
                ScriptTask scriptTask = createPlantScriptTask(scriptTasksSection, taskId, context);
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

    ScriptTask createPlantScriptTask(ConfigurationSection section, String subsectionName, ExecutionContext context) {
        ScriptTask scriptTask = new ScriptTask(context.getPlantData().getPlant().getId(), subsectionName);
        ConfigurationSection taskSection = section.getConfigurationSection(subsectionName);
        // check that task section exists
        if (taskSection == null) {
            return null;
        }
        // set script block
        currentTaskLoader.setCurrentTask(subsectionName);
        ScriptBlock scriptBlock = createPlantScript(taskSection, "script", context);
        if (scriptBlock == null) {
            return null;
        }
        scriptTask.setTaskScriptBlock(scriptBlock);
        // set delay, if present
        if (taskSection.isSet("delay")) {
            ScriptBlock delay = createPlantScript(taskSection, "delay", context);
            if (delay == null || !ScriptHelper.isLong(delay)) {
                performantPlants.getLogger().warning(String.format("Task's delay must be ScriptType LONG in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setDelay(delay);
        }
        // set specific player id, if present
        if (taskSection.isSet("player-id")) {
            ScriptBlock playerId = createPlantScript(taskSection, "player-id", context);
            if (playerId == null || !ScriptHelper.isString(playerId)) {
                performantPlants.getLogger().warning(String.format("Task's player-id must be ScriptType STRING in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setPlayerId(playerId);
        }
        // set current player, if present
        if (taskSection.isSet("current-player")) {
            ScriptBlock currentPlayer = createPlantScript(taskSection, "current-player", context);
            if (currentPlayer == null || !ScriptHelper.isBoolean(currentPlayer)) {
                performantPlants.getLogger().warning(String.format("Task's current-player must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setCurrentPlayer(currentPlayer);
        }
        // set current block, if present
        if (taskSection.isSet("current-block")) {
            ScriptBlock currentBlock = createPlantScript(taskSection, "current-block", context);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning(String.format("Task's current-block must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setCurrentBlock(currentBlock);
        }
        // set autostart, if present
        if (taskSection.isSet("autostart")) {
            ScriptBlock currentBlock = createPlantScript(taskSection, "autostart", context);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning(String.format("Task's autostart must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            scriptTask.setAutostart(currentBlock);
        }
        // add hooks
        ArrayList<ScriptHook> hooks = createPlantScriptHooks(scriptTask, taskSection, "hooks", context);
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
    ArrayList<ScriptHook> createPlantScriptHooks(ScriptTask scriptTask, ConfigurationSection section, String subsectionName, ExecutionContext context) {
        // if section exists
        ArrayList<ScriptHook> hooks = new ArrayList<>();
        if (section.isSet(subsectionName)) {
            ConfigurationSection hooksSection = section.getConfigurationSection(subsectionName);
            // create hooks with START action
            if (hooksSection.isSet("start")) {
                ConfigurationSection typeSection = hooksSection.getConfigurationSection("start");
                for (String hookName : typeSection.getKeys(false)) {
                    ScriptHook hook = createPlantScriptHook(HookAction.START, scriptTask, typeSection, hookName, context);
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
                    ScriptHook hook = createPlantScriptHook(HookAction.PAUSE, scriptTask, typeSection, hookName, context);
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
                    ScriptHook hook = createPlantScriptHook(HookAction.CANCEL, scriptTask, typeSection, hookName, context);
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

    ScriptHook createPlantScriptHook(HookAction action, ScriptTask task, ConfigurationSection section, String subsectionName, ExecutionContext context) {
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
            // player
            case "player_alive":
                scriptHook = createPlantScriptHookPlayerAlive(action, task, hookSection, context); break;
            case "player_dead":
                scriptHook = createPlantScriptHookPlayerDead(action, task, hookSection, context); break;
            case "player_online":
                scriptHook = createPlantScriptHookPlayerOnline(action, task, hookSection, context); break;
            case "player_offline":
                scriptHook = createPlantScriptHookPlayerOffline(action, task, hookSection, context); break;
            // plant block
            case "plantblock_broken":
                scriptHook = createPlantScriptHookPlantBlockBroken(action, task, hookSection, context); break;
            // plant chunk
            case "plantchunk_loaded":
                scriptHook = createPlantScriptHookPlantChunkLoaded(action, task, hookSection, context); break;
            case "plantchunk_unloaded":
                scriptHook = createPlantScriptHookPlantChunkUnloaded(action, task, hookSection, context); break;
            default:
                performantPlants.getLogger().warning(String.format("Type '%s' not recognized as a hook in section: %s",
                        type, hookSection.getCurrentPath()));
                return null;
        }
        // set script block + config id, if applicable
        scriptHook = createPlantScriptHookScriptBlockAndConfigId(scriptHook, action, hookSection, context);
        return scriptHook;
    }

    ArrayList<ScriptBlock> createPlantScriptHookPlayerInputs(ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = new ArrayList<>();
        // get current-player block, if set
        if (section.isSet("current-player")) {
            ScriptBlock currentPlayer = createPlantScript(section, "current-player", context);
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
            ScriptBlock playerId = createPlantScript(section, "player-id", context);
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

    ArrayList<ScriptBlock> createPlantScriptHookPlantBlockInputs(ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = new ArrayList<>();
        // get current-block block, if set
        if (section.isSet("current-block")) {
            ScriptBlock currentBlock = createPlantScript(section, "current-block", context);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning(String.format("Hook's current-block must be ScriptType BOOLEAN in section: %s",
                        section.getCurrentPath()));
                return null;
            }
            playerHookInputs.add(currentBlock);
        } else {
            // otherwise set to scriptTask's default value
            playerHookInputs.add(task.getCurrentBlock());
        }
        // verify list of length 1 as expected
        if (playerHookInputs.size() != 1) {
            return null;
        }
        return playerHookInputs;
    }

    ArrayList<ScriptBlock> createPlantScriptHookPlantChunkInputs(ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        // same inputs as plant block hooks
        return createPlantScriptHookPlantBlockInputs(task, section, context);
    }

    ScriptHook createPlantScriptHookScriptBlockAndConfigId(ScriptHook hook, HookAction action, ConfigurationSection section, ExecutionContext context) {
        if (hook == null) {
            return null;
        }
        if (section.isSet("script")) {
            ScriptBlock scriptBlock = createPlantScript(section, "script", context);
            if (scriptBlock == null) {
                performantPlants.getLogger().warning("Plant-script section is not valid plant script block in section: " + section.getCurrentPath());
                return null;
            }
            hook.setHookScriptBlock(scriptBlock);
        }
        hook.setHookConfigId(action.toString() + "." + section.getName());
        return hook;
    }
    // player hooks
    ScriptHookPlayerAlive createPlantScriptHookPlayerAlive(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, context);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerAlive hook = new ScriptHookPlayerAlive(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerDead createPlantScriptHookPlayerDead(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, context);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerDead hook = new ScriptHookPlayerDead(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerOnline createPlantScriptHookPlayerOnline(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, context);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerOnline hook = new ScriptHookPlayerOnline(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }

    ScriptHookPlayerOffline createPlantScriptHookPlayerOffline(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> playerHookInputs = createPlantScriptHookPlayerInputs(task, section, context);
        if (playerHookInputs == null) {
            return null;
        }
        ScriptHookPlayerOffline hook = new ScriptHookPlayerOffline(action);
        hook.setCurrentPlayer(playerHookInputs.get(0));
        hook.setPlayerId(playerHookInputs.get(1));
        return hook;
    }
    // plant block hooks
    ScriptHookPlantBlockBroken createPlantScriptHookPlantBlockBroken(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> plantBlockHookInputs = createPlantScriptHookPlantBlockInputs(task, section, context);
        if (plantBlockHookInputs == null) {
            return null;
        }
        ScriptHookPlantBlockBroken hook = new ScriptHookPlantBlockBroken(action);
        hook.setCurrentBlock(plantBlockHookInputs.get(0));
        return hook;
    }
    // plant chunk hooks
    ScriptHookPlantChunkLoaded createPlantScriptHookPlantChunkLoaded(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> plantChunkHookInputs = createPlantScriptHookPlantChunkInputs(task, section, context);
        if (plantChunkHookInputs == null) {
            return null;
        }
        ScriptHookPlantChunkLoaded hook = new ScriptHookPlantChunkLoaded(action);
        hook.setCurrentBlock(plantChunkHookInputs.get(0));
        return hook;
    }

    ScriptHookPlantChunkUnloaded createPlantScriptHookPlantChunkUnloaded(HookAction action, ScriptTask task, ConfigurationSection section, ExecutionContext context) {
        ArrayList<ScriptBlock> plantChunkHookInputs = createPlantScriptHookPlantChunkInputs(task, section, context);
        if (plantChunkHookInputs == null) {
            return null;
        }
        ScriptHookPlantChunkUnloaded hook = new ScriptHookPlantChunkUnloaded(action);
        hook.setCurrentBlock(plantChunkHookInputs.get(0));
        return hook;
    }
    //endregion

    ScriptBlock createPlantScript(ConfigurationSection section, String subsectionName, ExecutionContext context) {
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
                    return createPlantScriptSpecific(section.getConfigurationSection(subsectionName), context);
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


    ScriptBlock createPlantScriptSpecific(ConfigurationSection section, ExecutionContext context) {
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
            try {
                switch (blockName.toLowerCase()) {
                    // constant/variable
                    case "value":
                    case "variable":
                    case "result":
                        returned = createPlantScriptResult(blockSection, context); break;
                    // reference to defined stored-script-block in plant
                    case "stored":
                        returned = getStoredScriptBlock(blockSection, directValue, blockName, context); break;
                    // math
                    case "+":
                    case "add":
                        returned = createScriptOperationAdd(blockSection, directValue, context); break;
                    case "+=":
                    case "addto":
                        returned = createScriptOperationAddTo(blockSection, directValue, context); break;
                    case "-":
                    case "subtract":
                        returned = createScriptOperationSubtract(blockSection, directValue, context); break;
                    case "-=":
                    case "subtractfrom":
                        returned = createScriptOperationSubtractFrom(blockSection, directValue, context); break;
                    case "*":
                    case "multiply":
                        returned = createScriptOperationMultiply(blockSection, directValue, context); break;
                    case "*=":
                    case "multiplyby":
                        returned = createScriptOperationMultiplyBy(blockSection, directValue, context); break;
                    case "/":
                    case "divide":
                        returned = createScriptOperationDivide(blockSection, directValue, context); break;
                    case "/=":
                    case "divideby":
                        returned = createScriptOperationDivideBy(blockSection, directValue, context); break;
                    case "%":
                    case "modulus":
                        returned = createScriptOperationModulus(blockSection, directValue, context); break;
                    case "%=":
                    case "modulusof":
                        returned = createScriptOperationModulusOf(blockSection, directValue, context); break;
                    case "**":
                    case "power":
                        returned = createScriptOperationPower(blockSection, directValue, context); break;
                    case "**=":
                    case "powerof":
                        returned = createScriptOperationPowerOf(blockSection, directValue, context); break;
                    // logic
                    case "&&":
                    case "and":
                        returned = createScriptOperationAnd(blockSection, directValue, context); break;
                    case "!&&":
                    case "nand":
                        returned = createScriptOperationNand(blockSection, directValue, context); break;
                    case "||":
                    case "or":
                        returned = createScriptOperationOr(blockSection, directValue, context); break;
                    case "^":
                    case "xor":
                        returned = createScriptOperationXor(blockSection, directValue, context); break;
                    case "!||":
                    case "nor":
                        returned = createScriptOperationNor(blockSection, directValue, context); break;
                    case "!":
                    case "not":
                        returned = createScriptOperationNot(blockSection, directValue, blockName, context); break;
                    // compare
                    case "==":
                    case "equal":
                    case "equals":
                        returned = createScriptOperationEqual(blockSection, directValue, context); break;
                    case "!=":
                    case "notequal":
                    case "notequals":
                        returned = createScriptOperationNotEqual(blockSection, directValue, context); break;
                    case ">":
                    case "greaterthan":
                        returned = createScriptOperationGreaterThan(blockSection, directValue, context); break;
                    case ">=":
                    case "greaterthanorequalto":
                        returned = createScriptOperationGreaterThanOrEqualTo(blockSection, directValue, context); break;
                    case "<":
                    case "lessthan":
                        returned = createScriptOperationLessThan(blockSection, directValue, context); break;
                    case "<=":
                    case "lessthanorequalto":
                        returned = createScriptOperationLessThanOrEqualTo(blockSection, directValue, context); break;
                    // cast
                    case "(boolean)":
                    case "toboolean":
                        returned = createScriptOperationToBoolean(blockSection, directValue, blockName, context); break;
                    case "(double)":
                    case "todouble":
                        returned = createScriptOperationToDouble(blockSection, directValue, blockName, context); break;
                    case "(long)":
                    case "tolong":
                        returned = createScriptOperationToLong(blockSection, directValue, blockName, context); break;
                    case "(string)":
                    case "tostring":
                        returned = createScriptOperationToString(blockSection, directValue, blockName, context); break;
                    // functions
                    case "contains":
                        returned = createScriptOperationContains(blockSection, directValue, context); break;
                    case "length":
                        returned = createScriptOperationLength(blockSection, directValue, blockName, context); break;
                    case "=":
                    case "setvalue":
                        returned = createScriptOperationSetValue(blockSection, directValue, context); break;
                    case "setvaluescope":
                    case "setvaluescopeparameter":
                        returned = createScriptOperationSetValueScopeParameter(blockSection, directValue, context); break;
                    case "getvaluescope":
                    case "getvaluescopeparameter":
                        returned = createScriptOperationGetValueScopeParameter(blockSection, directValue, context); break;
                    case "removescope":
                    case "removescopeparameter":
                        returned = createScriptOperationRemoveScopeParameter(blockSection, directValue, context); break;
                    case "containsscope":
                    case "containsscopeparameter":
                        returned = createScriptOperationContainsScopeParameter(blockSection, directValue, context); break;
                    case "wrapdata":
                        returned = createScriptOperationWrapData(blockSection, directValue, context); break;
                    case "wrapitem":
                        returned = createScriptOperationWrapItem(blockSection, directValue, context); break;
                    case "delay":
                        returned = createScriptOperationDelay(blockSection, directValue, context); break;
                    // flow
                    case "if":
                        returned = createScriptOperationIf(blockSection, directValue, context); break;
                    case "doif":
                        returned = createScriptOperationDoIf(blockSection, directValue, context); break;
                    case "func":
                    case "function":
                        returned = createScriptOperationFunction(blockSection, directValue, context); break;
                    case "untiltrue":
                        returned = createScriptOperationUntilTrue(blockSection, directValue, context); break;
                    case "untiltruelimit":
                        returned = createScriptOperationUntilTrueLimit(blockSection, directValue, context); break;
                    case "switch":
                        returned = createScriptOperationSwitch(blockSection, directValue, context); break;
                    // action
                    case "changestage":
                        returned = createScriptOperationChangeStage(blockSection, directValue, context); break;
                    case "createblocks":
                        returned = createScriptOperationCreatePlantBlocks(blockSection, directValue, context); break;
                    case "scheduletask":
                        returned = createScriptOperationScheduleTask(blockSection, directValue, context); break;
                    case "canceltask":
                        returned = createScriptOperationCancelTask(blockSection, directValue, context); break;
                    case "dodrops":
                        returned = createScriptOperationDoDrops(blockSection, directValue, context); break;
                    case "console":
                    case "consolecommand":
                        returned = createScriptOperationConsoleCommand(blockSection, directValue, blockName, context); break;
                    case "sound":
                    case "soundeffect":
                        returned = createScriptOperationSoundEffect(blockSection, directValue, context); break;
                    case "particle":
                    case "particleeffect":
                        returned = createScriptOperationParticleEffect(blockSection, directValue, context); break;
                    case "explosion":
                        returned = createScriptOperationExplosion(blockSection, directValue, context); break;
                    case "area":
                    case "areaeffect":
                        returned = createScriptOperationAreaEffect(blockSection, directValue, context); break;
                    // player
                    case "isplayernull":
                        returned = new ScriptOperationIsPlayerNull(); break;
                    case "isplayerdead":
                        returned = new ScriptOperationIsPlayerDead(); break;
                    case "issneaking":
                        returned = new ScriptOperationIsPlayerSneaking(); break;
                    case "issprinting":
                        returned = new ScriptOperationIsPlayerSprinting(); break;
                    case "ismissingfood":
                        returned = new ScriptOperationIsMissingFood(); break;
                    case "useplayer":
                    case "useplayerlocation":
                        returned = createScriptOperationUsePlayerLocation(blockSection, directValue, blockName, context); break;
                    case "useeye":
                    case "useeyelocation":
                        returned = createScriptOperationUseEyeLocation(blockSection, directValue, blockName, context); break;
                    case "passonlyplayer":
                        returned = createScriptOperationPassOnlyPlayer(blockSection, directValue, blockName, context); break;
                    case "heal":
                        returned = createScriptOperationHeal(blockSection, directValue, blockName, context); break;
                    case "feed":
                        returned = createScriptOperationFeed(blockSection, directValue, context); break;
                    case "air":
                        returned = createScriptOperationAir(blockSection, directValue, blockName, context); break;
                    case "message":
                        returned = createScriptOperationMessage(blockSection, directValue, blockName, context); break;
                    case "chat":
                        returned = createScriptOperationChat(blockSection, directValue, blockName, context); break;
                    case "playercommand":
                        returned = createScriptOperationPlayerCommand(blockSection, directValue, blockName, context); break;
                    case "potion":
                    case "potioneffect":
                        returned = createScriptOperationPotionEffect(blockSection, directValue, context); break;
                    // block
                    case "isblocknull":
                        returned = new ScriptOperationIsBlockNull(); break;
                    case "useblock":
                    case "useblocklocation":
                        returned = createScriptOperationUseBlockLocation(blockSection, directValue, blockName, context); break;
                    case "useblockbottom":
                    case "useblockbottomlocation":
                        returned = createScriptOperationUseBlockBottomLocation(blockSection, directValue, blockName, context); break;
                    case "passonlyblock":
                        returned = createScriptOperationPassOnlyBlock(blockSection, directValue, blockName, context); break;
                    case "breakblock":
                        returned = createScriptOperationBreakBlock(blockSection, directValue, blockName, context); break;
                    case "blockfaces":
                    case "requiredblockfaces":
                        returned = createScriptOperationRequiredBlockFaces(blockSection, directValue, blockName, context); break;
                    case "blockface":
                    case "isblockface":
                        returned = createScriptOperationIsBlockFace(blockSection, directValue, blockName, context); break;
                    // world
                    case "getworld":
                        returned = new ScriptOperationGetWorld(); break;
                    // inventory
                    case "getmainhand":
                        returned = new ScriptOperationGetMainHand(); break;
                    case "getoffhand":
                        returned = new ScriptOperationGetOffhand(); break;
                    case "getotherhand":
                        returned = new ScriptOperationGetOtherHand(); break;
                    case "getmatchingitem":
                        returned = createScriptOperationGetMatchingItem(blockSection, directValue, blockName, context); break;
                    case "gethelmet":
                        returned = new ScriptOperationGetHelmet(); break;
                    case "getchestplate":
                        returned = new ScriptOperationGetChestplate(); break;
                    case "getleggings":
                        returned = new ScriptOperationGetLeggings(); break;
                    case "getboots":
                        returned = new ScriptOperationGetBoots(); break;
                    case "hasmainhand":
                        returned = new ScriptOperationHasMainHand(); break;
                    case "hasoffhand":
                        returned = new ScriptOperationHasOffhand(); break;
                    case "hashelmet":
                        returned = new ScriptOperationHasHelmet(); break;
                    case "haschestplate":
                        returned = new ScriptOperationHasChestplate(); break;
                    case "hasleggings":
                        returned = new ScriptOperationHasLeggings(); break;
                    case "hasboots":
                        returned = new ScriptOperationHasBoots(); break;
                    case "ismainhand":
                        returned = new ScriptOperationIsMainHand(); break;
                    case "isoffhand":
                        returned = new ScriptOperationIsOffhand(); break;
                    case "iseitherhand":
                        returned = new ScriptOperationIsEitherHand(); break;
                    case "giveitem":
                        returned = createScriptOperationGiveItem(blockSection, directValue, blockName, context); break;
                    // item
                    case "getcurrentitem":
                        returned = new ScriptOperationCurrentItem(); break;
                    case "takeone":
                        returned = createScriptOperationTakeOne(blockSection, directValue, blockName, context); break;
                    case "getamount":
                        returned = createScriptOperationGetAmount(blockSection, directValue, blockName, context); break;
                    case "isitemanyplant":
                        returned = createScriptOperationIsItemAnyPlant(blockSection, directValue, blockName, context); break;
                    case "ispickaxe":
                        returned = createScriptOperationIsPickaxe(blockSection, directValue, blockName, context); break;
                    case "isaxe":
                        returned = createScriptOperationIsAxe(blockSection, directValue, blockName, context); break;
                    case "isshovel":
                        returned = createScriptOperationIsShovel(blockSection, directValue, blockName, context); break;
                    case "ishoe":
                        returned = createScriptOperationIsHoe(blockSection, directValue, blockName, context); break;
                    case "issword":
                        returned = createScriptOperationIsSword(blockSection, directValue, blockName, context); break;
                    case "iswearable":
                        returned = createScriptOperationIsWearable(blockSection, directValue, blockName, context); break;
                    case "isair":
                        returned = createScriptOperationIsAir(blockSection, directValue, blockName, context); break;
                    case "item":
                        returned = createScriptOperationCreateItemStack(blockSection, directValue); break;
                    case "aresimilar":
                        returned = createScriptOperationAreSimilar(blockSection, directValue, context); break;
                    case "itemismaterial":
                        returned = createScriptOperationItemIsMaterial(blockSection, directValue, context); break;
                    case "itemgetmaterial":
                        returned = createScriptOperationGetMaterial(blockSection, directValue, blockName, context); break;
                    case "getenchantmentlevel":
                        returned = createScriptOperationGetEnchantmentLevel(blockSection, directValue, context); break;
                    case "hasenchantment":
                        returned = createScriptOperationHasEnchantment(blockSection, directValue, context); break;
                    case "adddamage":
                        returned = createScriptOperationAddDamage(blockSection, directValue, context); break;
                    // random
                    case "chance":
                        returned = createScriptOperationChance(blockSection, directValue, blockName, context); break;
                    case "choice":
                        returned = createScriptOperationChoice(blockSection, directValue, context); break;
                    case "randomdouble":
                        returned = createScriptOperationRandomDouble(blockSection, directValue, context); break;
                    case "randomlong":
                        returned = createScriptOperationRandomLong(blockSection, directValue, context); break;
                    // not recognized
                    default:
                        performantPlants.getLogger().warning(String.format("PlantScript block of type '%s' not recognized; this " +
                                        "PlantScript will not be loaded until this is fixed in blockSection: %s",
                                blockName, blockSection.getCurrentPath()));
                        return null;
                }
            } catch (IllegalArgumentException e) {
                performantPlants.getLogger().warning(String.format("Invalid input for %s: '%s' in section: %s", blockName, e.getMessage(), section.getCurrentPath()));
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

    ScriptResult createPlantScriptResult(ConfigurationSection section, ExecutionContext context) {
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
            Object variableValue = ScriptHelper.getAnyDataVariableValue(context, variableName);
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
    ScriptBlock createScriptOperationUnary(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        if (!directValue) {
            return createPlantScript(section.getParent(), sectionName, context);
        }
        return createPlantScript(section, sectionName, context);
    }

    ArrayList<ScriptBlock> createScriptOperationBinary(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        return createScriptOperationBinary(section, directValue, context, "left", "right");
    }

    ArrayList<ScriptBlock> createScriptOperationBinary(ConfigurationSection section, boolean directValue, ExecutionContext context, String leftName, String rightName) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationBinary in section: %s", section.getCurrentPath()));
            return null;
        }
        if (!section.isSet(leftName)) {
            performantPlants.getLogger().warning(String.format("%s operand missing in section: %s", leftName, section.getCurrentPath()));
            return null;
        } else if (!section.isSet(rightName)) {
            performantPlants.getLogger().warning(String.format("%s operand missing in section: %s", rightName, section.getCurrentPath()));
            return null;
        }
        ScriptBlock left = createPlantScript(section, leftName, context);
        if (left == null) {
            return null;
        }
        ScriptBlock right = createPlantScript(section, rightName, context);
        if (right == null) {
            return null;
        }
        ArrayList<ScriptBlock> arrayList = new ArrayList<>();
        arrayList.add(left);
        arrayList.add(right);
        return arrayList;
    }

    HashMap<String,ScriptBlock> createScriptOperationMultiple(ConfigurationSection section, boolean directValue, ExecutionContext context, String... paramNames) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "createScriptOperationMultiple in section: %s", section.getCurrentPath()));
            return null;
        }
        HashMap<String,ScriptBlock> valueMap = new HashMap<>();
        for (String paramName : paramNames) {
            if (!section.isSet(paramName)) {
                performantPlants.getLogger().warning(String.format("%s operand missing in section: %s", paramName, section.getCurrentPath()));
                return null;
            }
            ScriptBlock scriptBlock = createPlantScript(section, paramName, context);
            if (scriptBlock == null) {
                return null;
            }
            valueMap.put(paramName, scriptBlock);
        }
        return valueMap;
    }
    HashMap<String,ScriptBlock> createScriptOperationMultipleOptional(ConfigurationSection section, boolean directValue, ExecutionContext context, String... paramNames) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "createScriptOperationMultipleOptional in section: %s", section.getCurrentPath()));
            return null;
        }
        HashMap<String,ScriptBlock> valueMap = new HashMap<>();
        for (String paramName : paramNames) {
            if (!section.isSet(paramName)) {
                continue;
            }
            ScriptBlock scriptBlock = createPlantScript(section, paramName, context);
            if (scriptBlock == null) {
                continue;
            }
            valueMap.put(paramName, scriptBlock);
        }
        return valueMap;
    }

    // stored script block
    ScriptBlock getStoredScriptBlock(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        if (context == null || !context.isPlantDataPossible() || context.getPlantData().getPlant() == null) {
            return null;
        }
        ScriptBlock scriptBlockName = createScriptOperationUnary(section, directValue, sectionName, context);
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
        String storedBlockName = scriptBlockName.loadValue(new ExecutionContext()).getStringValue();
        ScriptBlock storedBlock = context.getPlantData().getPlant().getScriptBlock(storedBlockName);
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
    ScriptOperation createScriptOperationAdd(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAdd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationAddTo(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAddTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtract(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtract(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtractFrom(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtractFrom(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiply(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiply(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiplyBy(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiplyBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivide(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivide(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivideBy(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivideBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulus(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulus(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulusOf(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulusOf(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPower(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPower(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPowerOf(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPowerOf(operands.get(0), operands.get(1));
    }
    
    //logic
    ScriptOperation createScriptOperationAnd(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAnd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNand(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNand(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationOr(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationOr(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationXor(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationXor(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNor(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNor(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNot(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationNot(operand);
    }
    
    //compare
    ScriptOperation createScriptOperationEqual(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNotEqual(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNotEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThan(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThanOrEqualTo(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThanOrEqualTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThan(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThanOrEqualTo(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThanOrEqualTo(operands.get(0), operands.get(1));
    }

    // cast
    ScriptOperation createScriptOperationToBoolean(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToBoolean(operand);
    }
    ScriptOperation createScriptOperationToDouble(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToDouble(operand);
    }
    ScriptOperation createScriptOperationToLong(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToLong(operand);
    }
    ScriptOperation createScriptOperationToString(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToString(operand);
    }

    // functions
    ScriptOperation createScriptOperationContains(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationContains(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLength(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationLength(operand);
    }
    ScriptOperation createScriptOperationSetValue(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSetValue(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationRemoveScopeParameter(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
        ScriptBlock plantId = createPlantScript(section, plantIdString, context);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, context);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, context);
        if (parameter == null) {
            return null;
        }
        return new ScriptOperationRemoveScopeParameter(plantId, scope, parameter);
    }
    ScriptOperation createScriptOperationContainsScopeParameter(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
        ScriptBlock plantId = createPlantScript(section, plantIdString, context);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, context);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, context);
        if (parameter == null) {
            return null;
        }
        return new ScriptOperationContainsScopeParameter(plantId, scope, parameter);
    }
    ScriptOperation createScriptOperationSetValueScopeParameter(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
        ScriptBlock plantId = createPlantScript(section, plantIdString, context);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, context);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, context);
        if (parameter == null) {
            return null;
        }
        // set variableName
        if (!section.isSet(variableNameString)) {
            performantPlants.getLogger().warning("variable operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock variableName = createPlantScript(section, variableNameString, context);
        if (variableName == null) {
            return null;
        }
        // set value
        if (!section.isSet(valueString)) {
            performantPlants.getLogger().warning("value operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock value = createPlantScript(section, valueString, context);
        if (value == null) {
            return null;
        }
        return new ScriptOperationSetValueScopeParameter(plantId, scope, parameter, variableName, value);
    }
    ScriptOperation createScriptOperationGetValueScopeParameter(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
        ScriptBlock plantId = createPlantScript(section, plantIdString, context);
        if (plantId == null) {
            return null;
        }
        // set scope
        if (!section.isSet(scopeString)) {
            performantPlants.getLogger().warning("scope operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scope = createPlantScript(section, scopeString, context);
        if (scope == null) {
            return null;
        }
        // set parameter
        if (!section.isSet(parameterString)) {
            performantPlants.getLogger().warning("parameter operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock parameter = createPlantScript(section, parameterString, context);
        if (parameter == null) {
            return null;
        }
        // set variableName
        if (!section.isSet(variableNameString)) {
            performantPlants.getLogger().warning("variable operand missing in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock variableName = createPlantScript(section, variableNameString, context);
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
    ScriptOperation createScriptOperationWrapData(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationWrapData in section: %s", section.getCurrentPath()));
            return null;
        }
        // get data
        if (!section.isSet("data")) {
            performantPlants.getLogger().warning("Data definition missing for WrapData in section: " + section.getCurrentPath());
        }
        ScriptPlantData scriptData = createScriptPlantData(section, "data", context);
        if (scriptData == null) {
            return null;
        }
        // get script block
        if (!section.isSet("script")) {
            performantPlants.getLogger().warning("Script operand missing for WrapData in section: " + section.getCurrentPath());
            return null;
        }
        // wrap and unwrap context with data to facilitate checking valid usage of local variables
        ExecutionWrapper wrapper = new ExecutionWrapper(scriptData.getBaseData());
        wrapper.wrap(context);
        ScriptBlock scriptBlock = createPlantScript(section, "script", context);
        wrapper.unwrap(context);
        if (scriptBlock == null) {
            return null;
        }
        return new ScriptOperationWrapData(scriptData, scriptBlock);
    }
    ScriptOperation createScriptOperationWrapItem(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationWrapItem in section: %s", section.getCurrentPath()));
            return null;
        }
        // get item block
        if (!section.isSet("itemstack")) {
            performantPlants.getLogger().warning("Itemstack operand missing for WrapItem in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock itemBlock = createPlantScript(section, "itemstack", context);
        if (itemBlock == null) {
            return null;
        } else if (itemBlock.getType() != ScriptType.ITEMSTACK) {
            performantPlants.getLogger().warning(
                    String.format("Item operand must be ScriptType ITEMSTACK, not %s in section: %s",
                            itemBlock.getType().toString(), section.getCurrentPath()));
            return null;
        }
        // get script block
        if (!section.isSet("script")) {
            performantPlants.getLogger().warning("Script operand missing for WrapItem in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock scriptBlock = createPlantScript(section, "script", context);
        if (scriptBlock == null) {
            return null;
        }
        return new ScriptOperationWrapItem(itemBlock, scriptBlock);
    }
    private ScriptBlock createScriptOperationDelay(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "amount", "script");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDelay(operands.get(0), operands.get(1));
    }

    //flow
    ScriptOperation createScriptOperationIf(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationIf in section: %s", section.getCurrentPath()));
            return null;
        }
        String conditionString = "condition";
        String ifTrueString = "then";
        String ifFalseString = "else";
        if (!section.isSet(conditionString) || !section.isSet(ifTrueString)) {
            performantPlants.getLogger().warning(String.format("%s or %s operand missing in section: %s",
                    conditionString, ifTrueString, section.getCurrentPath()));
            return null;
        }
        ScriptBlock condition = createPlantScript(section, conditionString, context);
        if (condition == null) {
            return null;
        }
        ScriptBlock ifTrue = createPlantScript(section, ifTrueString, context);
        if (ifTrue == null) {
            return null;
        }
        if (!section.isSet(ifFalseString)) {
            return new ScriptOperationIf(condition, ifTrue);
        }
        ScriptBlock ifFalse = createPlantScript(section, ifFalseString, context);
        if (ifFalse == null) {
            return null;
        }
        return new ScriptOperationIf(condition, ifTrue, ifFalse);
    }
    ScriptOperation createScriptOperationDoIf(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "condition", "script");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDoIf(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationFunction(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
            ScriptBlock scriptBlock = createPlantScript(section, placeholder, context);
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
    ScriptOperation createScriptOperationUntilTrue(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationUntilTrue in section: %s", section.getCurrentPath()));
            return null;
        }
        int index = 0;
        ScriptBlock[] scriptBlocks = new ScriptBlock[section.getKeys(false).size()];
        for (String placeholder : section.getKeys(false)) {
            if (!section.isSet(placeholder)) {
                performantPlants.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
                        "UntilTrue in section: %s", placeholder, section));
                return null;
            }
            ScriptBlock scriptBlock = createPlantScript(section, placeholder, context);
            if (scriptBlock == null) {
                return null;
            }
            scriptBlocks[index] = scriptBlock;
            index++;
        }
        if (scriptBlocks.length == 0) {
            performantPlants.getLogger().warning("No subsections found to generate UntilTrue in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationUntilTrue(scriptBlocks);
    }
    ScriptOperation createScriptOperationUntilTrueLimit(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationUntilTrue in section: %s", section.getCurrentPath()));
            return null;
        }
        // get limit
        if (!section.isSet("limit")) {
            performantPlants.getLogger().warning("No limit set in UntilTrueLimit in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock limit = createPlantScript(section, "limit", context);
        if (limit == null) {
            return null;
        }
        // get scripts
        if (!section.isConfigurationSection("scripts")) {
            performantPlants.getLogger().warning("No scripts set in UntilTrueLimit in section: " + section.getCurrentPath());
            return null;
        }
        // load scripts
        ArrayList<ScriptBlock> scriptBlocks = new ArrayList<>();
        ConfigurationSection scriptsSection = section.getConfigurationSection("scripts");
        for (String placeholder : scriptsSection.getKeys(false)) {
            if (!scriptsSection.isSet(placeholder)) {
                performantPlants.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
                        "UntilTrueLimit in section: %s", placeholder, scriptsSection));
                return null;
            }
            ScriptBlock scriptBlock = createPlantScript(scriptsSection, placeholder, context);
            if (scriptBlock == null) {
                return null;
            }
            scriptBlocks.add(scriptBlock);
        }
        if (scriptBlocks.size() == 0) {
            performantPlants.getLogger().warning("No subsections found to generate scripts in UntilTrueLimit in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationUntilTrueLimit(limit, scriptBlocks);
    }
    ScriptOperation createScriptOperationSwitch(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
        ScriptBlock condition = createPlantScript(section, conditionString, context);
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
                ScriptBlock scriptBlock = createPlantScript(casesSection, caseName, context);
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
            defaultCase = createPlantScript(section, defaultString, context);
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
    ScriptOperation createScriptOperationChangeStage(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
            stage = createPlantScript(section, stageString, context);
        }
        if (section.isSet(ifNextString)) {
            ifNext = createPlantScript(section, ifNextString, context);
        }
        return new ScriptOperationChangeStage(performantPlants, stage, ifNext);
    }
    ScriptOperation createScriptOperationCreatePlantBlocks(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationCreatePlantBlocks in section: %s", section.getCurrentPath()));
            return null;
        }
        // TODO: fill out
        return null;
    }
    ScriptOperation createScriptOperationScheduleTask(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
            scriptTask = context.getPlantData().getPlant().getScriptTask(taskConfigId);
            if (scriptTask == null) {
                performantPlants.getLogger().warning(String.format("Task '%s' not recognized for plant '%s' in section: '%s'",
                        taskConfigId, context.getPlantData().getPlant().getId(), section.getCurrentPath()));
                return null;
            }
        } else {
            if (!currentTaskLoader.isTask(taskConfigId)) {
                performantPlants.getLogger().warning(String.format("Task '%s' not recognized as valid task for plant '%s' in section '%s'",
                        taskConfigId, context.getPlantData().getPlant().getId(), section.getCurrentPath()));
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
        scriptTask = new ScriptTask(context.getPlantData().getPlant().getId(), taskConfigId);
        // get delay, if present
        ScriptBlock delay = scriptTask.getDelay();
        if (section.isSet("delay")) {
            delay = createPlantScript(section, "delay", context);
            if (delay == null || !ScriptHelper.isLong(delay)) {
                performantPlants.getLogger().warning("Delay must be ScriptType LONG in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get current player, if present
        ScriptBlock currentPlayer = scriptTask.getCurrentPlayer();
        if (section.isSet("current-player")) {
            currentPlayer = createPlantScript(section, "current-player", context);
            if (currentPlayer == null || !ScriptHelper.isBoolean(currentPlayer)) {
                performantPlants.getLogger().warning("Current-player must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get current block, if present
        ScriptBlock currentBlock = scriptTask.getCurrentBlock();
        if (section.isSet("current-block")) {
            currentBlock = createPlantScript(section, "current-block", context);
            if (currentBlock == null || !ScriptHelper.isBoolean(currentBlock)) {
                performantPlants.getLogger().warning("Current-block must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get player id, if present
        ScriptBlock playerId = scriptTask.getPlayerId();
        if (section.isSet("player-id")) {
            playerId = createPlantScript(section, "player-id", context);
            if (playerId == null || !ScriptHelper.isString(playerId)) {
                performantPlants.getLogger().warning("Player-id must be ScriptType STRING in section: " + section.getCurrentPath());
                return null;
            }
        }
        // get autostart, if present
        ScriptBlock autostart = scriptTask.getAutostart();
        if (section.isSet("autostart")) {
            autostart = createPlantScript(section, "autostart", context);
            if (autostart == null || !ScriptHelper.isBoolean(autostart)) {
                performantPlants.getLogger().warning("Autostart must be ScriptType BOOLEAN in section: " + section.getCurrentPath());
                return null;
            }
        }
        // return operation
        return new ScriptOperationScheduleTask(context.getPlantData().getPlant().getId(), taskConfigId, delay,
                currentPlayer, currentBlock, playerId, autostart);
    }
    ScriptOperation createScriptOperationCancelTask(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationCancelTask in section: %s", section.getCurrentPath()));
            return null;
        }
        // get task id
        ScriptBlock taskId = createPlantScript(section, "task-id", context);
        if (taskId == null || !ScriptHelper.isString(taskId)) {
            performantPlants.getLogger().warning("Task-id must be ScriptType STRING in section: " + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationCancelTask(taskId);
    }
    ScriptOperation createScriptOperationDoDrops(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationDoDrops in section: %s", section.getCurrentPath()));
            return null;
        }
        DropStorage dropStorage = new DropStorage();
        boolean added = addDropsToDropStorage(section, dropStorage, context);
        if (!added) {
            performantPlants.getLogger().warning("DoDrops cannot be created; issue getting drops");
            return null;
        }
        return new ScriptOperationDoDrops(dropStorage);
    }
    private ScriptOperation createScriptOperationConsoleCommand(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationConsoleCommand(operand);
    }
    private ScriptOperation createScriptOperationSoundEffect(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        String soundString = "sound";
        String volumeString = "volume";
        String pitchString = "pitch";
        String offsetXString = "offset-x";
        String offsetYString = "offset-y";
        String offsetZString = "offset-z";
        String multiplierString = "multiplier";
        String ignoreDirectionYString = "ignore-direction-y";
        String clientsideString = "clientside";
        HashMap<String, ScriptBlock> paramMap = createScriptOperationMultipleOptional(section, directValue, context,
                soundString, volumeString, pitchString,
                offsetXString, offsetYString, offsetZString, multiplierString, ignoreDirectionYString, clientsideString);
        if (paramMap == null) {
            return null;
        }

        // check if sound is set
        if (!paramMap.containsKey(soundString)) {
            performantPlants.getLogger().warning("SoundEffect invalid; sound not found in section: " + section.getCurrentPath());
            return null;
        }
        // if no variables, then check if sound is recognized
        ScriptBlock sound = paramMap.get(soundString);
        if (!sound.containsVariable()) {
            String soundName = sound.loadValue(new ExecutionContext()).getStringValue();
            if (EnumHelper.getSound(soundName) == null) {
                performantPlants.getLogger().warning(String.format("SoundEffect invalid; sound '%s' not recognized", soundName));
                return null;
            }
        }
        // set volume, if not present
        if (!paramMap.containsKey(volumeString)) {
            ScriptBlock scriptBlock = new ScriptResult(1.0);
            paramMap.put(volumeString, scriptBlock);
            if (section.isSet(volumeString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", volumeString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set pitch, if not present
        if (!paramMap.containsKey(pitchString)) {
            ScriptBlock scriptBlock = new ScriptResult(1.0);
            paramMap.put(pitchString, scriptBlock);
            if (section.isSet(pitchString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", pitchString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset x, if not present
        if (!paramMap.containsKey(offsetXString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetXString, scriptBlock);
            if (section.isSet(offsetXString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetXString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset y, if not present
        if (!paramMap.containsKey(offsetYString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetYString, scriptBlock);
            if (section.isSet(offsetYString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetYString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset z, if not present
        if (!paramMap.containsKey(offsetZString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetZString, scriptBlock);
            if (section.isSet(offsetZString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetZString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set multiplier, if not present
        if (!paramMap.containsKey(multiplierString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(multiplierString, scriptBlock);
            if (section.isSet(multiplierString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", multiplierString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set ignore direction y, if not present
        if (!paramMap.containsKey(ignoreDirectionYString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(ignoreDirectionYString, scriptBlock);
            if (section.isSet(ignoreDirectionYString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", ignoreDirectionYString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set clientside, if not present
        if (!paramMap.containsKey(clientsideString)) {
            ScriptBlock scriptBlock = ScriptResult.TRUE;
            paramMap.put(clientsideString, scriptBlock);
            if (section.isSet(clientsideString)) {
                performantPlants.getLogger().warning(
                        String.format("SoundEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", clientsideString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }

        ScriptBlock volume = paramMap.get(volumeString);
        ScriptBlock pitch = paramMap.get(pitchString);
        ScriptBlock offsetX = paramMap.get(offsetXString);
        ScriptBlock offsetY = paramMap.get(offsetYString);
        ScriptBlock offsetZ = paramMap.get(offsetZString);
        ScriptBlock multiplier = paramMap.get(multiplierString);
        ScriptBlock ignoreDirectionY = paramMap.get(ignoreDirectionYString);
        ScriptBlock clientside = paramMap.get(clientsideString);
        if (volume == null || pitch == null || offsetX == null || offsetY == null || offsetZ == null ||
                multiplier == null || ignoreDirectionY == null || clientside == null) {
            performantPlants.getLogger().warning(
                    "BAD CODE: SoundEffect had unexpected null ScriptBlock in section: "
                            + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationSoundEffect(sound,volume,pitch,offsetX,offsetY,offsetZ,multiplier,ignoreDirectionY,clientside);
    }
    private ScriptOperation createScriptOperationParticleEffect(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        String particleString = "particle";
        String countString = "count";
        String offsetXString = "offset-x";
        String offsetYString = "offset-y";
        String offsetZString = "offset-z";
        String dataOffsetXString = "data-offset-x";
        String dataOffsetYString = "data-offset-y";
        String dataOffsetZString = "data-offset-z";
        String extraString = "extra";
        String multiplierString = "multiplier";
        String ignoreDirectionYString = "ignore-direction-y";
        String clientsideString = "clientside";
        HashMap<String, ScriptBlock> paramMap = createScriptOperationMultipleOptional(section, directValue, context,
                particleString, countString,
                offsetXString, offsetYString, offsetZString,
                dataOffsetXString, dataOffsetYString, dataOffsetZString,
                extraString, multiplierString, ignoreDirectionYString, clientsideString);
        if (paramMap == null) {
            return null;
        }

        // check if sound is set
        if (!paramMap.containsKey(particleString)) {
            performantPlants.getLogger().warning("ParticleEffect invalid; particle not found in section: " + section.getCurrentPath());
            return null;
        }
        // if no variables, then check if sound is recognized
        ScriptBlock particle = paramMap.get(particleString);
        if (!particle.containsVariable()) {
            String particleName = particle.loadValue(new ExecutionContext()).getStringValue();
            if (EnumHelper.getParticle(particleName) == null) {
                performantPlants.getLogger().warning(String.format("ParticleEffect invalid; particle '%s' not recognized", particleName));
                return null;
            }
        }
        // set count, if not present
        if (!paramMap.containsKey(countString)) {
            ScriptBlock scriptBlock = new ScriptResult(1);
            paramMap.put(countString, scriptBlock);
            if (section.isSet(countString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", countString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset x, if not present
        if (!paramMap.containsKey(offsetXString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetXString, scriptBlock);
            if (section.isSet(offsetXString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetXString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset y, if not present
        if (!paramMap.containsKey(offsetYString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetYString, scriptBlock);
            if (section.isSet(offsetYString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetYString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check offset z, if not present
        if (!paramMap.containsKey(offsetZString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(offsetZString, scriptBlock);
            if (section.isSet(offsetZString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", offsetZString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }

        // check data offset x, if not present
        if (!paramMap.containsKey(dataOffsetXString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(dataOffsetXString, scriptBlock);
            if (section.isSet(dataOffsetXString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", dataOffsetXString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check data offset y, if not present
        if (!paramMap.containsKey(dataOffsetYString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(dataOffsetYString, scriptBlock);
            if (section.isSet(dataOffsetYString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", dataOffsetYString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // check data offset z, if not present
        if (!paramMap.containsKey(dataOffsetZString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(dataOffsetZString, scriptBlock);
            if (section.isSet(dataOffsetZString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", dataOffsetZString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set extra, if not present
        if (!paramMap.containsKey(extraString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(extraString, scriptBlock);
            if (section.isSet(extraString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", extraString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set multiplier, if not present
        if (!paramMap.containsKey(multiplierString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(multiplierString, scriptBlock);
            if (section.isSet(multiplierString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", multiplierString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set ignore direction y, if not present
        if (!paramMap.containsKey(ignoreDirectionYString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(ignoreDirectionYString, scriptBlock);
            if (section.isSet(ignoreDirectionYString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", ignoreDirectionYString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set clientside, if not present
        if (!paramMap.containsKey(clientsideString)) {
            ScriptBlock scriptBlock = ScriptResult.TRUE;
            paramMap.put(clientsideString, scriptBlock);
            if (section.isSet(clientsideString)) {
                performantPlants.getLogger().warning(
                        String.format("ParticleEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", clientsideString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }

        ScriptBlock count = paramMap.get(countString);
        ScriptBlock offsetX = paramMap.get(offsetXString);
        ScriptBlock offsetY = paramMap.get(offsetYString);
        ScriptBlock offsetZ = paramMap.get(offsetZString);
        ScriptBlock dataOffsetX = paramMap.get(dataOffsetXString);
        ScriptBlock dataOffsetY = paramMap.get(dataOffsetYString);
        ScriptBlock dataOffsetZ = paramMap.get(dataOffsetZString);
        ScriptBlock extra = paramMap.get(extraString);
        ScriptBlock multiplier = paramMap.get(multiplierString);
        ScriptBlock ignoreDirectionY = paramMap.get(ignoreDirectionYString);
        ScriptBlock clientside = paramMap.get(clientsideString);
        if (count == null || offsetX == null || offsetY == null || offsetZ == null ||
                dataOffsetX == null || dataOffsetY == null || dataOffsetZ == null ||
                extra == null || multiplier == null || ignoreDirectionY == null || clientside == null) {
            performantPlants.getLogger().warning(
                    "BAD CODE: ParticleEffect had unexpected null ScriptBlock in section: "
                            + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationParticleEffect(particle,count,
                offsetX,offsetY,offsetZ,
                dataOffsetX,dataOffsetY,dataOffsetZ,
                extra,multiplier,ignoreDirectionY,clientside);
    }
    private ScriptOperation createScriptOperationExplosion(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        String powerString = "power";
        String fireString = "fire";
        String breakString = "break";
        HashMap<String, ScriptBlock> paramMap = createScriptOperationMultipleOptional(section, directValue, context,
                powerString, fireString, breakString);
        if (paramMap == null) {
            return null;
        }
        // set power, if not present
        if (!paramMap.containsKey(powerString)) {
            ScriptBlock scriptBlock = new ScriptResult(1.0);
            paramMap.put(powerString, scriptBlock);
            if (section.isSet(powerString)) {
                performantPlants.getLogger().warning(
                        String.format("Explosion %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", powerString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set fire, if not present
        if (!paramMap.containsKey(fireString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(fireString, scriptBlock);
            if (section.isSet(fireString)) {
                performantPlants.getLogger().warning(
                        String.format("Explosion %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", fireString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set break, if not present
        if (!paramMap.containsKey(breakString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(breakString, scriptBlock);
            if (section.isSet(breakString)) {
                performantPlants.getLogger().warning(
                        String.format("Explosion %s invalid, will be set to %b; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", breakString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        ScriptBlock power = paramMap.get(powerString);
        ScriptBlock fire = paramMap.get(fireString);
        ScriptBlock breakScript = paramMap.get(breakString);
        if (power == null || fire == null || breakScript == null) {
            performantPlants.getLogger().warning(
                    "BAD CODE: Explosion had unexpected null ScriptBlock in section: "
                            + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationExplosion(power,fire,breakScript);
    }
    ScriptOperation createScriptOperationAreaEffect(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        // get potion effects, if present
        List<PotionEffect> potionEffects = loadPotionEffects(section);
        // get color, if set
        ScriptColor color = new ScriptColor();
        if (section.isConfigurationSection("color")) {
            ConfigurationSection colorSection = section.getConfigurationSection("color");
            if (colorSection != null) {
                color = createColor(colorSection, context);
            }
        }

        String durationString = "duration";
        String durationOnUseString = "duration-on-use";
        String particleString = "particle";
        String radiusString = "radius";
        String radiusOnUseString = "radius-on-use";
        String radiusPerTickString = "radius-per-tick";
        String reapplicationDelayString = "reapplication-delay";
        HashMap<String, ScriptBlock> paramMap = createScriptOperationMultipleOptional(section, directValue, context,
                durationString, durationOnUseString, particleString,
                radiusString, radiusOnUseString, radiusPerTickString, reapplicationDelayString);
        if (paramMap == null) {
            return null;
        }

        // set duration, if not present
        if (!paramMap.containsKey(durationString)) {
            ScriptBlock scriptBlock = new ScriptResult(200);
            paramMap.put(durationString, scriptBlock);
            if (section.isSet(durationString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", durationString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set duration on use, if not present
        if (!paramMap.containsKey(durationOnUseString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(durationOnUseString, scriptBlock);
            if (section.isSet(durationOnUseString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", durationOnUseString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set particle, if not present
        if (!paramMap.containsKey(particleString)) {
            ScriptBlock scriptBlock = ScriptResult.EMPTY;
            paramMap.put(particleString, scriptBlock);
            if (section.isSet(particleString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to none; must be ScriptType STRING in " +
                                        "section: %s", particleString, section.getCurrentPath()));
            }
        }
        // set radius, if not present
        if (!paramMap.containsKey(radiusString)) {
            ScriptBlock scriptBlock = new ScriptResult(1.0);
            paramMap.put(radiusString, scriptBlock);
            if (section.isSet(radiusString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", radiusString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set radius on use, if not present
        if (!paramMap.containsKey(radiusOnUseString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(radiusOnUseString, scriptBlock);
            if (section.isSet(radiusOnUseString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", radiusOnUseString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set radius per tick, if not present
        if (!paramMap.containsKey(radiusPerTickString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(radiusPerTickString, scriptBlock);
            if (section.isSet(radiusPerTickString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG or DOUBLE in " +
                                        "section: %s", radiusPerTickString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set reapplication delay, if not present
        if (!paramMap.containsKey(reapplicationDelayString)) {
            ScriptBlock scriptBlock = new ScriptResult(5);
            paramMap.put(reapplicationDelayString, scriptBlock);
            if (section.isSet(reapplicationDelayString)) {
                performantPlants.getLogger().warning(
                        String.format("AreaEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", reapplicationDelayString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        ScriptBlock duration = paramMap.get(durationString);
        ScriptBlock durationOnUse = paramMap.get(durationOnUseString);
        ScriptBlock particle = paramMap.get(particleString);
        ScriptBlock radius = paramMap.get(radiusString);
        ScriptBlock radiusOnUse = paramMap.get(radiusOnUseString);
        ScriptBlock radiusPerTick = paramMap.get(radiusPerTickString);
        ScriptBlock reapplicationDelay = paramMap.get(reapplicationDelayString);
        if (duration == null || durationOnUse == null || particle == null || radius == null || radiusOnUse == null || radiusPerTick == null || reapplicationDelay == null) {
            performantPlants.getLogger().warning(
                    "BAD CODE: AreaEffect had unexpected null ScriptBlock in section: "
                            + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationAreaEffect(potionEffects, color, duration, durationOnUse, particle, radius, radiusOnUse, radiusPerTick, reapplicationDelay);
    }

    //random
    ScriptOperation createScriptOperationChance(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationChance(operand);
    }
    ScriptOperation createScriptOperationChoice(ConfigurationSection section, boolean directValue, ExecutionContext context) {
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
            ScriptBlock scriptBlock = createPlantScript(section, placeholder, context);
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
    ScriptOperation createScriptOperationRandomDouble(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomDouble(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationRandomLong(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomLong(operands.get(0), operands.get(1));
    }

    //player
    private ScriptOperation createScriptOperationUsePlayerLocation(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationUsePlayerLocation(operand);
    }
    private ScriptOperation createScriptOperationUseEyeLocation(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationUseEyeLocation(operand);
    }
    private ScriptOperation createScriptOperationPassOnlyPlayer(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationPassOnlyPlayer(operand);
    }
    private ScriptOperation createScriptOperationHeal(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationHeal(operand);
    }
    private ScriptOperation createScriptOperationFeed(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "food-amount", "saturate-amount");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationFeed(operands.get(0), operands.get(1));
    }
    private ScriptOperation createScriptOperationAir(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationAir(operand);
    }
    private ScriptOperation createScriptOperationMessage(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationMessage(operand);
    }
    private ScriptOperation createScriptOperationChat(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationChat(operand);
    }
    private ScriptOperation createScriptOperationPlayerCommand(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationPlayerCommand(operand);
    }
    private ScriptOperation createScriptOperationPotionEffect(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        String potionString = "potion";
        String durationString = "duration";
        String amplifierString = "amplifier";
        String ambientString = "ambient";
        String particlesString = "particles";
        String iconString = "icon";
        HashMap<String, ScriptBlock> paramMap = createScriptOperationMultipleOptional(section, directValue, context,
                potionString, durationString, amplifierString, ambientString, particlesString, iconString);
        if (paramMap == null) {
            return null;
        }

        if (!paramMap.containsKey(potionString)) {
            performantPlants.getLogger().warning("PotionEffect invalid; potion not found in section: " + section.getCurrentPath());
            return null;
        }
        // if no variables, then check if potion is recognized
        ScriptBlock potion = paramMap.get(potionString);
        if (!potion.containsVariable()) {
            String potionNameString = potion.loadValue(new ExecutionContext()).getStringValue();
            PotionEffectType potionEffectType = PotionEffectType.getByName(potionNameString);
            if (potionEffectType == null) {
                performantPlants.getLogger().warning(String.format("PotionEffect invalid; potion '%s' not recognized", potionNameString));
                return null;
            }
        }
        // set duration, if not present
        if (!paramMap.containsKey(durationString)) {
            ScriptBlock scriptBlock = new ScriptResult(200);
            paramMap.put(durationString, scriptBlock);
            if (section.isSet(durationString)) {
                performantPlants.getLogger().warning(
                        String.format("PotionEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", durationString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // set amplifier, if not present
        if (!paramMap.containsKey(amplifierString)) {
            ScriptBlock scriptBlock = ScriptResult.ZERO;
            paramMap.put(amplifierString, scriptBlock);
            if (section.isSet(amplifierString)) {
                performantPlants.getLogger().warning(
                        String.format("PotionEffect %s invalid, will be set to %d; must be ScriptType LONG in " +
                                        "section: %s", amplifierString, scriptBlock.loadValue(new ExecutionContext()).getIntegerValue(),
                                section.getCurrentPath()));
            }
        }
        // check ambient, if not present
        if (!paramMap.containsKey(ambientString)) {
            ScriptBlock scriptBlock = ScriptResult.TRUE;
            paramMap.put(ambientString, scriptBlock);
            if (section.isSet(ambientString)) {
                performantPlants.getLogger().warning(
                        String.format("PotionEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", ambientString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set particles, if not present
        if (!paramMap.containsKey(particlesString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(particlesString, scriptBlock);
            if (section.isSet(particlesString)) {
                performantPlants.getLogger().warning(
                        String.format("PotionEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", particlesString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }
        // set icon, if not present
        if (!paramMap.containsKey(iconString)) {
            ScriptBlock scriptBlock = ScriptResult.FALSE;
            paramMap.put(iconString, scriptBlock);
            if (section.isSet(iconString)) {
                performantPlants.getLogger().warning(
                        String.format("PotionEffect %s invalid, will be set to %b; must be ScriptType BOOLEAN in " +
                                        "section: %s", iconString, scriptBlock.loadValue(new ExecutionContext()).getBooleanValue(),
                                section.getCurrentPath()));
            }
        }

        ScriptBlock duration = paramMap.get(durationString);
        ScriptBlock amplifier = paramMap.get(amplifierString);
        ScriptBlock ambient = paramMap.get(ambientString);
        ScriptBlock particles = paramMap.get(particlesString);
        ScriptBlock icon = paramMap.get(iconString);
        if (duration == null || amplifier == null || ambient == null || particles == null || icon == null) {
            performantPlants.getLogger().warning(
                    "BAD CODE: PotionEffect had unexpected null ScriptBlock in section: "
                            + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationPotionEffect(potion,duration,amplifier,ambient,particles,icon);
    }

    //block
    private ScriptOperation createScriptOperationUseBlockLocation(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationUseBlockLocation(operand);
    }
    private ScriptOperation createScriptOperationUseBlockBottomLocation(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationUseBlockBottomLocation(operand);
    }
    private ScriptOperation createScriptOperationPassOnlyBlock(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationPassOnlyBlock(operand);
    }
    private ScriptOperation createScriptOperationBreakBlock(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationBreakBlock(operand);
    }
    private ScriptOperation createScriptOperationRequiredBlockFaces(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        if (!directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section required in " +
                    "ScriptOperationRequiredBlockFaces in section: %s", section.getCurrentPath()));
            return null;
        }
        HashSet<BlockFace> blockFaces = new HashSet<>();
        // set required block faces, if present
        if (section.isList(sectionName)) {
            for (String name : section.getStringList(sectionName)) {
                BlockFace blockFace = EnumHelper.getBlockFace(name);
                if (blockFace == null) {
                    performantPlants.getLogger().warning(String.format("BlockFace '%s' not recognized in RequiredBlockFaces section: %s",
                            name, section.getCurrentPath()));
                    return null;
                }
                if (!BlockHelper.isOmnidirectionalBlockFace(blockFace)) {
                    performantPlants.getLogger().warning(String.format("BlockFace '%s' is not a valid omnidirectional block face " +
                                    "(UP,DOWN,EAST,WEST,NORTH,SOUTH) in RequiredBlockFaces section: %s",
                            name, section.getCurrentPath()));
                    return null;
                }
                blockFaces.add(blockFace);
            }
        } else {
            performantPlants.getLogger().warning(String.format("No list of strings found in RequiredBlockFaces " +
                    "section: %s", section.getCurrentPath()));
            return null;
        }
        if (blockFaces.isEmpty()) {
            performantPlants.getLogger().warning("No block faces present for RequiredBlockFaces section: "
                    + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationRequiredBlockFaces(blockFaces);
    }
    private ScriptOperation createScriptOperationIsBlockFace(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        if (!operand.containsVariable()) {
            String blockFaceName = operand.loadValue(new ExecutionContext()).getStringValue();
            BlockFace blockFace = EnumHelper.getBlockFace(blockFaceName);
            if (blockFace == null) {
                performantPlants.getLogger().warning(String.format("IsBlockFace invalid; BlockFace '%s' not " +
                        "recognized in section: %s", blockFaceName, section.getCurrentPath()));
                return null;
            }
            if (!BlockHelper.isOmnidirectionalBlockFace(blockFace)) {
                performantPlants.getLogger().warning(String.format("BlockFace '%s' is not a valid omnidirectional block face " +
                                "(UP,DOWN,EAST,WEST,NORTH,SOUTH) in IsBlockFace section: %s",
                        blockFaceName, section.getCurrentPath()));
                return null;
            }
        }
        return new ScriptOperationIsBlockFace(operand);
    }

    //inventory
    private ScriptOperation createScriptOperationGetMatchingItem(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationGetMatchingItem(operand);
    }
    private ScriptOperation createScriptOperationGiveItem(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationGiveItem(operand);
    }

    //item
    private ScriptOperation createScriptOperationIsAir(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsAir(operand);
    }
    private ScriptOperation createScriptOperationIsPickaxe(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsPickaxe(operand);
    }
    private ScriptOperation createScriptOperationIsAxe(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsAxe(operand);
    }
    private ScriptOperation createScriptOperationIsShovel(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsShovel(operand);

    }
    private ScriptOperation createScriptOperationIsHoe(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsHoe(operand);
    }
    private ScriptOperation createScriptOperationIsSword(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsSword(operand);
    }
    private ScriptOperation createScriptOperationIsWearable(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsWearable(operand);
    }
    private ScriptOperation createScriptOperationIsItemAnyPlant(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationIsItemAnyPlant(operand);
    }
    private ScriptOperation createScriptOperationTakeOne(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationTakeOne(operand);
    }
    private ScriptOperation createScriptOperationGetAmount(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationGetAmount(operand);
    }
    private ScriptOperation createScriptOperationGetMaterial(ConfigurationSection section, boolean directValue, String sectionName, ExecutionContext context) {
        ScriptBlock operand = createScriptOperationUnary(section, directValue, sectionName, context);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationItemGetMaterial(operand);
    }
    private ScriptOperation createScriptOperationCreateItemStack(ConfigurationSection section, boolean directValue) {
        if (directValue) {
            performantPlants.getLogger().warning(String.format("DirectValue section not supported in " +
                    "ScriptOperationItemStack in section: %s", section.getCurrentPath()));
            return null;
        }
        // load item stack
        ItemSettings itemSettings = loadItemConfig(section, true);
        if (itemSettings == null) {
            performantPlants.getLogger().warning(
                    "Item could not be generated for ScriptOperationItemStack in section: " + section.getCurrentPath());
            return null;
        }
        return new ScriptOperationCreateItemStack(itemSettings.generateItemStack());
    }
    private ScriptOperation createScriptOperationAreSimilar(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAreSimilar(operands.get(0), operands.get(1));
    }
    private ScriptOperation createScriptOperationItemIsMaterial(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "itemstack", "material");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationItemIsMaterial(operands.get(0), operands.get(1));
    }
    private ScriptOperation createScriptOperationGetEnchantmentLevel(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "itemstack", "enchantment");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGetEnchantmentLevel(operands.get(0), operands.get(1));
    }
    private ScriptOperation createScriptOperationHasEnchantment(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "itemstack", "enchantment");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationHasEnchantment(operands.get(0), operands.get(1));
    }
    private ScriptOperation createScriptOperationAddDamage(ConfigurationSection section, boolean directValue, ExecutionContext context) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, directValue, context, "itemstack", "amount");
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAddDamage(operands.get(0), operands.get(1));
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
