package org.skysongdev.skysongCrafting.utils;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongstats.api.SkysongStatsApi;

/**
 * The two APIs SkysongCrafting is built on. Both plugins are in plugin.yml's {@code depend}
 * list, so they are enabled before SkysongCrafting and their APIs are registered.
 */
public final class SkysongApis {
    private SkysongApis() {}

    public static SkysongItemsApi items() {
        return SkysongItemsApi.get();
    }

    public static SkysongStatsApi stats() {
        return SkysongStatsApi.get();
    }

    /**
     * The ingredient id of an item: the value of the {@code crafting_key} string tag
     * (default {@code material_id}). {@code null} when the item is not an ingredient.
     */
    public static @Nullable String craftingId(@Nullable ItemStack item) {
        return items().tags().getString(item, Constants.CRAFTING_KEY_STRING).orElse(null);
    }
}
