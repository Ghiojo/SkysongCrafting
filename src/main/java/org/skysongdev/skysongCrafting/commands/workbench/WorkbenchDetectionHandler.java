package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class WorkbenchDetectionHandler {
    public static WorkstationReference handleDetection(Player player){

        Block targetBlock = player.getTargetBlock(null, 10);
        if (targetBlock.getType().isAir()) {
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Please look at a valid workbench within 10 blocks!"));
            return null;
        }
        ArrayList<Integer> location = new ArrayList<>(List.of(targetBlock.getLocation().toBlockLocation().getBlockX(),
                targetBlock.getLocation().toBlockLocation().getBlockY(),
                targetBlock.getLocation().toBlockLocation().getBlockZ()));
        Bukkit.getLogger().info("Attempted to detect workbench at location: " + location);
        ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(location);
        WorkstationReference ref = Utils.Instance().getWorkstationReferenceByWorld(existingRefs, player.getWorld().getName().toString());
        if (ref == null) {
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>No workbench found at this location!"));
            return null;
        }

        return ref;
    }
}
