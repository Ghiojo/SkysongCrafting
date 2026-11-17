package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.api.TimerChange;

/**
 * Makes the pivot item transform into its next form later, or on a repeat, as {@code /si transform
 * after|every} does.
 * <pre>
 *   - type: transform
 *     after: 7d                # or every: 1mo; one of the two is required
 * </pre>
 * The craft doesn't transform the item itself (SkysongItems only transforms an item a player
 * holds); it stores the timer, and SkysongItems moves the item to its next form once the time has
 * passed. An item without a transformation is left as it is when the timer comes due.
 */
public class TransformStep extends OutputStep {
    private final StepTiming timing;

    public TransformStep(StepTiming timing) {
        if (!timing.isScheduled()) {
            throw new IllegalArgumentException("'transform' needs 'after' or 'every'");
        }
        this.timing = timing;
    }

    public StepTiming getTiming() {
        return timing;
    }

    @Override
    public String getType() {
        return "transform";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack item = requirePivot(stepData);
        schedule(item, TimerChange.transform(), timing);
        stepData.setPivotItem(item);
        return stepData;
    }
}
