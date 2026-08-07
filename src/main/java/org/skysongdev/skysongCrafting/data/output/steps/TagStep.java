package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.Constants;
import org.skysongdev.skysongCrafting.utils.TypeCastingUtils;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;

public class TagStep extends OutputStep {
    String operation;
    String tagId;
    ArrayList<String> amounts;

    public TagStep(String operation, String tagId, ArrayList<String> amounts) {
        super();
        this.operation = operation;
        this.tagId = tagId;
        this.amounts = amounts;
    }

    @Override
    public StepData executeStep(StepData stepData) {
        StepData newData = stepData;
        if(!Utils.isATag(tagId)){
            Instance().getLogger().warning("Invalid tag: " + tagId);
            this.doesGoToNextStep = false;
            return newData;
        }
        ItemStack item = stepData.getPivotItem();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        NamespacedKey tagKey = Utils.getTag(tagId);
        PersistentDataType tagType = org.skysongdev.skysongitems.utils.Constants.tagList.get(tagKey);

        float numberAmount = 0;
        if(tagType == PersistentDataType.INTEGER || tagType == PersistentDataType.FLOAT){
            for(String amount : amounts){
                String parsedAmount = PlaceholderParser.parsePlaceholder(amount, stepData);
                try{
                    numberAmount += Float.parseFloat(parsedAmount);
                } catch (NumberFormatException e){
                    Instance().getLogger().warning("Invalid number format for tag amount: " + parsedAmount);
                    this.doesGoToNextStep = false;
                    return newData;
                }
            }
        }

        Object finalAmount = (tagType == PersistentDataType.INTEGER || tagType == PersistentDataType.FLOAT) ? numberAmount : amounts.get(0);

        switch(operation){
            case "set":
                container.set(tagKey, tagType, TypeCastingUtils.castObjectToType(tagType, finalAmount));
                break;
            case "add":
                if(tagType == PersistentDataType.STRING || tagType == PersistentDataType.BOOLEAN){
                    Instance().getLogger().warning("Cannot add to a tag of type STRING or BOOLEAN: " + tagId);
                    this.doesGoToNextStep = false;
                    return newData;
                }
                if(!container.has(tagKey, tagType)){
                    container.set(tagKey, tagType, TypeCastingUtils.castObjectToType(tagType, finalAmount));
                } else {
                    Object currentValue = container.get(tagKey, tagType);
                    if(currentValue instanceof Integer){
                        container.set(tagKey, tagType, (Integer) currentValue + (Integer) TypeCastingUtils.castObjectToType(tagType, finalAmount));
                    } else if(currentValue instanceof Float){
                        container.set(tagKey, tagType, (Float) currentValue + (Float) TypeCastingUtils.castObjectToType(tagType, finalAmount));
                    }
                }
            case "remove":
                if(!container.has(tagKey, tagType)){
                    Instance().getLogger().warning("Item does not have tag to remove: " + tagId);
                    break;
                }
                container.remove(tagKey);
        }
        item.setItemMeta(meta);
        newData.setPivotItem(item);


        return null;
    }

    @Override
    public boolean goToNextStep() {
        return doesGoToNextStep;
    }
}
