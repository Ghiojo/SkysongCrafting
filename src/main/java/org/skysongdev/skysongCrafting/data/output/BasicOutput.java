package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.ResultItem;
import org.skysongdev.skysongCrafting.data.item.TemplateItem;

import java.util.ArrayList;

public class BasicOutput extends OutputInstance{
    public BasicOutput(ArrayList<TemplateItem> outputItems, ArrayList<String> outputStrings) {
        super(outputItems, outputStrings, OutputType.BASIC);
    }

    @Override
    public ArrayList<ResultItem> getOutput(ArrayList<MarkedItem> markedItems) throws OutputException {
        ArrayList<ResultItem> results = new ArrayList<>();
        for (TemplateItem item : outputItems) {
            results.add(create(item));
        }
        return results;
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        return outputStrings;
    }
}
