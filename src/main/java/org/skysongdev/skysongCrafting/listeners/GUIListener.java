package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.GUI.AbstractWorkbench;
import org.skysongdev.skysongCrafting.GUI.WorkbenchGUI;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.utils.GUIUtils;

import java.util.Objects;

import static org.skysongdev.skysongCrafting.utils.Constants.*;

public class GUIListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AbstractWorkbench)) return;
        AbstractWorkbench gui = (AbstractWorkbench) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        ItemMeta meta = null;
        PersistentDataContainer pdc = null;

        ItemStack current = event.getCurrentItem();
        if (current != null)
            meta = current.getItemMeta();
        if (meta != null)
            pdc = meta.getPersistentDataContainer();

        if(pdc != null) {
            if (pdc.has(GUIUtils.NonSelectableKey) && pdc.get(GUIUtils.NonSelectableKey, PersistentDataType.BOOLEAN) == true) {
                event.setCancelled(true);
                return;
            }

            if (pdc.has(GUIUtils.ClickableKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
                String value = pdc.get(GUIUtils.ClickableKey, PersistentDataType.STRING);

                if (Objects.equals(value, NEXT_CRAFT)) {
                    gui.NextRecipe(player);
                    player.playSound(player.getLocation(), Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", "ui.button.click")), 0.5f, 1.0f);
                } else if (Objects.equals(value, PREV_CRAFT)) {
                    gui.PreviousRecipe(player);
                    player.playSound(player.getLocation(), Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", "ui.button.click")), 0.5f, 1.0f);
                } else if (Objects.equals(value, "craft")) {
                    gui.CraftItem(player);
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(SkysongCrafting.Instance(), new Runnable() {
            @Override
            public void run() {
                gui.CheckForRecipes(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof AbstractWorkbench)) return;
        AbstractWorkbench gui = (AbstractWorkbench) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        ItemMeta meta = null;
        PersistentDataContainer pdc = null;

        ItemStack current = event.getOldCursor();
        if (current != null)
            meta = current.getItemMeta();
        if (meta != null)
            pdc = meta.getPersistentDataContainer();

        if(pdc != null) {
            if (pdc.has(GUIUtils.NonSelectableKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
                return;
            }

            if (pdc.has(GUIUtils.ClickableKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
                String value = pdc.get(GUIUtils.ClickableKey, PersistentDataType.STRING);

                if (Objects.equals(value, NEXT_CRAFT)) {
                    gui.NextRecipe(player);
                    org.bukkit.Sound bSound = Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", "ui_button_click"));
                    if (bSound != null) player.playSound(player.getLocation(), bSound, 1.0f, 1.0f);
                } else if (Objects.equals(value, PREV_CRAFT)) {
                    gui.PreviousRecipe(player);
                    Sound bSound = Registry.SOUND_EVENT.get(new NamespacedKey("minecraft", "ui_button_click"));
                    if (bSound != null) player.playSound(player.getLocation(), bSound, 1.0f, 1.0f);
                } else if (Objects.equals(value, "craft")) {
                    gui.CraftItem(player);
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(SkysongCrafting.Instance(), new Runnable() {
            @Override
            public void run() {
                gui.CheckForRecipes(player);
            }
        }, 1L);
    }


    @EventHandler
    public void onGUIClose(InventoryCloseEvent event){
        if(!(event.getInventory().getHolder() instanceof AbstractWorkbench)) return;
        AbstractWorkbench gui = (AbstractWorkbench) event.getInventory().getHolder();

        Bukkit.getScheduler().runTaskLater(SkysongCrafting.Instance(), () -> {
            if (event.getInventory().getViewers().isEmpty()) {
                gui.ReturnItems((Player) event.getPlayer());
            }
        }, 1L);
    }
}
