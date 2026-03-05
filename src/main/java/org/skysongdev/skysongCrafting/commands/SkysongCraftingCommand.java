package org.skysongdev.skysongCrafting.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.commands.management.ForceCloseInventories;
import org.skysongdev.skysongCrafting.commands.workbench.WorkbenchCommand;
import org.skysongdev.skysongCrafting.commands.management.ReloadCommand;

import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;


public class SkysongCraftingCommand implements TabExecutor {
    private final WorkbenchCommand workbenchCommand = new WorkbenchCommand();
    private final ReloadCommand reloadCommand = new ReloadCommand();
    private final ForceCloseInventories forceCloseInventories = new ForceCloseInventories();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length < 1){
            sender.sendMessage("Usage: /skysongcrafting <subcommand> [args]");
            return true;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        switch(args[0].toLowerCase()){
            case "workbench":
            case "wb":
                workbenchCommand.onCommand(sender, command, label, newArgs);
                break;
            case "forceclose":
                forceCloseInventories.onCommand(sender, command, label, newArgs);
                break;
            case "reload":
                reloadCommand.onCommand(sender, command, label, newArgs);
                break;
            default:
                sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>Unknown subcommand."));
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String[] newstrings = Arrays.copyOfRange(args, 1, args.length);
        if(args.length == 1){
            return Arrays.asList("workbench", "wb", "forceclose", "reload").stream().filter(a -> a.startsWith(args[0])).toList();
        } if(args.length > 1){
            switch(args[0].toLowerCase()){
                case "workbench":
                case "wb":
                    return workbenchCommand.onTabComplete(sender, command, label, newstrings);
                case "reload":
                    return null;
                case "forceclose":
                    return null;
            }
        }
        return null;
    }
}
