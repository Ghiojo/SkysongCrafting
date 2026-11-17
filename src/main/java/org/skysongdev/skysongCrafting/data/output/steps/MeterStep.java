package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemHp;
import org.skysongdev.skysongitems.api.TimerChange;

import java.util.List;

/**
 * Edits the pivot item's SkysongItems charges or HP, now, for a while, later or on a repeat.
 * <pre>
 *   - type: charges            # or hp
 *     operation: set | add | set_max | remove
 *     values: ["2"]            # not for remove; all entries are summed; placeholders allowed
 *     duration: 24h            # optional, at most one of duration / after / every (see StepTiming)
 * </pre>
 * <ul>
 *     <li>{@code set}: the amount, capped at the max (an item without any gets it as its max).</li>
 *     <li>{@code add}: adds the amount, a negative one takes it off; stays between 0 and the max.
 *     With {@code duration}, the same amount is taken back off (or given back) once the time has passed.</li>
 *     <li>{@code set_max}: the max.</li>
 *     <li>{@code remove}: takes the charges or HP off the item; only with {@code after} or {@code every},
 *     as SkysongItems has no direct way to do it.</li>
 * </ul>
 * Only {@code add} takes a {@code duration}. With {@code after} / {@code every} the craft doesn't change
 * the item; SkysongItems makes the change once the time has passed, or each time it passes.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view.
 */
public class MeterStep extends OutputStep {
    public enum Meter {
        CHARGES("charges"), HP("hp");

        private final String type;

        Meter(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }
    }

    public enum Operation { SET, ADD, SET_MAX, REMOVE }

    private final Meter meter;
    private final Operation operation;
    private final List<String> values;
    private final StepTiming timing;

    /**
     * @throws IllegalArgumentException when the operation and timing don't go together, or a
     *                                  value is missing; the parser checks both first
     */
    public MeterStep(Meter meter, Operation operation, List<String> values, StepTiming timing) {
        if (!allows(operation, timing.kind())) {
            throw new IllegalArgumentException(describeAllowed(operation));
        }
        if (operation != Operation.REMOVE && values.isEmpty()) {
            throw new IllegalArgumentException("'" + operation.name().toLowerCase() + "' needs a 'values' list");
        }
        this.meter = meter;
        this.operation = operation;
        this.values = List.copyOf(values);
        this.timing = timing;
    }

    /** Whether SkysongItems can make this change with this timing. */
    public static boolean allows(Operation operation, StepTiming.Kind kind) {
        return switch (operation) {
            case ADD -> true;
            case SET, SET_MAX -> kind != StepTiming.Kind.FOR;
            case REMOVE -> kind == StepTiming.Kind.AFTER || kind == StepTiming.Kind.EVERY;
        };
    }

    /** Why a timing isn't allowed for the operation, for load-time warnings. */
    public static String describeAllowed(Operation operation) {
        return switch (operation) {
            case ADD -> "";
            case SET, SET_MAX -> "'" + operation.name().toLowerCase() + "' can't take a duration (use after or every, or 'add' with a duration)";
            case REMOVE -> "'remove' needs 'after' or 'every'";
        };
    }

    public Meter getMeter() {
        return meter;
    }

    public Operation getOperation() {
        return operation;
    }

    public StepTiming getTiming() {
        return timing;
    }

    @Override
    public String getType() {
        return meter.type();
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack item = requirePivot(stepData);
        int amount = operation == Operation.REMOVE ? 0 : resolveAmount(stepData);
        try {
            if (timing.isScheduled()) {
                schedule(item, change(amount), timing);
            } else {
                applyNow(SkysongApis.items().detached(), item, amount);
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to change the item's " + meter.type() + ": " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            throw newerSkysongItemsNeeded();
        }
        stepData.setPivotItem(item);
        return stepData;
    }

    private void applyNow(ItemAccess access, ItemStack item, int amount) {
        boolean forAWhile = timing.kind() == StepTiming.Kind.FOR;
        if (meter == Meter.CHARGES) {
            ItemCharges charges = access.charges();
            switch (operation) {
                case SET -> charges.set(item, amount);
                case ADD -> {
                    if (forAWhile) charges.addFor(item, amount, timing.time());
                    else charges.add(item, amount);
                }
                case SET_MAX -> charges.setMax(item, amount);
                case REMOVE -> throw new IllegalStateException(describeAllowed(operation));
            }
        } else {
            ItemHp hp = access.itemHp();
            switch (operation) {
                case SET -> hp.set(item, amount);
                case ADD -> {
                    if (forAWhile) hp.addFor(item, amount, timing.time());
                    else hp.add(item, amount);
                }
                case SET_MAX -> hp.setMax(item, amount);
                case REMOVE -> throw new IllegalStateException(describeAllowed(operation));
            }
        }
    }

    private TimerChange change(int amount) {
        boolean charges = meter == Meter.CHARGES;
        return switch (operation) {
            case SET -> charges ? TimerChange.setCharges(amount) : TimerChange.setHp(amount);
            case ADD -> charges ? TimerChange.addCharges(amount) : TimerChange.addHp(amount);
            case SET_MAX -> charges ? TimerChange.setMaxCharges(amount) : TimerChange.setMaxHp(amount);
            case REMOVE -> charges ? TimerChange.removeCharges() : TimerChange.removeHp();
        };
    }

    private int resolveAmount(StepData stepData) throws OutputException {
        float sum = 0;
        for (String value : values) {
            sum += PlaceholderParser.resolveFloat(value, stepData, "value for " + meter.type());
        }
        return Math.round(sum);
    }
}
