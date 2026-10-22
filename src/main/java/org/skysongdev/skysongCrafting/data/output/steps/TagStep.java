package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;

/**
 * Edits a SkysongItems tag on the pivot item.
 * <pre>
 *   - type: tag
 *     operation: set | add | remove
 *     tag_id: layer_count
 *     values: ["1"]                       # set/add only; numeric tags sum all values, others take the first
 * </pre>
 * Values may be placeholders, e.g. {@code "marked:1;tag:dye_color"}.
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StepData execute(StepData stepData) throws OutputException {
        if (!Utils.isATag(tagId)) {
            throw new OutputException("'" + tagId + "' is not a tag registered in SkysongItems");
        }
        NamespacedKey key = Utils.getTag(tagId);
        PersistentDataType type = Constants.tagList.get(key);

        ItemStack item = requirePivot(stepData);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        switch (operation) {
            case REMOVE:
                if (!container.has(key)) {
                    throw new OutputException("item has no '" + tagId + "' tag to remove");
                }
                container.remove(key);
                break;
            case SET:
                container.set(key, type, resolveValue(type, stepData));
                break;
            case ADD: {
                if (type != PersistentDataType.INTEGER && type != PersistentDataType.FLOAT) {
                    throw new OutputException("'add' only works on number tags; '" + tagId + "' is not a number tag (use 'set')");
                }
                Object delta = resolveValue(type, stepData);
                if (!container.has(key, type)) {
                    container.set(key, type, delta);
                } else if (type == PersistentDataType.INTEGER) {
                    container.set(key, type, container.get(key, PersistentDataType.INTEGER) + (Integer) delta);
                } else {
                    container.set(key, type, container.get(key, PersistentDataType.FLOAT) + (Float) delta);
                }
                break;
            }
        }
        item.setItemMeta(meta);
        stepData.setPivotItem(item);
        return stepData;
    }

    /** Converts the configured values into the tag's own type. */
    private Object resolveValue(PersistentDataType<?, ?> type, StepData stepData) throws OutputException {
        if (values == null || values.isEmpty()) {
            throw new OutputException("'" + operation.name().toLowerCase() + "' on tag '" + tagId + "' needs at least one entry in 'values'");
        }
        if (type == PersistentDataType.INTEGER || type == PersistentDataType.FLOAT) {
            float sum = 0;
            for (String value : values) {
                sum += PlaceholderParser.resolveFloat(value, stepData, "value for tag '" + tagId + "'");
            }
            return type == PersistentDataType.INTEGER ? (Object) Math.round(sum) : (Object) sum;
        }
        if (type == PersistentDataType.BOOLEAN) {
            return PlaceholderParser.resolveBoolean(values.get(0), stepData, "value for tag '" + tagId + "'");
        }
        if (type == PersistentDataType.STRING) {
            return PlaceholderParser.resolve(values.get(0), stepData);
        }
        throw new OutputException("tag '" + tagId + "' has a type this step cannot edit (" + type + ")");
    }
}
