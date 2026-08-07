package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.Constants;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class BlendStep extends OutputStep {
    private FetchStep.FetchType fetchTypeOne;
    private String referenceOne;
    private FetchStep.FetchType fetchTypeTwo;
    private String referenceTwo;

    public BlendStep(String referenceOne, String referenceTwo) {
        super();
        this.referenceOne = referenceOne;
        this.referenceTwo = referenceTwo;
    }


    @Override
    public StepData executeStep(StepData stepData) {
        StepData returnData = stepData;
        ItemStack itemOne = fetchReference(fetchTypeOne, referenceOne, stepData);
        ItemStack itemTwo = fetchReference(fetchTypeTwo, referenceTwo, stepData);
        ItemMeta metaOne = itemOne.getItemMeta();
        ItemMeta metaTwo = itemTwo.getItemMeta();

        Color colorOne = Color.fromRGB(metaOne.getPersistentDataContainer().get(Utils.getTag("dye_color"), PersistentDataType.INTEGER));
        Color colorTwo = Color.fromRGB(metaTwo.getPersistentDataContainer().get(Utils.getTag("dye_color"), PersistentDataType.INTEGER));

        Color colorResult = colorOne.mixColors(colorTwo);

        ItemStack pivotItem = returnData.getPivotItem();
        ItemMeta pivotMeta = pivotItem.getItemMeta();
        PersistentDataContainer container = pivotMeta.getPersistentDataContainer();
        container.set(Utils.getTag("dye_color"), PersistentDataType.INTEGER, colorResult.asRGB());
        pivotItem.setItemMeta(pivotMeta);
        returnData.setPivotItem(pivotItem);
        return returnData;
    }

    private ItemStack fetchReference(FetchStep.FetchType type, String reference, StepData stepData) {
        if(type == FetchStep.FetchType.GENERATED) {
            File itemFile = Constants.ITEMS_PATH.resolve(reference + ".yml").toFile();
            if(!itemFile.exists()) {
                System.out.println("Item file not found for id: " + reference);
                return null;
            }
            SkysongItem item = Utils.LoadSkysongItem(itemFile);
            return getPlugin().getUtils().GenerateSkysongItem(item);
        } else if(type == FetchStep.FetchType.MARKED) {
            int index = 0;
            try {
                index = Integer.parseInt(reference);
            } catch (NumberFormatException e) {
                System.out.println("Invalid id to fetch: " + reference);
                return null;
            }
            if(index < 0 || index >= stepData.getMarkedItems().size()) {
                System.out.println("Index out of bounds for marked items: " + index);
                return null;
            }
            MarkedItem itemToFetch = stepData.getMarkedItems().get(index);
            return itemToFetch.getItemStack();
        }
        return null;
    }

    @Override
    public boolean goToNextStep() {
        return false;
    }
}
