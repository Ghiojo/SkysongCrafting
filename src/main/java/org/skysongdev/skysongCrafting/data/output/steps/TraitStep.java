package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTraits;

/**
 * Adds or removes a SkysongItems trait on the pivot item, for good or for a while.
 * <pre>
 *   - type: trait
 *     operation: set | remove
 *     trait_id: painted
 *     duration: 24h                 # optional: s, m, h, d, w, mo, y, combinable (1d12h)
 * </pre>
 * Without {@code duration} the change is for good. With it, SkysongItems stores a timer on the
 * item: a set trait is removed once the time has passed, a removed trait is added back.
 * <p>
 * Setting a trait the item already has, or removing one it lacks, is not an error. Setting a
 * trait for a while on an item that already has it for good leaves it for good; on an item
 * that has it for a while, its time starts again. The legacy spelling {@code type: property}
 * with {@code property_id} reads into this step too.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * only the traits list and timers change (no value tags are set, no lore is redrawn, no event fires).
 */
public class TraitStep extends OutputStep {
    public enum Operation { SET, REMOVE }

    /** The action SkysongItems stores on a timer that takes a trait off, see {@code TimerChange.removeTrait}. */
    private static final String REMOVE_TRAIT_ACTION = "remove_trait";

    private final Operation operation;
    private final String traitId;
    private final @Nullable String duration;

    public TraitStep(Operation operation, String traitId) {
        this(operation, traitId, null);
    }

    /**
     * @param duration how long the change lasts, as SkysongItems reads it ({@code 24h}, {@code 1d12h}),
     *                 or null for a change for good
     */
    public TraitStep(Operation operation, String traitId, @Nullable String duration) {
        this.operation = operation;
        this.traitId = traitId;
        this.duration = duration;
    }

    public @Nullable String getDuration() {
        return duration;
    }

    @Override
    public String getType() {
        return "trait";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemAccess access = SkysongApis.items().detached();
        ItemTraits traits = access.traits();
        if (!traits.exists(traitId)) {
            throw new OutputException("'" + traitId + "' is not a trait registered in SkysongItems");
        }
        ItemStack item = requirePivot(stepData);
        try {
            if (duration == null) {
                changeForGood(traits, item);
            } else {
                changeForAWhile(access, item);
            }
        } catch (IllegalArgumentException e) {
            // An unknown trait, or a duration SkysongItems doesn't read as a time.
            throw new OutputException("SkysongItems refused to change trait '" + traitId + "': " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            // The server runs a SkysongItems older than the API this plugin is built against.
            throw new OutputException("timed traits need SkysongItems 1.2.0 or newer on the server");
        }
        stepData.setPivotItem(item);
        return stepData;
    }

    private void changeForGood(ItemTraits traits, ItemStack item) {
        switch (operation) {
            case SET:
                traits.add(item, traitId);
                break;
            case REMOVE:
                traits.remove(item, traitId);
                break;
        }
    }

    private void changeForAWhile(ItemAccess access, ItemStack item) {
        ItemTraits traits = access.traits();
        switch (operation) {
            case SET:
                // addFor on an item that has the trait for good would start a removal timer on it,
                // so a craft that gives a trait for a while never takes away one the item already owns.
                if (traits.has(item, traitId) && !hasRemovalTimer(access, item)) {
                    return;
                }
                traits.addFor(item, traitId, duration);
                break;
            case REMOVE:
                traits.removeFor(item, traitId, duration);
                break;
        }
    }

    /** Whether the item has a timer that will take this trait off, i.e. it has the trait only for a while. */
    private boolean hasRemovalTimer(ItemAccess access, ItemStack item) {
        for (ItemTimer timer : access.timers().list(item)) {
            if (REMOVE_TRAIT_ACTION.equals(timer.action()) && traitId.equals(timer.target())) {
                return true;
            }
        }
        return false;
    }
}
