package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.TimerChange;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Edits a SkysongItems tag on the pivot item.
 * <pre>
 *   - type: tag
 *     operation: set | add | remove
 *     tag_id: layer_count
 *     values: ["1"]                       # set/add only; numeric tags sum all values, others take the first
 *     duration: 24h                       # optional, at most one of duration / after / every (see StepTiming)
 * </pre>
 * Values may be placeholders, e.g. {@code "marked:1;tag:dye_color"}; they are read at craft time,
 * also for a change that happens later.
 * <ul>
 *     <li>{@code duration}: {@code set} sets the tag and SkysongItems removes it once the time has
 *     passed (it is not restored to an earlier value); {@code add} adds the amount and takes the
 *     same amount back off later. Not available for {@code remove}.</li>
 *     <li>{@code after} / {@code every}: the craft doesn't change the tag; SkysongItems sets, adds to
 *     or removes it once the time has passed, or each time it passes.</li>
 * </ul>
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * the value is stored, no lore is redrawn and no event fires.
 */
public class TagStep extends OutputStep {
    public enum Operation { SET, ADD, REMOVE }

    private final Operation operation;
    private final String tagId;
    private final List<String> values;
    private final StepTiming timing;

    public TagStep(Operation operation, String tagId, List<String> values) {
        this(operation, tagId, values, StepTiming.NOW);
    }

    public TagStep(Operation operation, String tagId, List<String> values, StepTiming timing) {
        if (operation == Operation.REMOVE && timing.kind() == StepTiming.Kind.FOR) {
            throw new IllegalArgumentException("'remove' on a tag can't take a duration");
        }
        this.operation = operation;
        this.tagId = tagId;
        this.values = values;
        this.timing = timing;
    }

    public StepTiming getTiming() {
        return timing;
    }

    @Override
    public String getType() {
        return "tag";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemTags tags = SkysongApis.items().detached().tags();
        Optional<TagType> foundType = tags.type(tagId);
        if (foundType.isEmpty()) {
            throw new OutputException("'" + tagId + "' is not a tag registered in SkysongItems");
        }
        TagType type = foundType.get();
        ItemStack item = requirePivot(stepData);

        if (operation == Operation.ADD && type != TagType.INT && type != TagType.FLOAT) {
            throw new OutputException("'add' only works on number tags; '" + tagId + "' is not a number tag (use 'set')");
        }
        if (timing.kind() != StepTiming.Kind.NOW) {
            applyTimed(tags, type, item, stepData);
            stepData.setPivotItem(item);
            return stepData;
        }

        try {
            switch (operation) {
                case REMOVE:
                    if (!tags.remove(item, tagId)) {
                        throw new OutputException("item has no '" + tagId + "' tag to remove");
                    }
                    break;
                case SET:
                    tags.set(item, tagId, resolveValue(type, stepData));
                    break;
                case ADD: {
                    if (type == TagType.INT) {
                        int delta = (Integer) resolveValue(type, stepData);
                        OptionalInt current = tags.getInt(item, tagId);
                        tags.set(item, tagId, current.isPresent() ? current.getAsInt() + delta : delta);
                    } else if (type == TagType.FLOAT) {
                        float delta = (Float) resolveValue(type, stepData);
                        Optional<Float> current = tags.getFloat(item, tagId);
                        tags.set(item, tagId, current.isPresent() ? current.get() + delta : delta);
                    } else {
                        throw new OutputException("'add' only works on number tags; '" + tagId + "' is not a number tag (use 'set')");
                    }
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            // SkysongItems refused the write, for example the read-only 'version' tag.
            throw new OutputException("SkysongItems refused to change '" + tagId + "': " + e.getMessage());
        }
        stepData.setPivotItem(item);
        return stepData;
    }

    /** The change for a while, later or on a repeat; the values are read now, at craft time. */
    private void applyTimed(ItemTags tags, TagType type, ItemStack item, StepData stepData) throws OutputException {
        try {
            if (timing.isScheduled()) {
                TimerChange change = switch (operation) {
                    case SET -> TimerChange.setTag(tagId, resolveValue(type, stepData));
                    case ADD -> TimerChange.addToTag(tagId, (Number) resolveValue(type, stepData));
                    case REMOVE -> TimerChange.removeTag(tagId);
                };
                schedule(item, change, timing);
                return;
            }
            switch (operation) {
                case SET -> tags.setFor(item, tagId, resolveValue(type, stepData), timing.time());
                case ADD -> tags.addFor(item, tagId, (Number) resolveValue(type, stepData), timing.time());
                case REMOVE -> throw new OutputException("'remove' on a tag can't take a duration");
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to change '" + tagId + "' " + timing.describe() + ": " + e.getMessage());
        } catch (AbstractMethodError | NoSuchMethodError e) {
            throw newerSkysongItemsNeeded();
        }
    }

    /** Converts the configured values into the tag's own type. */
    private Object resolveValue(TagType type, StepData stepData) throws OutputException {
        if (values == null || values.isEmpty()) {
            throw new OutputException("'" + operation.name().toLowerCase() + "' on tag '" + tagId + "' needs at least one entry in 'values'");
        }
        switch (type) {
            case INT:
            case FLOAT: {
                float sum = 0;
                for (String value : values) {
                    sum += PlaceholderParser.resolveFloat(value, stepData, "value for tag '" + tagId + "'");
                }
                return type == TagType.INT ? (Object) Math.round(sum) : (Object) sum;
            }
            case BOOL:
                return PlaceholderParser.resolveBoolean(values.get(0), stepData, "value for tag '" + tagId + "'");
            case STRING:
                return PlaceholderParser.resolve(values.get(0), stepData);
            default:
                throw new OutputException("tag '" + tagId + "' has a type this step cannot edit (" + type + ")");
        }
    }
}
