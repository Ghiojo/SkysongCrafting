package org.skysongdev.skysongCrafting.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;

import java.util.List;

/**
 * Fired once a recipe's output has been produced, before the ingredients are consumed and before
 * the results are placed. {@link #getResults()} holds the exact stacks that will be placed, so
 * listeners can edit them in place (the random-id listener does this).
 */
public class CraftingEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final RecipeInstance recipe;
    private final List<ItemStack> results;
    private final List<Integer> inputSlotReferences;
    private final List<Integer> outputSlotReferences;
    private final Player player;
    private final Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }

    public CraftingEvent(RecipeInstance recipe, List<ItemStack> results, List<Integer> inputSlotReferences, List<Integer> outputSlotReferences, Player player, Inventory inventory) {
        this.recipe = recipe;
        this.results = results;
        this.inputSlotReferences = inputSlotReferences;
        this.outputSlotReferences = outputSlotReferences;
        this.player = player;
        this.inventory = inventory;
    }

    public RecipeInstance getRecipe() {
        return recipe;
    }

    /** The result stacks about to be placed; edits made here end up in the output slots. */
    public List<ItemStack> getResults() {
        return results;
    }

    public List<Integer> getInputSlotReferences() {
        return inputSlotReferences;
    }

    public List<Integer> getOutputSlotReferences() {
        return outputSlotReferences;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
