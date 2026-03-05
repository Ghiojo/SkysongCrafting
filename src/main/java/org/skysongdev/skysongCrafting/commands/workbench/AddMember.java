package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class AddMember implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length < 1){
            sender.sendMessage("Usage: /sc workbench addmember <player>");
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

        if(!ref.getOwnerID().equals(player.getUniqueId()) && !player.hasPermission("skysongcrafting.workbench.addmember.others")){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You are not the owner of this workbench!"));
            return true;
        }

        ArrayList<UUID> members = ref.getAuthorizedUsers();
        members.add(target.getUniqueId());
        try{
            ref.setAuthorizedUsers(members);
            SkysongCrafting.Instance().getDatabase().updateWorkstationData(ref);
        } catch (SQLException e) {
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Failed to update workbench data on the database! (Contact Tech Team)"));
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>Successfully added " + target.getName() + " as a member of this workbench!"));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1) {
            ArrayList<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return completions.stream().filter(a -> a.startsWith(args[0])).toList();
        }
        return null;
    }
}
