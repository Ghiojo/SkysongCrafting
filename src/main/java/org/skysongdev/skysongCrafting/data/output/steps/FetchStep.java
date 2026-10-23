package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.Constants;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;

/**
 * Sets the pivot item.
 * <pre>
 *   - type: fetch
 *     fetch_type: marked      # edit one of the marked recipe inputs (a copy)
 *     fetch_id: "0"           # marked index
 *
 *   - type: fetch
 *     fetch_type: generated   # start from a fresh SkysongItem
 *     fetch_id: pavise_shield # item file name in the items folder (without extension)
 * </pre>
 */
public class FetchStep extends OutputStep {
    public enum FetchType { MARKED, GENERATED }

    private final String fetchId;
    private final FetchType fetchType;

    public FetchStep(String fetchId, FetchType fetchType) {
        this.fetchId = fetchId;
        this.fetchType = fetchType;
    }

    @Override
    public String getType() {
        return "fetch";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        switch (fetchType) {
            case GENERATED: {
                File itemFile = Constants.resolveItemFile(fetchId);
                if (itemFile == null) {
                    throw new OutputException("no item file named '" + fetchId + "' in the items folder");
                }
                SkysongItem item = Utils.LoadSkysongItem(itemFile);
                ItemStack generated = SkysongItems.getPlugin().getUtils().GenerateSkysongItem(item);
                stepData.setPivotItem(generated);
                return stepData;
            }
            case MARKED: {
                // The marked items are already copies, but clone again so later placeholders that read
                // marked:N keep seeing the input as it was placed, not the half-edited pivot.
                ItemStack marked = PlaceholderParser.resolveReference("marked:" + fetchId, stepData);
                stepData.setPivotItem(marked.clone());
                return stepData;
            }
            default:
                throw new OutputException("unknown fetch_type " + fetchType);
        }
    }
}
