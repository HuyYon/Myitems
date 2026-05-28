package com.praya.myitems.tabcompleter;

import com.praya.myitems.MyItems;
import com.praya.myitems.manager.plugin.CommandManager;
import com.praya.myitems.manager.plugin.PluginManager;
import com.praya.agarthalib.utility.TabCompleterUtil;
import com.praya.agarthalib.utility.SenderUtil;
import core.praya.agarthalib.enums.branch.SoundEnum;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.praya.myitems.builder.handler.HandlerTabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterEnchantmentAdd extends HandlerTabCompleter implements TabCompleter {
    public TabCompleterEnchantmentAdd(final MyItems plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        final PluginManager pluginManager = this.plugin.getPluginManager();
        final CommandManager commandManager = pluginManager.getCommandManager();
        final List<String> tabList = new ArrayList<>();
        SenderUtil.playSound(sender, SoundEnum.BLOCK_WOOD_BUTTON_CLICK_ON);
        if (SenderUtil.isPlayer(sender) && args.length == 1 && commandManager.checkPermission(sender, "Enchant_Add")) {
            for (Enchantment enchantment : Enchantment.values()) {
                String enchantKey = enchantment.getKey().getKey();
                tabList.add(enchantKey);
            }
        }

        return (List<String>) TabCompleterUtil.returnList(tabList, args);
    }
}