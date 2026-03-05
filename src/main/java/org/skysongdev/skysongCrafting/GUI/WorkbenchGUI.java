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

public class WorkbenchGUI extends AbstractWorkbench {

    public WorkbenchGUI(String type, ItemStack icon, String name, WorkbenchGUIType guiType) {
        super(type, icon, name, guiType);
        inventory = SkysongCrafting.Instance().getServer().createInventory(this, 54, SkysongCrafting.GetMM().deserialize("<green>Workbench: " + type.substring(0, 1).toUpperCase() + type.substring(1)));
        foundRecipes = new ArrayList<RecipeInstance>();
        craftingButtonLocation = 16;
        SetupGUI();
        outputSlotReferences = new ArrayList<Integer>(){{
            add(24);
            add(25);
            add(26);
            add(33);
            add(34);
            add(35);
            add(42);
            add(43);
            add(44);
        }};
        inputSlotReferences = new ArrayList<Integer>(){{
            add(9);
            add(10);
            add(11);
            add(12);
            add(13);
            add(18);
            add(19);
            add(20);
            add(21);
            add(22);
            add(27);
            add(28);
            add(29);
            add(30);
            add(31);
            add(36);
            add(37);
            add(38);
            add(39);
            add(40);
            add(45);
            add(46);
            add(47);
            add(48);
            add(49);
        }};

    }

    @Override
    protected void SetupGUI(){
        for(int i = 0; i < 9; i++)
            inventory.setItem(i, GUIUtils.Instance().GUI_BORDER);
        for(int i = 1; i < 6; i++)
            inventory.setItem(5 + (i * 9), GUIUtils.Instance().GUI_BORDER);
        for(int i = 14; i < 18; i++) {
            inventory.setItem(i, GUIUtils.Instance().GUI_BORDER);
            inventory.setItem(i + 36, GUIUtils.Instance().GUI_BORDER);
        }
        inventory.setItem(23, GUIUtils.Instance().GUI_BORDER);
        inventory.setItem(32, GUIUtils.Instance().GUI_BORDER);
        inventory.setItem(41, GUIUtils.Instance().GUI_BORDER);

        ItemStack GUIHeader = icon;
        ItemMeta meta = GUIHeader.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<green>Workbench: " + name));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(GUIUtils.NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        GUIHeader.setItemMeta(meta);

        inventory.setItem(4, GUIHeader);
        inventory.setItem(craftingButtonLocation, GUIUtils.Instance().GUI_CRAFT_NONE_BUTTON);
        inventory.setItem(craftingButtonLocation-1, GUIUtils.GUI_PREVIOUS_CRAFT_BUTTON);
        inventory.setItem(craftingButtonLocation+1, GUIUtils.GUI_NEXT_CRAFT_BUTTON);
    }



    @Override
    public void CheckForRecipes(Player player){
        foundRecipes.clear();
        boolean didInputChange = ScanAndUpdateInputSlot();
        CheckForShapedRecipes();
        CheckForShapelessRecipes();
        if(didInputChange)
            recipeIndex = 0;
        UpdateRecipeDisplay(player);
    }

    private void CheckForShapedRecipes(){
        ArrayList<RecipeInstance> result = Utils.Instance().getShapedRecipeMap().get(inputSlot);
        if(result != null && !result.isEmpty())
            foundRecipes.addAll(result);

    }

    private void CheckForShapelessRecipes(){
        ArrayList<MaterialCount> materialCount = Utils.Instance().inputToMaterialCountList(inputSlot);
        ArrayList<RecipeInstance> result = Utils.Instance().getShapelessRecipeMap().get(materialCount);
        if(result != null && !result.isEmpty())
            foundRecipes.addAll(result);
    }
}
