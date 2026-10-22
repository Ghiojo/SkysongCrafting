package org.skysongdev.skysongCrafting.data.output;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.steps.OutputStep;

import java.util.ArrayList;
import java.util.List;

/**
 * An output defined as a template of instructions ({@link OutputStep}s) that is executed
 * top to bottom on every craft. See docs/COMPOSITE_FORMAT.md for the YAML format.
 */
public class CompositeOutput extends OutputInstance{
    private final List<OutputStep> steps;

    public CompositeOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, List<OutputStep> steps) {
        super(outputItems, outputStrings, OutputType.COMPOSITE);
        this.steps = steps;
    }

    public List<OutputStep> getSteps() {
        return steps;
    }

    @Override
    public ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<MarkedItem> markedItems) throws OutputException {
        StepData data = new StepData(markedItems, null, new ArrayList<>());
        for (int i = 0; i < steps.size(); i++) {
            OutputStep step = steps.get(i);
            try {
                data = step.execute(data);
            } catch (OutputException e) {
                throw new OutputException("Step " + (i + 1) + " (" + step.getType() + "): " + e.getMessage());
            } catch (RuntimeException e) {
                // A bug in a step must never cost the player their items.
                throw new OutputException("Step " + (i + 1) + " (" + step.getType() + ") failed unexpectedly: " + e);
            }
        }
        if (data.getOutputItems().isEmpty()) {
            throw new OutputException("the template produced no items; add a 'save' step");
        }
        return data.getOutputItems();
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        return outputStrings;
    }
}
