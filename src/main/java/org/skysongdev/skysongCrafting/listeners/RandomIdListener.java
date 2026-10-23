package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.RandomIdOutput;
import org.skysongdev.skysongCrafting.events.CraftingEvent;
import org.skysongdev.skysongitems.SkysongItems;

import java.util.UUID;

/**
 * For {@code random_id} outputs: gives every produced item whose id tag is empty a fresh UUID.
 * Works on the result stacks carried by the event, before they are placed (previously it searched
 * the output slots and the player's inventory, which did not contain the results yet).
 */
public class RandomIdListener implements Listener {
    @EventHandler
    public void randomizeId(CraftingEvent event){
        if(!(event.getRecipe().getResults() instanceof RandomIdOutput output))
            return;

        NamespacedKey key = new NamespacedKey(SkysongItems.getPlugin(), output.getIdToRandomize());
        for(ItemStack result : event.getResults()){
            ItemMeta meta = result.getItemMeta();
            if(meta == null) continue;
            String current = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if(current != null && !current.isEmpty()) continue;
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, UUID.randomUUID().toString());
            result.setItemMeta(meta);
        }
    }
}
