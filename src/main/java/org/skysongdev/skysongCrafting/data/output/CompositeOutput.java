package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.steps.OutputStep;

import java.util.ArrayList;

public class CompositeOutput extends OutputInstance{
    private ArrayList<OutputStep> outputSteps;

    public CompositeOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, ArrayList<OutputStep> outputSteps) {
        super(outputItems, outputStrings, OutputType.COMPOSITE);
        this.outputSteps = outputSteps;
    }

    public ArrayList<OutputStep> getOutputSteps() {
        return outputSteps;
    }

    @Override
    public ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems) {
        StepData stepData = new StepData(markedItems, null, new ArrayList<SkysongItemWithAmmount>());
        for(OutputStep step : outputSteps){
            stepData = step.executeStep(stepData);
            if(!step.goToNextStep()){
                return new ArrayList<SkysongItemWithAmmount>();
            }
        }
        return stepData.getOutputItems();
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        return outputStrings;
    }

}
