package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.SkysongApis;

import java.util.Optional;

/**
 * Sets the pivot item.
 * <pre>
 *   - type: fetch
 *     fetch_type: marked      # edit one of the marked recipe inputs (a copy)
 *     fetch_id: "0"           # marked index
 *
 *   - type: fetch
 *     fetch_type: generated   # start from a fresh SkysongItems saved item
 *     fetch_id: pavise_shield # the saved item's id, as /si loaditem takes it (e.g. shields/pavise_shield)
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
                Optional<ItemStack> generated = SkysongApis.items().templates().create(fetchId);
                if (generated.isEmpty()) {
                    throw new OutputException("SkysongItems has no saved item named '" + fetchId + "'");
                }
                stepData.setPivotItem(generated.get());
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
