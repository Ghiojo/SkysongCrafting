package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShapedRecipeInstance extends RecipeInstance {
    /** Crafting ids per grid slot ("" for empty), already stripped of consumption suffixes. */
    protected ArrayList<String> materialConsumption;
    /** One rule per grid slot, parallel to {@link #materialConsumption}; defaults to consume-one. */
    protected List<ConsumptionRule> slotRules = new ArrayList<>();

    public ShapedRecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, ArrayList<String> materialConsumption, Map<Utils.Skills, Integer> skillRequirements) {
        super(output, displayItem, recipeName, craftingTableType, craftingSound, skillRequirements);
        this.materialConsumption = materialConsumption;
    }

    public void setSlotRules(List<ConsumptionRule> slotRules) {
        this.slotRules = slotRules == null ? new ArrayList<>() : slotRules;
    }

    @Override
    public void ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory) {
        for (int i = 0; i < materialConsumption.size() && i < inputSlots.size(); i++) {
            if (materialConsumption.get(i) == null || materialConsumption.get(i).isEmpty()) continue;
            ConsumptionRule rule = i < slotRules.size() ? slotRules.get(i) : ConsumptionRule.CONSUME_ONE;
            consumeSlot(inventory, inputSlots.get(i), rule);
        }
    }
}
