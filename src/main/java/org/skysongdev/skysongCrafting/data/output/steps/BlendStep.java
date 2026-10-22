package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;

/**
 * Mixes the colours stored in an integer colour tag of two or more items and writes the
 * result to the same tag on the pivot item.
 * <pre>
 *   - type: blend
 *     tag_id: dye_color
 *     sources: ["marked:1", "marked:2"]     # item references (marked:N or pivot)
 * </pre>
 */
public class BlendStep extends OutputStep {
    private final String tagId;
    private final List<String> sources;

    public BlendStep(String tagId, List<String> sources) {
        this.tagId = tagId;
        this.sources = sources;
    }

    @Override
    public String getType() {
        return "blend";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        if (!Utils.isATag(tagId)) {
            throw new OutputException("'" + tagId + "' is not a tag registered in SkysongItems");
        }
        NamespacedKey key = Utils.getTag(tagId);
        if (Constants.tagList.get(key) != PersistentDataType.INTEGER) {
            throw new OutputException("'" + tagId + "' is not an integer tag, so it cannot hold a colour");
        }
        if (sources == null || sources.size() < 2) {
            throw new OutputException("'blend' needs at least two entries in 'sources'");
        }

        Color result = null;
        for (String source : sources) {
            ItemStack item = PlaceholderParser.resolveReference(source, stepData);
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                throw new OutputException("item '" + source + "' has no '" + tagId + "' tag");
            }
            Color color = Color.fromRGB(meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));
            result = result == null ? color : result.mixColors(color);
        }

        ItemStack pivot = requirePivot(stepData);
        ItemMeta pivotMeta = pivot.getItemMeta();
        pivotMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, result.asRGB());
        pivot.setItemMeta(pivotMeta);
        stepData.setPivotItem(pivot);
        return stepData;
    }
}
