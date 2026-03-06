package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.Map;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;

public class ConditionalShapedRecipeInstance extends ShapedMarkingRecipeInstance{
    ArrayList<String> conditions;
    public ConditionalShapedRecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, ArrayList<String> materialConsumption, Map<Utils.Skills, Integer> skillRequirements, ArrayList<Boolean> materialMarks, ArrayList<String> conditions) {
        super(output, displayItem, recipeName, craftingTableType, craftingSound, materialConsumption, skillRequirements, materialMarks);
        this.conditions = conditions;
    }

    public boolean doesMeetConditions(ArrayList<Integer> inputSlots, Inventory inventory){
        ArrayList<Boolean> conditionResults = new ArrayList<>();
        for(String condition : conditions){
            String[] conditionSplit = condition.split(";");
            if(conditionSplit.length < 4){
                Instance().getLogger().warning("Invalid condition format for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                continue;
            }
            int markedSlot = Integer.parseInt(conditionSplit[0]);
            String type = conditionSplit[1];
            String operator = conditionSplit[2];
            String value = conditionSplit[3];

            ItemStack itemToCompare = inventory.getItem(inputSlots.get(markedSlot));
            if(itemToCompare == null || itemToCompare.getType() == Material.AIR){
                Instance().getLogger().warning("Invalid item in marked slot for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                return true;
            }

            Object valueFromType = getValueFromType(type, itemToCompare);

            switch(operator){
                case "==":
                    if(valueFromType == null || !valueFromType.toString().equals(value)){
                        conditionResults.add(false);
                    }
                    break;
                case "!=":
                    if(valueFromType == null || valueFromType.toString().equals(value)){
                        conditionResults.add(false);
                    }
                    break;
                case ">":
                    try {
                        float valueFloat = Float.parseFloat(value);
                        float valueFromTypeFloat = Float.parseFloat(valueFromType.toString());
                        if(valueFromTypeFloat <= valueFloat){
                            conditionResults.add(false);
                        }
                    } catch (NumberFormatException e) {
                        Instance().getLogger().warning("Invalid number format in condition for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                    }
                    break;
                case "<":
                    try {
                        float valueFloat = Float.parseFloat(value);
                        float valueFromTypeFloat = Float.parseFloat(valueFromType.toString());
                        if(valueFromTypeFloat >= valueFloat){
                            conditionResults.add(false);
                        }
                    } catch (NumberFormatException e) {
                        Instance().getLogger().warning("Invalid number format in condition for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                    }
                    break;
                case ">=":
                    try {
                        float valueFloat = Float.parseFloat(value);
                        float valueFromTypeFloat = Float.parseFloat(valueFromType.toString());
                        if(valueFromTypeFloat < valueFloat){
                            conditionResults.add(false);
                        }
                    } catch (NumberFormatException e) {
                        Instance().getLogger().warning("Invalid number format in condition for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                    }
                    break;
                case "<=":
                    try {
                        float valueFloat = Float.parseFloat(value);
                        float valueFromTypeFloat = Float.parseFloat(valueFromType.toString());
                        if(valueFromTypeFloat > valueFloat){
                            conditionResults.add(false);
                        }
                    } catch (NumberFormatException e) {
                        Instance().getLogger().warning("Invalid number format in condition for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
                    }
                    break;
                default:
                    Instance().getLogger().warning("Invalid operator for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Condition: " + condition + ")");
            }
            conditionResults.add(true);
        }
        return !conditionResults.contains(false);
    }

    private Object getValueFromType(String type, ItemStack item){
        String[] typeSplit = type.split("!");
        String baseType = typeSplit[0];
        String modifier = null;
        if(typeSplit.length > 1){
            modifier = typeSplit[1];
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        switch (baseType.toLowerCase()) {
            case "material":
                return item.getType().toString();
            case "amount":
                return item.getAmount();
            case "tag":
                if(modifier == null){
                    Instance().getLogger().warning("Missing modifier for tag type in recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Type: " + type + ")");
                    return null;
                }
                if(!container.has(org.skysongdev.skysongitems.utils.Utils.getTag(modifier))){
                    Instance().getLogger().warning("Item in marked slot does not have required tag for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Type: " + type + ")");
                    return null;
                }
                return container.get(org.skysongdev.skysongitems.utils.Utils.getTag(modifier), org.skysongdev.skysongitems.utils.Utils.tagList.get(org.skysongdev.skysongitems.utils.Utils.getTag(modifier)));
            default:
                Instance().getLogger().warning("Invalid condition type for recipe " + displayItem.getItemMeta().displayName() + "! Skipping condition check. (Type: " + type + ")");
        }
        return null;
    }
}
