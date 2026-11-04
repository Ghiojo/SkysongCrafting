package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.item.ResultItem;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTemplates;

import java.util.Set;

/**
 * Adds the pivot item to the craft's results.
 * <pre>
 *   - type: save
 *     amount: 1
 * </pre>
 * The item is rebuilt by SkysongItems from the data it stores ({@link ItemTemplates#rebuild}), so
 * only SkysongItems data (name, lore, model, properties, registered tags, custom model data) is kept.
 */
public class SaveStep extends OutputStep {
    private static final Set<Material> UNSUPPORTED = Set.of(
            Material.PLAYER_HEAD, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);

    private final int amount;

    public SaveStep(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String getType() {
        return "save";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack pivot = requirePivot(stepData);
        ItemTemplates templates = SkysongApis.items().templates();

        if (!templates.isSkysongItem(pivot)) {
            throw new OutputException("the item being saved is not a SkysongItem");
        }
        // A rebuild keeps only what SkysongItems stores, which loses a head's owner and a potion's effects.
        if (UNSUPPORTED.contains(pivot.getType())) {
            throw new OutputException("saving " + pivot.getType() + " items is not supported by composite outputs yet");
        }

        ItemStack rebuilt;
        try {
            rebuilt = templates.rebuild(pivot);
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems could not rebuild the item: " + e.getMessage());
        }
        stepData.getOutputItems().add(new ResultItem(rebuilt, amount));
        return stepData;
    }
}
