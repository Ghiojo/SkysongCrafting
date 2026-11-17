package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTraits;
import org.skysongdev.skysongitems.api.TimerChange;

/**
 * Adds or removes a SkysongItems trait on the pivot item, now, for a while, later or on a repeat.
 * <pre>
 *   - type: trait
 *     operation: set | remove
 *     trait_id: painted
 *     duration: 24h       # optional, at most one of duration / after / every (see StepTiming)
 * </pre>
 * <ul>
 *     <li>no time: the change is for good.</li>
 *     <li>{@code duration}: a set trait is removed once the time has passed; a removed trait is
 *     added back. Setting a trait for a while on an item that already has it for good leaves it
 *     for good; on an item that has it for a while, its time starts again.</li>
 *     <li>{@code after} / {@code every}: the item is not changed by the craft; SkysongItems sets or
 *     removes the trait once the time has passed, or each time it passes.</li>
 * </ul>
 * Setting a trait the item already has, or removing one it lacks, is not an error. The legacy
 * spelling {@code type: property} with {@code property_id} reads into this step too.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * only the traits list and timers change (no value tags are set, no lore is redrawn, no event fires).
 */
public class TraitStep extends OutputStep {
    public enum Operation { SET, REMOVE }

    /** The action SkysongItems stores on a timer that takes a trait off, see {@link TimerChange#removeTrait}. */
    private static final String REMOVE_TRAIT_ACTION = TimerChange.removeTrait("").action();

    private final Operation operation;
    private final String traitId;
    private final StepTiming timing;

    public TraitStep(Operation operation, String traitId) {
        this(operation, traitId, StepTiming.NOW);
    }

    public TraitStep(Operation operation, String traitId, StepTiming timing) {
        this.operation = operation;
        this.traitId = traitId;
        this.timing = timing;
    }

    public StepTiming getTiming() {
        return timing;
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
            switch (timing.kind()) {
                case NOW -> changeForGood(traits, item);
                case FOR -> changeForAWhile(access, item);
                case AFTER, EVERY -> schedule(item, operation == Operation.SET
                        ? TimerChange.addTrait(traitId) : TimerChange.removeTrait(traitId), timing);
            }
        } catch (IllegalArgumentException e) {
            // An unknown trait, or a duration SkysongItems doesn't read as a time.
            throw new OutputException("SkysongItems refused to change trait '" + traitId + "': " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            throw newerSkysongItemsNeeded();
        }
        stepData.setPivotItem(item);
        return stepData;
    }

    private void changeForGood(ItemTraits traits, ItemStack item) {
        switch (operation) {
            case SET -> traits.add(item, traitId);
            case REMOVE -> traits.remove(item, traitId);
        }
    }

    private void changeForAWhile(ItemAccess access, ItemStack item) {
        ItemTraits traits = access.traits();
        switch (operation) {
            case SET -> {
                // addFor on an item that has the trait for good would start a removal timer on it,
                // so a craft that gives a trait for a while never takes away one the item already owns.
                if (traits.has(item, traitId) && !hasRemovalTimer(access, item)) {
                    return;
                }
                traits.addFor(item, traitId, timing.time());
            }
            case REMOVE -> traits.removeFor(item, traitId, timing.time());
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
