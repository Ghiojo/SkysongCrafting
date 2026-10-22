package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds or removes a SkysongItems property on the pivot item.
 * <pre>
 *   - type: property
 *     operation: set | remove
 *     property_id: painted
 * </pre>
 * Setting a property the item already has, or removing one it lacks, is not an error.
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
        if (!Utils.isAProperty(propertyId)) {
            throw new OutputException("'" + propertyId + "' is not a property registered in SkysongItems");
        }
        ItemStack item = requirePivot(stepData);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String stored = container.get(Constants.propertiesKey, PersistentDataType.STRING);
        List<String> properties = new ArrayList<>();
        if (stored != null && !stored.isEmpty()) {
            properties.addAll(Arrays.asList(stored.split(";")));
        }

        switch (operation) {
            case SET:
                if (!properties.contains(propertyId)) properties.add(propertyId);
                break;
            case REMOVE:
                properties.remove(propertyId);
                break;
        }

        if (properties.isEmpty()) {
            container.remove(Constants.propertiesKey);
        } else {
            container.set(Constants.propertiesKey, PersistentDataType.STRING, String.join(";", properties));
        }
        item.setItemMeta(meta);
        stepData.setPivotItem(item);
        return stepData;
    }
}
