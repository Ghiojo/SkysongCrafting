package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.item.TemplateItem;

import java.util.ArrayList;

public class RandomIdOutput extends BasicOutput {
    private String idToRandomize;

    public String getIdToRandomize() {
        return idToRandomize;
    }

    /** @param idToRandomize the SkysongItems string tag that gets a fresh UUID, lowercase */
    public RandomIdOutput(ArrayList<TemplateItem> outputItems, ArrayList<String> outputStrings, String idToRandomize) {
        super(outputItems, outputStrings);
        outputType = OutputType.RANDOM_ID;
        this.idToRandomize = idToRandomize;
    }
}
