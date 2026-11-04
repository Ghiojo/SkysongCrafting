package org.skysongdev.skysongCrafting.data.item;

import org.bukkit.inventory.ItemStack;

/**
 * One item a craft produces, already built, and how many of it to give.
 * The stack is new for every craft, so the workbench may change and place it.
 */
public record ResultItem(ItemStack item, int amount) {
}
