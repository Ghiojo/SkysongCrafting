package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShapedRecipeInstance extends RecipeInstance {
    protected ArrayList<String> materialConsumption;

    public ShapedRecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, ArrayList<String> materialConsumption, Map<Utils.Skills, Integer> skillRequirements) {
        super(output, displayItem, recipeName, craftingTableType, craftingSound, skillRequirements);
        this.materialConsumption = materialConsumption;
    }

    @Override
    public ArrayList<MarkedItem> ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory) {
        Pattern pattern = Pattern.compile("([\\^])([0-9]+)");
        for(int i = 0; i < materialConsumption.size();  i++){
            if(Objects.equals(materialConsumption.get(i), "")) continue;
            if(inventory.getItem(inputSlots.get(i)) == null || inventory.getItem(inputSlots.get(i)).getType() == org.bukkit.Material.AIR) continue;
            Matcher matcher = pattern.matcher(materialConsumption.get(i));
            if (matcher.find()) {
                String symbol = matcher.group(1);
                int number = Integer.parseInt(matcher.group(2));
                if (symbol.equals("^")) {
                    if(inventory.getItem(inputSlots.get(i)).getItemMeta().getPersistentDataContainer().has(Constants.chargesKey)){
                        int charges = inventory.getItem(inputSlots.get(i)).getItemMeta().getPersistentDataContainer().get(Constants.chargesKey, PersistentDataType.INTEGER);
                        ItemStack item = inventory.getItem(inputSlots.get(i));
                        ItemMeta meta = item.getItemMeta();

                        if(charges > number){
                            meta.getPersistentDataContainer().set(Constants.chargesKey, PersistentDataType.INTEGER, charges - number);
                            item.setItemMeta(meta);
                            inventory.setItem(inputSlots.get(i), item);
                        } else {
                            meta.getPersistentDataContainer().set(Constants.chargesKey, PersistentDataType.INTEGER, 0);
                            item.setItemMeta(meta);
                            inventory.setItem(inputSlots.get(i), item);
                        }
                    }
                }
            } else {
                if (inventory.getItem(inputSlots.get(i)) != null && inventory.getItem(inputSlots.get(i)).getType() != org.bukkit.Material.AIR) {
                    int amount = inventory.getItem(inputSlots.get(i)).getAmount();

                    if (amount > 1) {
                        // Just reduce the stack amount by 1
                        inventory.getItem(inputSlots.get(i)).setAmount(amount - 1);
                    } else {
                        // If only 1 left, remove the item entirely
                        inventory.setItem(inputSlots.get(i), null);
                    }
                }
            }
        }
        return null;
    }

}
