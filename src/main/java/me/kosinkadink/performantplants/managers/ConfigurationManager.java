package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.effects.*;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.*;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.action.ScriptOperationChangeStage;
import me.kosinkadink.performantplants.scripting.operations.action.ScriptOperationConsumable;
import me.kosinkadink.performantplants.scripting.operations.action.ScriptOperationInteract;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToBoolean;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToDouble;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToLong;
import me.kosinkadink.performantplants.scripting.operations.cast.ScriptOperationToString;
import me.kosinkadink.performantplants.scripting.operations.compare.*;
import me.kosinkadink.performantplants.scripting.operations.flow.ScriptOperationFunction;
import me.kosinkadink.performantplants.scripting.operations.flow.ScriptOperationIf;
import me.kosinkadink.performantplants.scripting.operations.function.ScriptOperationContains;
import me.kosinkadink.performantplants.scripting.operations.function.ScriptOperationLength;
import me.kosinkadink.performantplants.scripting.operations.function.ScriptOperationSetValue;
import me.kosinkadink.performantplants.scripting.operations.logic.ScriptOperationAnd;
import me.kosinkadink.performantplants.scripting.operations.logic.ScriptOperationNot;
import me.kosinkadink.performantplants.scripting.operations.logic.ScriptOperationOr;
import me.kosinkadink.performantplants.scripting.operations.math.*;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationChance;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationChoice;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationRandomDouble;
import me.kosinkadink.performantplants.scripting.operations.random.ScriptOperationRandomLong;
import me.kosinkadink.performantplants.scripting.storage.ScriptColor;
import me.kosinkadink.performantplants.settings.*;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.*;
import me.kosinkadink.performantplants.util.EnchantmentLevel;
import me.kosinkadink.performantplants.util.ScriptHelper;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.*;
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

    private Main main;
    private File configFile;
    private YamlConfiguration config = new YamlConfiguration();
    private HashMap<String, YamlConfiguration> plantConfigMap = new HashMap<>();
    private ConfigSettings configSettings = new ConfigSettings();

    public ConfigurationManager(Main mainClass) {
        main = mainClass;
        configFile = new File(main.getDataFolder(), "config.yml");
        loadMainConfig();
        loadPlantConfigs();
        loadVanillaDropConfig();
    }

    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    void loadMainConfig() {
        // create file if doesn't exist
        if (!configFile.exists()) {
            main.saveResource("config.yml", false);
        }
        try {
            config.load(configFile);
        } catch (Exception e) {
            main.getLogger().severe("Error occurred trying to load configFile");
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
        String plantsPath = Paths.get(main.getDataFolder().getPath(), "plants").toString();
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
            main.getLogger().info("Loading plant config from file: " + file.getPath());
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
            main.getLogger().severe("Error occurred trying to load plantConfig: " + plantId);
            e.printStackTrace();
            return;
        }
        // put plant yaml config in config map for future reference
        plantConfigMap.put(plantId, plantConfig);
    }

    void loadPlantsFromConfigs() {
        // now that all configs are loaded in, create plants from loaded configs
        for (Map.Entry<String, YamlConfiguration> entry : plantConfigMap.entrySet()) {
            String plantId = entry.getKey();
            main.getLogger().info("Attempting to load plant: " + plantId);
            YamlConfiguration plantConfig = entry.getValue();
            loadPlantFromConfig(plantId, plantConfig);
        }
    }

    void loadPlantFromConfig(String plantId, YamlConfiguration plantConfig) {
        // get item info; if section not present, log error and return
        ItemSettings itemSettings;
        ConfigurationSection itemConfig = plantConfig.getConfigurationSection("item");
        if (itemConfig != null) {
            itemSettings = loadItemConfig(itemConfig,false);
            if (itemSettings == null) {
                main.getLogger().warning("Item could not be queried for plant: " + plantId);
                return;
            }
        } else {
            main.getLogger().warning("Could not load plant " + plantId + "; item section not found");
            return;
        }
        // create plant ItemStack and add it to plant type manager
        PlantItem plantItem = new PlantItem(itemSettings.generatePlantItemStack(plantId));
        // set buy/sell prices
        addPricesToPlantItem(itemConfig, plantItem);
        // add consumable behavior
        if (itemConfig.isConfigurationSection("consumable")) {
            PlantConsumableStorage consumable = loadPlantConsumableStorage(itemConfig.getConfigurationSection("consumable"), null);
            if (consumable != null) {
                plantItem.setConsumableStorage(consumable);
            }
        }
        // Plant to be saved
        Plant plant = new Plant(plantId, plantItem);
        // if growing section is present, get seed item + stages
        ConfigurationSection growingConfig = plantConfig.getConfigurationSection("growing");
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
                        addPricesToPlantItem(seedConfig, seedItem);
                        plant.setSeedItem(seedItem);
                    } else {
                        main.getLogger().info("seedItemSettings were null for plant: " + plantId);
                        return;
                    }
                }
                // if seed item has been set, get growth requirements + stages
                if (plant.getSeedItem() != null) {
                    // set growth bounds for general growth (overridable by stage-specific min/max growth time)
                    if (!growingConfig.isInt("min-growth-time") || !growingConfig.isInt("max-growth-time")) {
                        main.getLogger().warning("Growth time bounds not set/integer for growing for plant: " + plantId);
                        return;
                    }
                    plant.setMinGrowthTime(growingConfig.getLong("min-growth-time"));
                    plant.setMaxGrowthTime(growingConfig.getLong("max-growth-time"));
                    // set plant requirements, if present
                    if (growingConfig.isConfigurationSection("plant-requirements")) {
                        if (!addRequirementsToStorage(growingConfig.getConfigurationSection("plant-requirements"),
                                plant.getPlantRequirementStorage())) {
                            main.getLogger().warning("plant-requirements could not be loaded for plant: " + plantId);
                            return;
                        }
                    }
                    // set growth requirements, if present
                    if (growingConfig.isConfigurationSection("growth-requirements")) {
                        if (!addRequirementsToStorage(growingConfig.getConfigurationSection("growth-requirements"),
                                plant.getGrowthRequirementStorage())) {
                            main.getLogger().warning("growth-requirements could not be loaded for plant: " + plantId);
                            return;
                        }
                    }
                    // load plant data, if present
                    PlantData plantData = createPlantData(growingConfig);
                    if (plantData != null) {
                        plant.setPlantData(plantData);
                    }
                    // get growth stages; since seed is present, growth stages are REQUIRED
                    if (!growingConfig.isSet("stages") && growingConfig.isConfigurationSection("stages")) {
                        main.getLogger().warning("No stages are configured for plant while seed is present: " + plantId);
                        return;
                    } else {
                        ConfigurationSection stagesConfig = growingConfig.getConfigurationSection("stages");
                        for (String stageId : stagesConfig.getKeys(false)) {
                            ConfigurationSection stageConfig = stagesConfig.getConfigurationSection(stageId);
                            if (stageConfig == null) {
                                main.getLogger().warning("Could not load stageConfig for plant: " + plantId);
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
                            if (!addDropsToDropStorage(stageConfig, growthStage.getDropStorage())) {
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
                                    main.getLogger().warning(String.format("Stage %s's on-execute could not be loaded from section: %s", stageId, onExecuteSection.getCurrentPath()));
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
                                    main.getLogger().warning(String.format("Stage %s's on-fail could not be loaded from section: %s", stageId, onFailSection.getCurrentPath()));
                                    return;
                                }
                                growthStage.setOnFail(onFail);
                            }
                            // set growth requirements, if present
                            if (stageConfig.isConfigurationSection("growth-requirements")) {
                                if (!addRequirementsToStorage(stageConfig.getConfigurationSection("growth-requirements"),
                                        growthStage.getRequirementStorage())) {
                                    main.getLogger().warning(String.format("Stage growth-requirements could not be loaded for stage %s of plant: %s", stageId, plantId));
                                    return;
                                }
                            }
                            // set blocks for growth
                            if (!stageConfig.isConfigurationSection("blocks")) {
                                main.getLogger().warning(String.format("No blocks provided for growth stage %s in plant %s: ", stageId, plantId));
                                return;
                            }
                            ConfigurationSection blocksConfig = stageConfig.getConfigurationSection("blocks");
                            for (String blockName : blocksConfig.getKeys(false)) {
                                ConfigurationSection blockConfig = blocksConfig.getConfigurationSection(blockName);
                                if (blockConfig == null) {
                                    main.getLogger().warning("Could not load stage's blockConfig for plant: " + plantId);
                                    return;
                                }
                                BlockSettings blockSettings = loadBlockConfig(blockConfig.getConfigurationSection("block-data"));
                                if (blockSettings == null) {
                                    main.getLogger().warning("blockSettings for growth block returned null for plant: " + plantId);
                                    return;
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
                                    main.getLogger().warning(String.format("Could not create growth stage block in plant %s due to: %s", plantId, e.getMessage()));
                                    return;
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
                                // set childOf, if present
                                if (blockConfig.isSet("child-of")) {
                                    if (!blockConfig.isConfigurationSection("child-of")
                                            || !blockConfig.isInt("child-of.x")
                                            || !blockConfig.isInt("child-of.y")
                                            || !blockConfig.isInt("child-of.z")) {
                                        main.getLogger().warning("child-of is not configured properly for plant: " + plantId);
                                        return;
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
                                    boolean valid = addDropsToDropStorage(blockConfig, growthStageBlock.getDropStorage());
                                    if (!valid) {
                                        return;
                                    }
                                    // if no limit defined for growthStageBlock but is defined for growth stage, apply it
                                    if (!growthStageBlock.getDropStorage().isDropLimitSet() && growthStage.getDropStorage().isDropLimitSet()) {
                                        growthStageBlock.getDropStorage().setDropLimit(growthStage.getDropStorage().getDropLimit());
                                    }
                                }
                                // set interact behavior, if present
                                if (blockConfig.isConfigurationSection("on-interact")) {
                                    ConfigurationSection onInteractSection = blockConfig.getConfigurationSection("on-interact");
                                    PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onInteractSection, plant.getPlantData());
                                    if (plantInteractStorage == null) {
                                        main.getLogger().warning("Could not load on-interact section: " + onInteractSection.getCurrentPath());
                                        return;
                                    }
                                    // add interactions to growth stage block
                                    growthStageBlock.setOnInteract(plantInteractStorage);
                                }
                                // set break behavior, if present
                                if (blockConfig.isConfigurationSection("on-break")) {
                                    ConfigurationSection onBreakSection = blockConfig.getConfigurationSection("on-break");
                                    PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onBreakSection, plant.getPlantData());
                                    if (plantInteractStorage == null) {
                                        main.getLogger().warning("Could not load on-break section: " + onBreakSection.getCurrentPath());
                                        return;
                                    }
                                    // add interactions to growth stage block
                                    growthStageBlock.setOnBreak(plantInteractStorage);
                                }
                                // add growth stage block to stage
                                growthStage.addGrowthStageBlock(growthStageBlock);
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
                main.getLogger().info("seedConfig was null for plant: " + plantId);
            }
        }
        // load plant goods; failure here shouldn't abort plant loading
        if (plantConfig.isConfigurationSection("goods")) {
            ConfigurationSection goodsSection = plantConfig.getConfigurationSection("goods");
            for (String goodId : goodsSection.getKeys(false)) {
                ConfigurationSection goodSection = goodsSection.getConfigurationSection(goodId);
                ItemSettings goodSettings = loadItemConfig(goodSection, false);
                if (goodSettings != null) {
                    PlantItem goodItem = new PlantItem(goodSettings.generatePlantItemStack(goodId));
                    addPricesToPlantItem(goodSection, goodItem);
                    if (goodSection.isConfigurationSection("consumable")) {
                        PlantConsumableStorage goodConsumable = loadPlantConsumableStorage(goodSection.getConfigurationSection("consumable"), null);
                        if (goodConsumable != null) {
                            goodItem.setConsumableStorage(goodConsumable);
                        }
                    }
                    plant.addGoodItem(goodId, goodItem);
                    main.getLogger().info(String.format("Added good item '%s' to plant: %s", goodId, plantId));
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

        // add plant to plant type manager
        main.getPlantTypeManager().addPlantType(plant);
        main.getLogger().info("Successfully loaded plant: " + plantId);
    }

    void loadVanillaDropConfig() {
        if (config == null || !config.isConfigurationSection("vanilla-drops")) {
            return;
        }
        ConfigurationSection vanillaDropsSection = config.getConfigurationSection("vanilla-drops");
        for (String placeholder : vanillaDropsSection.getKeys(false)) {
            ConfigurationSection dropSection = vanillaDropsSection.getConfigurationSection(placeholder);
            if (dropSection == null) {
                main.getLogger().warning("Vanilla drop section was null in section: " + dropSection.getCurrentPath());
                return;
            }
            if (!dropSection.isString("type")) {
                main.getLogger().warning("Vanilla drop section does not include type in section: " + dropSection.getCurrentPath());
                return;
            }
            String type = dropSection.getString("type");
            if (type.equalsIgnoreCase("block")) {
                addVanillaBlockDrop(dropSection);
            } else if (type.equalsIgnoreCase("entity")) {
                addVanillaEntityDrop(dropSection);
            }
        }
    }

    void addVanillaBlockDrop(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        if (!section.isString("material")) {
            main.getLogger().warning("Vanilla block drop not added; no entity-type provided");
            return;
        }
        String name = section.getString("material");
        Material material = Material.getMaterial(name.toUpperCase());
        if (material == null) {
            main.getLogger().warning(String.format("Vanilla block drop not added; material '%s' not recognized", name));
            return;
        }
        // set interact
        ConfigurationSection onDropSection = section.getConfigurationSection("on-drop");
        if (onDropSection == null) {
            main.getLogger().warning("Vanilla block drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
        }
        PlantInteractStorage storage = loadPlantInteractStorage(onDropSection);
        if (storage == null) {
            main.getLogger().warning("Vanilla block drop not added; issue reading contents of on-drop section");
        }
        // add to vanilla drop manager
        main.getVanillaDropManager().addInteract(material, storage);
        main.getLogger().info("Added vanilla drop behavior for block: " + material.toString());
    }

    void addVanillaEntityDrop(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        if (!section.isString("entity-type")) {
            main.getLogger().warning("Vanilla entity drop not added; no entity provided");
            return;
        }
        String name = section.getString("entity-type");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' not recognized", name));
            return;
        }
        if (!entityType.isAlive()) {
            main.getLogger().warning(String.format("Vanilla entity drop not added; entity '%s' is not alive", name));
            return;
        }
        // set interact
        ConfigurationSection onDropSection = section.getConfigurationSection("on-drop");
        if (onDropSection == null) {
            main.getLogger().warning("Vanilla entity drop not added; no on-drop section found in section: " + section.getCurrentPath());
            return;
        }
        PlantInteractStorage storage = loadPlantInteractStorage(onDropSection);
        if (storage == null) {
            main.getLogger().warning("Vanilla entity drop not added; issue reading contents of on-drop section");
        }
        // add to vanilla drop manager
        main.getVanillaDropManager().addInteract(entityType, storage);
        main.getLogger().info("Added vanilla drop behavior for entity: " + entityType.getKey().getKey().toUpperCase());
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
                        main.getLogger().warning("link string could not be read in item section: " + section.getCurrentPath());
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
                        main.getLogger().warning("PlantId '" + plantId + "' does not match any plant config in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if item, get item
                    if (itemString.equals("")) {
                        if (!linkedPlantConfig.isConfigurationSection("item")) {
                            main.getLogger().warning(String.format("Linked plant %s does not contain item section",
                                    plantId));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection("item");
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else if (itemString.equalsIgnoreCase("seed")) {
                        // if seed, get growing.seed-item
                        if (!linkedPlantConfig.isConfigurationSection("growing.seed-item")) {
                            main.getLogger().warning(String.format("Linked plant %s does not contain item section",
                                    plantId));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection("growing.seed-item");
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else if (itemString.startsWith("goods.")) {
                        String[] goodsInfo = itemString.split("\\.", 2);
                        if (goodsInfo.length < 2) {
                            main.getLogger().warning(String.format("Link %s does not contain an id for its goods request in section: %s", link, section.getCurrentPath()));
                            return null;
                        }
                        String goodId = goodsInfo[1];
                        if (!linkedPlantConfig.isConfigurationSection(itemString)) {
                            main.getLogger().warning(String.format("Linked plant %s does not contain good %s", plantId, itemString));
                            return null;
                        }
                        ConfigurationSection itemSection = linkedPlantConfig.getConfigurationSection(itemString);
                        linkedItemSettings = loadItemConfig(itemSection, false);
                    } else {
                        main.getLogger().warning("Linked item was neither item, seed, or good in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if linkedItem settings null, return null
                    if (linkedItemSettings == null) {
                        main.getLogger().warning("Linked item could not be loaded from item section: " + section.getCurrentPath());
                        return null;
                    }
                }
            }
            // get item-related details from section
            String materialName = section.getString("material");
            if (materialName == null && linkedItemSettings == null) {
                main.getLogger().warning("Material not provided in item section: " + section.getCurrentPath());
                return null;
            }
            Material material = Material.getMaterial(materialName);
            if (material == null && linkedItemSettings == null) {
                main.getLogger().warning("Material '" + materialName + "' not recognized in item section: " + section.getCurrentPath());
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
                    main.getLogger().warning(String.format("ItemFlag '%s' not recognized in item section: %s", itemFlagString, section.getCurrentPath()));
                    return null;
                }
                finalItemSettings.addItemFlag(flag);
            }
            // get enchantments -> Enchantment:Level, if present
            List<String> enchantmentStrings = section.getStringList("enchantments");
            for (String enchantmentString : enchantmentStrings) {
                String[] splitString = enchantmentString.split(":");
                if (splitString.length > 2 || splitString.length == 0) {
                    main.getLogger().warning("Enchantment string was invalid in item section: " + section.getCurrentPath());
                    continue;
                }
                String enchantmentName = splitString[0].toLowerCase();
                int level = 1;
                // set custom level, if present
                if (splitString.length == 2) {
                    try {
                        level = Math.max(1, Integer.parseInt(splitString[1]));
                    } catch (NumberFormatException e) {
                        main.getLogger().warning("Enchantment level was not an integer in item section: " + section.getCurrentPath());
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
                    main.getLogger().warning(String.format("Enchantment '%s' not recognized in item section: %s",
                            enchantmentName, section.getCurrentPath()));
                    continue;
                }
                finalItemSettings.addEnchantmentLevel(new EnchantmentLevel(enchantment, level));
            }
            // get potion effects -> PotionEffectType:Duration:Amplifier, if present
            List<String> potionStrings = section.getStringList("potion-effects");
            for (String potionString : potionStrings) {
                String[] splitString = potionString.split(":");
                if (splitString.length > 3 || splitString.length < 2) {
                    main.getLogger().warning("Potion string was invalid in item section: " + section.getCurrentPath());
                    continue;
                }
                String potionName = splitString[0].toUpperCase();
                // get potion name
                PotionEffectType potionEffectType = PotionEffectType.getByName(potionName);
                if (potionEffectType == null) {
                    main.getLogger().warning(String.format("Potion '%s' not recognized in item section: %s",
                            potionName, section.getCurrentPath()));
                    continue;
                }
                // get duration
                int duration;
                try {
                    duration = Math.max(1, Integer.parseInt(splitString[1]));
                } catch (NumberFormatException e) {
                    main.getLogger().warning("Potion duration was not an integer in item section: " + section.getCurrentPath());
                    continue;
                }
                int amplifier = 0;
                if (splitString.length == 3) {
                    try {
                        amplifier = Math.max(0, Integer.parseInt(splitString[2])-1);
                    } catch (NumberFormatException e) {
                        main.getLogger().warning("Potion amplifier was not an integer in item section: " + section.getCurrentPath());
                        continue;
                    }
                }
                finalItemSettings.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
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
                    main.getLogger().warning(String.format("CustomModelData '%d' cannot be less than 0 in section: %s",
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
                    main.getLogger().warning("Material not provided in block section: " + section.getCurrentPath());
                    return null;
                }
            }
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                main.getLogger().warning("Material '" + materialName + "' not recognized in block section: " + section.getCurrentPath());
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

    DropSettings loadDropConfig(ConfigurationSection section) {
        if (section != null) {
            DropSettings dropSettings = new DropSettings();
            // set min and max amounts if present
            if (section.isSet("max-amount")) {
                if (section.isInt("max-amount")) {
                    // set max amount
                    int maxAmount = section.getInt("max-amount");
                    if (maxAmount < 1) {
                        main.getLogger().warning("max-amount for drops must be at least 1 for drop section: " + section.getCurrentPath());
                        return null;
                    }
                    dropSettings.setMaxAmount(maxAmount);
                    // set min amount if present
                    if (section.isInt("min-amount")) {
                        int minAmount = section.getInt("min-amount");
                        if (minAmount > maxAmount) {
                            main.getLogger().warning("min-amount for drops cannot be greater than max-amount for drop section: " + section.getCurrentPath());
                            return null;
                        }
                        dropSettings.setMinAmount(minAmount);
                    }
                } else {
                    main.getLogger().warning("max-amount was not an int for drop section: " + section.getCurrentPath());
                    return null;
                }
            }
            // set drop chance if present
            if (section.isSet("chance")) {
                if (section.isDouble("chance")) {
                    double chance = section.getDouble("chance");
                    if (chance <= 0.0 || chance > 100.0) {
                        main.getLogger().warning("chance was not greater than 0.0 and less/equal to 100.0 for drop section: " + section.getCurrentPath());
                        return null;
                    }
                    dropSettings.setChance(chance);
                } else {
                    main.getLogger().warning("chance was not a double for drop section: " + section.getCurrentPath());
                    return null;
                }
            }
            // set ItemSettings
            if (!section.isSet("item")) {
                main.getLogger().warning("item not set for drop section: " + section.getCurrentPath());
                return null;
            }
            ItemSettings itemSettings = loadItemConfig(section.getConfigurationSection("item"),true);
            if (itemSettings == null) {
                main.getLogger().warning("itemSettings could not be created for drop section: " + section.getCurrentPath());
            }
            dropSettings.setItemSettings(itemSettings);
            return dropSettings;
        }
        return null;
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
                main.getLogger().warning("Default PlantInteract could not be loaded from section: " + defaultInteractSection.getCurrentPath());
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
                    main.getLogger().warning("Item PlantInteract section was null from section: " + itemsInteractSection.getCurrentPath());
                    return null;
                }
                ConfigurationSection itemSection = itemInteractSection.getConfigurationSection("item");
                if (itemSection == null) {
                    main.getLogger().warning("Item section not present in PlantInteract section from section: " + itemInteractSection.getCurrentPath());
                    return null;
                }
                ItemSettings itemInteractSettings = loadItemConfig(itemSection, true);
                if (itemInteractSettings == null) {
                    return null;
                }
                PlantInteract plantInteract = loadPlantInteract(itemInteractSection, data);
                if (plantInteract == null) {
                    main.getLogger().warning("Item PlantInteract could not be loaded from section: " + itemInteractSection.getCurrentPath());
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
                main.getLogger().warning(String.format("Interact will not have chosen break-block value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isBreakBlock(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setBreakBlock(value);
            }
        }
        if (section.isSet("only-break-block-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-break-block-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                main.getLogger().warning(String.format("Interact will not have chosen only-break-block-on-do value and instead will be" +
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
                main.getLogger().warning(String.format("Interact will not have chosen give-block-drops value and instead will be" +
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
                main.getLogger().warning(String.format("Interact will not have chosen take-item value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isTakeItem(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setTakeItem(value);
            }
        }
        if (section.isSet("only-take-item-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-take-item-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                main.getLogger().warning(String.format("Interact will not have chosen only-take-item-on-do value and instead will be" +
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

        // set condition for doing actions, if present
        if (section.isSet("do-if")) {
            ScriptBlock value = createPlantScript(section, "do-if", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                main.getLogger().warning(String.format("Interact will not have chosen do-if value and instead will be" +
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
                main.getLogger().warning(String.format("Interact will not have chosen only-effects-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyEffectsOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyEffectsOnDo(value);
            }
        }
        if (section.isSet("only-consumable-effects-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-consumable-effects-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                main.getLogger().warning(String.format("Interact will not have chosen only-consumable-effects-on-do value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        plantInteract.isOnlyConsumableEffectsOnDo(null, null), section.getCurrentPath()));
            } else {
                plantInteract.setOnlyConsumableEffectsOnDo(value);
            }
        }

        // add drops, if present
        if (section.isConfigurationSection("drops")) {
            boolean valid = addDropsToDropStorage(section, plantInteract.getDropStorage());
            if (!valid) {
                return null;
            }
        }
        if (section.isSet("only-drop-on-do")) {
            ScriptBlock value = createPlantScript(section, "only-drop-on-do", data);
            if (value == null || !ScriptHelper.isBoolean(value)) {
                main.getLogger().warning(String.format("Interact will not have chosen only-drop-on-do value and instead will be" +
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
        if (section.isBoolean("take-item")) {
            consumable.setTakeItem(section.getBoolean("take-item"));
        }
        // set missing food, if present
        if (section.isBoolean("missing-food")) {
            consumable.setMissingFood(section.getBoolean("missing-food"));
        }
        // set normal eat, if present
        if (section.isBoolean("normal-eat")) {
            consumable.setNormalEat(section.getBoolean("normal-eat"));
        }
        // set damage to add to item, if present
        if (section.isInt("add-damage")) {
            consumable.setAddDamage(section.getInt("add-damage"));
        }
        // set give item, if present
        if (section.isConfigurationSection("give-items")) {
            ConfigurationSection itemsSection = section.getConfigurationSection("give-items");
            for (String placeholder : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(placeholder);
                ItemSettings itemSettings = loadItemConfig(itemSection, true);
                if (itemSettings == null) {
                    main.getLogger().warning(String.format("Problem getting give-items in consumable section %s;" +
                                    "will continue to load, but this consumable method will not be available (fix config)",
                            itemSection.getCurrentPath()));
                    return null;
                }
                consumable.addItemToGive(itemSettings.generateItemStack());
            }
        }
        // set required items, if present
        if (section.isConfigurationSection("required-items")) {
            ConfigurationSection requiredItemsSection = section.getConfigurationSection("required-items");
            for (String placeholder : requiredItemsSection.getKeys(false)) {
                ConfigurationSection requiredItemSection = requiredItemsSection.getConfigurationSection(placeholder);
                // set item
                ConfigurationSection itemSection = requiredItemSection.getConfigurationSection("item");
                if (itemSection == null) {
                    main.getLogger().warning(String.format("No item section found for required item '%s' in section: %s;" +
                                    "will continue to load, but item won't be consumable (fix config)",
                            placeholder, requiredItemsSection));
                    return null;
                }
                ItemSettings itemSettings = loadItemConfig(itemSection, true);
                if (itemSettings == null) {
                    main.getLogger().warning(String.format("Problem getting required item in section %s;" +
                                    "will continue to load, but this item will not be consumable (fix config)",
                            itemSection.getCurrentPath()));
                    return null;
                }
                // create required item from item
                RequiredItem requiredItem = new RequiredItem(itemSettings.generateItemStack());
                // set take item, if set
                if (requiredItemSection.isBoolean("take-item")) {
                    requiredItem.setTakeItem(requiredItemSection.getBoolean("take-item"));
                }
                // set if required item should be in hand (offhand or main hand), if set
                if (requiredItemSection.isBoolean("in-hand")) {
                    requiredItem.setInHand(requiredItemSection.getBoolean("in-hand"));
                }
                // set damage to add to item, if present
                if (requiredItemSection.isInt("add-damage")) {
                    requiredItem.setAddDamage(requiredItemSection.getInt("add-damage"));
                }
                consumable.addRequiredItem(requiredItem);
            }
        }
        // add effects, if present
        addEffectsToEffectStorage(section, consumable.getEffectStorage(), data);
        // return consumable
        return consumable;
    }

    ScriptColor createColor(ConfigurationSection section, PlantData data) {
        ScriptColor color = new ScriptColor();
        if (section.isSet("r")) {
            ScriptBlock red = createPlantScript(section, "r", data);
            if (red == null || !ScriptHelper.isLong(red)) {
                main.getLogger().warning(String.format("Color will not have chosen red value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getRed().loadValue(null, null).getIntegerValue(), section.getCurrentPath()));
            }
            color.setRed(red);
        }
        if (section.isSet("g")) {
            ScriptBlock green = createPlantScript(section, "g", data);
            if (green == null || !ScriptHelper.isLong(green)) {
                main.getLogger().warning(String.format("Color will not have chosen green value and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        color.getGreen().loadValue(null, null).getIntegerValue(), section.getCurrentPath()));
            }
            color.setGreen(green);
        }
        if (section.isSet("b")) {
            ScriptBlock blue = createPlantScript(section, "b", data);
            if (blue == null || !ScriptHelper.isLong(blue)) {
                main.getLogger().warning(String.format("Color will not have chosen blue value and instead will be" +
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
                main.getLogger().warning(String.format("PotionType '%s' not recognized in item section: %s",
                        name, section.getCurrentPath()));
                return null;
            }
        } else {
            main.getLogger().warning("PotionType not set in item section: " + section.getCurrentPath());
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
            main.getLogger().warning("No RequirementStorage provided in section: " + section.getCurrentPath());
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
        // set block requirements, if present
        if (section.isSet("required-blocks") && section.isConfigurationSection("required-blocks")) {
            ConfigurationSection requiredBlocksSection = section.getConfigurationSection("required-blocks");
            for (String blockName : requiredBlocksSection.getKeys(false)) {
                ConfigurationSection requiredBlockSection = requiredBlocksSection.getConfigurationSection(blockName);
                if (requiredBlockSection == null) {
                    main.getLogger().warning("config section was null for required block in section: " + requiredBlocksSection.getCurrentPath());
                    return false;
                }
                BlockSettings blockSettings = loadBlockConfig(requiredBlockSection);
                if (blockSettings == null) {
                    main.getLogger().warning("blockSettings for required block returned null in section: " + requiredBlockSection.getCurrentPath());
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
                    main.getLogger().warning(String.format("Could not create required block in section due to: %s",
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

    boolean addDropsToDropStorage(ConfigurationSection section, DropStorage dropStorage) {
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
                    main.getLogger().warning("dropConfig was null in section: " + section.getCurrentPath());
                    return false;
                }
                // get drop settings
                DropSettings dropSettings = loadDropConfig(dropConfig);
                if (dropSettings == null) {
                    main.getLogger().warning("dropSettings were null in section: " + section.getCurrentPath());
                    return false;
                }
                ItemSettings dropItemSettings = dropSettings.getItemSettings();
                if (dropItemSettings == null) {
                    main.getLogger().warning("dropItemSettings were null in section: " + section.getCurrentPath());
                    return false;
                }
                Drop drop = new Drop(
                        dropItemSettings.generateItemStack(),
                        dropSettings.getMinAmount(),
                        dropSettings.getMaxAmount(),
                        dropSettings.getChance()
                );
                dropStorage.addDrop(drop);
            }
        }
        return true;
    }

    void addPricesToPlantItem(ConfigurationSection section, PlantItem plantItem) {
        if (section == null) {
            return;
        }
        // get buy price, if present
        if (section.isDouble("buy-price") || section.isInt("buy-price")) {
            double price = section.getDouble("buy-price");
            if (price >= 0.0) {
                plantItem.setBuyPrice(price);
            }
        }
        // get sell price, if present
        if (section.isDouble("sell-price") || section.isInt("sell-price")) {
            double price = section.getDouble("sell-price");
            if (price >= 0.0) {
                plantItem.setSellPrice(price);
            }
        }
    }

    void addEffectsToEffectStorage(ConfigurationSection section, PlantEffectStorage effectStorage, PlantData data) {
        if (section == null || effectStorage == null) {
            return;
        }
        if (!section.isConfigurationSection("effects")) {
            return;
        }
        ConfigurationSection effectsSection = section.getConfigurationSection("effects");
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
            main.getLogger().warning("Type not set for effect at: " + section.getCurrentPath());
            return false;
        }
        PlantEffect effect = null;
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
            default:
                break;
        }
        if (effect != null) {
            addDoIfAndDelayToEffect(section, effect, data);
            effectStorage.addEffect(effect);
            return true;
        }
        main.getLogger().warning(String.format("Effect %s not recognized; not added to effect storage for section: %s",
                type, section.getCurrentPath()));
        return false;
    }

    PlantFeedEffect createFeedEffect(ConfigurationSection section, PlantData data) {
        PlantFeedEffect effect = new PlantFeedEffect();
        // set food amount, if present
        if (section.isSet("food-amount")) {
            ScriptBlock foodAmount = createPlantScript(section, "food-amount", data);
            if (foodAmount == null || !ScriptHelper.isLong(foodAmount)) {
                main.getLogger().warning(String.format("Feed effect will not have chosen food-amount and instead will be" +
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
                main.getLogger().warning(String.format("Feed effect will not have chosen saturate-amount and instead will be" +
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
                main.getLogger().warning(String.format("Heal effect will not have chosen heal-amount and instead will be" +
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
            main.getLogger().warning("Sound effect not added; sound field not found in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock sound = createPlantScript(section, "sound", data);
        if (sound == null || !ScriptHelper.isString(sound)) {
            main.getLogger().warning(String.format("Sound effect not added; sound field must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setSoundName(sound);
        // TODO: check that sound exists if does not include variables
        // set volume, if present
        if (section.isSet("volume")) {
            ScriptBlock volume = createPlantScript(section, "volume", data);
            if (volume == null || !ScriptHelper.isNumeric(volume)) {
                main.getLogger().warning(String.format("Sound effect will not have chosen volume and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen pitch and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetX(offsetX);
            }
        }
        if (section.isSet("offset-y")) {
            ScriptBlock offsetY = createPlantScript(section, "offset-y", data);
            if (offsetY == null || !ScriptHelper.isNumeric(offsetY)) {
                main.getLogger().warning(String.format("Sound effect will not have chosen offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetY(offsetY);
            }
        }
        if (section.isSet("offset-z")) {
            ScriptBlock offsetZ = createPlantScript(section, "offset-z", data);
            if (offsetZ == null || !ScriptHelper.isNumeric(offsetZ)) {
                main.getLogger().warning(String.format("Sound effect will not have chosen offset-z and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen multiplier and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen eye-location value and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen ignore-direction-y value and instead will be" +
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
                main.getLogger().warning(String.format("Sound effect will not have chosen client-side value and instead will be" +
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
            main.getLogger().warning("Particle effect not added; particle was null in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock particle = createPlantScript(section, "particle", data);
        if (particle == null || !ScriptHelper.isString(particle)) {
            main.getLogger().warning(String.format("Particle effect not added; particle must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setParticleName(particle);
        // set count, if present
        if (section.isSet("count")) {
            ScriptBlock count = createPlantScript(section, "count", data);
            if (count == null || !ScriptHelper.isLong(count)) {
                main.getLogger().warning(String.format("Particle effect will not have chosen count and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetX(offsetX);
            }
        }
        if (section.isSet("offset-y")) {
            ScriptBlock offsetY = createPlantScript(section, "offset-y", data);
            if (offsetY == null || !ScriptHelper.isNumeric(offsetY)) {
                main.getLogger().warning(String.format("Particle effect will not have chosen offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setOffsetY(offsetY);
            }
        }
        if (section.isSet("offset-z")) {
            ScriptBlock offsetZ = createPlantScript(section, "offset-z", data);
            if (offsetZ == null || !ScriptHelper.isNumeric(offsetZ)) {
                main.getLogger().warning(String.format("Particle effect will not have chosen offset-z and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen data-offset-x and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getDataOffsetXValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDataOffsetX(dataOffsetX);
            }
        }
        if (section.isSet("data-offset-y")) {
            ScriptBlock dataOffsetY = createPlantScript(section, "data-offset-y", data);
            if (dataOffsetY == null || !ScriptHelper.isNumeric(dataOffsetY)) {
                main.getLogger().warning(String.format("Particle effect will not have chosen data-offset-y and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getDataOffsetYValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDataOffsetY(dataOffsetY);
            }
        }
        if (section.isSet("data-offset-z")) {
            ScriptBlock dataOffsetZ = createPlantScript(section, "data-offset-z", data);
            if (dataOffsetZ == null || !ScriptHelper.isNumeric(dataOffsetZ)) {
                main.getLogger().warning(String.format("Particle effect will not have chosen data-offset-z and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen multiplier and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen extra value and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen eye-location value and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen ignore-direction-y value and instead will be" +
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
                main.getLogger().warning(String.format("Particle effect will not have chosen client-side value and instead will be" +
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
            main.getLogger().warning("Potion effect not added; potion not found in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock potionName = createPlantScript(section, "potion", data);
        if (potionName == null || !ScriptHelper.isString(potionName)) {
            main.getLogger().warning(String.format("Potion effect not added; potion must be ScriptType STRING in section %s",
                    section.getCurrentPath()));
            return null;
        }
        effect.setPotionEffectTypeName(potionName);
        // if no variables, then check if potion is recognized
        if (!potionName.containsVariable()) {
            PotionEffectType potionEffectType = effect.getPotionEffectType(null, null);
            if (potionEffectType == null) {
                String potionNameString = potionName.loadValue(null, null).getStringValue();
                main.getLogger().warning(String.format("Potion effect not added; potion '%s' not recognized", potionNameString));
                return null;
            }
        }
        // set duration, if present
        if (section.isSet("duration")) {
            ScriptBlock duration = createPlantScript(section, "duration", data);
            if (duration == null || !ScriptHelper.isLong(duration)) {
                main.getLogger().warning(String.format("Potion effect will not have chosen duration and instead will be" +
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
                main.getLogger().warning(String.format("Potion effect will not have chosen amplifier and instead will be" +
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
                main.getLogger().warning(String.format("Potion effect will not have chosen ambient value and instead will be" +
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
                main.getLogger().warning(String.format("Potion effect will not have chosen particles value and instead will be" +
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
                main.getLogger().warning(String.format("Potion effect will not have chosen icon value and instead will be" +
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
            main.getLogger().warning("Drop effect not added; no drops section provided in section: " + section.getCurrentPath());
            return null;
        }
        PlantDropEffect effect = new PlantDropEffect();
        boolean added = addDropsToDropStorage(section, effect.getDropStorage());
        if (!added) {
            main.getLogger().warning("Drop effect not added; issue getting drops");
            return null;
        }
        return effect;
    }

    PlantAirEffect createAirEffect(ConfigurationSection section, PlantData data) {
        PlantAirEffect effect = new PlantAirEffect();
        if (!section.isSet("amount")) {
            main.getLogger().warning("Air effect not added; no amount provided in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock amount = createPlantScript(section, "amount", data);
        if (amount == null || !ScriptHelper.isLong(amount)) {
            main.getLogger().warning("Air effect not added; amount must be ScriptType Long in section: " + section.getCurrentPath());
            return null;
        }
        effect.setAmount(amount);
        return effect;
    }

    PlantAreaEffect createAreaEffect(ConfigurationSection section, PlantData data) {
        PlantAreaEffect effect = new PlantAreaEffect();
        if (section.isConfigurationSection("color")) {
            ConfigurationSection colorSection = section.getConfigurationSection("color");
            if (colorSection != null) {
                effect.setColor(createColor(colorSection, data));
            }
        }
        if (section.isSet("duration")) {
            ScriptBlock duration = createPlantScript(section, "duration", data);
            if (duration == null || !ScriptHelper.isLong(duration)) {
                main.getLogger().warning(String.format("Area effect will not have chosen duration and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getDurationValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDuration(duration);
            }
        }
        if (section.isSet("duration-on-use")) {
            ScriptBlock durationOnUse = createPlantScript(section, "duration-on-use", data);
            if (durationOnUse == null || !ScriptHelper.isLong(durationOnUse)) {
                main.getLogger().warning(String.format("Area effect will not have chosen duration-on-use and instead will be" +
                                " %d; must be ScriptType LONG in section: %s",
                        effect.getDurationValue(null, null), section.getCurrentPath()));
            } else {
                effect.setDurationOnUse(durationOnUse);
            }
        }
        if (section.isSet("particle")) {
            ScriptBlock particle = createPlantScript(section, "particle", data);
            if (particle == null || !ScriptHelper.isString(particle)) {
                main.getLogger().warning(String.format("Area effect will not have chosen particle; must be ScriptType STRING in section %s",
                        section.getCurrentPath()));
            } else {
                effect.setParticleName(particle);
            }
        }
        if (section.isSet("radius")) {
            ScriptBlock radius = createPlantScript(section, "radius", data);
            if (radius == null || !ScriptHelper.isNumeric(radius)) {
                main.getLogger().warning(String.format("Area effect will not have chosen radius and instead will be" +
                        " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadius(radius);
            }
        }
        if (section.isSet("radius-on-use")) {
            ScriptBlock radiusOnUse = createPlantScript(section, "radius-on-use", data);
            if (radiusOnUse == null || !ScriptHelper.isNumeric(radiusOnUse)) {
                main.getLogger().warning(String.format("Area effect will not have chosen radius-on-use and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusOnUseValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadiusOnUse(radiusOnUse);
            }
        }
        if (section.isSet("radius-per-tick")) {
            ScriptBlock radiusPerTick = createPlantScript(section, "radius-per-tick", data);
            if (radiusPerTick == null || !ScriptHelper.isNumeric(radiusPerTick)) {
                main.getLogger().warning(String.format("Area effect will not have chosen radius-per-tick and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getRadiusPerTickValue(null, null), section.getCurrentPath()));
            } else {
                effect.setRadiusPerTick(radiusPerTick);
            }
        }
        if (section.isSet("reapplication-delay")) {
            ScriptBlock reapplicationDelay = createPlantScript(section, "reapplication-delay", data);
            if (reapplicationDelay == null || !ScriptHelper.isLong(reapplicationDelay)) {
                main.getLogger().warning(String.format("Area effect will not have chosen reapplication-delay and instead will be" +
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
            main.getLogger().warning("Durability effect not added; no damage-amount provided in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock amount = createPlantScript(section, "damage-amount", data);
        if (amount == null || !ScriptHelper.isLong(amount)) {
            main.getLogger().warning(String.format("Durability effect not added; must be ScriptType LONG in section: %s",
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
                main.getLogger().warning(String.format("Chat effect will not have chosen from-player message and instead will be" +
                        " %s; must be neither null nor ScriptType NULL in section: %s",
                    effect.getFromPlayerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setFromPlayer(fromPlayer);
            }
        }
        if (section.isSet("to-player")) {
            ScriptBlock toPlayer = createPlantScript(section, "to-player", data);
            if (toPlayer == null || ScriptHelper.isNull(toPlayer)) {
                main.getLogger().warning(String.format("Chat effect will not have chosen to-player message and instead will be" +
                                " %s; must be neither null nor ScriptType NULL in section: %s",
                        effect.getToPlayerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setToPlayer(toPlayer);
            }
        }
        if (effect.getFromPlayer() == ScriptResult.EMPTY && effect.getToPlayer() == ScriptResult.EMPTY) {
            main.getLogger().warning("Chat effect not added; from-player and to-player messages both not set in section: " + section.getCurrentPath());
            return null;
        }
        return effect;
    }

    PlantExplosionEffect createExplosionEffect(ConfigurationSection section, PlantData data) {
        PlantExplosionEffect effect = new PlantExplosionEffect();
        if (section.isSet("power")) {
            ScriptBlock power = createPlantScript(section, "power", data);
            if (power == null || !ScriptHelper.isNumeric(power)) {
                main.getLogger().warning(String.format("Explosion effect will not have chosen power and instead will be" +
                                " %f; must be ScriptType LONG or DOUBLE in section: %s",
                        effect.getPowerValue(null, null), section.getCurrentPath()));
            } else {
                effect.setPower(power);
            }
        }
        if (section.isSet("fire")) {
            ScriptBlock fire = createPlantScript(section, "fire", data);
            if (fire == null || !ScriptHelper.isBoolean(fire)) {
                main.getLogger().warning(String.format("Explosion effect will not have chosen fire value and instead will be" +
                                " %b; must be ScriptType BOOLEAN in section: %s",
                        effect.isFire(null, null), section.getCurrentPath()));
            } else {
                effect.setFire(fire);
            }
        }
        if (section.isSet("break-blocks")) {
            ScriptBlock breakBlocks = createPlantScript(section, "break-blocks", data);
            if (breakBlocks == null || !ScriptHelper.isBoolean(breakBlocks)) {
                main.getLogger().warning(String.format("Command effect will not have chosen break-blocks value and instead will be" +
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
            main.getLogger().warning("Command effect not added; command value not present in section: " + section.getCurrentPath());
            return null;
        }
        ScriptBlock command = createPlantScript(section, "command", data);
        if (command == null || !ScriptHelper.isString(command)) {
            main.getLogger().warning("Command effect not added; command value must be ScriptType STRING in section: " + section.getCurrentPath());
            return null;
        }
        effect.setCommand(command);
        if (section.isSet("console")) {
            ScriptBlock console = createPlantScript(section, "console", data);
            if (console == null || !ScriptHelper.isBoolean(console)) {
                main.getLogger().warning(String.format("Command effect will not have chosen console value and instead will be" +
                        " %b; must be ScriptType BOOLEAN in section: %s",
                    effect.isConsole(null, null), section.getCurrentPath()));
            } else {
                effect.setConsole(console);
            }
        }
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

    void addCraftingRecipe(ConfigurationSection section, String recipeName) {
        String type = section.getString("type");
        if (type == null) {
            main.getLogger().warning("Type not set for crafting recipe at: " + section.getCurrentPath());
            return;
        }
        // load PlantInteractStorage, if present
        PlantInteractStorage storage = null;
        ConfigurationSection storageSection = section.getConfigurationSection("on-craft");
        if (storageSection != null) {
            storage = loadPlantInteractStorage(storageSection);
        }
        boolean ignoreResultPresent = false;
        boolean ignoreResult = false;
        if (section.isBoolean("ignore-result")) {
            ignoreResultPresent = true;
            ignoreResult = section.getBoolean("ignore-result");
        }
        // get shaped recipe
        if (type.equalsIgnoreCase("shaped")) {
            // get result
            if (!section.isConfigurationSection("result")) {
                main.getLogger().warning("No result section for shapeless crafting recipe at " + section.getCurrentPath());
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
                ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(main, "shaped_" + recipeName), resultStack);
                // get shape
                if (!section.isList("shape")) {
                    main.getLogger().warning("No shape section (must be list) for shaped crafting recipe at " + section.getCurrentPath());
                    return;
                }
                recipe.shape(section.getStringList("shape").toArray(new String[0]));
                // get ingredients to understand shape
                if (!section.isConfigurationSection("ingredients")) {
                    main.getLogger().warning("No ingredients section for shaped crafting recipe at " + section.getCurrentPath());
                    return;
                }
                ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
                for (String ingredientString : ingredientsSection.getKeys(false)) {
                    // check that ingredientString was only a single char
                    if (ingredientString.length() != 1) {
                        main.getLogger().warning(String.format("Ingredient designation must be a single character; was %s instead at %s", ingredientString, section.getCurrentPath()));
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
                main.getServer().addRecipe(recipe);
                // add recipe to recipe manager
                PlantRecipe plantRecipe = new PlantRecipe(recipe, storage);
                if (ignoreResultPresent) {
                    plantRecipe.setIgnoreResult(ignoreResult);
                }
                main.getRecipeManager().addRecipe(plantRecipe);
                main.getLogger().info("Registered shaped crafting recipe: " + recipeName);
            } catch (Exception e) {
                main.getLogger().warning(String.format("Failed to add shaped crafting recipe at %s due to exception: %s",
                        section.getCurrentPath(), e.getMessage()));
            }
        }
        // get shapeless recipe
        else if (type.equalsIgnoreCase("shapeless")) {
            // get result
            if (!section.isConfigurationSection("result")) {
                main.getLogger().warning("No result section for shapeless crafting recipe at " + section.getCurrentPath());
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
                ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(main, "shapeless_" + recipeName), resultStack);
                // get ingredients
                if (!section.isConfigurationSection("ingredients")) {
                    main.getLogger().warning("No ingredients section for shapeless crafting recipe at " + section.getCurrentPath());
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
                    main.getLogger().warning("No ingredients were found for shapeless crafting recipe at " + section.getCurrentPath());
                    return;
                }
                // otherwise add recipe to server
                main.getServer().addRecipe(recipe);
                // add recipe to recipe manager
                PlantRecipe plantRecipe = new PlantRecipe(recipe, storage);
                if (ignoreResultPresent) {
                    plantRecipe.setIgnoreResult(ignoreResult);
                }
                main.getRecipeManager().addRecipe(plantRecipe);
                main.getLogger().info("Registered shapeless crafting recipe: " + recipeName);
            } catch (Exception e) {
                main.getLogger().warning(String.format("Failed to add shapeless crafting recipe at %s due to exception: %s",
                        section.getCurrentPath(), e.getMessage()));
            }
        } else {
            main.getLogger().warning(String.format("Type '%s' not a recognized crafting recipe at %s",
                    type, section.getCurrentPath()));
        }
    }

    CookingRecipeSettings loadCookingRecipeConfig(ConfigurationSection section) {
        // get result
        if (!section.isConfigurationSection("result")) {
            main.getLogger().warning("No result section for cooking recipe at " + section.getCurrentPath());
            return null;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return null;
        }
        // get input
        if (!section.isConfigurationSection("result")) {
            main.getLogger().warning("No result section for cooking recipe at " + section.getCurrentPath());
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
        return new CookingRecipeSettings(resultStack, new RecipeChoice.ExactChoice(inputStack), experience, cookingTime);
    }

    void addFurnaceRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            FurnaceRecipe recipe = new FurnaceRecipe(new NamespacedKey(main, "furnace_" + recipeName),
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            // add recipe to server
            main.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            main.getRecipeManager().addRecipe(recipe);
            main.getLogger().info("Registered furnace recipe: " + recipeName);
        } catch (Exception e) {
            main.getLogger().warning(String.format("Failed to add furnace recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addBlastingRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            BlastingRecipe recipe = new BlastingRecipe(new NamespacedKey(main, "blasting_" + recipeName),
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            // add recipe to server
            main.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            main.getRecipeManager().addRecipe(recipe);
            main.getLogger().info("Registered blast furnace recipe: " + recipeName);
        } catch (Exception e) {
            main.getLogger().warning(String.format("Failed to add blast furnace recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addSmokingRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            SmokingRecipe recipe = new SmokingRecipe(new NamespacedKey(main, "smoking_" + recipeName),
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            // add recipe to server
            main.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            main.getRecipeManager().addRecipe(recipe);
            main.getLogger().info("Registered smoker recipe: " + recipeName);
        } catch (Exception e) {
            main.getLogger().warning(String.format("Failed to add smoker recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addCampfireRecipe(ConfigurationSection section, String recipeName) {
        CookingRecipeSettings recipeSettings = loadCookingRecipeConfig(section);
        if (recipeSettings == null) {
            return;
        }
        try {
            CampfireRecipe recipe = new CampfireRecipe(new NamespacedKey(main, "campfire_" + recipeName),
                    recipeSettings.getResult(),
                    recipeSettings.getInputChoice(),
                    recipeSettings.getExperience(),
                    recipeSettings.getCookingTime());
            // add recipe to server
            main.getServer().addRecipe(recipe);
            // add recipe to recipe manager
            main.getRecipeManager().addRecipe(recipe);
            main.getLogger().info("Registered campfire recipe: " + recipeName);
        } catch (Exception e) {
            main.getLogger().warning(String.format("Failed to add campfire recipe at %s due to exception: %s",
                    section.getCurrentPath(), e.getMessage()));
        }
    }

    void addStonecuttingRecipe(ConfigurationSection section, String recipeName) {
        // get result
        if (!section.isConfigurationSection("result")) {
            main.getLogger().warning("No result section for stonecutting recipe at " + section.getCurrentPath());
            return;
        }
        ItemSettings resultSettings = loadItemConfig(section.getConfigurationSection("result"), true);
        // if no item settings, return (something went wrong or linked item did not exist)
        if (resultSettings == null) {
            return;
        }
        // get input
        if (!section.isConfigurationSection("result")) {
            main.getLogger().warning("No result section for stonecutting recipe at " + section.getCurrentPath());
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
        StonecuttingRecipe recipe = new StonecuttingRecipe(new NamespacedKey(main, "stonecutting_" + recipeName),
                resultStack,
                new RecipeChoice.ExactChoice(inputStack));
        // add recipe to server
        main.getServer().addRecipe(recipe);
        // add recipe to recipe manager
        main.getRecipeManager().addRecipe(recipe);
        main.getLogger().info("Registered stonecutting recipe: " + recipeName);
    }

    //endregion

    //region Plant Script

    PlantData createPlantData(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        JSONObject data = new JSONObject();
        if (section.isConfigurationSection("plant-data")) {
            ConfigurationSection plantDataSection = section.getConfigurationSection("plant-data");
            for (String variableName : plantDataSection.getKeys(false)) {
                // check that variable names don't use any reserved or restricted names
                if (variableName.isEmpty()) {
                    main.getLogger().warning("Variables names cannot be empty; no varialbes will be initialized " +
                            "until this is fixed in section: " + plantDataSection.getCurrentPath());
                    return null;
                }
                if (variableName.startsWith("_")) {
                    main.getLogger().warning(String.format("Variable name '%s' cannot begin with '_'; reserved for " +
                            "general block values. No variables will be initialized until this is fixed in section: %s",
                            variableName, plantDataSection.getCurrentPath()));
                    return null;
                }
                if (Character.isDigit(variableName.charAt(0))) {
                    main.getLogger().warning(String.format("Variable name '%s' cannot begin with a digit; no " +
                            "variables will be initialized until this is fixed in section: %s",
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
                        main.getLogger().warning(String.format("Variable type '%s' not recognized; no variables will " +
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
                            main.getLogger().warning(String.format("Variable type %s is not supported right now;" +
                                            "no variables will be initialized until this is fixed in section: %s",
                                    type,
                                    variableSection.getCurrentPath()));
                            return null;
                    }
                    // plantData.addVariableType(variableName, type);
                } else {
                    main.getLogger().warning(String.format("No type defined for variable '%s'; no variables will be " +
                            "initialized until this is fixed in section: %s",
                            variableName,
                            variableSection.getCurrentPath()));
                    return null;
                }
            }
            if (data.isEmpty()) {
                main.getLogger().warning("No variables were loaded, so no plant data will be created in section " +
                        section.getConfigurationSection("plant-data").getCurrentPath());
                return null;
            }
            return new PlantData(data);
        }
        return null;
    }

    ScriptBlock loadPlantScript(ConfigurationSection section, PlantData data) {
        if (section == null) {
            return null;
        }
        if (section.isConfigurationSection("plant-script")) {
            return createPlantScript(section, "plant-script", data);
        }
        return null;
    }

    ScriptBlock createPlantScript(ConfigurationSection section, String subsectionName, PlantData data) {
        try {
            if (section == null) {
                return null;
            } else if (section.isBoolean(subsectionName)) {
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
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("IllegalArgumentException loading PlantScript in section '%s' for " +
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
            ConfigurationSection blockSection = section.getConfigurationSection(blockName);
            if (blockSection == null) {
                if (blockName.toLowerCase().equals("variable") || blockName.toLowerCase().equals("value")) {
                    blockSection = section;
                } else {
                    break;
                }
            }
            ScriptBlock returned = null;
            switch (blockName.toLowerCase()) {
                // constant/variable
                case "value":
                case "variable":
                case "result":
                    returned = createPlantScriptResult(blockSection, data); break;
                // math
                case "+":
                case "add":
                    returned = createScriptOperationAdd(blockSection, data); break;
                case "+=":
                case "addto":
                    returned = createScriptOperationAddTo(blockSection, data); break;
                case "-":
                case "subtract":
                    returned = createScriptOperationSubtract(blockSection, data); break;
                case "-=":
                case "subtractfrom":
                    returned = createScriptOperationSubtractFrom(blockSection, data); break;
                case "*":
                case "multiply":
                    returned = createScriptOperationMultiply(blockSection, data); break;
                case "*=":
                case "multiplyby":
                    returned = createScriptOperationMultiplyBy(blockSection, data); break;
                case "/":
                case "divide":
                    returned = createScriptOperationDivide(blockSection, data); break;
                case "/=":
                case "divideby":
                    returned = createScriptOperationDivideBy(blockSection, data); break;
                case "%":
                case "modulus":
                    returned = createScriptOperationModulus(blockSection, data); break;
                case "%=":
                case "modulusof":
                    returned = createScriptOperationModulusOf(blockSection, data); break;
                case "**":
                case "power":
                    returned = createScriptOperationPower(blockSection, data); break;
                case "**=":
                case "powerof":
                    returned = createScriptOperationPowerOf(blockSection, data); break;
                // logic
                case "&&":
                case "and":
                    returned = createScriptOperationAnd(blockSection, data); break;
                case "||":
                case "or":
                    returned = createScriptOperationOr(blockSection, data); break;
                case "!":
                case "not":
                    returned = createScriptOperationNot(blockSection, data); break;
                // compare
                case "==":
                case "equal":
                    returned = createScriptOperationEqual(blockSection, data); break;
                case "!=":
                case "notequal":
                    returned = createScriptOperationNotEqual(blockSection, data); break;
                case ">":
                case "greaterthan":
                    returned = createScriptOperationGreaterThan(blockSection, data); break;
                case ">=":
                case "greaterthanorequalto":
                    returned = createScriptOperationGreaterThanOrEqualTo(blockSection, data); break;
                case "<":
                case "lessthan":
                    returned = createScriptOperationLessThan(blockSection, data); break;
                case "<=":
                case "lessthanorequalto":
                    returned = createScriptOperationLessThanOrEqualTo(blockSection, data); break;
                // cast
                case "(boolean)":
                case "toboolean":
                    returned = createScriptOperationToBoolean(blockSection, data); break;
                case "(double)":
                case "todouble":
                    returned = createScriptOperationToDouble(blockSection, data); break;
                case "(long)":
                case "tolong":
                    returned = createScriptOperationToLong(blockSection, data); break;
                case "(string)":
                case "tostring":
                    returned = createScriptOperationToString(blockSection, data); break;
                // functions
                case "contains":
                    returned = createScriptOperationContains(blockSection, data); break;
                case "length":
                    returned = createScriptOperationLength(blockSection, data); break;
                case "=":
                case "setvalue":
                    returned = createScriptOperationSetValue(blockSection, data); break;
                // flow
                case "if":
                    returned = createScriptOperationIf(blockSection, data); break;
                case "func":
                case "function":
                    returned = createScriptOperationFunction(blockSection, data); break;
                // action
                case "changestage":
                    returned = createScriptOperationChangeStage(blockSection, data); break;
                case "interact":
                    returned = createScriptOperationInteract(blockSection, data); break;
                case "consumable":
                    returned = createScriptOperationConsumable(blockSection, data); break;
                // random
                case "chance":
                    returned = createScriptOperationChance(blockSection, data); break;
                case "choice":
                    returned = createScriptOperationChoice(blockSection, data); break;
                case "randomdouble":
                    returned = createScriptOperationRandomDouble(blockSection, data); break;
                case "randomlong":
                    returned = createScriptOperationRandomLong(blockSection, data); break;
                // not recognized
                default:
                    main.getLogger().warning(String.format("PlantScript block of type '%s' not recognized; this " +
                            "PlantScript will not be loaded until this is fixed in blockSection: %s",
                            blockName, blockSection.getCurrentPath()));
                    return null;
            }
            if (returned != null) {
                return returned.optimizeSelf();
            }
            return null;
        }
        main.getLogger().warning(String.format("No PlantScript block defined in section: %s",
                section.getCurrentPath()));
        return null;
    }

    ScriptResult createPlantScriptResult(ConfigurationSection section, PlantData data) {
        ScriptResult scriptResult = null;
        if (section.isString("variable")) {
            // get variable from data
            String variableName = section.getString("variable");
            // if no plant data present or data does not contain variable name, return null
            if (data == null || !data.getData().containsKey(variableName)) {
                main.getLogger().warning(String.format("Variable '%s' not found, so this PlantScript will not be " +
                        "loaded until that is fixed in section: %s", variableName, section.getCurrentPath()));
                return null;
            }
            // get type from returned object
            ScriptType type = ScriptHelper.getType(data.getData().get(variableName));
            if (type == null) {
                main.getLogger().warning(String.format("Variable '%s' is stored with unrecognized type; something " +
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
                main.getLogger().warning(String.format("Value could not be parsed into recognized type; this " +
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
        main.getLogger().warning(String.format("No variable section or value provided; this PlantScript will not be " +
                "loaded until this is fixed in section: %s", section.getCurrentPath()));
        return null;
    }

    // types - helpful
    ScriptBlock createScriptOperationUnary(ConfigurationSection section, PlantData data) {
        if (!section.isSet("input")) {
            main.getLogger().warning("Input operand missing in section: " + section.getCurrentPath());
            return null;
        }
        return createPlantScript(section, "input", data);
    }

    ArrayList<ScriptBlock> createScriptOperationBinary(ConfigurationSection section, PlantData data) {
        if (!section.isSet("left") || !section.isSet("right")) {
            main.getLogger().warning("Left or right operand missing in section: " + section.getCurrentPath());
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

    // math
    ScriptOperation createScriptOperationAdd(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAdd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationAddTo(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAddTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtract(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtract(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationSubtractFrom(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSubtractFrom(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiply(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiply(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationMultiplyBy(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationMultiplyBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivide(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivide(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationDivideBy(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationDivideBy(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulus(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulus(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationModulusOf(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationModulusOf(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPower(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPower(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationPowerOf(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationPowerOf(operands.get(0), operands.get(1));
    }
    
    //logic
    ScriptOperation createScriptOperationAnd(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationAnd(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationOr(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationOr(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNot(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationNot(operand);
    }
    
    //compare
    ScriptOperation createScriptOperationEqual(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationNotEqual(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationNotEqual(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThan(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationGreaterThanOrEqualTo(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationGreaterThanOrEqualTo(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThan(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThan(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLessThanOrEqualTo(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationLessThanOrEqualTo(operands.get(0), operands.get(1));
    }

    // cast
    ScriptOperation createScriptOperationToBoolean(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToBoolean(operand);
    }
    ScriptOperation createScriptOperationToDouble(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToDouble(operand);
    }
    ScriptOperation createScriptOperationToLong(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToLong(operand);
    }
    ScriptOperation createScriptOperationToString(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationToString(operand);
    }

    // functions
    ScriptOperation createScriptOperationContains(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationContains(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationLength(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationLength(operand);
    }
    ScriptOperation createScriptOperationSetValue(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationSetValue(operands.get(0), operands.get(1));
    }

    //flow
    ScriptOperation createScriptOperationIf(ConfigurationSection section, PlantData data) {
        String conditionString = "condition";
        String ifTrueString = "if-true";
        String ifFalseString = "if-false";
        if (!section.isSet(conditionString) || !section.isSet(ifTrueString)) {
            main.getLogger().warning("Condition or if-true operand missing in section: " + section.getCurrentPath());
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
    ScriptOperation createScriptOperationFunction(ConfigurationSection section, PlantData data) {
        int index = 0;
        ScriptBlock[] scriptBlocks = new ScriptBlock[section.getKeys(false).size()];
        for (String placeholder : section.getKeys(false)) {
            if (!section.isSet(placeholder)) {
                main.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
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
            main.getLogger().warning("No subsections found to generate PlantScript Function in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationFunction(scriptBlocks);
    }

    //action
    ScriptOperation createScriptOperationInteract(ConfigurationSection section, PlantData data) {
        PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(section, data);
        if (plantInteractStorage == null) {
            main.getLogger().warning("Could not load interact section to generate PlantScript Interact in section: " +
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
    ScriptOperation createScriptOperationConsumable(ConfigurationSection section, PlantData data) {
        PlantConsumableStorage plantConsumableStorage = loadPlantConsumableStorage(section, data);
        if (plantConsumableStorage == null) {
            main.getLogger().warning("Could not load consumable section to generate PlantScript Consumable in " +
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
    ScriptOperation createScriptOperationChangeStage(ConfigurationSection section, PlantData data) {
        String stageString = "go-to-stage";
        String ifNextString = "go-to-next";
        if (!section.isSet(stageString) && !section.isSet(ifNextString)) {
            main.getLogger().warning("Go-to-stage and go-to-next both missing in section: " + section.getCurrentPath());
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
        return new ScriptOperationChangeStage(main, stage, ifNext);
    }

    //random
    ScriptOperation createScriptOperationChance(ConfigurationSection section, PlantData data) {
        ScriptBlock operand = createScriptOperationUnary(section, data);
        if (operand == null) {
            return null;
        }
        return new ScriptOperationChance(operand);
    }
    ScriptOperation createScriptOperationChoice(ConfigurationSection section, PlantData data) {
        int index = 0;
        ScriptBlock[] scriptBlocks = new ScriptBlock[section.getKeys(false).size()];
        for (String placeholder : section.getKeys(false)) {
            if (!section.isSet(placeholder)) {
                main.getLogger().warning(String.format("No subsection found to generate PlantScript for line '%s' in " +
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
            main.getLogger().warning("No subsections found to generate PlantScript Choice in section: " +
                    section.getCurrentPath());
            return null;
        }
        return new ScriptOperationChoice(scriptBlocks);
    }
    ScriptOperation createScriptOperationRandomDouble(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomDouble(operands.get(0), operands.get(1));
    }
    ScriptOperation createScriptOperationRandomLong(ConfigurationSection section, PlantData data) {
        ArrayList<ScriptBlock> operands = createScriptOperationBinary(section, data);
        if (operands == null) {
            return null;
        }
        return new ScriptOperationRandomLong(operands.get(0), operands.get(1));
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
