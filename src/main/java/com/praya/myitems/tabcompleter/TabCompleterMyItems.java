package com.praya.myitems.tabcompleter;

import com.praya.myitems.MyItems;
import com.praya.agarthalib.utility.ServerUtil;
import core.praya.agarthalib.enums.main.VersionNMS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class TabCompleterMyItems implements TabCompleter {
    private final Map<String, TabCompleter> subTabs = new HashMap<>();
    private final List<String> mainSubs = new ArrayList<>();

    public TabCompleterMyItems(MyItems plugin) {
        subTabs.put("att", new TabCompleterAttributes(plugin));
        subTabs.put("addenchant", new TabCompleterEnchantmentAdd(plugin));
        subTabs.put("remenchant", new TabCompleterEnchantmentRemove(plugin));
        subTabs.put("remlore", new TabCompleterLoreRemove(plugin));
        subTabs.put("socket", new TabCompleterSocket(plugin));
        subTabs.put("unbreakable", new TabCompleterUnbreakable(plugin));
        if (ServerUtil.isCompatible(VersionNMS.V1_21_R3)) {
            subTabs.put("addflag", new TabCompleterFlagAdd(plugin));
            subTabs.put("remflag", new TabCompleterFlagRemove(plugin));
        } else {
            TabCompleter notCompatible = new TabCompleterNotCompatible(plugin);
            subTabs.put("addflag", notCompatible);
            subTabs.put("remflag", notCompatible);
        }
        mainSubs.addAll(subTabs.keySet());
        mainSubs.add("reload");
        mainSubs.add("help");
        mainSubs.add("setname");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return mainSubs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (args.length > 1) {
            String subLabel = args[0].toLowerCase();
            if (subTabs.containsKey(subLabel)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subTabs.get(subLabel).onTabComplete(sender, command, alias, subArgs);
            }
        }

        return new ArrayList<>();
    }
}