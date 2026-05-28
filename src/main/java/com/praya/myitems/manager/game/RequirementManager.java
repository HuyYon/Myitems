package com.praya.myitems.manager.game;

import api.praya.agarthalib.builder.support.main.SupportClass;
import api.praya.agarthalib.manager.plugin.SupportManagerAPI;
import api.praya.agarthalib.builder.support.main.SupportLevel;
import api.praya.agarthalib.main.AgarthaLibAPI;
import org.bukkit.entity.Player;
import com.praya.agarthalib.utility.MathUtil;
import java.util.List;
import com.praya.agarthalib.utility.PlayerUtil;
import java.util.UUID;
import core.praya.agarthalib.bridge.unity.Bridge;
import com.praya.agarthalib.utility.ServerUtil;
import core.praya.agarthalib.enums.main.VersionNMS;
import com.praya.agarthalib.utility.EquipmentUtil;
import org.bukkit.inventory.ItemStack;
import com.praya.agarthalib.utility.TextUtil;
import java.util.HashMap;
import org.bukkit.OfflinePlayer;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerManager;

public class RequirementManager extends HandlerManager {

    protected RequirementManager(final MyItems plugin) {
        super(plugin);
    }

    public final String getTextSoulUnbound() {
        final MainConfig mainConfig = MainConfig.getInstance();
        return mainConfig.getRequirementFormatSoulUnbound();
    }

