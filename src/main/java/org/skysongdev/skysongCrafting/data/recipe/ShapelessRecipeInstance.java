package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.*;

import static org.skysongdev.skysongCrafting.utils.Constants.CRAFTING_KEY;

public class ShapelessRecipeInstance extends RecipeInstance{
    protected ArrayList<MaterialCount> materialConsumption;
    /** Consumption rule per crafting id (lower-case); ids not present consume one. */
    protected Map<String, ConsumptionRule> rulesById = new HashMap<>();

    public ShapelessRecipeInstance(ArrayList<MaterialCount> materialConsumption, OutputInstance output, Material displayItemMaterial, String recipeName, String craftingTableType, String craftingSound, Map<Utils.Skills, Integer> skillRequirements) {
        super(output, displayItemMaterial, recipeName, craftingTableType, craftingSound, skillRequirements);
        this.materialConsumption = new ArrayList<>(materialConsumption);
        this.materialConsumption.sort(Comparator.comparing(MaterialCount::getId, String.CASE_INSENSITIVE_ORDER));
    }

    public void setRulesById(Map<String, ConsumptionRule> rulesById) {
        this.rulesById = rulesById == null ? new HashMap<>() : rulesById;
    }

    @Override
    public void ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory) {
        if (inputSlots == null || inventory == null) return;

        for (Integer slotIndex : inputSlots) {
            if (slotIndex == null) continue;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) continue;

            ConsumptionRule rule = ConsumptionRule.CONSUME_ONE;
            if (!rulesById.isEmpty() && item.getItemMeta() != null) {
                String id = item.getItemMeta().getPersistentDataContainer().get(CRAFTING_KEY, PersistentDataType.STRING);
                if (id != null) rule = rulesById.getOrDefault(id.toLowerCase(Locale.ROOT), ConsumptionRule.CONSUME_ONE);
            }
            consumeSlot(inventory, slotIndex, rule);
        }
    }
}
