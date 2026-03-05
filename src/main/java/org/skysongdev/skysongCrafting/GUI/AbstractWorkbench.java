package org.skysongdev.skysongCrafting.GUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;
import org.skysongdev.skysongCrafting.events.CraftingEvent;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongstats.SkysongStats;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.skysongdev.skysongCrafting.utils.Constants.CRAFTING_KEY;

public abstract class AbstractWorkbench implements InventoryHolder {
    protected WorkbenchGUIType guiType;
    protected Inventory inventory;
    protected String type;
    protected ArrayList<RecipeInstance> foundRecipes;
    protected ArrayList<String> inputSlot;
    protected int recipeIndex;
    protected ArrayList<Integer> outputSlotReferences;
    protected ArrayList<Integer> inputSlotReferences;
    protected int craftingButtonLocation;
    protected ItemStack icon;
    protected String name;

    public WorkbenchGUIType getGuiType() {
        return guiType;
    }

    public AbstractWorkbench(String type, ItemStack icon, String name, WorkbenchGUIType guiType){
        this.guiType = guiType;
        this.name = name;
        this.icon = icon;
        this.foundRecipes = new ArrayList<RecipeInstance>();
        this.inputSlot = new ArrayList<String>();
        this.recipeIndex = 0;
        this.type = type;
    }
    public void NextRecipe(Player player){
        if(foundRecipes.isEmpty())
            return;
        recipeIndex++;
        if(recipeIndex >= foundRecipes.size())
            recipeIndex = 0;
        UpdateRecipeDisplay(player);
    }

    public void PreviousRecipe(Player player){
        if(foundRecipes.isEmpty())
            return;
        recipeIndex--;
        if(recipeIndex < 0)
            recipeIndex = foundRecipes.size() - 1;
        UpdateRecipeDisplay(player);
    }

