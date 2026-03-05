package org.skysongdev.skysongCrafting.commands.workbench;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongCrafting.SkysongCrafting;

import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class WorkbenchCommand implements TabExecutor {
    private CreateWorkbench createWorkbench = new CreateWorkbench();
    private DeleteWorkbench deleteWorkbench = new DeleteWorkbench();
    private AddMember addMember = new AddMember();
    private RemoveMember removeMember = new RemoveMember();
    private SetOwner setOwner = new SetOwner();
    private TogglePublic togglePublic = new TogglePublic();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length < 1){
            sender.sendMessage("Usage: /skysongcrafting <subcommand> [args]");
            return true;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        Player player = (Player) sender;

        switch(args[0].toLowerCase()){
            case "create":
                if(player.hasPermission("skysongcrafting.workbench.create"))
                    createWorkbench.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                break;
            case "delete":
                if(player.hasPermission("skysongcrafting.workbench.delete"))
                    deleteWorkbench.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                break;
            case "addmember":
                if(player.hasPermission("skysongcrafting.workbench.addmember"))
                    addMember.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                break;
            case "removemember":
                if(player.hasPermission("skysongcrafting.workbench.removemember"))
                    removeMember.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                break;
            case "setowner":
                if(player.hasPermission("skysongcrafting.workbench.setowner"))
                    setOwner.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
                break;
            case "togglepublic":
                if(player.hasPermission("skysongcrafting.workbench.togglepublic"))
                    togglePublic.onCommand(sender, command, label, newArgs);
                else
                    sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
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
            return Arrays.asList("create", "delete", "addmember", "removemember", "setowner", "togglepublic").stream().filter(a -> a.startsWith(args[0])).toList();
        } if(args.length > 1){
            switch(args[0].toLowerCase()){
                case "create":
                    return createWorkbench.onTabComplete(sender, command, label, newstrings);
                case "addmember":
                    return addMember.onTabComplete(sender, command, label, newstrings);
                case "removemember":
                    return removeMember.onTabComplete(sender, command, label, newstrings);
                case "setowner":
                    return setOwner.onTabComplete(sender, command, label, newstrings);
                case "togglepublic":
                case "delete":
                    return null;
            }
        }
        return null;
    }
}
