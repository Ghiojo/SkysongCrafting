package org.skysongdev.skysongCrafting.data.output;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public abstract class OutputInstance {
    protected ArrayList<SkysongItemWithAmmount> outputItems;
    protected ArrayList<String> outputStrings;
    protected OutputType outputType;

    public OutputInstance(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, OutputType outputType) {
        this.outputItems = outputItems;
        for(SkysongItemWithAmmount item : outputItems){
            Bukkit.getLogger().info("Output Item: " + item.getItem().getName() + " x" + item.getAmmount() + " (Type: " + outputType + ")" );
        }
        this.outputStrings = outputStrings;
        this.outputType = outputType;
    }

    public ArrayList<SkysongItemWithAmmount> getOutputItems() {
        return outputItems;
    }

    public abstract ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems);

    public abstract ArrayList<String> getOutputStrings();
}
