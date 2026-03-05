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


    @Override
    public ArrayList<MarkedItem> ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory) {
        ArrayList<MarkedItem> markedItems = new ArrayList<>();
        for (Integer slotIndex : inputSlots) {
            if (slotIndex == null) continue;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) continue;

            for(int i = 0; i < materialConsumption.size(); i++){
                if(materialMarks.get(i) && item.getItemMeta().getPersistentDataContainer().get(CRAFTING_KEY, PersistentDataType.STRING).equals(materialConsumption.get(i))){
                    markedItems.add(new MarkedItem(i, item.clone()));
                    break;
                }
            }
        }
        super.ConsumeMaterials(inputSlots, inventory);

        return markedItems;
    }
}
