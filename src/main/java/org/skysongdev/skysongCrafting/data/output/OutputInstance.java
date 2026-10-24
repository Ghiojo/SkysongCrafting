package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public abstract class OutputInstance {
    protected ArrayList<SkysongItemWithAmmount> outputItems;
    protected ArrayList<String> outputStrings;
    protected OutputType outputType;

    public OutputInstance(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, OutputType outputType) {
        this.outputItems = outputItems;
        this.outputStrings = outputStrings;
        this.outputType = outputType;
    }

    public ArrayList<SkysongItemWithAmmount> getOutputItems() {
        return outputItems;
    }

    /**
     * Produces the items for one craft.
     *
     * @param markedItems copies of the marked recipe inputs, in marked order (empty for non-marking recipes)
     * @throws OutputException if the output cannot be produced; the craft is then refused and nothing is consumed
     */
    public abstract ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems) throws OutputException;

    public abstract ArrayList<String> getOutputStrings();
}
