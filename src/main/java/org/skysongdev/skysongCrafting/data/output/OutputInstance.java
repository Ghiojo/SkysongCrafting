package org.skysongdev.skysongCrafting.data.output;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.ResultItem;
import org.skysongdev.skysongCrafting.data.item.TemplateItem;
import org.skysongdev.skysongCrafting.utils.SkysongApis;

import java.util.ArrayList;
import java.util.Optional;

public abstract class OutputInstance {
    protected ArrayList<TemplateItem> outputItems;
    protected ArrayList<String> outputStrings;
    protected OutputType outputType;

    public OutputInstance(ArrayList<TemplateItem> outputItems, ArrayList<String> outputStrings, OutputType outputType) {
        this.outputItems = outputItems;
        this.outputStrings = outputStrings;
        this.outputType = outputType;
    }

    public ArrayList<TemplateItem> getOutputItems() {
        return outputItems;
    }

    /**
     * Produces the items for one craft. Every returned stack is new.
     *
     * @param markedItems copies of the marked recipe inputs, in marked order (empty for non-marking recipes)
     * @throws OutputException if the output cannot be produced; the craft is then refused and nothing is consumed
     */
    public abstract ArrayList<ResultItem> getOutput(ArrayList<MarkedItem> markedItems) throws OutputException;

    public abstract ArrayList<String> getOutputStrings();

    /**
     * Creates a fresh item from a SkysongItems saved item. The saved item is read on every craft,
     * so an item re-saved with {@code /si saveitem} changes crafts straight away.
     *
     * @throws OutputException when SkysongItems no longer has a saved item with that id
     */
    protected static ResultItem create(TemplateItem templateItem) throws OutputException {
        Optional<ItemStack> item = SkysongApis.items().templates().create(templateItem.templateId());
        if (item.isEmpty()) {
            throw new OutputException("SkysongItems has no saved item named '" + templateItem.templateId() + "'");
        }
        return new ResultItem(item.get(), templateItem.amount());
    }
}
