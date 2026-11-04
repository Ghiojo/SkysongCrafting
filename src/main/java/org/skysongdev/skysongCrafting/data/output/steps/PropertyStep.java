package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemProperties;

/**
 * Adds or removes a SkysongItems property on the pivot item.
 * <pre>
 *   - type: property
 *     operation: set | remove
 *     property_id: painted
 * </pre>
 * Setting a property the item already has, or removing one it lacks, is not an error.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * only the properties list changes (no value tags are set, no lore is redrawn, no event fires).
 */
public class PropertyStep extends OutputStep {
    public enum Operation { SET, REMOVE }

    private final Operation operation;
    private final String propertyId;

    public PropertyStep(Operation operation, String propertyId) {
        this.operation = operation;
        this.propertyId = propertyId;
    }

    @Override
    public String getType() {
        return "property";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemProperties properties = SkysongApis.items().detached().properties();
        if (!properties.exists(propertyId)) {
            throw new OutputException("'" + propertyId + "' is not a property registered in SkysongItems");
        }
        ItemStack item = requirePivot(stepData);
        try {
            switch (operation) {
                case SET:
                    properties.add(item, propertyId);
                    break;
                case REMOVE:
                    properties.remove(item, propertyId);
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to change property '" + propertyId + "': " + e.getMessage());
        }
        stepData.setPivotItem(item);
        return stepData;
    }
}
