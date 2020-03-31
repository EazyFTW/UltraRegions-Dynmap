package com.eazyftw.ultraregionsdynmap.cmds;

import com.eazyftw.api.color.EZMessage;
import com.eazyftw.api.color.FancyMessage;
import com.eazyftw.ultraregionsdynmap.UltraRegionsDynmap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UltraRegionsDynmapCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("urd.command")) {
            EZMessage.text("%prefix% &cYou cannot execute the command /urd!").sender(sender);
            return true;
        }
        if(args.length == 0) {
            sendHelp(sender);
        } else if(args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
        } else if(args[0].equalsIgnoreCase("update")) {
            UltraRegionsDynmap.getRegionsManager().update();
            EZMessage.text("%prefix% &aSuccessfully updated the map!").sender(sender);
        } else if(args[0].equalsIgnoreCase("reload")) {
            UltraRegionsDynmap.getInstance().reloadConfig();
            EZMessage.text("%prefix% &aSuccessfully reloaded!").sender(sender);
            UltraRegionsDynmap.getRegionsManager().stopThenStart();
        } else {
            sendHelp(sender);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if(args.length == 0) return list;
        if (args.length == 1 && args[0].length() == 0) {
            list.add("help");
            list.add("update");
            list.add("reload");
        }
        if(args.length == 1 && args[0].length() > 0) {
            if("help".startsWith(args[0])) list.add("help");
            if("update".startsWith(args[0])) list.add("update");
            if("reload".startsWith(args[0])) list.add("reload");
        }
        return list;
    }

    public void sendHelp(CommandSender sender) {
        EZMessage.text("").sender(sender);
        EZMessage.text("&9&lUltraRegions Dynmap").sender(sender, true, true, false);
        EZMessage.text("&7Commands").sender(sender, true, true, false);
        EZMessage.text("").sender(sender);
        if(sender instanceof Player) {
            Player p = (Player)sender;
            new FancyMessage().append("&9/urd <help> &8- &7Shows this message.", true).setHoverAsTooltip("&9/urd <help>", "&7Will show this message.", "", "&9Permission: &7urd.command", "", "&eClick to suggest.").setClickAsSuggestCmd("/urd help").save().send(p);
            new FancyMessage().append("&9/urd update &8- &7Updates the map automatically.", true).setHoverAsTooltip("&9/urd update", "&7Updates the map automatically, so you don't have to wait for the timer.", "", "&9Permission: &7urd.update", "", "&eClick to suggest.").setClickAsSuggestCmd("/urd update").save().send(p);
            new FancyMessage().append("&9/urd reload &8- &7Reloads the plugin.", true).setHoverAsTooltip("&9/urd reload", "&7Reloads the plugin.", "", "&9Permission: &7urd.reload", "", "&eClick to suggest.").setClickAsSuggestCmd("/urd reload").save().send(p);
        } else {
            EZMessage.text("&9/urd <help> &8- &7Shows this message.").sender(sender, true, true, false);
            EZMessage.text("&9/urd update &8- &7Updates the map automatically.").sender(sender, true, true, false);
            EZMessage.text("&9/urd reload &8- &7Reloads the plugin.").sender(sender, true, true, false);
        }
        EZMessage.text("").sender(sender);
    }
}