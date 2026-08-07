package org.skysongdev.skysongCrafting.data.output.data;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public class StepData {
    ArrayList<MarkedItem> markedItems;
    ItemStack pivotItem;
    ArrayList<SkysongItemWithAmmount> outputItems;

    public StepData(ArrayList<MarkedItem> markedItems, ItemStack pivotItem, ArrayList<SkysongItemWithAmmount> outputItems) {
        this.markedItems = markedItems;
        this.pivotItem = pivotItem;
        this.outputItems = outputItems;
    }

    public ArrayList<MarkedItem> getMarkedItems() {
        return markedItems;
    }

    public void setMarkedItems(ArrayList<MarkedItem> markedItems) {
        this.markedItems = markedItems;
    }

    public ArrayList<SkysongItemWithAmmount> getOutputItems() {
        return outputItems;
    }

    public void setOutputItems(ArrayList<SkysongItemWithAmmount> outputItems) {
        this.outputItems = outputItems;
    }

    public ItemStack getPivotItem() {
        return pivotItem;
    }

    public void setPivotItem(ItemStack pivotItem) {
        this.pivotItem = pivotItem;
    }
}
