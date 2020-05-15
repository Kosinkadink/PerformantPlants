package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.effects.*;
import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.*;
import me.kosinkadink.performantplants.settings.*;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.util.EnchantmentLevel;
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
        PlantConsumableStorage consumable = loadPlantConsumableStorage(itemConfig);
        if (consumable != null) {
            plantItem.setConsumableStorage(consumable);
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
                    // set water/lava requirements
                    plant.setWaterRequired(growingConfig.isBoolean("water-required") && growingConfig.getBoolean("water-required"));
                    plant.setLavaRequired(growingConfig.isBoolean("lava-required") && growingConfig.getBoolean("lava-required"));
                    // set growth bounds for general growth (overridable by stage-specific min/max growth time)
                    if (!growingConfig.isInt("min-growth-time") || !growingConfig.isInt("max-growth-time")) {
                        main.getLogger().warning("Growth time bounds not set/integer for growing for plant: " + plantId);
                        return;
                    }
                    plant.setMinGrowthTime(growingConfig.getLong("min-growth-time"));
                    plant.setMaxGrowthTime(growingConfig.getLong("max-growth-time"));
                    // get required blocks, if any
                    if (growingConfig.isSet("required-blocks") && growingConfig.isConfigurationSection("required-blocks")) {
                        ConfigurationSection requiredBlocks = growingConfig.getConfigurationSection("required-blocks");
                        for (String blockName : requiredBlocks.getKeys(false)) {
                            ConfigurationSection requiredBlockSection = requiredBlocks.getConfigurationSection(blockName);
                            if (requiredBlockSection == null) {
                                main.getLogger().warning("config section was null for required block for plant: " + plantId);
                                return;
                            }
                            BlockSettings blockSettings = loadBlockConfig(requiredBlockSection);
                            if (blockSettings == null) {
                                main.getLogger().warning("blockSettings for required block returned null for plant: " + plantId);
                                return;
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
                                main.getLogger().warning(String.format("Could not create required block in plant %s due to: %s", plantId, e.getMessage()));
                                return;
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
                            plant.addRequiredBlockToGrow(requiredBlock);
                        }
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
                            // set drop limit, if present
                            if (stageConfig.isInt("drop-limit")) {
                                int stageDropLimit = stageConfig.getInt("drop-limit");
                                growthStage.setDropLimit(stageDropLimit);
                            }
                            // set drops, if present
                            if (stageConfig.isSet("drops")) {
                                // add drops
                                boolean valid = addDropsToDroppable(stageConfig, growthStage);
                                if (!valid) {
                                    return;
                                }
                            }
                            // set growth checkpoint, if present
                            if (stageConfig.isBoolean("growth-checkpoint")) {
                                growthStage.setGrowthCheckpoint(stageConfig.getBoolean("growth-checkpoint"));
                            }
                            // set on-execute, if present
                            if (stageConfig.isConfigurationSection("on-execute")) {
                                ConfigurationSection onExecuteSection = stageConfig.getConfigurationSection("on-execute");
                                // load interaction from default path
                                PlantInteract onExecute = loadPlantInteract(onExecuteSection);
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
                                PlantInteract onFail = loadPlantInteract(onFailSection);
                                if (onFail == null) {
                                    main.getLogger().warning(String.format("Stage %s's on-fail could not be loaded from section: %s", stageId, onFailSection.getCurrentPath()));
                                    return;
                                }
                                growthStage.setOnFail(onFail);
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
                                // set drop limit, if present
                                if (blockConfig.isInt("drop-limit")) {
                                    growthStageBlock.setDropLimit(blockConfig.getInt("drop-limit"));
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
                                    boolean valid = addDropsToDroppable(blockConfig, growthStageBlock);
                                    if (!valid) {
                                        return;
                                    }
                                }
                                // set interact behavior, if present
                                if (blockConfig.isConfigurationSection("on-interact")) {
                                    ConfigurationSection onInteractSection = blockConfig.getConfigurationSection("on-interact");
                                    PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onInteractSection);
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
                                    PlantInteractStorage plantInteractStorage = loadPlantInteractStorage(onBreakSection);
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
                    PlantConsumableStorage goodConsumable = loadPlantConsumableStorage(goodSection);
                    if (goodConsumable != null) {
                        goodItem.setConsumableStorage(goodConsumable);
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
                addBlockDrop(dropSection);
            } else if (type.equalsIgnoreCase("entity")) {
                addEntityDrop(dropSection);
            }
        }
    }

    void addBlockDrop(ConfigurationSection section) {
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

    void addEntityDrop(ConfigurationSection section) {
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
            // get enchantments -> Enchantment:Level
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
            // get potion effects -> PotionEffectType:Duration:Amplifier
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
                    Color potionColor = createColor(colorSection);
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
        PlantInteractStorage plantInteractStorage = new PlantInteractStorage();
        // add default interaction, if present
        if (section.isConfigurationSection("default")) {
            ConfigurationSection defaultInteractSection = section.getConfigurationSection("default");
            // load interaction from default path
            PlantInteract plantInteract = loadPlantInteract(defaultInteractSection);
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
                PlantInteract plantInteract = loadPlantInteract(itemInteractSection);
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

    PlantInteract loadPlantInteract(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        PlantInteract plantInteract = new PlantInteract();
        // set if block should break on interact, if present
        if (section.isBoolean("break-block")) {
            plantInteract.setBreakBlock(section.getBoolean("break-block"));
        }
        // set if interaction should give block drops, if present
        if (section.isBoolean("give-block-drops")) {
            plantInteract.setGiveBlockDrops(section.getBoolean("give-block-drops"));
        }
        // set if should take item, if present
        if (section.isBoolean("take-item")) {
            plantInteract.setTakeItem(section.getBoolean("take-item"));
        }
        // set if should go to next (overridden by go to stage), if present
        if (section.isBoolean("go-to-next")) {
            plantInteract.setGoToNext(section.getBoolean("go-to-next"));
        }
        // set go to stage, if present
        if (section.isString("go-to-stage")) {
            plantInteract.setGoToStage(section.getString("go-to-stage"));
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
        // set chance of stage advancement, if present
        if (section.isDouble("chance") || section.isInt("chance")) {
            double chance = section.getDouble("chance");
            if (chance <= 0.0 || chance > 100.0) {
                main.getLogger().warning("chance was not greater than 0.0 and less/equal to 100.0 for interact section: " + section.getCurrentPath());
                return null;
            }
            plantInteract.setChance(chance);
        }
        // set if effects should only happen on successful chance, if present
        if (section.isBoolean("only-effects-on-chance")) {
            plantInteract.setOnlyEffectsOnChance(section.getBoolean("only-effects-on-chance"));
        }
        // add drops, if present
        if (section.isConfigurationSection("drops")) {
            DropStorage dropStorage = new DropStorage();
            boolean valid = addDropsToDroppable(section, dropStorage);
            if (!valid) {
                return null;
            }
            plantInteract.setDropStorage(dropStorage);
        }
        // add effects, if present
        addEffectsToEffectStorage(section, plantInteract.getEffectStorage());
        // add consumable, if present
        PlantConsumableStorage consumable = loadPlantConsumableStorage(section);
        if (consumable != null) {
            plantInteract.setConsumableStorage(consumable);
        }
        return plantInteract;
    }

    PlantConsumableStorage loadPlantConsumableStorage(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        if (!section.isConfigurationSection("consumable")) {
            return null;
        }
        ConfigurationSection consumableStorageSection = section.getConfigurationSection("consumable");
        PlantConsumableStorage consumableStorage = new PlantConsumableStorage();
        for (String placeholder : consumableStorageSection.getKeys(false)) {
            if (consumableStorageSection.isConfigurationSection(placeholder)) {
                PlantConsumable consumable = loadPlantConsumable(consumableStorageSection.getConfigurationSection(placeholder));
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

    PlantConsumable loadPlantConsumable(ConfigurationSection section) {
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
        addEffectsToEffectStorage(section, consumable.getEffectStorage());
        // return consumable
        return consumable;
    }

    Color createColor(ConfigurationSection section) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (section.isInt("r")) {
            r = Math.min(255, Math.max(0, section.getInt("r")));
        }
        if (section.isInt("g")) {
            g = Math.min(255, Math.max(0, section.getInt("g")));
        }
        if (section.isInt("b")) {
            b = Math.min(255, Math.max(0, section.getInt("b")));
        }
        return Color.fromRGB(r,g,b);
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

    boolean addDropsToDroppable(ConfigurationSection section, Droppable droppable) {
        // iterate through drops
        if (!section.isConfigurationSection("drops")) {
            main.getLogger().warning("No drops provided in section: " + section.getCurrentPath());
            return false;
        }
        // add drop limit, if present
        if (section.isInt("drop-limit")) {
            droppable.setDropLimit(section.getInt("drop-limit"));
        }
        ConfigurationSection dropsConfig = section.getConfigurationSection("drops");
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
            droppable.addDrop(drop);
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

    void addEffectsToEffectStorage(ConfigurationSection section, PlantEffectStorage effectStorage) {
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
                addEffect(effectSection, effectStorage);
            }
        }
        // set effect limit, if present
        if (section.isInt("effect-limit")) {
            effectStorage.setEffectLimit(section.getInt("effect-limit"));
        }
    }

    //region Add Effects

    boolean addEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        String type = section.getString("type");
        if (type == null) {
            main.getLogger().warning("Type not set for effect at: " + section.getCurrentPath());
            return false;
        }
        PlantEffect effect = null;
        switch (type.toLowerCase()) {
            case "feed":
                effect = createFeedEffect(section);
                break;
            case "heal":
                effect = createHealEffect(section);
                break;
            case "sound":
                effect = createSoundEffect(section);
                break;
            case "particle":
                effect = createParticleEffect(section);
                break;
            case "potion":
                effect = createPotionEffect(section);
                break;
            case "drop":
                effect = createDropEffect(section);
                break;
            case "air":
                effect = createAirEffect(section);
                break;
            case "area":
                effect = createAreaEffect(section);
                break;
            case "durability":
                effect = createDurabilityEffect(section);
                break;
            case "chat":
                effect = createChatEffect(section);
                break;
            case "explosion":
                effect = createExplosionEffect(section);
                break;
            case "command":
                effect = createCommandEffect(section);
                break;
            default:
                break;
        }
        if (effect != null) {
            addChanceAndDelayToEffect(section, effect);
            effectStorage.addEffect(effect);
            return true;
        }
        main.getLogger().warning(String.format("Effect %s not recognized; not added to effect storage for section: %s",
                type, section.getCurrentPath()));
        return false;
    }

    PlantFeedEffect createFeedEffect(ConfigurationSection section) {
        PlantFeedEffect effect = new PlantFeedEffect();
        // set food amount, if present
        if (section.isInt("food-amount")) {
            effect.setFoodAmount(section.getInt("food-amount"));
        }
        // set saturation amount, if present
        if (section.isDouble("saturate-amount") || section.isInt("saturate-amount")) {
            effect.setSaturationAmount((float)section.getDouble("saturate-amount"));
        }
        return effect;
    }

    PlantHealEffect createHealEffect(ConfigurationSection section) {
        PlantHealEffect effect = new PlantHealEffect();
        // set heal amount, if set
        if (section.isDouble("heal-amount") || section.isInt("heal-amount")) {
            effect.setHealAmount(section.getDouble("heal-amount"));
        }
        return effect;
    }

    PlantSoundEffect createSoundEffect(ConfigurationSection section) {
        // set sound
        PlantSoundEffect effect = new PlantSoundEffect();
        if (!section.isString("sound")) {
            main.getLogger().warning("Sound effect not added; sound field not found in section: " + section.getCurrentPath());
            return null;
        }
        String name = section.getString("sound");
        if (name == null) {
            main.getLogger().warning("Sound effect not added; sound field was null in section: " + section.getCurrentPath());
            return null;
        }
        Sound sound;
        try {
            sound = Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("Sound effect not added; sound '%s' not recognized", name));
            return null;
        }
        effect.setSound(sound);
        // set volume, if present
        if (section.isDouble("volume") || section.isInt("volume")) {
            effect.setVolume((float)section.getDouble("volume"));
        }
        // set pitch, if present
        if (section.isDouble("pitch") || section.isInt("pitch")) {
            effect.setPitch((float)section.getDouble("pitch"));
        }
        // set offsets, if present
        if (section.isInt("offset-x") || section.isDouble("offset-x")) {
            effect.setOffsetX(section.getDouble("offset-x"));
        }
        if (section.isInt("offset-y") || section.isDouble("offset-y")) {
            effect.setOffsetY(section.getDouble("offset-y"));
        }
        if (section.isInt("offset-z") || section.isDouble("offset-z")) {
            effect.setOffsetZ(section.getDouble("offset-z"));
        }
        // set multiplier, if present
        if (section.isInt("multiplier") || section.isDouble("multiplier")) {
            effect.setMultiplier(section.getDouble("multiplier"));
        }
        // set if should ignore y component of facing direction
        if (section.isBoolean("ignore-direction-y")) {
            effect.setIgnoreDirectionY(section.getBoolean("ignore-direction-y"));
        }
        // set if should be eye location, if present
        if (section.isBoolean("eye-location")) {
            effect.setEyeLocation(section.getBoolean("eye-location"));
        }
        // set client-side, if present
        if (section.isBoolean("client-side")) {
            effect.setClientSide(section.getBoolean("client-side"));
        }
        return effect;
    }

    PlantParticleEffect createParticleEffect(ConfigurationSection section) {
        // set particle
        PlantParticleEffect effect = new PlantParticleEffect();
        if (!section.isString("particle")) {
            main.getLogger().warning("Particle effect not added; particle field not found in section: " + section.getCurrentPath());
            return null;
        }
        String name = section.getString("particle");
        if (name == null) {
            main.getLogger().warning("Particle effect not added; particle field was null in section: " + section.getCurrentPath());
            return null;
        }
        Particle particle;
        try {
            particle = Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("Particle effect not added; particle '%s' not recognized", name));
            return null;
        }
        effect.setParticle(particle);
        // set count, if present
        if (section.isInt("count")) {
            effect.setCount(section.getInt("count"));
        }
        // set offsets, if present
        if (section.isInt("offset-x") || section.isDouble("offset-x")) {
            effect.setOffsetX(section.getDouble("offset-x"));
        }
        if (section.isInt("offset-y") || section.isDouble("offset-y")) {
            effect.setOffsetY(section.getDouble("offset-y"));
        }
        if (section.isInt("offset-z") || section.isDouble("offset-z")) {
            effect.setOffsetZ(section.getDouble("offset-z"));
        }
        // set data offsets, if present
        if (section.isInt("data-offset-x") || section.isDouble("data-offset-x")) {
            effect.setDataOffsetX(section.getDouble("data-offset-x"));
        }
        if (section.isInt("data-offset-y") || section.isDouble("data-offset-y")) {
            effect.setDataOffsetY(section.getDouble("data-offset-y"));
        }
        if (section.isInt("data-offset-z") || section.isDouble("data-offset-z")) {
            effect.setDataOffsetZ(section.getDouble("data-offset-z"));
        }
        // set multiplier, if present
        if (section.isInt("multiplier") || section.isDouble("multiplier")) {
            effect.setMultiplier(section.getDouble("multiplier"));
        }
        // set extra, if present
        if (section.isInt("extra") || section.isDouble("extra")) {
            effect.setExtra(section.getDouble("extra"));
        }
        // set if should be eye location, if present
        if (section.isBoolean("eye-location")) {
            effect.setEyeLocation(section.getBoolean("eye-location"));
        }
        // set if should ignore y component of facing direction
        if (section.isBoolean("ignore-direction-y")) {
            effect.setIgnoreDirectionY(section.getBoolean("ignore-direction-y"));
        }
        // set client-side, if present
        if (section.isBoolean("client-side")) {
            effect.setClientSide(section.getBoolean("client-side"));
        }
        return effect;
    }

    PlantPotionEffect createPotionEffect(ConfigurationSection section) {
        // set potion effect type
        PlantPotionEffect effect = new PlantPotionEffect();
        if (!section.isString("potion")) {
            main.getLogger().warning("Potion effect not added; potion field not found in section: " + section.getCurrentPath());
            return null;
        }
        String potionName = section.getString("potion");
        if (potionName == null) {
            main.getLogger().warning("Potion effect not added; potion field was null in section: " + section.getCurrentPath());
            return null;
        }
        PotionEffectType potionEffectType = PotionEffectType.getByName(potionName.toUpperCase());
        if (potionEffectType == null) {
            main.getLogger().warning(String.format("Potion effect not added; potion '%s' not recognized", potionName));
            return null;
        }
        effect.setPotionEffectType(potionEffectType);
        // set duration, if present
        if (section.isInt("duration")) {
            effect.setDuration(section.getInt("duration"));
        }
        // set amplifier, if present
        if (section.isInt("amplifier")) {
            effect.setAmplifier(section.getInt("amplifier")-1);
        }
        // set ambient, if present
        if (section.isBoolean("ambient")) {
            effect.setAmbient(section.getBoolean("ambient"));
        }
        // set particles, if present
        if (section.isBoolean("particles")) {
            effect.setParticles(section.getBoolean("particles"));
        }
        // set icon
        if (section.isBoolean("icon")) {
            effect.setIcon(section.getBoolean("icon"));
        }
        return effect;
    }

    PlantDropEffect createDropEffect(ConfigurationSection section) {
        PlantDropEffect effect = new PlantDropEffect();
        boolean added = addDropsToDroppable(section, effect.getDropStorage());
        if (!added) {
            main.getLogger().warning("Drop effect not added; issue getting drops");
            return null;
        }
        return effect;
    }

    PlantAirEffect createAirEffect(ConfigurationSection section) {
        PlantAirEffect effect = new PlantAirEffect();
        if (!section.isInt("amount")) {
            main.getLogger().warning("Air effect not added; no amount provided in section: " + section.getCurrentPath());
            return null;
        }
        effect.setAmount(section.getInt("amount"));
        return effect;
    }

    PlantAreaEffect createAreaEffect(ConfigurationSection section) {
        PlantAreaEffect effect = new PlantAreaEffect();
        if (section.isConfigurationSection("color")) {
            ConfigurationSection colorSection = section.getConfigurationSection("color");
            if (colorSection != null) {
                effect.setColor(createColor(colorSection));
            }
        }
        if (section.isInt("duration")) {
            effect.setDuration(section.getInt("duration"));
        }
        if (section.isInt("duration-on-use")) {
            effect.setDurationOnUse(section.getInt("duration-on-use"));
        }
        if (section.isString("particle")) {
            String name = section.getString("particle");
            try {
                Particle particle = Particle.valueOf(name.toUpperCase());
                effect.setParticle(particle);
            } catch (IllegalArgumentException e) {
                main.getLogger().warning(String.format("Area effect will not have chosen particle '%s'; not recognized in section %s",
                        name, section.getCurrentPath()));
            }
        }
        if (section.isInt("radius") || section.isDouble("radius")) {
            effect.setRadius((float) section.getDouble("radius"));
        }
        if (section.isInt("radius-on-use") || section.isDouble("radius-on-use")) {
            effect.setRadiusOnUse((float) section.getDouble("radius-on-use"));
        }
        if (section.isInt("radius-per-tick") || section.isDouble("radius-per-tick")) {
            effect.setRadiusPerTick((float) section.getDouble("radius-per-tick"));
        }
        if (section.isInt("reapplication-delay")) {
            effect.setReapplicationDelay(section.getInt("reapplication-delay"));
        }
        return effect;
    }

    PlantDurabilityEffect createDurabilityEffect(ConfigurationSection section) {
        PlantDurabilityEffect effect = new PlantDurabilityEffect();
        if (!section.isInt("damage-amount")) {
            main.getLogger().warning("Durability effect not added; no damage-amount provided in section: " + section.getCurrentPath());
            return null;
        }
        effect.setAmount(section.getInt("damage-amount"));
        return effect;
    }

    PlantChatEffect createChatEffect(ConfigurationSection section) {
        PlantChatEffect effect = new PlantChatEffect();
        if (section.isString("from-player")) {
            effect.setFromPlayer(section.getString("from-player"));
        }
        if (section.isString("to-player")) {
            effect.setToPlayer(section.getString("to-player"));
        }
        if (effect.getFromPlayer().isEmpty() && effect.getToPlayer().isEmpty()) {
            main.getLogger().warning("Chat effect not added; from-player and to-player messages both not set in section: " + section.getCurrentPath());
            return null;
        }
        return effect;
    }

    PlantExplosionEffect createExplosionEffect(ConfigurationSection section) {
        PlantExplosionEffect effect = new PlantExplosionEffect();
        if (section.isInt("power") || section.isDouble("power")) {
            effect.setPower((float) section.getDouble("power"));
        }
        if (section.isBoolean("fire")) {
            effect.setFire(section.getBoolean("fire"));
        }
        if (section.isBoolean("break-blocks")) {
            effect.setBreakBlocks(section.getBoolean("break-blocks"));
        }
        return effect;
    }

    PlantCommandEffect createCommandEffect(ConfigurationSection section) {
        PlantCommandEffect effect = new PlantCommandEffect();
        String command = section.getString("command");
        if (command == null || command.isEmpty()) {
            main.getLogger().warning("Command effect not added; command string not present or empty in section: " + section.getCurrentPath());
            return null;
        }
        effect.setCommand(command);
        if (section.isBoolean("console")) {
            effect.setConsole(section.getBoolean("console"));
        }
        return effect;
    }

    void addChanceAndDelayToEffect(ConfigurationSection section, PlantEffect effect) {
        // set chance, if present
        if (section.isDouble("chance") || section.isInt("chance")) {
            effect.setChance(section.getDouble("chance"));
        }
        // set delay, if present
        if (section.isInt("delay")) {
            effect.setDelay(section.getInt("delay"));
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

    String getFileNameWithoutExtension(File file) {
        String fileName = "";
        if (file != null && file.exists()) {
            fileName = file.getName().replaceFirst("[.].+$", "");
        }
        return fileName;
    }

}
