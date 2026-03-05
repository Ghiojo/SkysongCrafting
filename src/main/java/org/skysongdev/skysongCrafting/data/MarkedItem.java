package org.skysongdev.skysongCrafting.data;

import org.bukkit.inventory.ItemStack;

public class MarkedItem {
    int index;
    ItemStack itemStack;

    public MarkedItem(int index, ItemStack itemStack) {
        this.index = index;
        this.itemStack = itemStack;
    }
}
