package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class ShapelessRecipeInstance extends RecipeInstance{
    protected ArrayList<MaterialCount> materialConsumption;

    public ShapelessRecipeInstance(ArrayList<MaterialCount> materialConsumption, OutputInstance output, Material displayItemMaterial, String recipeName, String craftingTableType, String craftingSound, Map<Utils.Skills, Integer> skillRequirements) {
        super(output, displayItemMaterial, recipeName, craftingTableType, craftingSound, skillRequirements);
        this.materialConsumption = new ArrayList<>(materialConsumption);
        this.materialConsumption.sort(Comparator.comparing(MaterialCount::getId, String.CASE_INSENSITIVE_ORDER));
    }


    @Override
    public void ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory) {
        if (inputSlots == null || inventory == null) return;

        for (Integer slotIndex : inputSlots) {
            if (slotIndex == null) continue;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) continue;

            int amount = item.getAmount();
            if (amount > 1) {
                item.setAmount(amount - 1);
                inventory.setItem(slotIndex, item);
            } else {
                inventory.setItem(slotIndex, null);
            }
        }
    }
}
