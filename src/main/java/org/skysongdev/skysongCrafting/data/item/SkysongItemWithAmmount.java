package org.skysongdev.skysongCrafting.data.item;

import org.skysongdev.skysongitems.utils.SkysongItem;

public class SkysongItemWithAmmount {
    private SkysongItem item;
    private int ammount;

    public SkysongItemWithAmmount(SkysongItem item, int ammount) {
        this.item = item;
        this.ammount = ammount;
    }

    public SkysongItem getItem() {
        return item;
    }

    public int getAmmount() {
        return ammount;
    }
}
