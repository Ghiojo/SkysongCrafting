package org.skysongdev.skysongCrafting.data.output.utils;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;
import java.util.UUID;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;

public class PlaceholderParser {
    public static String parsePlaceholder(String placeholder, StepData stepData){
        //Placeholders are always in the format of referenceType:reference(if needed);dataType:data:index(if needed)
        //Example: marked:0;tag:dye_color would check the marked item in position 0's tag for the dye_color tag and return the
        //amount of that tag, but pivot;cmd:color:1 returns the second argument of the CustomModelData color arrayList from the pivot item.

        String[] parts = placeholder.split(";");
        if(parts.length == 1){
            Instance().getLogger().warning("Invalid placeholder format, missing ';' separator: " + placeholder);
            return placeholder;
        }
        String referencePart = parts[0];
        String dataPart = parts.length > 1 ? parts[1]  : "";

        String[] referenceParts = referencePart.split(":");
        String referenceType = referenceParts[0];
        int referenceIndex = 0;
        if(referenceParts.length > 1)
            referenceIndex = Integer.parseInt(referenceParts[1]);

        String[] dataParts = dataPart.split(":");
        String dataType = dataParts.length > 0 ? dataParts[0]: "";
        String dataKey = dataParts.length > 1 ? dataParts[1]: "";
        int dataIndex = 0;
        if(dataParts.length > 2)
            dataIndex = Integer.parseInt(dataParts[2]);

        ItemStack itemToCheck = null;
        switch(referenceType){
            case "marked":
                if(referenceIndex < 0 || referenceIndex >= stepData.getMarkedItems().size()){
                    Instance().getLogger().warning("Invalid reference index for marked items: " + referenceIndex);
                    return "";
                }
                itemToCheck = stepData.getMarkedItems().get(referenceIndex).getItemStack();
                break;
            case "pivot":
                itemToCheck = stepData.getPivotItem();
                break;
            default:
                Instance().getLogger().warning("Invalid reference type: " + referenceType);
                return null;
        }

        if(itemToCheck == null){
            Instance().getLogger().warning("Item to check is null for placeholder: " + placeholder);
            return null;
        }
        ItemMeta meta = itemToCheck.getItemMeta();

        switch(dataType){
            case "tag":
                if(!Utils.isATag(dataKey)){
                    Instance().getLogger().warning("Invalid data key for tag type in placeholder: " + placeholder);
                    return null;
                }
                NamespacedKey tagKey = Utils.getTag(dataKey);
                if(!meta.getPersistentDataContainer().has(tagKey, Constants.tagList.get(tagKey))) {
                    Instance().getLogger().warning("Item does not have required tag for placeholder: " + placeholder);
                    return null;
                }
                Object tagValue = meta.getPersistentDataContainer().get(tagKey, Constants.tagList.get(tagKey));
                return tagValue.toString();
            case "cmd":
                CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
                switch(dataKey){
                    case "color":
                        List<Color> colorList = cmdComponent.getColors();
                        Color color = colorList.get(dataIndex);
                        return color.asRGB() + "";
                    case "string":
                        List<String> stringList = cmdComponent.getStrings();
                        return stringList.get(dataIndex);
                    case "float":
                        List<Float> floatList = cmdComponent.getFloats();
                        return floatList.get(dataIndex) + "";
                    case "boolean":
                        List<Boolean> booleanList = cmdComponent.getFlags();
                        return booleanList.get(dataIndex) + "";
                    default:
                        Instance().getLogger().warning("Invalid data key for cmd type in placeholder: " + placeholder);
                        return null;
                }
            case "uuid":
                return UUID.randomUUID().toString();
            default:
                Instance().getLogger().warning("Invalid data type: " + dataType);
                return null;
        }
    }
}
