package com.praya.myitems.builder.placeholder;

import com.praya.myitems.manager.plugin.PlaceholderManager;
import com.praya.myitems.MyItems;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ReplacerPlaceholderAPIBuild extends PlaceholderExpansion {
    private final String identifier;
    private final MyItems plugin;

    public ReplacerPlaceholderAPIBuild(MyItems plugin, String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        PlaceholderManager placeholderManager = plugin.getPluginManager().getPlaceholderManager();
        return placeholderManager.getReplacement(player, params);
    }
}
