package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.workstations.Workstation;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class CreateWorkbench implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 0){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>No workbench type provided! Usage: /sc workbench create <type>"));
            return true;
        }
        Workstation workstationType = Utils.Instance().getWorkstationTypes().get(args[0].toLowerCase());
        if(workstationType == null){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Invalid workbench type provided!"));
            return true;
        }
        Player target;
        if(args.length > 1){
            target = Bukkit.getPlayer(args[1]);
            if(target == null){
                sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Player " + args[1] + " not found!"));
                return true;
            }
        } else {
            target = (Player) sender;
        }
        Player player = (Player) sender;

        Block targetBlock = player.getTargetBlock(null, 10);
        if(targetBlock.getType().isAir()){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Please look at a valid block within 10 blocks!"));
            return true;
        }

        ArrayList<Integer> location = new ArrayList<Integer>(List.of(targetBlock.getLocation().getBlockX(),
                targetBlock.getLocation().getBlockY(),
                targetBlock.getLocation().getBlockZ()));
        WorkstationReference reference = new WorkstationReference(workstationType, target.getUniqueId(), new ArrayList<UUID>(), false, location, targetBlock.getLocation().getWorld().getName());

        try {
            SkysongCrafting.Instance().getDatabase().createWorkstationData(reference);
        } catch (SQLException e){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Error creating workbench in database: " + e.getMessage()));
            return true;
        }

        ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(location);
        existingRefs = Utils.Instance().addOrReplaceWorkstationReference(existingRefs, reference);
        Utils.getWorkstationReferences().put(location, existingRefs);

        sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>Successfully created a " + workstationType.getType() + " workbench for " + target.getName() + "!"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            ArrayList<String> completions = new ArrayList<>(Utils.Instance().getWorkstationTypes().keySet());
            return completions.stream().filter(a -> a.startsWith(args[0])).toList();
        }
        if(args.length == 2){
            ArrayList<String> completions = new ArrayList<>();
            for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
            }
            return completions.stream().filter(a -> a.startsWith(args[1])).toList();
        }
        return List.of();
    }
}
