package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class TogglePublic implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length < 1){
            sender.sendMessage("Usage: /sc workbench togglepublic <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Player " + args[1] + " not found!"));
            return true;
        }
        Player player = (Player) sender;
        WorkstationReference ref = WorkbenchDetectionHandler.handleDetection(player);
        if(ref == null){
            return true;
        }

        if(!ref.getOwnerID().equals(player.getUniqueId()) && !player.hasPermission("skysongcrafting.workbench.togglepublic.others")){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You are not the owner of this workbench!"));
            return true;
        }

        try{
            ref.togglePublic();
            SkysongCrafting.Instance().getDatabase().updateWorkstationData(ref);
        } catch (Exception e) {
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Failed to update workbench data on the database! (Contact Tech Team)"));
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>Successfully toggled made the workbench" + (ref.isPublic() ? " <gold>public<green>!" : " <red>private<green>!")));

        return true;
    }
}
