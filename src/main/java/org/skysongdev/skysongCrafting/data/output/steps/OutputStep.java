package org.skysongdev.skysongCrafting.data.output.steps;

import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.data.StepData;

public abstract class OutputStep {
    boolean doesGoToNextStep = true;

    public OutputStep() {}

    public abstract StepData executeStep(StepData stepData);
    public abstract boolean goToNextStep();

}
