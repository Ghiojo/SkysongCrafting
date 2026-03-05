package org.skysongdev.skysongCrafting.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.SkysongCrafting;

import java.util.ArrayList;

import static org.skysongdev.skysongCrafting.utils.Constants.NEXT_CRAFT;
import static org.skysongdev.skysongCrafting.utils.Constants.PREV_CRAFT;

public class GUIUtils {
    private static GUIUtils instance;
    public static GUIUtils Instance(){
        if(instance == null){
            instance = new GUIUtils();
        }
        return instance;
    }

    public static ItemStack GUI_BORDER;
    public static ItemStack GUI_CRAFT_NONE_BUTTON;
    public static ItemStack GUI_CRAFT_NO_SKILL_BUTTON;
    public static ItemStack GUI_CRAFT_WRONG_WORKBENCH_BUTTON;
    public static ItemStack GUI_NEXT_CRAFT_BUTTON;
    public static ItemStack GUI_PREVIOUS_CRAFT_BUTTON;
    //Key for GUI Items that shouldn't be interactable
    public static NamespacedKey NonSelectableKey = new NamespacedKey(SkysongCrafting.Instance(), "non_selectable");
    //Key for GUI Items that can be clicked but not picked up
    public static NamespacedKey ClickableKey = new NamespacedKey(SkysongCrafting.Instance(), "clickable");

    public GUIUtils() {
        ItemStack border = new ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        meta.displayName(SkysongCrafting.GetMM().deserialize(""));
        meta.setHideTooltip(true);
        border.setItemMeta(meta);

        GUI_BORDER = border;

        ItemStack craftButton = new ItemStack(org.bukkit.Material.RED_STAINED_GLASS_PANE);
        meta = craftButton.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<red>Craft").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(SkysongCrafting.GetMM().deserialize("<red>No Recipe Found!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        container = meta.getPersistentDataContainer();
        container.set(NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        meta.setItemModel(new NamespacedKey("skysong", "gui/red_x"));

        craftButton.setItemMeta(meta);
        GUI_CRAFT_NONE_BUTTON = craftButton;

        ItemStack noSkillButton = new ItemStack(org.bukkit.Material.YELLOW_STAINED_GLASS_PANE);
        meta = noSkillButton.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<yellow>Craft").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore = new ArrayList<>();
        lore.add(SkysongCrafting.GetMM().deserialize("<yellow>You do not have the required skill to craft this!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(SkysongCrafting.GetMM().deserialize("<yellow>Required Skill: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        container = meta.getPersistentDataContainer();
        container.set(NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);

        noSkillButton.setItemMeta(meta);
        GUI_CRAFT_NO_SKILL_BUTTON = noSkillButton;

        ItemStack wrongWorkbenchButton = new ItemStack(org.bukkit.Material.RED_STAINED_GLASS_PANE);
        meta = wrongWorkbenchButton.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<red>Craft"));
        lore = new ArrayList<>();
        lore.add(SkysongCrafting.GetMM().deserialize("<red>You do not have the required workbench to craft this!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(SkysongCrafting.GetMM().deserialize("<red>Required Workbench: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        container = meta.getPersistentDataContainer();
        container.set(NonSelectableKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        meta.setItemModel(new NamespacedKey("skysong", "gui/red_x"));

        wrongWorkbenchButton.setItemMeta(meta);
        GUI_CRAFT_WRONG_WORKBENCH_BUTTON = wrongWorkbenchButton;

        ItemStack nextCraftButton = new ItemStack(org.bukkit.Material.GREEN_STAINED_GLASS_PANE);
        meta = nextCraftButton.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<green>Next Craft").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.setItemModel(new NamespacedKey("skysong", "gui/right_arrow"));
        container = meta.getPersistentDataContainer();
        container.set(ClickableKey, PersistentDataType.STRING, NEXT_CRAFT);

        nextCraftButton.setItemMeta(meta);
        GUI_NEXT_CRAFT_BUTTON = nextCraftButton;

        ItemStack previousCraftButton = new ItemStack(org.bukkit.Material.GREEN_STAINED_GLASS_PANE);
        meta = previousCraftButton.getItemMeta();
        meta.displayName(SkysongCrafting.GetMM().deserialize("<green>Previous Craft").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.setItemModel(new NamespacedKey("skysong", "gui/left_arrow"));
        container = meta.getPersistentDataContainer();
        container.set(ClickableKey, PersistentDataType.STRING, PREV_CRAFT);

        previousCraftButton.setItemMeta(meta);
        GUI_PREVIOUS_CRAFT_BUTTON = previousCraftButton;

    }
}
