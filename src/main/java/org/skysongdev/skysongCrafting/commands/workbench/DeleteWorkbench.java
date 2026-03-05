package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class DeleteWorkbench implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = (Player) sender;

        WorkstationReference ref = WorkbenchDetectionHandler.handleDetection(player);
        if(ref == null){
            return true;
        }

        try {
            SkysongCrafting.Instance().getDatabase().deleteWorkstationData(ref);
            ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(ref.getLocation());
            existingRefs = Utils.Instance().removeWorkstationReferenceByWorld(existingRefs, ref.getWorldName());
            if(existingRefs == null || existingRefs.isEmpty()){
                Utils.getWorkstationReferences().put(ref.getLocation(), null);
            } else {
                Utils.getWorkstationReferences().put(ref.getLocation(), existingRefs);
            }
        } catch (SQLException e){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Error creating workbench in database: " + e.getMessage()));
            return true;
        }

        sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>Workbench deleted successfully."));

        return true;
    }
}