    public final String getTextSoulBound(final OfflinePlayer player) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<String, String> map = new HashMap<>();
        String format = mainConfig.getRequirementFormatSoulBound();
        map.put("player", this.getKeyReqSoulBound(player, false));
        return TextUtil.placeholder(map, format, "<", ">");
    }

    public final String getTextLevel(final int level) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<String, String> map = new HashMap<>();
        String format = mainConfig.getRequirementFormatLevel();
        map.put("level", this.getKeyReqLevel(level, false));
        return TextUtil.placeholder(map, format, "<", ">");
    }

    public final String getTextPermission(final String permission) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<String, String> map = new HashMap<>();
        String format = mainConfig.getRequirementFormatPermission();
        map.put("permission", this.getKeyReqPermission(permission, false));
        return TextUtil.placeholder(map, format, "<", ">");
    }

    public final String getTextClass(final String playerClass) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<String, String> map = new HashMap<>();
        String format = mainConfig.getRequirementFormatClass();
        map.put("class", this.getKeyReqClass(playerClass, false));
        return TextUtil.placeholder(map, format, "<", ">");
    }

    public final Integer getLineRequirementSoulUnbound(final ItemStack item) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = mainConfig.getRequirementFormatSoulUnbound();
        if (EquipmentUtil.hasLore(item)) {
            final int line = EquipmentUtil.loreGetLineKey(item, key);
            return (line >= 0) ? line : null;
        }
        return null;
    }

    public final Integer getLineRequirementSoulBound(final ItemStack item) {
        final String key = this.getKeyReqSoulBound();
        if (EquipmentUtil.hasLore(item)) {
            final int line = EquipmentUtil.loreGetLineKey(item, key);
            return (line >= 0) ? line : null;
        }
        return null;
    }

    public final Integer getLineRequirementLevel(final ItemStack item) {
        final String key = this.getKeyReqLevel();
        if (EquipmentUtil.hasLore(item)) {
            final int line = EquipmentUtil.loreGetLineKey(item, key);
            return (line >= 0) ? line : null;
        }
        return null;
    }

    public final Integer getLineRequirementPermission(final ItemStack item) {
        final String key = this.getKeyReqPermission();
        if (EquipmentUtil.hasLore(item)) {
            final int line = EquipmentUtil.loreGetLineKey(item, key);
            return (line >= 0) ? line : null;
        }
        return null;
    }

    public final Integer getLineRequirementClass(final ItemStack item) {
        final String key = this.getKeyReqClass();
        if (EquipmentUtil.hasLore(item)) {
            final int line = EquipmentUtil.loreGetLineKey(item, key);
            return (line >= 0) ? line : null;
        }
        return null;
    }

    public final boolean hasRequirementSoulUnbound(final ItemStack item) {
        return this.getLineRequirementSoulUnbound(item) != null;
    }

    public final boolean hasRequirementSoulBound(final ItemStack item) {
        return this.getLineRequirementSoulBound(item) != null;
    }

    public final boolean hasrequirementLevel(final ItemStack item) {
        return this.getLineRequirementLevel(item) != null;
    }

    public final boolean hasRequirementPermission(final ItemStack item) {
        return this.getLineRequirementPermission(item) != null;
    }

    public final boolean hasRequirementClass(final ItemStack item) {
        return this.getLineRequirementClass(item) != null;
    }

    public final void setMetadataSoulbound(final OfflinePlayer player, final ItemStack item) {
        final String metadata = this.getMetadataSoulBound();
        final String bound = player.getUniqueId().toString();
        Bridge.getBridgeTagsNBT().setString(metadata, item, bound);
    }

    public final String getRequirementSoulBound(final ItemStack item) {
        final OfflinePlayer bound = this.getRequirementSoulBoundPlayer(item);
        return (bound != null) ? bound.getName() : null;
    }

    public final String getRequirementSoulBound(final String lore) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = MainConfig.KEY_REQ_BOUND;
        final String[] textListValue = lore.split(java.util.regex.Pattern.quote(key));
        if (textListValue.length > 1) {
            final String color = mainConfig.getRequirementColorSoulBound();
            return textListValue[1].replace(color, "");
        }
        return null;
    }

    public final OfflinePlayer getRequirementSoulBoundPlayer(final ItemStack item) {
        final String metadata = this.getMetadataSoulBound();
        final String bound = Bridge.getBridgeTagsNBT().getString(metadata, item);

        if (bound != null && !bound.isEmpty() && bound.length() >= 36) {
            try {
                final UUID playerID = UUID.fromString(bound);
                return PlayerUtil.getPlayer(playerID);
            } catch (IllegalArgumentException ignored) {
            }
        }

        final Integer line = this.getLineRequirementSoulBound(item);
        if (line != null) {
            final List<String> lores = EquipmentUtil.getLores(item);
            if (line > 0 && line <= lores.size()) {
                final String lore = lores.get(line - 1);
                final String boundName = this.getRequirementSoulBound(lore);
                return (boundName != null) ? PlayerUtil.getPlayer(boundName) : null;
            }
        }
        return null;
    }

    public final Integer getRequirementLevel(final ItemStack item) {
        final Integer line = this.getLineRequirementLevel(item);
        if (line != null) {
            final List<String> lores = EquipmentUtil.getLores(item);
            final String lore = lores.get(line - 1);
            return this.getRequirementLevel(lore);
        }
        return null;
    }

    public final Integer getRequirementLevel(final String lore) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = MainConfig.KEY_REQ_LEVEL;
        final String[] textListValue = lore.split(java.util.regex.Pattern.quote(key));
        if (textListValue.length > 1) {
            final String color = mainConfig.getRequirementColorLevel();
            final String textValue = textListValue[1].replace(color, "");
            if (MathUtil.isNumber(textValue)) {
                return MathUtil.parseInteger(textValue);
            }
        }
        return null;
    }

    public final String getRequirementPermission(final ItemStack item) {
        final Integer line = this.getLineRequirementPermission(item);
        if (line != null) {
            final List<String> lores = EquipmentUtil.getLores(item);
            final String lore = lores.get(line - 1);
            return this.getRequirementPermission(lore);
        }
        return null;
    }

    public final String getRequirementPermission(final String lore) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = MainConfig.KEY_REQ_PERMISSION;
        final String[] textListValue = lore.split(java.util.regex.Pattern.quote(key));
        if (textListValue.length > 1) {
            final String color = mainConfig.getRequirementColorPermission();
            return textListValue[1].replace(color, "");
        }
        return null;
    }

    public final String getRequirementClass(final ItemStack item) {
        final Integer line = this.getLineRequirementClass(item);
        if (line != null) {
            final List<String> lores = EquipmentUtil.getLores(item);
            final String lore = lores.get(line - 1);
            return this.getRequirementClass(lore);
        }
        return null;
    }

    public final String getRequirementClass(final String lore) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = MainConfig.KEY_REQ_CLASS;
        final String[] textListValue = lore.split(java.util.regex.Pattern.quote(key));
        if (textListValue.length > 1) {
            final String color = mainConfig.getRequirementColorClass();
            return textListValue[1].replace(color, "");
        }
        return null;
    }

    public final boolean isAllowed(final Player player, final ItemStack item) {
        return this.isAllowedReqSoulBound(player, item) &&
                this.isAllowedReqLevel(player, item) &&
                this.isAllowedReqPermission(player, item) &&
                this.isAllowedReqClass(player, item);
    }

    public final boolean isAllowedReqSoulBound(final Player player, final ItemStack item) {
        final String bound = this.getRequirementSoulBound(item);
        return bound == null || this.isAllowedReqSoulBound(player, bound);
    }

    public final boolean isAllowedReqSoulBound(final Player player, final String bound) {
        final String name = player.getName();
        final String id = player.getUniqueId().toString();
        return bound.equalsIgnoreCase(name) || bound.equalsIgnoreCase(id);
    }

    public final boolean isAllowedReqLevel(final Player player, final ItemStack item) {
        final Integer reqLevel = this.getRequirementLevel(item);
        return reqLevel == null || this.isAllowedReqLevel(player, reqLevel);
    }

    public final boolean isAllowedReqLevel(final Player player, final int reqLevel) {
        final AgarthaLibAPI agarthaLibAPI = AgarthaLibAPI.getInstance();
        final SupportManagerAPI supportManagerAPI = agarthaLibAPI.getPluginManagerAPI().getSupportManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        final String textLevelType = mainConfig.getSupportTypeLevel();
        SupportLevel supportLevel;

        switch (textLevelType.toUpperCase()) {
            case "SKILLAPI":
            case "BATTLELEVELS":
                supportLevel = supportManagerAPI.getSupportLevel(SupportLevel.SupportLevelType.SKILL_API);
                break;
            case "SKILLSPRO":
                supportLevel = supportManagerAPI.getSupportLevel(SupportLevel.SupportLevelType.SKILLS_PRO);
                break;
            case "HEROES":
                supportLevel = supportManagerAPI.getSupportLevel(SupportLevel.SupportLevelType.HEROES);
                break;
            case "AUTO":
                supportLevel = supportManagerAPI.getSupportLevel();
                break;
            default:
                supportLevel = null;
                break;
        }

        final int playerLevel = (supportLevel != null) ? supportLevel.getPlayerLevel(player) : player.getLevel();
        return reqLevel <= playerLevel;
    }

    public final boolean isAllowedReqPermission(final Player player, final ItemStack item) {
        final String reqPermission = this.getRequirementPermission(item);
        return reqPermission == null || player.hasPermission(reqPermission);
    }

    public final boolean isAllowedReqClass(final Player player, final ItemStack item) {
        final AgarthaLibAPI agarthaLibAPI = AgarthaLibAPI.getInstance();
        final SupportManagerAPI supportManagerAPI = agarthaLibAPI.getPluginManagerAPI().getSupportManager();
        final String itemClass = this.getRequirementClass(item);
        if (itemClass == null) return true;
        if (!supportManagerAPI.isSupportClass()) return true;

        final SupportClass supportClass = this.getSupportReqClass();
        if (supportClass == null) return true;

        final String playerClass = supportClass.getPlayerMainClassName(player);
        return playerClass != null && playerClass.equalsIgnoreCase(itemClass);
    }

    public final SupportClass getSupportReqClass() {
        final AgarthaLibAPI agarthaLibAPI = AgarthaLibAPI.getInstance();
        final SupportManagerAPI supportManagerAPI = agarthaLibAPI.getPluginManagerAPI().getSupportManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        final String textClassType = mainConfig.getSupportTypeClass();

        switch (textClassType.toUpperCase()) {
            case "SKILLAPI":
                return supportManagerAPI.getSupportClass(SupportClass.SupportClassType.SKILL_API);
            case "SKILLSPRO":
                return supportManagerAPI.getSupportClass(SupportClass.SupportClassType.SKILLS_PRO);
            case "HEROES":
                return supportManagerAPI.getSupportClass(SupportClass.SupportClassType.HEROES);
            case "AUTO":
                return supportManagerAPI.getSupportClass();
            default:
                return null;
        }
    }

    public final boolean isSupportReqClass() {
        return this.getSupportReqClass() != null;
    }

    private String getMetadataSoulBound() {
        return "SoulBound";
    }

    private String getKeyReqSoulBound() {
        return this.getKeyReqSoulBound(null, true);
    }

    private String getKeyReqSoulBound(final OfflinePlayer player, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String bound = (player != null) ? player.getName() : "";
        final String colorSoulBound = mainConfig.getRequirementColorSoulBound();
        final String key = MainConfig.KEY_REQ_BOUND;
        return justCheck ? (key + colorSoulBound) : (key + colorSoulBound + bound + key + colorSoulBound);
    }

    private String getKeyReqLevel() {
        return this.getKeyReqLevel(0, true);
    }

    private String getKeyReqLevel(final int level, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String colorLevel = mainConfig.getRequirementColorLevel();
        final String key = MainConfig.KEY_REQ_LEVEL;
        return justCheck ? (key + colorLevel) : (key + colorLevel + level + key + colorLevel);
    }

    private String getKeyReqPermission() {
        return this.getKeyReqPermission(null, true);
    }

    private String getKeyReqPermission(final String permission, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String colorPermission = mainConfig.getRequirementColorPermission();
        final String key = MainConfig.KEY_REQ_PERMISSION;
        return justCheck ? (key + colorPermission) : (key + colorPermission + permission + key + colorPermission);
    }

    private String getKeyReqClass() {
        return this.getKeyReqClass(null, true);
    }

    private String getKeyReqClass(final String reqClass, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String colorClass = mainConfig.getRequirementColorClass();
        final String key = MainConfig.KEY_REQ_CLASS;
        return justCheck ? (key + colorClass) : (key + colorClass + reqClass + key + colorClass);
    }
}