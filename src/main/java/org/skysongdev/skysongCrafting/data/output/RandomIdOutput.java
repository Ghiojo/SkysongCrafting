package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;

import java.util.ArrayList;

public class RandomIdOutput extends BasicOutput {
    private String idToRandomize;

    public String getIdToRandomize() {
        return idToRandomize;
    }

    public RandomIdOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, String idToRandomize) {
        super(outputItems, outputStrings);
        outputType = OutputType.RANDOM_ID;
        this.idToRandomize = idToRandomize;
    }
}
