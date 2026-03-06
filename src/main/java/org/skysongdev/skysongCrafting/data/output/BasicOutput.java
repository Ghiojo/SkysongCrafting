package org.skysongdev.skysongCrafting.data.output;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public class BasicOutput extends OutputInstance{
    public BasicOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings) {
        super(outputItems, outputStrings, OutputType.BASIC);
    }

    @Override
    public ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems) {
        return outputItems;
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        return outputStrings;
    }
}
