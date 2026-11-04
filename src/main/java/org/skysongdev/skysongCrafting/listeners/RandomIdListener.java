package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.output.RandomIdOutput;
import org.skysongdev.skysongCrafting.events.CraftingEvent;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongitems.api.ItemTags;

import java.util.UUID;

/**
 * For {@code random_id} outputs: gives every produced item whose id tag is empty a fresh UUID.
 * Works on the result stacks carried by the event, before they are placed, so the tag is written
 * through SkysongItems' detached view (no lore redraw, no event). SkysongCrafting registers the
 * tag itself (see {@link SkysongItemsRegistrationListener}).
 */
public class RandomIdListener implements Listener {
    @EventHandler
    public void randomizeId(CraftingEvent event){
        if(!(event.getRecipe().getResults() instanceof RandomIdOutput output))
            return;

        String tagId = output.getIdToRandomize();
        ItemTags tags = SkysongApis.items().detached().tags();
        for(ItemStack result : event.getResults()){
            String current = tags.getString(result, tagId).orElse("");
            if(!current.isEmpty()) continue;
            try {
                tags.set(result, tagId, UUID.randomUUID().toString());
            } catch (IllegalArgumentException e) {
                SkysongCrafting.Instance().getLogger().warning("Could not give a random id to a crafted item: SkysongItems refused tag '"
                        + tagId + "' (" + e.getMessage() + "). If the tag was added by /sc reload, run /si reload so SkysongItems registers it.");
            }
        }
    }
}
