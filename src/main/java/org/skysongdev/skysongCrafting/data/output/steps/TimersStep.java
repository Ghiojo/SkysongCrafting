package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTimers;

import java.util.List;

/**
 * Cancels SkysongItems timers on the pivot item, for example a repair craft that stops a trait
 * from running out.
 * <pre>
 *   - type: timers
 *     operation: clear
 *     action: remove_trait     # optional: only timers with this action, e.g. remove_trait, add_tag, transform
 *     target: honed            # optional: only timers about this tag or trait
 * </pre>
 * Without {@code action} and {@code target} every timer on the item is cancelled. Cancelling
 * timers the item doesn't have is not an error.
 */
public class TimersStep extends OutputStep {
    public enum Operation { CLEAR }

    private final @Nullable String action;
    private final @Nullable String target;

    public TimersStep(@Nullable String action, @Nullable String target) {
        this.action = action;
        this.target = target;
    }

    @Override
    public String getType() {
        return "timers";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack item = requirePivot(stepData);
        try {
            ItemTimers timers = SkysongApis.items().detached().timers();
            if (action == null && target == null) {
                if (!timers.list(item).isEmpty()) {
                    timers.clear(item);
                }
            } else {
                List<ItemTimer> stored = timers.list(item);
                // From the end, so cancelling one doesn't move the ones still to check.
                for (int i = stored.size() - 1; i >= 0; i--) {
                    if (matches(stored.get(i))) {
                        timers.cancel(item, i);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to cancel the item's timers: " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            throw newerSkysongItemsNeeded();
        }
        stepData.setPivotItem(item);
        return stepData;
    }

    private boolean matches(ItemTimer timer) {
        return (action == null || action.equalsIgnoreCase(timer.action()))
                && (target == null || target.equals(timer.target()));
    }
}
