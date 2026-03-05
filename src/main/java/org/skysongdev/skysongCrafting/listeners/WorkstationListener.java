package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class WorkstationListener implements Listener {
    @EventHandler
    public void onWorkstationUse(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if(block == null) {
            return;
        }
        ArrayList<Integer> coords = new ArrayList<>(List.of(block.getX(), block.getY(), block.getZ()));
        ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(coords);

        WorkstationReference reference = Utils.Instance().getWorkstationReferenceByWorld(existingRefs, event.getPlayer().getWorld().getName());
        if(reference == null) {
            return;
        }
        event.setCancelled(true);
        if(!reference.isPublic())
            if(event.getPlayer().getUniqueId() != reference.getOwnerID() && !reference.getAuthorizedUsers().contains(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("skysongcrafting.workstation.use.bypass")) {
                event.getPlayer().sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this workstation."));
                return;
            }
        event.getPlayer().openInventory(reference.getWorkstation().getGui().getInventory());
    }

    @EventHandler
    public void onWorkstationDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        ArrayList<Integer> coords = new ArrayList<>(List.of(block.getX(), block.getY(), block.getZ()));
        ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(coords);

        WorkstationReference reference = Utils.Instance().getWorkstationReferenceByWorld(existingRefs, event.getPlayer().getWorld().getName());
        if(reference == null) {
            return;
        }
        try {
            SkysongCrafting.Instance().getDatabase().deleteWorkstationData(Utils.Instance().getWorkstationReferenceByWorld(Utils.getWorkstationReferences().get(coords), event.getPlayer().getWorld().getName()));
        } catch (SQLException e) {
            event.getPlayer().sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>An error occurred while trying to remove the workstation from the database."));
            event.setCancelled(true);
            return;
        }
        if(event.getPlayer().getUniqueId() != reference.getOwnerID() && !event.getPlayer().hasPermission("skysongcrafting.workstation.destroy.others")) {
            event.getPlayer().sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to destroy this workstation."));
            event.setCancelled(true);
            return;
        }

        Utils.getWorkstationReferences().remove(coords);
        event.getPlayer().sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>Workstation removed successfully."));
    }
}
