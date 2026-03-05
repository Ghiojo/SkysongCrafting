package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.RandomIdOutput;
import org.skysongdev.skysongCrafting.events.CraftingEvent;
import org.skysongdev.skysongitems.SkysongItems;

import java.util.UUID;

public class RandomIdListener implements Listener {
    @EventHandler
    public void randomizeId(CraftingEvent event){
        if(!(event.getRecipe().getResults() instanceof RandomIdOutput))
            return;
        RandomIdOutput output = (RandomIdOutput) event.getRecipe().getResults();
        boolean success = false;

        NamespacedKey key = new NamespacedKey(SkysongItems.getPlugin(), output.getIdToRandomize());
        for(Integer i: event.getOutputSlotReferences() ){
            if(event.getInventory().getItem(i).getItemMeta().getPersistentDataContainer().has(key) && event.getInventory().getItem(i).getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING).isEmpty()){
                success = true;
                ItemMeta meta = event.getInventory().getItem(i).getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(SkysongItems.getPlugin(), output.getIdToRandomize()), PersistentDataType.STRING, UUID.randomUUID().toString());
                event.getInventory().getItem(i).setItemMeta(meta);
                break;
            }
        }
        if(!success){
            for(int i = 0; i < event.getPlayer().getInventory().getSize(); i++){
                if(event.getPlayer().getInventory().getItem(i) == null) continue;
                if(event.getPlayer().getInventory().getItem(i).getItemMeta().getPersistentDataContainer().has(key) && event.getPlayer().getInventory().getItem(i).getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING).isEmpty()){
                    ItemMeta meta = event.getPlayer().getInventory().getItem(i).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(SkysongItems.getPlugin(), output.getIdToRandomize()), PersistentDataType.STRING, UUID.randomUUID().toString());
                    event.getPlayer().getInventory().getItem(i).setItemMeta(meta);
                    break;
                }
            }
        }

    }
}
