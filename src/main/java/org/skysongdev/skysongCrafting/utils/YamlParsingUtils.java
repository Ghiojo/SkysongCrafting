package org.skysongdev.skysongCrafting.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.skysongdev.skysongCrafting.GUI.WorkbenchGUIType;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.*;
import org.skysongdev.skysongCrafting.data.recipe.*;
import org.skysongdev.skysongCrafting.data.workstations.Workstation;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongCrafting.utils.Constants.*;
import static org.skysongdev.skysongCrafting.utils.Utils.Instance;

public class YamlParsingUtils {

    public static OutputInstance parseOutputInstance(File file, OutputType type){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ArrayList<String> outputItems = new ArrayList<>(yaml.getStringList("output_items"));
        ArrayList<String> outputStrings = new ArrayList<>(yaml.getStringList("output_strings"));

        ArrayList<SkysongItemWithAmmount> itemStacks = new ArrayList<>();
        for(String itemID : outputItems){
            if(itemID == null || itemID.isEmpty()) continue;
            Pattern pattern = Pattern.compile("([\\*])([0-9]+)");
            Matcher matcher = pattern.matcher(itemID);

            if(matcher.find()){
                int number = Integer.parseInt(matcher.group(2));
                itemID = itemID.replace(matcher.group(0), "");
                File itemFile = resolveItemFile(itemID);
                if(itemFile == null){
                    SkysongCrafting.Instance().getLogger().warning("Item with ID " + itemID + " does not exist! (File: " + file.getName() + ")");
                    continue;
                }
                SkysongItemWithAmmount item = new SkysongItemWithAmmount(Utils.LoadSkysongItem(itemFile), number);
                Bukkit.getLogger().info("Loading output item: " + itemID + " with amount " + number);
                itemStacks.add(item);
            } else {
                File itemFile = resolveItemFile(itemID);
                if(itemFile == null){
                    SkysongCrafting.Instance().getLogger().warning("Item with ID " + itemID + " does not exist! (File: " + file.getName() + ")");
                    continue;
                }
                Bukkit.getLogger().info("Loading output item: " + itemID);
                itemStacks.add(new SkysongItemWithAmmount(Utils.LoadSkysongItem(itemFile), 1));
            }

        }

        switch (type){
            case BASIC -> {
                return new BasicOutput(itemStacks, outputStrings);
            }
            case RANDOM -> {
                ArrayList<Float> chances = new ArrayList<>(yaml.getFloatList("chances"));
                int lowerBoundOfRolls = yaml.getInt("rolls_lower_bound", 1);
                int upperBoundOfRolls = yaml.getInt("rolls_upper_bound", 1);
                return new RandomOutput(itemStacks, outputStrings, chances, lowerBoundOfRolls, upperBoundOfRolls);
            }
            case RANDOM_ID -> {
                String idTypeString = yaml.getString("id_to_randomize", "NONE").toUpperCase();
                return new RandomIdOutput(itemStacks, outputStrings, idTypeString);
            }
            case COMPOSITE -> {
                return CompositeOutputParser.parse(file, yaml, itemStacks, outputStrings);
            }
            default -> {
                return null;
            }
        }
    }

    public static void InstanceTags(){
        Instance().tagMap.clear();
        for(File file : TAGS_PATH.toFile().listFiles()){
            if(file.isFile() && file.getName().endsWith(".yaml")){
                String tagId = YamlConfiguration.loadConfiguration(file).getString("tag_id", "NONE");
                List<String> items = YamlConfiguration.loadConfiguration(file).getStringList("items");
                if(items.isEmpty()){
                    SkysongCrafting.Instance().getLogger().warning("Tag " + tagId + " is empty!");
                    continue;
                }
                Instance().tagMap.put(tagId, new ArrayList<String>(items));
            }
        }
    }

