package org.skysongdev.skysongCrafting.GUI;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.util.ArrayList;

public class SmeltingGUI extends AbstractWorkbench{

    public SmeltingGUI(String type, ItemStack icon, String name, WorkbenchGUIType guiType) {
        super(type, icon, name, guiType);
        inventory = SkysongCrafting.Instance().getServer().createInventory(this, 54, SkysongCrafting.GetMM().deserialize("<green>Smelter: " + type.substring(0, 1).toUpperCase() + type.substring(1)));
        foundRecipes = new ArrayList<RecipeInstance>();
        craftingButtonLocation = 15;
        SetupGUI();
        outputSlotReferences = new ArrayList<Integer>(){{
            add(23);
            add(24);
            add(25);
            add(32);
            add(33);
            add(34);
            add(41);
            add(42);
            add(43);
        }};
        inputSlotReferences = new ArrayList<Integer>(){{
            add(19);
            add(20);
            add(21);
            add(28);
            add(29);
            add(30);
            add(37);
            add(38);
            add(39);
        }};
    }

    @Override
    public void CheckForRecipes(Player player) {
        foundRecipes.clear();
        boolean didInputChange = ScanAndUpdateInputSlot();
        CheckForSmeltingRecipes();
        if(didInputChange)
            recipeIndex = 0;
        UpdateRecipeDisplay(player);
    }

    private void CheckForSmeltingRecipes() {
        ArrayList<MaterialCount> materialCount = Utils.Instance().inputToMaterialCountList(inputSlot);
        ArrayList<RecipeInstance> result = Utils.Instance().getShapelessRecipeMap().get(materialCount);
        if(result != null && !result.isEmpty())
            foundRecipes.addAll(result);
    }


    @Override
    protected void SetupGUI() {
        for(int i = 0; i < 18; i++)
            inventory.setItem(i, GUIUtils.GUI_BORDER);
        for(int i = 2; i < 5; i++){
            inventory.setItem((i * 9), GUIUtils.GUI_BORDER);
            inventory.setItem(4 + (i * 9), GUIUtils.GUI_BORDER);
            inventory.setItem(8 + (i * 9), GUIUtils.GUI_BORDER);
        }
        for(int i = 0; i < 9; i++)
            inventory.setItem(45 + i, GUIUtils.GUI_BORDER);
        inventory.setItem(craftingButtonLocation, GUIUtils.GUI_CRAFT_NONE_BUTTON);
        inventory.setItem((craftingButtonLocation + 1), GUIUtils.GUI_NEXT_CRAFT_BUTTON);
        inventory.setItem((craftingButtonLocation - 1), GUIUtils.GUI_PREVIOUS_CRAFT_BUTTON);

        ItemStack GUIHeader = icon;
        ItemMeta meta = GUIHeader.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<green>Smelter: " + name));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(GUIUtils.NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        GUIHeader.setItemMeta(meta);

        inventory.setItem(4, GUIHeader);
    }
}
