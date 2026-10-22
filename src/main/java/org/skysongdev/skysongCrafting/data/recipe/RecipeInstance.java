package org.skysongdev.skysongCrafting.data.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongstats.SkysongStats;
import org.skysongdev.skysongstats.Utils.Utils.Skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RecipeInstance {
    protected OutputInstance results;
    protected ItemStack displayItem;
    protected Map<Skills, Integer> skillRequirements;
    protected String craftingTableType;
    protected String craftingSound;

    public String getCraftingSound() {
        return craftingSound;
    }

    public RecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, Map<org.skysongdev.skysongstats.Utils.Utils.Skills, Integer> skillRequirements) {
        this.results = output;
        ItemStack buffer = new ItemStack(displayItem);
        ItemMeta metaBuffer = buffer.getItemMeta();
        PersistentDataContainer metaDataContainer = metaBuffer.getPersistentDataContainer();
        metaBuffer.itemName(SkysongCrafting.GetMM().deserialize(recipeName));
        ArrayList<Component> loreOutput = new ArrayList<>(List.of(SkysongCrafting.GetMM().deserialize("<gray>Recipe Ouputs:")));
        for(String s : output.getOutputStrings()){
            loreOutput.add(SkysongCrafting.GetMM().deserialize(s));
        }
        metaBuffer.lore(loreOutput.stream().toList());
        metaDataContainer.set(GUIUtils.ClickableKey, PersistentDataType.STRING, "craft");
        buffer.setItemMeta(metaBuffer);
        this.displayItem = buffer;
        this.skillRequirements = skillRequirements;
        this.craftingTableType = craftingTableType;
        this.craftingSound = craftingSound.toLowerCase();
    }

    public void addSkillRequirement(Skills skill, int experience) {
        skillRequirements.put(skill, experience);
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getCraftingTableType(){
        return craftingTableType;
    }

    public OutputInstance getResults(){
        return results;
    }

    public boolean doesPlayerHaveSkillToCraft(Player player){
        for(Skills skill : skillRequirements.keySet()){
            int playerLevel = SkysongStats.getPlugin().getUtils().getSkillManager().findActiveSkills(player.getUniqueId().toString()).getSkill(skill);
            if(playerLevel < skillRequirements.get(skill)){
                return false;
            }
        }
        return true;
    }
    public Map<Skills, Integer> getSkillRequirements() {
        return skillRequirements;
    }

    /**
     * Returns copies of the marked inputs, numbered 0..n-1 in grid order, for the output to read.
     * Called before anything is consumed; must not modify the inventory. Non-marking recipes return an empty list.
     */
    public ArrayList<MarkedItem> collectMarkedItems(ArrayList<Integer> inputSlots, Inventory inventory) {
        return new ArrayList<>();
    }

    /** Removes the consumed materials from the grid. Only called once the output has been produced successfully. */
    public abstract void ConsumeMaterials(ArrayList<Integer> inputSlots, Inventory inventory);
}
