package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.RequiredBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.settings.BlockSettings;
import me.kosinkadink.performantplants.settings.ConfigSettings;
import me.kosinkadink.performantplants.settings.DropSettings;
import me.kosinkadink.performantplants.settings.ItemSettings;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.TextHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

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
            addPricesToPlantItem(plantConfig, plantItem);
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
                            addPricesToPlantItem(growingConfig, seedItem);
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
                                BlockSettings blockSettings = loadBlockConfig(requiredBlocks.getConfigurationSection(blockName));
                                if (blockSettings == null) {
                                    main.getLogger().warning("blockSettings for required block returned null for plant: " + plantId);
                                    return;
                                }
                                RequiredBlock requiredBlock = new RequiredBlock(
                                        blockSettings.getXRel(),
                                        blockSettings.getYRel(),
                                        blockSettings.getZRel(),
                                        blockSettings.getMaterial(),
                                        blockSettings.getBlockDataStrings(),
                                        blockSettings.isRequired());
                                plant.addRequiredBlockToGrow(requiredBlock);
                            }
                        }
                        // get growth stages; since seed is present, growth stages are REQUIRED
                        if (!growingConfig.isSet("stages") && growingConfig.isConfigurationSection("stages")) {
                            main.getLogger().warning("No stages are configured for plant while seed is present: " + plantId);
                            return;
                        } else {
                            ConfigurationSection stagesConfig = growingConfig.getConfigurationSection("stages");
                            for (String stageName : stagesConfig.getKeys(false)) {
                                ConfigurationSection stageConfig = stagesConfig.getConfigurationSection(stageName);
                                if (stageConfig == null) {
                                    main.getLogger().warning("Could not load stageConfig for plant: " + plantId);
                                    return;
                                }
                                GrowthStage growthStage = new GrowthStage();
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
                                    boolean valid = addDropsToDroppable(stageConfig, growthStage, plant);
                                    if (!valid) {
                                        return;
                                    }
                                }
                                // set blocks for growth
                                if (!stageConfig.isConfigurationSection("blocks")) {
                                    main.getLogger().warning("No blocks provided for growth stage in plant: " + plantId);
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
                                        boolean valid = addDropsToDroppable(blockConfig, growthStageBlock, plant);
                                        if (!valid) {
                                            return;
                                        }
                                    }
                                    growthStage.addGrowthStageBlock(growthStageBlock);
                                }
                                plant.addGrowthStage(growthStage);
                            }
                        }
                    }
                }
                else {
                    main.getLogger().info("seedConfig was null for plant: " + plantId);
                }
            }
            // load crafting recipes; failure here shouldn't abort plant loading
            if (plantConfig.isConfigurationSection("crafting-recipes")) {
                ConfigurationSection craftingSection = plantConfig.getConfigurationSection("crafting-recipes");
                for (String recipeName : craftingSection.getKeys(false)) {
                    ConfigurationSection recipeSection = craftingSection.getConfigurationSection(recipeName);
                    addCraftingRecipe(recipeSection, recipeName);
                }
            }
            // load furnace recipes; failure here shouldn't abort plant loading
            if (plantConfig.isConfigurationSection("furnace-recipes")) {
                ConfigurationSection furnaceSection = plantConfig.getConfigurationSection("furnace-recipes");
                for (String recipeName : furnaceSection.getKeys(false)) {
                    ConfigurationSection recipeSection = furnaceSection.getConfigurationSection(recipeName);
                    addFurnaceRecipe(recipeSection, recipeName);
                }
            }
            // add plant to plant type manager
            main.getPlantTypeManager().addPlantType(plant);
            main.getLogger().info("Successfully loaded plant: " + plantId);
        }
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
                    String[] plantInfo = link.split(":", 2);
                    plantId = plantInfo[0];
                    if (plantInfo.length < 2) {
                        main.getLogger().warning(String.format("Link %s is not formatted as plantId:item|seed in item section: %s", link, section.getCurrentPath()));
                        return null;
                    }
                    String itemString = plantInfo[1];
                    // load plant item from config file
                    YamlConfiguration linkedPlantConfig = plantConfigMap.get(plantId);
                    if (linkedPlantConfig == null) {
                        main.getLogger().warning("PlantId '" + plantId + "' does not match any plant config in item section: " + section.getCurrentPath());
                        return null;
                    }
                    // if item, get item
                    if (itemString.equalsIgnoreCase("item")) {
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
                    } else {
                        main.getLogger().warning("Linked item was neither item or seed in item section: " + section.getCurrentPath());
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
            String materialName = section.getString("material");
            if (materialName == null) {
                main.getLogger().warning("Material not provided in block section: " + section.getCurrentPath());
                return null;
            }
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                main.getLogger().warning("Material '" + materialName + "' not recognized in block section: " + section.getCurrentPath());
                return null;
            }
            // get if required
            boolean required = section.isBoolean("required") && section.getBoolean("required");
            // get skull texture
            String skullTexture = section.getString("skull-texture");
            ArrayList<String> blockDataStrings = new ArrayList<>(section.getStringList("data"));
            // get offset
            if (!section.isSet("offset")) {
                main.getLogger().warning("Offset not defined for block section: " + section.getCurrentPath());
                return null;
            }
            if (!section.isInt("offset.x") || !section.isInt("offset.y") || !section.isInt("offset.z")) {
                main.getLogger().warning("Offset's x, y, or z not defined/integers for block section: " + section.getCurrentPath());
                return null;
            }
            int xRel = section.getInt("offset.x");
            int yRel = section.getInt("offset.y");
            int zRel = section.getInt("offset.z");
            // create BlockSettings from values
            BlockSettings blockSettings = new BlockSettings(xRel, yRel, zRel, material, required);
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

    DropSettings loadDropConfig(ConfigurationSection section, Plant plant) {
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

    boolean addDropsToDroppable(ConfigurationSection section, Droppable droppable, Plant plant) {
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
            DropSettings dropSettings = loadDropConfig(dropConfig, plant);
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

    void addCraftingRecipe(ConfigurationSection section, String recipeName) {
        String type = section.getString("type");
        if (type == null) {
            main.getLogger().warning("Type not set for crafting recipe at: " + section.getCurrentPath());
            return;
        }
        // TODO: get shaped recipe
        if (type.equalsIgnoreCase("shaped")) {

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
                // initialize recipe
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
            } catch (Exception e) {
                main.getLogger().warning(String.format("Failed to add shapeless crafting recipe at %s due to exception: %s",
                        section.getCurrentPath(), e.getMessage()));
                return;
            }
            main.getLogger().info("Registered shapeless crafting recipe: " + recipeName);
        } else {
            main.getLogger().warning(String.format("Type '%s' not a recognized crafting recipe at %s",
                    type, section.getCurrentPath()));
        }
    }

    void addFurnaceRecipe(ConfigurationSection section, String recipeName) {
        // TODO: get furnace recipe
    }

    String getFileNameWithoutExtension(File file) {
        String fileName = "";
        if (file != null && file.exists()) {
            fileName = file.getName().replaceFirst("[.].+$", "");
        }
        return fileName;
    }

}
