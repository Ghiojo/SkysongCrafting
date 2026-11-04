package org.skysongdev.skysongCrafting.commands.management;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.listeners.SkysongItemsRegistrationListener;

import java.util.Set;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Works for the console too: no Player cast, permission checked on the sender.
        if(!sender.hasPermission("skysongcrafting.management.reload")){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
            return true;
        }

        // Same sequence as startup: tags, workstation types, recipes (each initializer clears its own map).
        SkysongCrafting.Instance().InitializeFiles();

        sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>The plugin has been reloaded successfully."));

        // SkysongItems only takes new tag registrations during its own load, so a random_id tag that this
        // reload added is unknown until /si reload.
        Set<String> missing = SkysongItemsRegistrationListener.warnAboutMissingTags();
        if(!missing.isEmpty()){
            sender.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<yellow>SkysongItems does not have these tags yet: "
                    + String.join(", ", missing) + ". Run <white>/si reload</white> so SkysongCrafting can register them."));
        }
        return true;
    }
}
