package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTimers;
import org.skysongdev.skysongitems.api.TimerChange;

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

    /**
     * Stores a SkysongItems timer on the pivot that makes {@code change} after, or every,
     * {@code timing}'s time. The pivot is a copy nobody holds yet, so it goes through the detached view.
     *
     * @throws OutputException when SkysongItems refuses the timer: a time it doesn't read, or a
     *                         change that can't apply to the item
     */
    protected static void schedule(ItemStack item, TimerChange change, StepTiming timing) throws OutputException {
        ItemTimers timers = SkysongApis.items().detached().timers();
        try {
            switch (timing.kind()) {
                case AFTER -> timers.after(item, change, timing.time());
                case EVERY -> timers.every(item, change, timing.time());
                default -> throw new IllegalStateException("not a scheduled timing: " + timing.describe());
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused the timer '" + timing.describe() + "': " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            throw newerSkysongItemsNeeded();
        }
    }

    /** The message for a server whose SkysongItems is older than the API this plugin is built against. */
    protected static OutputException newerSkysongItemsNeeded() {
        return new OutputException("item timers need SkysongItems 1.2.0 or newer on the server");
    }
}
