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
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongstats.api.CharacterId;
import org.skysongdev.skysongstats.api.SkysongStatsApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class RecipeInstance {
    protected OutputInstance results;
    protected ItemStack displayItem;
    protected List<SkillRequirement> skillRequirements;
    protected String craftingTableType;
    protected String craftingSound;
    protected List<RecipeCondition> conditions = new ArrayList<>();

    public String getCraftingSound() {
        return craftingSound;
    }

    public RecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, List<SkillRequirement> skillRequirements) {
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

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getCraftingTableType(){
        return craftingTableType;
    }

    public OutputInstance getResults(){
        return results;
    }

    /**
     * Checks every skill requirement against the character the player is playing now,
     * comparing skill levels (not raw XP). Empty when the recipe has no requirements.
     */
    public List<SkillRequirement.Check> checkSkills(Player player){
        List<SkillRequirement.Check> checks = new ArrayList<>();
        if(skillRequirements.isEmpty()) return checks;
        SkysongStatsApi stats = SkysongApis.stats();
        Optional<CharacterId> character = stats.profiles().active(player.getUniqueId());
        for(SkillRequirement requirement : skillRequirements){
            checks.add(requirement.check(stats.skills(), character));
        }
        return checks;
    }

    public boolean doesPlayerHaveSkillToCraft(Player player){
        for(SkillRequirement.Check check : checkSkills(player)){
            if(!check.met()) return false;
        }
        return true;
    }

    public List<SkillRequirement> getSkillRequirements() {
        return skillRequirements;
    }

    public void setConditions(List<RecipeCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    public List<RecipeCondition> getConditions() {
        return conditions;
    }

    /**
     * Evaluates the recipe's conditions against the current grid.
     * @return {@code null} when every condition holds (or there are none); otherwise the reason the
     *         recipe cannot be crafted right now, in the same wording as the condition in the file.
     */
    public String checkConditions(ArrayList<Integer> inputSlots, Inventory inventory) {
        if (conditions.isEmpty()) return null;
        StepData data = new StepData(collectMarkedItems(inputSlots, inventory), null, new ArrayList<>());
        for (RecipeCondition condition : conditions) {
            try {
                String failure = condition.test(data);
                if (failure != null) return failure;
            } catch (OutputException e) {
                return e.getMessage();
            }
        }
        return null;
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

    /** Applies one ingredient's {@link ConsumptionRule} to the item in {@code slot}. */
    protected void consumeSlot(Inventory inventory, int slot, ConsumptionRule rule) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) return;

        switch (rule.getKind()) {
            case KEEP:
                return;
            case CHARGES: {
                ItemCharges charges = SkysongApis.items().charges();
                OptionalInt current = charges.get(item);
                if (current.isEmpty()) {
                    // The recipe asked for charges on an item that has none: consume it like a normal ingredient
                    // rather than silently keeping it (keeping would make the ingredient free).
                    SkysongCrafting.Instance().getLogger().warning("Recipe wanted to take " + rule.getCharges()
                            + " charge(s) from an item without a charges tag (" + item.getType() + "); consuming one instead.");
                    consumeSlot(inventory, slot, ConsumptionRule.CONSUME_ONE);
                    return;
                }
                // Takes the charges like /si charges add with a negative amount: never below 0,
                // and the item's lore is redrawn by SkysongItems.
                try {
                    charges.add(item, -rule.getCharges());
                } catch (IllegalArgumentException e) {
                    SkysongCrafting.Instance().getLogger().warning("SkysongItems refused to take " + rule.getCharges()
                            + " charge(s) from " + item.getType() + " (" + e.getMessage() + "); consuming one instead.");
                    consumeSlot(inventory, slot, ConsumptionRule.CONSUME_ONE);
                    return;
                }
                inventory.setItem(slot, item);
                return;
            }
            case CONSUME:
            default:
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    inventory.setItem(slot, item);
                } else {
                    inventory.setItem(slot, null);
                }
        }
    }
}
