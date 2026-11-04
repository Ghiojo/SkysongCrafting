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
import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.item.ResultItem;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;
import org.skysongdev.skysongCrafting.data.recipe.SkillRequirement;
import org.skysongdev.skysongCrafting.events.CraftingEvent;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

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
        List<SkillRequirement.Check> skillChecks = foundRecipes.get(recipeIndex).checkSkills(player);
        if(!skillChecks.stream().allMatch(SkillRequirement.Check::met)){
            ItemStack noSkillItem = new ItemStack(GUIUtils.Instance().GUI_CRAFT_NO_SKILL_BUTTON);
            ItemMeta meta = noSkillItem.getItemMeta();
            List<Component> loreList = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
            // Names come from SkysongStats' skills.yml as plain text, so they are escaped, not parsed.
            MiniMessage mm = SkysongCrafting.GetMM();
            for(SkillRequirement.Check check : skillChecks){
                String line = (check.met() ? "<green>" : "<red>") + mm.escapeTags(check.skillName())
                        + " | Your Skill: " + mm.escapeTags(check.playerLevel())
                        + " -> Required Skill Level: " + mm.escapeTags(check.requiredLevel());
                loreList.add(mm.deserialize(line).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
            meta.lore(loreList);
            noSkillItem.setItemMeta(meta);
            inventory.setItem(craftingButtonLocation, noSkillItem);
            return;
        }
        String conditionFailure = foundRecipes.get(recipeIndex).checkConditions(inputSlotReferences, inventory);
        if(conditionFailure != null){
            ItemStack button = new ItemStack(GUIUtils.Instance().GUI_CRAFT_CONDITIONS_NOT_MET_BUTTON);
            ItemMeta meta = button.getItemMeta();
            List<Component> lore = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
            lore.add(SkysongCrafting.GetMM().deserialize("<gray>" + conditionFailure).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            button.setItemMeta(meta);
            inventory.setItem(craftingButtonLocation, button);
            return;
        }
        // "ANY" means the recipe can be crafted on any workbench type. (The old condition had this inverted.)
        String requiredTable = foundRecipes.get(recipeIndex).getCraftingTableType();
        if(!Objects.equals(requiredTable, type) && !"ANY".equalsIgnoreCase(requiredTable)){
            inventory.setItem(craftingButtonLocation, GUIUtils.Instance().GUI_CRAFT_WRONG_WORKBENCH_BUTTON);
            return;
        }
        inventory.setItem(craftingButtonLocation, foundRecipes.get(recipeIndex).getDisplayItem());
    }

    public boolean ScanAndUpdateInputSlot(){
        ArrayList<String> previousInput = new ArrayList<>(inputSlot);
        inputSlot.clear();
        for(int slot : inputSlotReferences){
            String craftingId = SkysongApis.craftingId(inventory.getItem(slot));
            inputSlot.add(craftingId == null ? "" : craftingId);
        }
        return !previousInput.equals(inputSlot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Crafting order, chosen so a failure can never cost or duplicate items:
     * 1. copy the marked inputs; 2. run the output on the copies; 3. generate every result stack;
     * 4. only then consume the materials; 5. place the results.
     * Steps 1-3 touch nothing in the grid, so if any of them fails the craft is refused and the grid is untouched.
     */
    public void CraftItem(Player player){
        if(foundRecipes.isEmpty())
            return;
        RecipeInstance recipe = foundRecipes.get(recipeIndex);

        // Re-check conditions at craft time: the grid may have changed since the button was drawn.
        String conditionFailure = recipe.checkConditions(inputSlotReferences, inventory);
        if(conditionFailure != null){
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Could not craft: <gray>" + conditionFailure));
            return;
        }

        ArrayList<MarkedItem> markedItems = recipe.collectMarkedItems(inputSlotReferences, inventory);

        ArrayList<ItemStack> results = new ArrayList<>();
        try {
            for(ResultItem result : recipe.getResults().getOutput(markedItems)){
                ItemStack stack = result.item();
                stack.setAmount(result.amount());
                results.add(stack);
            }
        } catch (OutputException e) {
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Could not craft: <gray>" + e.getMessage()));
            SkysongCrafting.Instance().getLogger().warning("Craft refused for " + player.getName() + " (recipe '" + recipe.getDisplayItem().getItemMeta().getItemName() + "'): " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Could not craft: <gray>an internal error occurred, please report it."));
            SkysongCrafting.Instance().getLogger().severe("Craft failed for " + player.getName() + " while generating results: " + e);
            e.printStackTrace();
            return;
        }

        Bukkit.getPluginManager().callEvent(new CraftingEvent(recipe, results, inputSlotReferences, outputSlotReferences, player, inventory));
        recipe.ConsumeMaterials(inputSlotReferences, inventory);

        for(ItemStack newItem : results){
            boolean added = false;
            for(int slot : outputSlotReferences){
                ItemStack currentItem = inventory.getItem(slot);
                if(currentItem == null || currentItem.getType() == org.bukkit.Material.AIR){
                    added = true;
                    inventory.setItem(slot, newItem);
                    break;
                } else if(currentItem.isSimilar(newItem) && currentItem.getAmount() + newItem.getAmount() <= currentItem.getMaxStackSize()){
                    added = true;
                    currentItem.setAmount(currentItem.getAmount() + newItem.getAmount());
                    inventory.setItem(slot, currentItem);
                    break;
                }
            }
            if(!added){
                player.getInventory().addItem(newItem);
            }
        }

        String soundName = recipe.getCraftingSound();
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