    public void UpdateRecipeDisplay(Player player){
        if(foundRecipes.isEmpty()){
            inventory.setItem(craftingButtonLocation, GUIUtils.Instance().GUI_CRAFT_NONE_BUTTON);
            return;
        }
        if(!foundRecipes.get(recipeIndex).doesPlayerHaveSkillToCraft(player)){
            ItemStack noSkillItem = new ItemStack(GUIUtils.Instance().GUI_CRAFT_NO_SKILL_BUTTON);
            List<Component> loreList = noSkillItem.getItemMeta().lore();
            for(Utils.Skills  skill : foundRecipes.get(recipeIndex).getSkillRequirements().keySet()){
                int requiredLevel = foundRecipes.get(recipeIndex).getSkillRequirements().get(skill);
                int playerLevel = SkysongStats.getPlugin().getUtils().getSkillManager().findActiveSkills(player.getUniqueId().toString()).getSkill(skill);
                if(playerLevel < requiredLevel)
                    loreList.add(SkysongCrafting.GetMM().deserialize("<red>" + Utils.Skills.getSkillName(skill) + " | Your Skill: " + SkysongStats.getPlugin().getUtils().getSkillManager().getSkillLevel(playerLevel) + " -> Required Skill Level: " + SkysongStats.getPlugin().getUtils().getSkillManager().getSkillLevel(requiredLevel)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                else
                    loreList.add(SkysongCrafting.GetMM().deserialize("<green>" + Utils.Skills.getSkillName(skill) + "  | Your Skill: " + SkysongStats.getPlugin().getUtils().getSkillManager().getSkillLevel(playerLevel) + " -> Required Skill Level: " + SkysongStats.getPlugin().getUtils().getSkillManager().getSkillLevel(requiredLevel)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
            ItemMeta meta = noSkillItem.getItemMeta();
            meta.lore(loreList);
            noSkillItem.setItemMeta(meta);
            inventory.setItem(craftingButtonLocation, noSkillItem);
            return;
        }
        if(!Objects.equals(foundRecipes.get(recipeIndex).getCraftingTableType(), type) || Objects.equals(foundRecipes.get(recipeIndex).getCraftingTableType(), "ANY")){
            inventory.setItem(craftingButtonLocation, GUIUtils.Instance().GUI_CRAFT_WRONG_WORKBENCH_BUTTON);
            return;
        }
        inventory.setItem(craftingButtonLocation, foundRecipes.get(recipeIndex).getDisplayItem());
    }

    public boolean ScanAndUpdateInputSlot(){
        ArrayList<String> previousInput = new ArrayList<>(inputSlot);
        inputSlot.clear();
        for(int slot : inputSlotReferences){
            ItemStack item = inventory.getItem(slot);
            if(item != null
                    && item.getType() != org.bukkit.Material.AIR
                    && item.getItemMeta().getPersistentDataContainer().has(CRAFTING_KEY)) {
                        inputSlot.add(item.getItemMeta().getPersistentDataContainer().get(CRAFTING_KEY, org.bukkit.persistence.PersistentDataType.STRING));
            } else
                inputSlot.add("");
        }
        return !previousInput.equals(inputSlot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void CraftItem(Player player){
        if(foundRecipes.isEmpty())
            return;
        Bukkit.getPluginManager().callEvent(new CraftingEvent(foundRecipes.get(recipeIndex), inputSlotReferences, outputSlotReferences, player, inventory));
        ArrayList<ItemStack> markedItems = foundRecipes.get(recipeIndex).ConsumeMaterials(inputSlotReferences, inventory);

        for(SkysongItemWithAmmount skysongItem : foundRecipes.get(recipeIndex).getResults().getOutput(markedItems)){
            boolean added = false;
            for(int slot : outputSlotReferences){
                ItemStack currentItem = inventory.getItem(slot);
                if(currentItem == null || currentItem.getType() == org.bukkit.Material.AIR){
                    added = true;
                    ItemStack newItem = SkysongItems.getPlugin().getUtils().GenerateSkysongItem(skysongItem.getItem());
                    newItem.setAmount(skysongItem.getAmmount());
                    inventory.setItem(slot, newItem);
                    break;
                } else if(currentItem.isSimilar(SkysongItems.getPlugin().getUtils().GenerateSkysongItem(skysongItem.getItem())) && currentItem.getAmount() + skysongItem.getAmmount() <= currentItem.getMaxStackSize()){
                    added = true;
                    currentItem.setAmount(currentItem.getAmount() + skysongItem.getAmmount());
                    inventory.setItem(slot, currentItem);
                    break;
                }
            }
            if(!added){
                ItemStack newItem = SkysongItems.getPlugin().getUtils().GenerateSkysongItem(skysongItem.getItem());
                newItem.setAmount(skysongItem.getAmmount());
                player.getInventory().addItem(newItem);
            }
        }
        player.playSound(player.getLocation(), Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", foundRecipes.get(recipeIndex).getCraftingSound())), 0.5f, 1.0f);

        String soundName = foundRecipes.get(recipeIndex).getCraftingSound();
        if (soundName != null && !soundName.isBlank()) {
            try {
                String key = soundName.toLowerCase(Locale.ROOT);
                org.bukkit.Sound bSound = Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", key));
                if (bSound != null) {
                    player.playSound(player.getLocation(), bSound, 1.0f, 1.0f);
                } else {
                    Bukkit.getLogger().warning("Sound not found: " + soundName);
                }
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid sound key: " + soundName);
            }
        }

        CheckForRecipes(player);
    }

    public void ReturnItems(Player player){
        for(int slot : inputSlotReferences){
            ItemStack item = inventory.getItem(slot);
            if(item != null && item.getType() != org.bukkit.Material.AIR){
                player.getInventory().addItem(item);
            }
            inventory.clear(slot);
        }
        for(int slot : outputSlotReferences){
            ItemStack item = inventory.getItem(slot);
            if(item != null && item.getType() != org.bukkit.Material.AIR){
                player.getInventory().addItem(item);
            }
            inventory.clear(slot);
        }
        CheckForRecipes(player);
    }


    public abstract void CheckForRecipes(Player player);
    protected abstract void SetupGUI();
}
