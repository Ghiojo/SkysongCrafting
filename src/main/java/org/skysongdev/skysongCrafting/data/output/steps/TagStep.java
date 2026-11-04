package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.TagType;

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
 * </pre>
 * Values may be placeholders, e.g. {@code "marked:1;tag:dye_color"}.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * the value is stored, no lore is redrawn and no event fires.
 */
public class TagStep extends OutputStep {
    public enum Operation { SET, ADD, REMOVE }

    private final Operation operation;
    private final String tagId;
    private final List<String> values;

    public TagStep(Operation operation, String tagId, List<String> values) {
        this.operation = operation;
        this.tagId = tagId;
        this.values = values;
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
