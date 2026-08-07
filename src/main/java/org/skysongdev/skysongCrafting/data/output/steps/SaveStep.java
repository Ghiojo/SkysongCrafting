package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.SkysongItem;

public class SaveStep extends OutputStep {
    int amount;

    public SaveStep(int amount) {
        this.amount = amount;
    }

    @Override
    public StepData executeStep(StepData stepData) {
        StepData returnData = stepData;

        ItemStack pivotItem = returnData.getPivotItem();

        SkysongItem skysongItem = new SkysongItem(pivotItem);

        SkysongItemWithAmmount skysongItemWithAmmount = new SkysongItemWithAmmount(skysongItem, amount);

        returnData.getOutputItems().add(skysongItemWithAmmount);

        return returnData;
    }

    @Override
    public boolean goToNextStep() {
        return doesGoToNextStep;
    }
}
