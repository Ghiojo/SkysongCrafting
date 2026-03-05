package org.skysongdev.skysongCrafting.data.workstations;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.GUI.AbstractWorkbench;
import org.skysongdev.skysongCrafting.GUI.SmeltingGUI;
import org.skysongdev.skysongCrafting.GUI.WorkbenchGUI;
import org.skysongdev.skysongCrafting.GUI.WorkbenchGUIType;

public class Workstation {
    private String id;
    private String type;
    private ItemStack icon;
    private AbstractWorkbench gui;

    public Workstation(String type, String icon, WorkbenchGUIType guiLayout, String id) {
        this.id = id;
        this.type = type;
        this.icon = new ItemStack(org.bukkit.Material.valueOf(icon.toUpperCase()));
        switch (guiLayout){
            case WORKBENCH -> this.gui = new WorkbenchGUI(id, this.icon, type, guiLayout);
            case SMELTER -> this.gui =  new SmeltingGUI(id, this.icon, type, guiLayout);
            default -> this.gui = null;
        }
    }

    public String getType() {
        return type;
    }
    public ItemStack getIcon() {
        return icon;
    }
    public AbstractWorkbench getGui() {
        return gui;
    }


    public String getId() {
        return id;
    }
}
