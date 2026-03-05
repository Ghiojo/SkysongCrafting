package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.Map;

public class ShapedMarkingRecipeInstance extends ShapedRecipeInstance{
    private ArrayList<Boolean> materialMarks;

    public ShapedMarkingRecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, ArrayList<String> materialConsumption, Map<Utils.Skills, Integer> skillRequirements, ArrayList<Boolean> materialMarks) {
        super(output, displayItem, recipeName, craftingTableType, craftingSound, materialConsumption, skillRequirements);
        this.materialMarks = materialMarks;
    }

    @Override
    public ArrayList<MarkedItem> ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory){
        ArrayList<MarkedItem> markedItems = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < materialConsumption.size();  i++) {
            if (materialMarks.get(i)) {
                ItemStack item = inventory.getItem(inputSlots.get(i));
                markedItems.add(new MarkedItem(j, item.clone()));
                j++;
            }
        }
        super.ConsumeMaterials(inputSlots, inventory);

        return markedItems;
    }
}
