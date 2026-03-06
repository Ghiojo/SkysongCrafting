package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public class CompositeOutput extends OutputInstance{
    private ArrayList<String> outputSteps;

    public CompositeOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, ArrayList<String> outputSteps) {
        super(outputItems, outputStrings, OutputType.COMPOSITE);
        this.outputSteps = outputSteps;
    }

    @Override
    public ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems) {
        // The format for each step would be something like:
        // grab;type:item (Grabs an item from the type input, can be output or marked, "item" is the position of the item to  grab, saves it as the current item)
        // add/remove/set;type:reference;amount (edits a field on the current item, type can be tag, property, cmd (custommodeldata))
        // save (saves the current item into the output list)
        // command;sender:cmd (Runs a command, sender can be player or console, cmd can use {player} and {item} as placeholders)
        return null;
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        return null;
    }
}
