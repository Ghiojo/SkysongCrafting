package org.skysongdev.skysongCrafting.data.output.data;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

/**
 * Per-craft state passed from one composite step to the next.
 * <ul>
 *   <li>{@code markedItems}: copies of the recipe inputs that were marked, numbered 0..n-1
 *       in grid order (only marked inputs count).</li>
 *   <li>{@code pivotItem}: the item the steps are currently editing; set by a fetch step.</li>
 *   <li>{@code outputItems}: what the craft will produce; filled by save steps.</li>
 * </ul>
 */
public class StepData {
    private ArrayList<MarkedItem> markedItems;
    private ItemStack pivotItem;
    private ArrayList<SkysongItemWithAmmount> outputItems;

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
