package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;

/**
 * One instruction of a composite output template.
 * <p>
 * Steps are stateless: the same instance is shared by every craft of the recipe, so all
 * per-craft information lives in the {@link StepData} that is passed through the pipeline.
 * A step that cannot proceed throws {@link OutputException}; the composite output turns
 * that into a refused craft with the message shown to the player.
 */
public abstract class OutputStep {

    /** The {@code type} value used in the YAML template, for messages. */
    public abstract String getType();

    public abstract StepData execute(StepData stepData) throws OutputException;

    /** Returns the pivot item, failing with a clear message if no fetch step has set one yet. */
    protected ItemStack requirePivot(StepData stepData) throws OutputException {
        ItemStack pivot = stepData.getPivotItem();
        if (pivot == null || pivot.getType() == Material.AIR) {
            throw new OutputException("no item to work on; add a 'fetch' step before this one");
        }
        return pivot;
    }
}
