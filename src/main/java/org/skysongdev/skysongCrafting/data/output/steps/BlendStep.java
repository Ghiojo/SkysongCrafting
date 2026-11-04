package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Color;
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
        ItemTags tags = SkysongApis.items().detached().tags();
        Optional<TagType> type = tags.type(tagId);
        if (type.isEmpty()) {
            throw new OutputException("'" + tagId + "' is not a tag registered in SkysongItems");
        }
        if (type.get() != TagType.INT) {
            throw new OutputException("'" + tagId + "' is not an integer tag, so it cannot hold a colour");
        }
        if (sources == null || sources.size() < 2) {
            throw new OutputException("'blend' needs at least two entries in 'sources'");
        }

        Color result = null;
        for (String source : sources) {
            ItemStack item = PlaceholderParser.resolveReference(source, stepData);
            OptionalInt value = tags.getInt(item, tagId);
            if (value.isEmpty()) {
                throw new OutputException("item '" + source + "' has no '" + tagId + "' tag");
            }
            Color color;
            try {
                color = Color.fromRGB(value.getAsInt());
            } catch (IllegalArgumentException e) {
                throw new OutputException("item '" + source + "' has '" + tagId + "' = " + value.getAsInt() + ", which is not an RGB colour");
            }
            result = result == null ? color : result.mixColors(color);
        }

        ItemStack pivot = requirePivot(stepData);
        try {
            tags.set(pivot, tagId, result.asRGB());
        } catch (IllegalArgumentException e) {
            throw new OutputException("SkysongItems refused to change '" + tagId + "': " + e.getMessage());
        }
        stepData.setPivotItem(pivot);
        return stepData;
    }
}
