package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.Map;

import static org.skysongdev.skysongCrafting.utils.Constants.CRAFTING_KEY;

public class ShapelessMarkingRecipeInstance extends ShapelessRecipeInstance {
    private ArrayList<Boolean> materialMarks;

    public ShapelessMarkingRecipeInstance(ArrayList<MaterialCount> materialConsumption, OutputInstance output, Material displayItemMaterial, String recipeName, String craftingTableType, String craftingSound, Map<Utils.Skills, Integer> skillRequirements, ArrayList<Boolean> materialMarks) {
        super(materialConsumption, output, displayItemMaterial, recipeName, craftingTableType, craftingSound, skillRequirements);
        this.materialMarks = materialMarks;
    }


    /**
     * Marks every grid item whose crafting id matches a marked entry of the recipe, in grid order.
     * (Previously compared the id string against a MaterialCount object, so nothing was ever marked.)
     */
    @Override
    public ArrayList<MarkedItem> collectMarkedItems(ArrayList<Integer> inputSlots, Inventory inventory) {
        ArrayList<MarkedItem> markedItems = new ArrayList<>();
        int j = 0;
        for (Integer slotIndex : inputSlots) {
            if (slotIndex == null) continue;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) continue;
            String craftingId = item.getItemMeta().getPersistentDataContainer().get(CRAFTING_KEY, PersistentDataType.STRING);
            if (craftingId == null) continue;

            for (int i = 0; i < materialConsumption.size() && i < materialMarks.size(); i++) {
                if (materialMarks.get(i) && craftingId.equalsIgnoreCase(materialConsumption.get(i).getId())) {
                    markedItems.add(new MarkedItem(j, item.clone()));
                    j++;
                    break;
                }
            }
        }
        return markedItems;
    }
}
