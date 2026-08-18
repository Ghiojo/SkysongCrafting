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
    private String referenceOne;
    private String referenceTwo;

    public BlendStep(String referenceOne, String referenceTwo) {
        super();
        this.referenceOne = referenceOne;
        this.referenceTwo = referenceTwo;
    }


    @Override
    public StepData executeStep(StepData stepData) {
        StepData returnData = stepData;
        ItemStack itemOne = fetchReference(referenceOne, stepData);
        ItemStack itemTwo = fetchReference(referenceTwo, stepData);
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

    private ItemStack fetchReference(String reference, StepData stepData) {
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

    @Override
    public boolean goToNextStep() {
        return true;
    }
}
