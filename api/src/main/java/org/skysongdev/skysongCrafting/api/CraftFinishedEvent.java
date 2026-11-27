package org.skysongdev.skysongCrafting.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A player finished a craft on a workstation. Fired on the main thread once per craft, after the
 * ingredients were consumed and the results were placed. It can't be cancelled: the craft is done.
 * <p>
 * It is not fired for a craft that was refused (unmet conditions, an output that failed), since
 * nothing was used or made.
 * <p>
 * Everything it holds is a copy taken when the craft finished: changing it changes nothing in the
 * game. To give the player something extra, add it yourself.
 *
 * @since 1.1.0
 */
public final class CraftFinishedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final WorkstationInfo workstation;
    private final RecipeInfo recipe;
    private final List<ItemStack> results;
    private final List<ItemStack> ingredients;

    /**
     * Only SkysongCrafting fires this event. The item lists are copied, stack by stack.
     */
    public CraftFinishedEvent(@NotNull Player player, @NotNull WorkstationInfo workstation, @NotNull RecipeInfo recipe,
                              @NotNull List<ItemStack> results, @NotNull List<ItemStack> ingredients) {
        this.player = Objects.requireNonNull(player, "player");
        this.workstation = Objects.requireNonNull(workstation, "workstation");
        this.recipe = Objects.requireNonNull(recipe, "recipe");
        this.results = copy(results, "results");
        this.ingredients = copy(ingredients, "ingredients");
    }

    private static List<ItemStack> copy(List<ItemStack> items, String what) {
        Objects.requireNonNull(items, what);
        List<ItemStack> copies = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            copies.add(Objects.requireNonNull(item, what + " holds null").clone());
        }
        return Collections.unmodifiableList(copies);
    }

    /** @return the player who crafted */
    public @NotNull Player getPlayer() {
        return player;
    }

    /** @return the workstation the craft was made on, as it was when the craft finished */
    public @NotNull WorkstationInfo getWorkstation() {
        return workstation;
    }

    /** @return the recipe that was crafted */
    public @NotNull RecipeInfo getRecipe() {
        return recipe;
    }

    /**
     * The items the craft made, one stack per result with its amount, in the recipe's output order.
     * They went into the workstation's output slots (stacking onto equal items already there) or, when
     * those were full, into the player's inventory.
     *
     * @return copies of the results; unmodifiable
     */
    public @NotNull List<ItemStack> getResults() {
        return results;
    }

    /**
     * The items in the workstation's input slots just before the craft used them, one stack per
     * occupied slot, in slot order. Some may not have been used up: recipes can keep an ingredient
     * ({@code !keep}) or only take charges from it ({@code ^N}).
     *
     * @return copies of the ingredients; unmodifiable
     */
    public @NotNull List<ItemStack> getIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
