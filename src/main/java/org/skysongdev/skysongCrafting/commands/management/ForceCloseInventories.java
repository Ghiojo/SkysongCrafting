package org.skysongdev.skysongCrafting.commands.management;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class ForceCloseInventories implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(sender instanceof Player player){
            if(!player.hasPermission("skysongcrafting.management.forcecloseinventories")){
                player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                return true;
            }
        }

        for(Player player : Bukkit.getOnlinePlayers()){
            player.closeInventory();
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Your inventory has been forcefully closed to avoid potential breaks on restarts"));
        }

        return true;
    }
}
