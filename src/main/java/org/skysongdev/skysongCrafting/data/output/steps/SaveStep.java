package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.Set;

/**
 * Adds the pivot item to the craft's results.
 * <pre>
 *   - type: save
 *     amount: 1
 * </pre>
 * The item is stored as SkysongItems data and regenerated when placed in the output slots, so
 * only SkysongItems data (name, lore, model, properties, registered tags, custom model data) is kept.
 */
public class SaveStep extends OutputStep {
    private static final Set<Material> UNSUPPORTED = Set.of(
            Material.PLAYER_HEAD, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);

    private final int amount;

    public SaveStep(int amount) {
        this.amount = amount;
    }

    @Override
    public String getType() {
        return "save";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack pivot = requirePivot(stepData);
        ItemMeta meta = pivot.getItemMeta();

        // SkysongItem(ItemStack) reads these tags without null checks.
        if (meta == null
                || !meta.getPersistentDataContainer().has(Constants.versionKey, PersistentDataType.STRING)
                || !meta.getPersistentDataContainer().has(Constants.loreKey, PersistentDataType.STRING)) {
            throw new OutputException("the item being saved is not a SkysongItem");
        }
        // SkysongItems regenerates heads and potions through subclasses that SkysongItem(ItemStack) does not produce.
        if (UNSUPPORTED.contains(pivot.getType())) {
            throw new OutputException("saving " + pivot.getType() + " items is not supported by composite outputs yet");
        }
        // SkysongItem(ItemStack) stores null custom model data when the item has none, and
        // GenerateSkysongItem then dereferences it. Make sure the component exists.
        if (!meta.hasCustomModelDataComponent()) {
            meta.setCustomModelDataComponent(meta.getCustomModelDataComponent());
            pivot.setItemMeta(meta);
        }

        SkysongItem skysongItem = new SkysongItem(pivot);
        stepData.getOutputItems().add(new SkysongItemWithAmmount(skysongItem, amount));
        return stepData;
    }
}
