package org.skysongdev.skysongCrafting.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;

import java.util.List;

public class CraftingEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final RecipeInstance recipe;
    private final List<Integer> inputSlotReferences;
    private final List<Integer> outputSlotReferences;
    private final Player player;
    private final Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }

    public CraftingEvent(RecipeInstance recipe, List<Integer> inputSlotReferences, List<Integer> outputSlotReferences, Player player, Inventory inventory) {
        this.recipe = recipe;
        this.inputSlotReferences = inputSlotReferences;
        this.outputSlotReferences = outputSlotReferences;
        this.player = player;
        this.inventory = inventory;
    }

    public RecipeInstance getRecipe() {
        return recipe;
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
