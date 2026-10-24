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
                itemStacks.add(new SkysongItemWithAmmount(Utils.LoadSkysongItem(itemFile), number));
            } else {
                File itemFile = resolveItemFile(itemID);
                if(itemFile == null){
                    SkysongCrafting.Instance().getLogger().warning("Item with ID " + itemID + " does not exist! (File: " + file.getName() + ")");
                    continue;
                }
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
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String recipeType = yaml.getString("recipe_type", "NONE").toUpperCase();
        Material icon = Material.valueOf(yaml.getString("icon", "RED_WOOL"));
        String sound = yaml.getString("sound", "ENTITY_VILLAGER_WORK_TOOLSMITH");
        Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements = readSkillRequirements(file, yaml);

        OutputInstance output = loadOutput(file, yaml);
        if(output == null) return; // already warned

        String tableType = yaml.getString("table_type", "NONE");
        String name = yaml.getString("name", "Unnamed Recipe");
        List<RecipeCondition> conditions = RecipeConditionParser.parse(file, yaml);
        if(conditions == null) return; // malformed; already warned

        switch(recipeType){
            case "SHAPED":
                registerShaped(file, yaml, output, icon, name, tableType, sound, skillRequirements, conditions, null);
                return;
            case "SHAPED_MARKING":
                registerShaped(file, yaml, output, icon, name, tableType, sound, skillRequirements, conditions, readMarkingPattern(yaml));
                return;
            case "SHAPELESS":
                registerShapeless(file, yaml, output, icon, name, tableType, sound, skillRequirements, conditions, null);
                return;
            case "SHAPELESS_MARKING":
                registerShapeless(file, yaml, output, icon, name, tableType, sound, skillRequirements, conditions, readMarkingPattern(yaml));
                return;
            default:
                SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " has unknown recipe_type '" + recipeType
                        + "' (expected SHAPED, SHAPELESS, SHAPED_MARKING or SHAPELESS_MARKING). Recipe not loaded.");
        }
    }

    private static Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> readSkillRequirements(File file, YamlConfiguration yaml){
        Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements = new HashMap<>();
        for(String skill : yaml.getStringList("skill_requirements")){
            try{
                String[] skillParts = skill.split(":");
                org.skysongdev.skysongstats.Utils.Utils.Skills skillEnum = org.skysongdev.skysongstats.Utils.Utils.Skills.valueOf(skillParts[0].toUpperCase());
                int level = Integer.parseInt(skillParts[1]);
                skillRequirements.put(skillEnum, level);
            } catch (Exception e){
                SkysongCrafting.Instance().getLogger().warning("Skill " + skill + " does not exist! (File: " + file.getName() + ")");
                Bukkit.getLogger().warning(e.getMessage());
            }
        }
        return skillRequirements;
    }

    /** Resolves and parses the recipe's {@code output: "type:file"}; {@code null} (after a warning) if it cannot be used. */
    private static OutputInstance loadOutput(File file, YamlConfiguration yaml){
        String[] outputString = yaml.getString("output", "NONE").split(":");
        if(outputString.length < 2 || outputString[0].isEmpty() || outputString[1].isEmpty()){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " does not have an output string!");
            return null;
        }
        OutputType outputType;
        try {
            outputType = OutputType.valueOf(outputString[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " has unknown output type '" + outputString[0] + "' (expected basic, random, random_id or composite). Recipe not loaded.");
            return null;
        }
        File outputFile = resolveYamlFile(OUTPUT_PATH.resolve(outputString[0].toLowerCase()), outputString[1]);
        if(outputFile == null){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " references output '" + outputString[0] + ":" + outputString[1] + "' but no such file exists. Recipe not loaded.");
            return null;
        }
        OutputInstance output = parseOutputInstance(outputFile, outputType);
        if(output == null){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " not loaded because its output '" + outputFile.getName() + "' failed to load (see warnings above).");
            return null;
        }
        return output;
    }

    /** Flattens the 5x5 {@code pattern} rows into one list of 25 ingredient tokens; {@code null} if the key is missing. */
    private static ArrayList<String> readPattern(YamlConfiguration yaml){
        List<?> rows = yaml.getList("pattern");
        if(rows == null) return null;
        ArrayList<String> pattern1D = new ArrayList<>();
        for(Object row : rows){
            if(row instanceof List<?> cells){
                for(Object cell : cells) pattern1D.add(cell == null ? "" : String.valueOf(cell));
            }
        }
        return pattern1D;
    }

    /**
     * Registers a shaped recipe under every tag combination of its pattern.
     * @param markingPattern {@code null} for a plain SHAPED recipe, the marks for SHAPED_MARKING
     */
    private static void registerShaped(File file, YamlConfiguration yaml, OutputInstance output, Material icon, String name,
                                       String tableType, String sound, Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements,
                                       List<RecipeCondition> conditions, ArrayList<Boolean> markingPattern){
        ArrayList<String> pattern1D = readPattern(yaml);
        if(pattern1D == null){
            SkysongCrafting.Instance().getLogger().warning("Recipe " + file.getName() + " has no 'pattern'. Recipe not loaded.");
            return;
        }
        List<ConsumptionRule> slotRules = consumptionRules(pattern1D);
        List<List<String>> combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(pattern1D));

        for (List<String> combination : combinations) {
            ArrayList<String> key = new ArrayList<>(combination);
            ShapedRecipeInstance recipe = markingPattern == null
                    ? new ShapedRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements)
                    : new ShapedMarkingRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements, markingPattern);
            recipe.setSlotRules(slotRules);
            recipe.setConditions(conditions);
            Instance().getShapedRecipeMap().computeIfAbsent(key, k -> new ArrayList<>()).add(recipe);
        }
    }

    /**
     * Registers a shapeless recipe under every tag combination of its inputs.
     * @param markingPattern {@code null} for a plain SHAPELESS recipe, the marks for SHAPELESS_MARKING
     */
    private static void registerShapeless(File file, YamlConfiguration yaml, OutputInstance output, Material icon, String name,
                                          String tableType, String sound, Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements,
                                          List<RecipeCondition> conditions, ArrayList<Boolean> markingPattern){
        ArrayList<String> inputs = new ArrayList<>(yaml.getStringList("inputs"));
        List<ConsumptionRule> inputRules = consumptionRules(inputs);
        List<List<String>> combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(stripConsumption(inputs));

        for (List<String> combination : combinations) {
            Map<String, ConsumptionRule> rulesById = shapelessRulesById(combination, inputRules, file);
            if (rulesById == null) return; // conflicting rules; already warned
            ArrayList<MaterialCount> materialCounts = Instance().inputToMaterialCountList(new ArrayList<>(combination));
            materialCounts.sort(Comparator.comparing(mc -> mc.getId().toLowerCase()));

            ShapelessRecipeInstance recipe = markingPattern == null
                    ? new ShapelessRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements)
                    : new ShapelessMarkingRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements, markingPattern);
            recipe.setRulesById(rulesById);
            recipe.setConditions(conditions);
            Instance().getShapelessRecipeMap().computeIfAbsent(materialCounts, k -> new ArrayList<>()).add(recipe);
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
            Instance().getWorkstationTypes().put(key, workstation);
        }
    }
}
