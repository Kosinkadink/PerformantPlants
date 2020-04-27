package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.effects.*;
import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.*;
import me.kosinkadink.performantplants.settings.*;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffectType;

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
        File[] plantFiles = plantsDir.listFiles();
        if (plantFiles == null || plantFiles.length == 0) {
            // if plantFiles is null or of length zero, add default plants?
            // TODO: add default plants
            return;
        }
        // otherwise read all of files
        for (File file : plantFiles) {
            main.getLogger().info("Loading plant config from file: " + file.getName());
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
        PlantItem plantItem = new PlantItem(ItemHelper.fromItemSettings(itemSettings, plantId));
        // set buy/sell prices
        addPricesToPlantItem(itemConfig, plantItem);
        // add consumable behavior
        PlantConsumable consumable = loadPlantConsumable(itemConfig);
        if (consumable != null) {
            plantItem.setConsumable(consumable);
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
                        PlantItem seedItem = new PlantItem(ItemHelper.fromItemSettings(seedItemSettings, plantId, true));
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
                    plant.setMinGrowthTime(growingConfig.getInt("min-growth-time"));
                    plant.setMaxGrowthTime(growingConfig.getInt("max-growth-time"));
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
                            RequiredBlock requiredBlock = new RequiredBlock(
                                    blockSettings.getXRel(),
                                    blockSettings.getYRel(),
                                    blockSettings.getZRel(),
                                    blockSettings.getMaterial(),
                                    blockSettings.getBlockDataStrings());
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
                                int stageMinGrowthTime = stageConfig.getInt("min-growth-time");
                                int stageMaxGrowthTime = stageConfig.getInt("max-growth-time");
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
                                GrowthStageBlock growthStageBlock = new GrowthStageBlock(
                                        blockName,
                                        blockSettings.getXRel(),
                                        blockSettings.getYRel(),
                                        blockSettings.getZRel(),
                                        blockSettings.getMaterial(),
                                        blockSettings.getBlockDataStrings(),
                                        blockSettings.getSkullTexture()
                                );
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
                                    PlantInteractStorage plantInteractStorage = new PlantInteractStorage();
                                    // add default interaction, if present
                                    if (onInteractSection.isConfigurationSection("default")) {
                                        ConfigurationSection defaultInteractSection = onInteractSection.getConfigurationSection("default");
                                        // load interaction from default path
                                        PlantInteract plantInteract = loadPlantInteract(defaultInteractSection);
                                        if (plantInteract == null) {
                                            main.getLogger().warning("Default on-interact could not be loaded from section: " + defaultInteractSection.getCurrentPath());
                                            return;
                                        }
                                        plantInteractStorage.setDefaultInteract(plantInteract);
                                    }
                                    // add item interactions, if present
                                    if (onInteractSection.isConfigurationSection("items")) {
                                        ConfigurationSection itemsInteractSection = onInteractSection.getConfigurationSection("items");
                                        for (String placeholder : itemsInteractSection.getKeys(false)) {
                                            ConfigurationSection itemInteractSection = itemsInteractSection.getConfigurationSection(placeholder);
                                            if (itemInteractSection == null) {
                                                main.getLogger().warning("Item on-interact section was null from section: " + itemsInteractSection.getCurrentPath());
                                                return;
                                            }
                                            ConfigurationSection itemSection = itemInteractSection.getConfigurationSection("item");
                                            if (itemSection == null) {
                                                main.getLogger().warning("Item section not present in on-interact section from section: " + itemInteractSection.getCurrentPath());
                                                return;
                                            }
                                            ItemSettings itemInteractSettings = loadItemConfig(itemSection, true);
                                            if (itemInteractSettings == null) {
                                                return;
                                            }
                                            PlantInteract plantInteract = loadPlantInteract(itemInteractSection);
                                            if (plantInteract == null) {
                                                main.getLogger().warning("Item on-interact could not be loaded from section: " + itemInteractSection.getCurrentPath());
                                                return;
                                            }
                                            // set interact's item stack
                                            plantInteract.setItemStack(itemInteractSettings.generateItemStack());
                                            plantInteractStorage.addPlantInteract(plantInteract);
                                        }
                                    }
                                    // add interactions to growth stage block
                                    growthStageBlock.setOnInteract(plantInteractStorage);
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
                    PlantItem goodItem = new PlantItem(ItemHelper.fromItemSettings(goodSettings, goodId, false));
                    addPricesToPlantItem(goodSection, goodItem);
                    PlantConsumable goodConsumable = loadPlantConsumable(goodSection);
                    if (goodConsumable != null) {
                        goodItem.setConsumable(goodConsumable);
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

            if (linkedItemSettings == null) {
                return new ItemSettings(material, displayName, lore, skullTexture, amount);
            } else {
                linkedItemSettings.setAmount(amount);
                return new ItemSettings(ItemHelper.fromItemSettings(linkedItemSettings, plantId));
            }
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
        PlantConsumable consumable = loadPlantConsumable(section);
        if (consumable != null) {
            plantInteract.setConsumable(consumable);
        }
        return plantInteract;
    }

    PlantConsumable loadPlantConsumable(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        if (!section.isConfigurationSection("consumable")) {
            return null;
        }
        ConfigurationSection consumableSection = section.getConfigurationSection("consumable");
        PlantConsumable consumable = new PlantConsumable();
        // set take item, if present
        if (consumableSection.isBoolean("take-item")) {
            consumable.setTakeItem(consumableSection.getBoolean("take-item"));
        }
        // set missing food, if present
        if (consumableSection.isBoolean("missing-food")) {
            consumable.setMissingFood(consumableSection.getBoolean("missing-food"));
        }
        // set normal eat, if present
        if (consumableSection.isBoolean("normal-eat")) {
            consumable.setNormalEat(consumableSection.getBoolean("normal-eat"));
        }
        // set damage to add to item, if present
        if (consumableSection.isInt("add-damage")) {
            consumable.setAddDamage(consumableSection.getInt("add-damage"));
        }
        // set give item, if present
        if (consumableSection.isConfigurationSection("give-item")) {
            ConfigurationSection itemSection = consumableSection.getConfigurationSection("add-item");
            ItemSettings itemSettings = loadItemConfig(itemSection, true);
            if (itemSettings == null) {
                main.getLogger().warning(String.format("Problem getting add-item in consumable section %s;" +
                                "will continue to load, but this item will not be consumable (fix config)",
                        itemSection.getCurrentPath()));
                return null;
            }
            consumable.setItemToAdd(itemSettings.generateItemStack());
        }
        // set required items, if present
        if (consumableSection.isConfigurationSection("required-items")) {
            ConfigurationSection requiredItemsSection = consumableSection.getConfigurationSection("required-items");
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
        addEffectsToEffectStorage(consumableSection, consumable.getEffectStorage());
        // return consumable
        return consumable;
    }

    boolean addDropsToDroppable(ConfigurationSection section, Droppable droppable) {
        // iterate through drops
        if (!section.isConfigurationSection("drops")) {
            main.getLogger().warning("No drops provided for growth for plant: " + section.getCurrentPath());
            return false;
        }
        ConfigurationSection dropsConfig = section.getConfigurationSection("drops");
        for (String dropName : dropsConfig.getKeys(false)) {
            ConfigurationSection dropConfig = dropsConfig.getConfigurationSection(dropName);
            if (dropConfig == null) {
                main.getLogger().warning("dropConfig was null for growth for plant: " + section.getCurrentPath());
                return false;
            }
            // get drop settings
            DropSettings dropSettings = loadDropConfig(dropConfig);
            if (dropSettings == null) {
                main.getLogger().warning("dropSettings were null for growth for plant: " + section.getCurrentPath());
                return false;
            }
            ItemSettings dropItemSettings = dropSettings.getItemSettings();
            ItemStack dropItemStack;
            // if no item stack, then generate one from settings
            if (dropItemSettings.getItemStack() == null) {
                dropItemStack = new ItemBuilder(dropItemSettings.getMaterial())
                        .lore(dropItemSettings.getLore())
                        .displayName(dropItemSettings.getDisplayName())
                        .build();
            } else {
                // otherwise use provided item stack
                dropItemStack = dropItemSettings.getItemStack();
            }
            Drop drop = new Drop(
                    dropItemStack,
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
        if (type.equalsIgnoreCase("feed")) {
            return addFeedEffect(section, effectStorage);
        }
        if (type.equalsIgnoreCase("heal")) {
            return addHealEffect(section, effectStorage);
        }
        if (type.equalsIgnoreCase("sound")) {
            return addSoundEffect(section, effectStorage);
        }
        if (type.equalsIgnoreCase("particle")) {
            return addParticleEffect(section, effectStorage);
        }
        if (type.equalsIgnoreCase("potion")) {
            return addPotionEffect(section, effectStorage);
        }
        main.getLogger().warning(String.format("Effect %s not recognized; not added to effect storage for section: %s",
                type, section.getCurrentPath()));
        return false;
    }

    boolean addFeedEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        PlantFeedEffect effect = new PlantFeedEffect();
        // set food amount, if present
        if (section.isInt("food-amount")) {
            effect.setFoodAmount(section.getInt("food-amount"));
        }
        // set saturation amount, if present
        if (section.isDouble("saturate-amount") || section.isInt("saturate-amount")) {
            effect.setSaturationAmount((float)section.getDouble("saturate-amount"));
        }
        addChanceAndDelayToEffect(section, effect);
        effectStorage.addEffect(effect);
        return true;
    }

    boolean addHealEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        PlantHealEffect effect = new PlantHealEffect();
        // set heal amount, if set
        if (section.isDouble("heal-amount") || section.isInt("heal-amount")) {
            effect.setHealAmount(section.getDouble("heal-amount"));
        }
        addChanceAndDelayToEffect(section, effect);
        effectStorage.addEffect(effect);
        return true;
    }

    boolean addSoundEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        // set sound
        PlantSoundEffect effect = new PlantSoundEffect();
        if (!section.isString("sound")) {
            main.getLogger().warning("Sound effect not added; sound field not found in section: " + section.getCurrentPath());
            return false;
        }
        String name = section.getString("sound");
        if (name == null) {
            main.getLogger().warning("Sound effect not added; sound field was null in section: " + section.getCurrentPath());
            return false;
        }
        Sound sound;
        try {
            sound = Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("Sound effect not added; sound '%s' not recognized", name));
            return false;
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
        // set if should be eye location, if present
        if (section.isBoolean("eye-location")) {
            effect.setEyeLocation(section.getBoolean("eye-location"));
        }
        // set client-side, if present
        if (section.isBoolean("client-side")) {
            effect.setClientSide(section.getBoolean("client-side"));
        }
        addChanceAndDelayToEffect(section, effect);
        effectStorage.addEffect(effect);
        return true;
    }

    boolean addParticleEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        // set particle
        PlantParticleEffect effect = new PlantParticleEffect();
        if (!section.isString("particle")) {
            main.getLogger().warning("Particle effect not added; particle field not found in section: " + section.getCurrentPath());
            return false;
        }
        String name = section.getString("particle");
        if (name == null) {
            main.getLogger().warning("Particle effect not added; particle field was null in section: " + section.getCurrentPath());
            return false;
        }
        Particle particle;
        try {
            particle = Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            main.getLogger().warning(String.format("Particle effect not added; particle '%s' not recognized", name));
            return false;
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
        addChanceAndDelayToEffect(section, effect);
        effectStorage.addEffect(effect);
        return true;
    }

    boolean addPotionEffect(ConfigurationSection section, PlantEffectStorage effectStorage) {
        // set potion effect type
        PlantPotionEffect effect = new PlantPotionEffect();
        if (!section.isString("potion")) {
            main.getLogger().warning("Potion effect not added; potion field not found in section: " + section.getCurrentPath());
            return false;
        }
        String potionName = section.getString("potion");
        if (potionName == null) {
            main.getLogger().warning("Potion effect not added; potion field was null in section: " + section.getCurrentPath());
            return false;
        }
        PotionEffectType potionEffectType = PotionEffectType.getByName(potionName.toUpperCase());
        if (potionEffectType == null) {
            main.getLogger().warning(String.format("Potion effect not added; potion '%s' not recognized", potionName));
            return false;
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
        // add chance + delay and store in effect storage
        addChanceAndDelayToEffect(section, effect);
        effectStorage.addEffect(effect);
        return true;
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
                main.getRecipeManager().addRecipe(recipe);
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
                main.getRecipeManager().addRecipe(recipe);
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