    public static void initializeRecipeInstance(File file){
        String recipeType = YamlConfiguration.loadConfiguration(file).getString("recipe_type", "NONE").toUpperCase();
        Material icon =  Material.valueOf(YamlConfiguration.loadConfiguration(file).getString("icon", "RED_WOOL"));
        String sound = YamlConfiguration.loadConfiguration(file).getString("sound", "ENTITY_VILLAGER_WORK_TOOLSMITH");
        Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements = new HashMap<>();
        for(String skill : YamlConfiguration.loadConfiguration(file).getStringList("skill_requirements")){
            try{
                String[] skillParts = skill.split(":");
                org.skysongdev.skysongstats.Utils.Utils.Skills skillEnum = org.skysongdev.skysongstats.Utils.Utils.Skills.valueOf(skillParts[0].toUpperCase());
                int level = Integer.parseInt(skillParts[1]);
                skillRequirements.put(skillEnum, level);
                Bukkit.getLogger().info("Added skill requirement for recipe " + file.getName() + ": " + skillEnum + " level " + level);
            } catch (Exception e){
                SkysongCrafting.Instance().getLogger().warning("Skill " + skill + " does not exist! (File: " + file.getName() + ")");
                Bukkit.getLogger().warning(e.getMessage());
            }
        }
        String[] outputString = YamlConfiguration.loadConfiguration(file).getString("output", "NONE").split(":");
        if(outputString.length < 2 || outputString[0].isEmpty() || outputString[1].isEmpty()){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " does not have an output string!");
            return;
        }
        OutputType outputType;
        try {
            outputType = OutputType.valueOf(outputString[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " has unknown output type '" + outputString[0] + "' (expected basic, random, random_id or composite). Recipe not loaded.");
            return;
        }
        File outputFile = resolveYamlFile(OUTPUT_PATH.resolve(outputString[0].toLowerCase()), outputString[1]);
        if(outputFile == null){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " references output '" + outputString[0] + ":" + outputString[1] + "' but no such file exists. Recipe not loaded.");
            return;
        }
        OutputInstance output = parseOutputInstance(outputFile, outputType);
        if(output == null){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " not loaded because its output '" + outputFile.getName() + "' failed to load (see warnings above).");
            return;
        }
        Bukkit.getLogger().info("Parsed output for recipe " + file.getName() + " as type " + outputType + "| result is:  " + output.getOutputItems().toString());
        String tableType = YamlConfiguration.loadConfiguration(file).getString("table_type", "NONE");
        String name = YamlConfiguration.loadConfiguration(file).getString("name", "Unnamed Recipe");
        List<RecipeCondition> conditions = RecipeConditionParser.parse(file, YamlConfiguration.loadConfiguration(file));
        if(conditions == null) return; // malformed; already warned
        switch(recipeType){
            case "SHAPED":
                ArrayList<?> pattern = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getList("pattern"));
                ArrayList<String> pattern1D = new ArrayList<>();
                for(Object row : pattern){
                    if(row instanceof ArrayList){
                        for(String col : (ArrayList<String>) row){
                            pattern1D.add(col);
                        }
                    }
                }

                List<ConsumptionRule> slotRules = consumptionRules(pattern1D);
                List<List<String>> combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(pattern1D));

                for (List<String> combination : combinations) {
                    ArrayList<String> key = new ArrayList<>(combination);
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapedRecipeMap()
                            .computeIfAbsent(key, k -> new ArrayList<>());
                    ShapedRecipeInstance recipe = new ShapedRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements);
                    recipe.setSlotRules(slotRules);
                    list.add(withConditions(recipe, conditions));
                }
                return;
            case "SHAPELESS":
                ArrayList<String> inputs = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getStringList("inputs"));

                List<ConsumptionRule> inputRules = consumptionRules(inputs);
                List<List<String>> combinationsShapeless = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(inputs));

                for (List<String> combination : combinationsShapeless) {
                    Map<String, ConsumptionRule> rulesById = shapelessRulesById(combination, inputRules, file);
                    if (rulesById == null) return; // conflicting rules; already warned
                    ArrayList<MaterialCount> materialCounts = Instance().inputToMaterialCountList(new ArrayList<>(combination));
                    materialCounts.sort(Comparator.comparing(mc -> mc.getId().toLowerCase()));

                    ArrayList<RecipeInstance> list = Instance()
                            .getShapelessRecipeMap()
                            .computeIfAbsent(materialCounts, k -> new ArrayList<>());
                    ShapelessRecipeInstance recipe = new ShapelessRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements);
                    recipe.setRulesById(rulesById);
                    list.add(withConditions(recipe, conditions));
                    Bukkit.getLogger().info("Registered shapeless recipe: " + name + " with inputs: ");
                    for(MaterialCount mc : materialCounts){
                        Bukkit.getLogger().info(" - " + mc.getId() + " x" + mc.getCount());
                    }
                }
                return;
            case "SHAPED_MARKING":
                ArrayList<Boolean> markingPattern = readMarkingPattern(YamlConfiguration.loadConfiguration(file));
                pattern = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getList("pattern"));
                pattern1D = new ArrayList<>();
                for(Object row : pattern){
                    if(row instanceof ArrayList){
                        for(String col : (ArrayList<String>) row){
                            pattern1D.add(col);
                        }
                    }
                }

                slotRules = consumptionRules(pattern1D);
                combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(pattern1D));

                for (List<String> combination : combinations) {
                    ArrayList<String> key = new ArrayList<>(combination);
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapedRecipeMap()
                            .computeIfAbsent(key, k -> new ArrayList<>());
                    ShapedMarkingRecipeInstance recipe = new ShapedMarkingRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements, markingPattern);
                    recipe.setSlotRules(slotRules);
                    list.add(withConditions(recipe, conditions));
                }
                return;
            case "SHAPELESS_MARKING":
                markingPattern = readMarkingPattern(YamlConfiguration.loadConfiguration(file));
                inputs = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getStringList("inputs"));

                inputRules = consumptionRules(inputs);
                combinationsShapeless = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(inputs));

                for (List<String> combination : combinationsShapeless) {
                    Map<String, ConsumptionRule> rulesById = shapelessRulesById(combination, inputRules, file);
                    if (rulesById == null) return; // conflicting rules; already warned
                    ArrayList<MaterialCount> materialCounts = Instance().inputToMaterialCountList(new ArrayList<>(combination));
                    materialCounts.sort(Comparator.comparing(mc -> mc.getId().toLowerCase()));

                    ArrayList<RecipeInstance> list = Instance()
                            .getShapelessRecipeMap()
                            .computeIfAbsent(materialCounts, k -> new ArrayList<>());
                    ShapelessMarkingRecipeInstance recipe = new ShapelessMarkingRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements, markingPattern);
                    recipe.setRulesById(rulesById);
                    list.add(withConditions(recipe, conditions));
                    Bukkit.getLogger().info("Registered shapeless recipe: " + name + " with inputs: ");
                    for(MaterialCount mc : materialCounts){
                        Bukkit.getLogger().info(" - " + mc.getId() + " x" + mc.getCount());
                    }
                }
                return;
            default:
                return;
        }
    }

    /** One {@link ConsumptionRule} per ingredient token, in order. */
    static List<ConsumptionRule> consumptionRules(List<String> tokens){
        List<ConsumptionRule> rules = new ArrayList<>();
        for(String token : tokens) rules.add(ConsumptionRule.parse(token));
        return rules;
    }

    /** The ingredient tokens without their consumption suffixes (tags and *N counts untouched). */
    static ArrayList<String> stripConsumption(List<String> tokens){
        ArrayList<String> stripped = new ArrayList<>();
        for(String token : tokens) stripped.add(ConsumptionRule.strip(token));
        return stripped;
    }

    /**
     * Shapeless rules are per crafting id: every slot holding that id is consumed the same way.
     * Returns {@code null} (after a warning) when the same id is listed with two different rules.
     */
    static Map<String, ConsumptionRule> shapelessRulesById(List<String> combination, List<ConsumptionRule> rules, File file){
        Map<String, ConsumptionRule> byId = new HashMap<>();
        for(int i = 0; i < combination.size(); i++){
            String id = combination.get(i).split("\\*", 2)[0].trim().toLowerCase(Locale.ROOT);
            if(id.isEmpty()) continue;
            ConsumptionRule rule = i < rules.size() ? rules.get(i) : ConsumptionRule.CONSUME_ONE;
            ConsumptionRule existing = byId.get(id);
            if(existing != null && !existing.equals(rule)){
                SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " lists ingredient '" + id
                        + "' twice with different consumption rules (" + existing + " and " + rule
                        + "). In a shapeless recipe every slot of the same ingredient is consumed the same way. Recipe not loaded.");
                return null;
            }
            byId.put(id, rule);
        }
        return byId;
    }

    private static RecipeInstance withConditions(RecipeInstance recipe, List<RecipeCondition> conditions){
        recipe.setConditions(conditions);
        return recipe;
    }

    /**
     * Reads {@code marking_pattern} either as a flat list of booleans or, like {@code pattern},
     * as a list of rows; rows are flattened in order.
     */
    static ArrayList<Boolean> readMarkingPattern(YamlConfiguration yaml){
        ArrayList<Boolean> marks = new ArrayList<>();
        List<?> raw = yaml.getList("marking_pattern");
        if(raw == null) return marks;
        for(Object entry : raw){
            if(entry instanceof List<?> row){
                for(Object cell : row) marks.add(Boolean.TRUE.equals(cell) || "true".equalsIgnoreCase(String.valueOf(cell)));
            } else {
                marks.add(Boolean.TRUE.equals(entry) || "true".equalsIgnoreCase(String.valueOf(entry)));
            }
        }
        return marks;
    }

    public static void initializeWorkbenches(){
        org.skysongdev.skysongCrafting.utils.Utils.Instance().getWorkstationTypes().clear();
        File file = WORKSTATIONS_PATH.toFile();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if(!WORKSTATIONS_PATH.toFile().exists())
            Bukkit.getLogger().warning("[SkysongCrafting] workstations.yml file does not exist!");
        Set<String> keys = config.getKeys(false);

        for (String key : keys) {
            String name = config.getString(key + ".name", key);
            String icon = config.getString(key + ".icon");
            String guiType  = config.getString(key + ".gui_layout", "WORKBENCH").toUpperCase();
            Workstation workstation = new Workstation(name, icon, WorkbenchGUIType.valueOf(guiType), key);
            Bukkit.getLogger().info("Initialized workstation: " + name + " of type " + guiType + " with key " + key);
            Instance().getWorkstationTypes().put(key, workstation);
        }
    }
}
