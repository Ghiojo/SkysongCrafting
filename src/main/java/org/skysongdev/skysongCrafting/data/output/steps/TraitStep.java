package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTraits;

/**
 * Adds or removes a SkysongItems trait on the pivot item.
 * <pre>
 *   - type: trait
 *     operation: set | remove
 *     trait_id: painted
 * </pre>
 * Setting a trait the item already has, or removing one it lacks, is not an error.
 * The legacy spelling {@code type: property} with {@code property_id} reads into this step too.
 * <p>
 * The pivot is a copy nobody holds yet, so the edit goes through SkysongItems' detached view:
 * only the traits list changes (no value tags are set, no lore is redrawn, no event fires).
 */
public class TraitStep extends OutputStep {
    public enum Operation { SET, REMOVE }

    private final Operation operation;
    private final String traitId;

    public TraitStep(Operation operation, String traitId) {
        this.operation = operation;
        this.traitId = traitId;
    }

    @Override
    public String getType() {
        return "trait";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemTraits traits = SkysongApis.items().detached().traits();
        if (!traits.exists(traitId)) {
            throw new OutputException("'" + traitId + "' is not a trait registered in SkysongItems");
        }
        ItemStack item = requirePivot(stepData);
        try {
            switch (operation) {
                case SET:
                    traits.add(item, traitId);
                    break;
                case REMOVE:
                    traits.remove(item, traitId);
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to change trait '" + traitId + "': " + e.getMessage());
        }
        stepData.setPivotItem(item);
        return stepData;
    }
}
