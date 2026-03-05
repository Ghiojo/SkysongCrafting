package org.skysongdev.skysongCrafting.commands.management;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.utils.Utils;
import org.skysongdev.skysongCrafting.utils.YamlParsingUtils;

import static org.skysongdev.skysongCrafting.utils.Constants.PLUGIN_TAG;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = (Player) sender;
        if(!player.hasPermission("skysongcrafting.management.reload")){
            player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<red>You do not have permission to use this command."));
            return true;
        }

        Utils.Instance().getShapedRecipeMap().clear();
        Utils.Instance().getShapelessRecipeMap().clear();
        Utils.Instance().getWorkstationTypes().clear();
        YamlParsingUtils.InstanceTags();
        SkysongCrafting.Instance().initializeRecipes();
        YamlParsingUtils.initializeWorkbenches();

        player.sendMessage(SkysongCrafting.GetMM().deserialize(PLUGIN_TAG + "<green>The plugin has been reloaded successfully."));

        return true;
    }
}
