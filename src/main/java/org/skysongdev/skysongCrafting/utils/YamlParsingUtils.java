package org.skysongdev.skysongCrafting.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.skysongdev.skysongCrafting.GUI.WorkbenchGUIType;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.*;
import org.skysongdev.skysongCrafting.data.output.data.AmountType;
import org.skysongdev.skysongCrafting.data.output.steps.*;
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
                if(!ITEMS_PATH.resolve(itemID + ".yml").toFile().exists()){
                    SkysongCrafting.Instance().getLogger().warning("Item with ID " + itemID + " does not exist! (File: " + file.getName() + ")");
                    continue;
                }
                SkysongItemWithAmmount item = new SkysongItemWithAmmount(Utils.LoadSkysongItem(ITEMS_PATH.resolve(itemID + ".yml").toFile()), number);
                Bukkit.getLogger().info("Loading output item: " + itemID + " with amount " + number);
                itemStacks.add(item);
            } else {
                if(!ITEMS_PATH.resolve(itemID + ".yml").toFile().exists()){
                    SkysongCrafting.Instance().getLogger().warning("Item with ID " + itemID + " does not exist! (File: " + file.getName() + ")");
                    continue;
                }
                Bukkit.getLogger().info("Loading output item: " + itemID);
                itemStacks.add(new SkysongItemWithAmmount(Utils.LoadSkysongItem(ITEMS_PATH.resolve(itemID + ".yml").toFile()), 1));
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
                //Iterate through each key in the "output_steps" key.

                ArrayList<OutputStep> unorganizedSteps = new ArrayList<>(Collections.nCopies(yaml.getConfigurationSection("output_steps").getKeys(false).size(), null));

                for(String stepKey : yaml.getConfigurationSection("output_steps").getKeys(false)){
                    String step_type = yaml.getString("output_steps." + stepKey + ".step_type", "NONE");
                    switch (step_type.toUpperCase()){
                        case "TAG":
                            String operation = yaml.getString("output_steps." + stepKey + ".operation", "NONE");
                            String tagId = yaml.getString("output_steps." + stepKey + ".tag_id", "NONE");
                            ArrayList<String> amounts = new ArrayList<>(yaml.getStringList("output_steps." + stepKey + ".amounts"));
                            int order = yaml.getInt("output_steps." + stepKey + ".order", -1);
                            unorganizedSteps.set(order ,new TagStep(operation, tagId, amounts));
                            break;
                        case "CMD":
                            String cmdOperation = yaml.getString("output_steps." + stepKey + ".operation", "NONE");
                            String amountTypeString = yaml.getString("output_steps." + stepKey + ".amount_type", "NONE").toUpperCase();
                            int index = yaml.getInt("output_steps." + stepKey + ".index", -1);
                            ArrayList<String> cmdAmounts = new ArrayList<>(yaml.getStringList("output_steps." + stepKey + ".amounts"));
                            AmountType amountType;
                            try{
                                amountType = AmountType.valueOf(amountTypeString);
                            } catch (IllegalArgumentException e){
                                SkysongCrafting.Instance().getLogger().warning("Invalid amount type for CMD step: " + amountTypeString + "! Skipping step. (File: " + file.getName() + ")");
                                continue;
                            }
                            unorganizedSteps.set(index, new CMDStep(cmdOperation, amountType, index));
                            break;
                        case "PROPERTY":
                            String propertyOperation = yaml.getString("output_steps." + stepKey + ".operation", "NONE");
                            String property = yaml.getString("output_steps." + stepKey + ".property", "NONE");
                            int propertyOrder = yaml.getInt("output_steps." + stepKey + ".order", -1);
                            unorganizedSteps.set(propertyOrder, new PropertyStep(propertyOperation, property));
                            break;
                        case "BLEND":
                            String referenceOne = yaml.getString("output_steps." + stepKey + ".reference_1", "NONE");
                            String referenceTwo = yaml.getString("output_steps." + stepKey + ".reference_2", "NONE");
                            unorganizedSteps.set(yaml.getInt("output_steps." + stepKey + ".order", -1), new BlendStep(referenceOne, referenceTwo));
                            break;
                        case "SAVE":
                            int saveIndex = yaml.getInt("output_steps." + stepKey + ".index", -1);
                            unorganizedSteps.set(saveIndex, new SaveStep(saveIndex));
                            break;
                        default:
                            SkysongCrafting.Instance().getLogger().warning("Invalid step type: " + step_type + " in composite output! Skipping step. (File: " + file.getName() + ")");
                    }
                }
                return null;
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
        OutputInstance output = parseOutputInstance(OUTPUT_PATH.resolve(outputString[0].toLowerCase()).resolve(outputString[1] + ".yaml").toFile(), OutputType.valueOf(outputString[0].toUpperCase()));
        Bukkit.getLogger().info("Parsed output for recipe " + file.getName() + " as type " + outputString[0].toUpperCase() + "| result is:  " + output.getOutputItems().toString());
        String tableType = YamlConfiguration.loadConfiguration(file).getString("table_type", "NONE");
        String name = YamlConfiguration.loadConfiguration(file).getString("name", "Unnamed Recipe");
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

                List<List<String>> combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(new ArrayList<>(pattern1D));

                for (List<String> combination : combinations) {
                    ArrayList<String> key = new ArrayList<>(combination);
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapedRecipeMap()
                            .computeIfAbsent(key, k -> new ArrayList<>());
                    list.add(new ShapedRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements));
                }
                return;
            case "SHAPELESS":
                ArrayList<String> inputs = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getStringList("inputs"));

                List<List<String>> combinationsShapeless = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(inputs);

                ArrayList<ArrayList<MaterialCount>> inputMaterialCounts = new ArrayList<>();
                for (List<String> combination : combinationsShapeless) {
                    ArrayList<MaterialCount> materialCounts = Instance().inputToMaterialCountList(new ArrayList<>(combination));
                    materialCounts.sort(Comparator.comparing(mc -> mc.getId().toLowerCase()));
                    inputMaterialCounts.add(materialCounts);
                }

                for(ArrayList<MaterialCount> materialCounts : inputMaterialCounts){
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapelessRecipeMap()
                            .computeIfAbsent(materialCounts, k -> new ArrayList<>());
                    list.add(new ShapelessRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements));
                    Bukkit.getLogger().info("Registered shapeless recipe: " + name + " with inputs: ");
                    for(MaterialCount mc : materialCounts){
                        Bukkit.getLogger().info(" - " + mc.getId() + " x" + mc.getCount());
                    }
                }
                return;
            case "SHAPED_MARKING":
                ArrayList<Boolean> markingPattern = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getBooleanList("marking_pattern"));
                pattern = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getList("pattern"));
                pattern1D = new ArrayList<>();
                for(Object row : pattern){
                    if(row instanceof ArrayList){
                        for(String col : (ArrayList<String>) row){
                            pattern1D.add(col);
                        }
                    }
                }

                combinations = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(new ArrayList<>(pattern1D));

                for (List<String> combination : combinations) {
                    ArrayList<String> key = new ArrayList<>(combination);
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapedRecipeMap()
                            .computeIfAbsent(key, k -> new ArrayList<>());
                    list.add(new ShapedMarkingRecipeInstance(output, icon, name, tableType, sound, key, skillRequirements, markingPattern));
                }
                return;
            case "SHAPELESS_MARKING":
                markingPattern = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getBooleanList("marking_pattern"));
                inputs = new ArrayList<>(YamlConfiguration.loadConfiguration(file).getStringList("inputs"));

                combinationsShapeless = org.skysongdev.skysongCrafting.utils.Utils.generateTagCombinations(inputs);

                inputMaterialCounts = new ArrayList<>();
                for (List<String> combination : combinationsShapeless) {
                    ArrayList<MaterialCount> materialCounts = Instance().inputToMaterialCountList(new ArrayList<>(combination));
                    materialCounts.sort(Comparator.comparing(mc -> mc.getId().toLowerCase()));
                    inputMaterialCounts.add(materialCounts);
                }

                for(ArrayList<MaterialCount> materialCounts : inputMaterialCounts){
                    ArrayList<RecipeInstance> list = Instance()
                            .getShapelessRecipeMap()
                            .computeIfAbsent(materialCounts, k -> new ArrayList<>());
                    list.add(new ShapelessMarkingRecipeInstance(materialCounts, output, icon, name, tableType, sound, skillRequirements, markingPattern));
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
