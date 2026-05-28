package com.praya.myitems.manager.game;

import api.praya.myitems.builder.passive.PassiveEffectEnum;
import java.util.Collection;
import com.praya.myitems.manager.plugin.LanguageManager;
import com.praya.myitems.manager.plugin.PluginManager;
import com.praya.agarthalib.utility.PlayerUtil;
import org.bukkit.command.CommandSender;
import com.praya.agarthalib.utility.SenderUtil;
import core.praya.agarthalib.enums.branch.SoundEnum;
import com.praya.agarthalib.utility.ServerUtil;
import core.praya.agarthalib.enums.main.VersionNMS;
import api.praya.myitems.builder.lorestats.LoreStatsArmor;
import core.praya.agarthalib.enums.main.SlotType;
import api.praya.myitems.builder.lorestats.LoreStatsWeapon;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import com.praya.agarthalib.utility.EntityUtil;
import org.bukkit.entity.LivingEntity;
import com.praya.agarthalib.utility.MathUtil;
import api.praya.myitems.builder.lorestats.LoreStatsOption;
import com.praya.agarthalib.utility.TextUtil;
import java.util.HashMap;
import java.util.List;

import com.praya.myitems.config.plugin.MainConfig;
import com.praya.agarthalib.utility.EquipmentUtil;
import org.bukkit.inventory.ItemStack;
import api.praya.myitems.builder.lorestats.LoreStatsEnum;
import core.praya.agarthalib.enums.main.Slot;
import core.praya.agarthalib.bridge.unity.Bridge;
import org.bukkit.entity.Player;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerManager;

public class LoreStatsManager extends HandlerManager
{
    protected LoreStatsManager(final MyItems plugin) {
        super(plugin);
    }

    public final boolean hasLoreStats(final Player player, final String lorestats) {
        return this.hasLoreStats(Bridge.getBridgeEquipment().getEquipment(player, Slot.MAINHAND), LoreStatsEnum.get(lorestats));
    }

    public final boolean hasLoreStats(final Player player, final LoreStatsEnum lorestats) {
        return this.hasLoreStats(Bridge.getBridgeEquipment().getEquipment(player, Slot.MAINHAND), lorestats);
    }

    public final boolean hasLoreStats(final ItemStack item, final String lorestats) {
        return this.hasLoreStats(item, LoreStatsEnum.get(lorestats));
    }

    public final boolean hasLoreStats(final ItemStack item, final LoreStatsEnum loreStats) {
        return this.getLineLoreStats(item, loreStats) != -1;
    }

    public final String getTextLoreStats(final LoreStatsEnum loreStats, final double value) {
        return loreStats.equals(LoreStatsEnum.LEVEL) ? this.getTextLoreStats(loreStats, value, 0.0) : this.getTextLoreStats(loreStats, value, value);
    }

    public final String getTextLoreStats(final LoreStatsEnum loreStats, final double value1, final double value2) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<String, String> map = new HashMap<String, String>();
        String format = mainConfig.getStatsFormatValue();
        map.put("stats", this.getKeyStats(loreStats));
        map.put("value", this.statsValue(loreStats, value1, value2));
        format = TextUtil.placeholder(map, format, "<", ">");
        return format;
    }

    public final double getLoreValue(final ItemStack item, final LoreStatsEnum loreStats, final LoreStatsOption option) {
        final int line = this.getLineLoreStats(item, loreStats);
        return (line != -1) ? this.getLoreValue(item, loreStats, option, line) : 0.0;
    }

    public final double getLoreValue(final ItemStack item, final LoreStatsEnum loreStats, final LoreStatsOption option, final int line) {
        final String lore = EquipmentUtil.getLores(item).get(line - 1);
        return this.getLoreValue(loreStats, option, lore);
    }

    public final double getLoreValue(final LoreStatsEnum loreStats, final LoreStatsOption option, final String lore) {
        final MainConfig mainConfig = MainConfig.getInstance();
        String plainLore = org.bukkit.ChatColor.stripColor(lore);
        if (!plainLore.contains(":")) return 0.0;
        String[] parts = plainLore.split(":");
        if (parts.length < 2) return 0.0;
        String textValue = parts[1].trim();
        final String positiveValue = mainConfig.getStatsLorePositiveValue();
        final String negativeValue = mainConfig.getStatsLoreNegativeValue();
        final String symbolDivide = mainConfig.getStatsLoreDividerSymbol().trim();
        final String symbolMultiplier = mainConfig.getStatsLoreMultiplierSymbol().trim();
        if (loreStats.equals(LoreStatsEnum.DAMAGE)) {
            final String symbolRange = mainConfig.getStatsLoreRangeSymbol();
            if (textValue.contains(symbolRange)) {
                final String[] valueList = textValue.split(java.util.regex.Pattern.quote(symbolRange));
                if (valueList.length >= 2) {
                    String textValueMin = valueList[0].replaceAll("[^0-9.-]", "");
                    String textValueMax = valueList[1].replaceAll("[^0-9.-]", "");
                    if (MathUtil.isNumber(textValueMin) && MathUtil.isNumber(textValueMax)) {
                        double valueMin = MathUtil.parseDouble(textValueMin);
                        double valueMax = MathUtil.parseDouble(textValueMax);
                        if (option == null || option.equals(LoreStatsOption.CURRENT)) {
                            return valueMin + Math.random() * (valueMax - valueMin);
                        } else if (option.equals(LoreStatsOption.MIN)) {
                            return valueMin;
                        } else if (option.equals(LoreStatsOption.MAX)) {
                            return valueMax;
                        }
                    }
                }
            }
            else {
                textValue = textValue
                        .replaceFirst(java.util.regex.Pattern.quote(positiveValue), "")
                        .replaceFirst(java.util.regex.Pattern.quote(negativeValue), "").trim();
                if (MathUtil.isNumber(textValue)) {
                    return MathUtil.parseDouble(textValue);
                }
            }
        } else if (loreStats.equals(LoreStatsEnum.DURABILITY)) {
            if (textValue.contains(symbolDivide)) {
                final String[] valueList = textValue.split(java.util.regex.Pattern.quote(symbolDivide));
                if (valueList.length >= 2) {
                    String curText = valueList[0].trim();
                    String maxText = valueList[1].trim();
                    java.util.regex.Matcher curMatcher = java.util.regex.Pattern.compile("\\d+").matcher(curText);
                    java.util.regex.Matcher maxMatcher = java.util.regex.Pattern.compile("\\d+").matcher(maxText);
                    if (curMatcher.find() && maxMatcher.find()) {
                        int current = Integer.parseInt(curMatcher.group());
                        int max = Integer.parseInt(maxMatcher.group());
                        if (option == null || option.equals(LoreStatsOption.CURRENT)) {
                            return current;
                        } else if (option.equals(LoreStatsOption.MIN)) {
                            return current;
                        } else if (option.equals(LoreStatsOption.MAX)) {
                            return max;
                        }
                    }
                }
            }
        }
        String finalNumber = textValue.replaceAll("[^0-9.-]", "");
        if (MathUtil.isNumber(finalNumber)) {
            return MathUtil.parseDouble(finalNumber);
        }
        else if (loreStats.equals(LoreStatsEnum.LEVEL)) {
            String levelPart = textValue.split(" ")[0].replaceAll("[^0-9]", "");
            if (option == LoreStatsOption.MIN || option == LoreStatsOption.MAX || option == null) {
                return MathUtil.parseDouble(levelPart);
            }
            if (textValue.contains("/") && textValue.contains("[")) {
                String expPart = textValue.substring(textValue.indexOf("[") + 1, textValue.indexOf("/")).trim();
                return MathUtil.parseDouble(expPart.replaceAll("[^0-9]", ""));
            }
            return MathUtil.parseDouble(levelPart);
        }
        textValue = textValue.replaceAll("[^0-9.-]", "");
        if (MathUtil.isNumber(textValue)) {
            double finalValue = MathUtil.parseDouble(textValue);
            if (loreStats.equals(LoreStatsEnum.CRITICAL_DAMAGE) && plainLore.contains(symbolMultiplier)) {
                return finalValue - 1.0;
            }
            return finalValue;
        }
        return 0.0;
    }

    public final void itemRepair(final ItemStack item, final int repair) {
        final int line = this.getLineLoreStats(item, LoreStatsEnum.DURABILITY);
        if (line != -1) {
            final int nowDurability = (int)this.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.CURRENT);
            final int maxDurability = (int)this.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.MAX);
            int durability = nowDurability + repair;
            durability = MathUtil.limitInteger(durability, 0, maxDurability);
            if (maxDurability != 0) {
                final String newDurability = (repair != -1) ? this.getTextLoreStats(LoreStatsEnum.DURABILITY, durability, maxDurability) : this.getTextLoreStats(LoreStatsEnum.DURABILITY, maxDurability);
                EquipmentUtil.setLore(item, line, newDurability);
            }
        }
    }

    public final String statsValue(final LoreStatsEnum loreStats, double value1, double value2) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String positiveValue = mainConfig.getStatsLorePositiveValue();
        final String negativeValue = mainConfig.getStatsLoreNegativeValue();
        final String symbolDivide = mainConfig.getStatsLoreDividerSymbol();
        final String symbolMultiplier = mainConfig.getStatsLoreMultiplierSymbol();
        final String colorValue = mainConfig.getStatsColorValue();
        if (loreStats.equals(LoreStatsEnum.DURABILITY)) {
            value1 = MathUtil.limitDouble(value1, 1.0, value1);
            value2 = MathUtil.limitDouble(value2, 1.0, value2);
        }
        else if (loreStats.equals(LoreStatsEnum.LEVEL)) {
            final int maxLevel = mainConfig.getStatsMaxLevelValue();
            value1 = MathUtil.limitDouble(value1, 1.0, (double)maxLevel);
        }
        else if (loreStats.equals(LoreStatsEnum.CRITICAL_CHANCE)) {
            value1 = MathUtil.limitDouble(value1, 0.0, 100.0);
        }
        String textValue1;
        String textValue2;
        if (loreStats.equals(LoreStatsEnum.DURABILITY)) {
            textValue1 = ((value1 < 0.0) ? (String.valueOf(negativeValue) + (int)value1) : (String.valueOf(positiveValue) + (int)value1));
            textValue2 = ((value2 < 0.0) ? (String.valueOf(negativeValue) + (int)value2) : (String.valueOf(positiveValue) + (int)value2));
        }
        else if (loreStats.equals(LoreStatsEnum.LEVEL)) {
            textValue1 = ((value1 < 0.0) ? (String.valueOf(negativeValue) + (int)value1) : (String.valueOf(positiveValue) + (int)value1));
            textValue2 = ((value2 < 0.0) ? "0" : String.valueOf(value2));
        }
        else {
            textValue1 = ((value1 < 0.0) ? (String.valueOf(negativeValue) + value1) : (String.valueOf(positiveValue) + value1));
            textValue2 = ((value2 < 0.0) ? (String.valueOf(negativeValue) + value2) : (String.valueOf(positiveValue) + value2));
        }
        String statsValue;
        if (loreStats.equals(LoreStatsEnum.DAMAGE)) {
            final String symbolRange = mainConfig.getStatsLoreRangeSymbol();
            if (value1 == value2) {
                statsValue = String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue;
            }
            else {
                statsValue = ((value2 > value1) ? (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + symbolRange + textValue2 + MainConfig.KEY_STATS_VALUE + colorValue) : (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue2 + symbolRange + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue));
            }
        }
        else if (loreStats.equals(LoreStatsEnum.CRITICAL_CHANCE) || loreStats.equals(LoreStatsEnum.PENETRATION) || loreStats.equals(LoreStatsEnum.ATTACK_AOE_DAMAGE)) {
            statsValue = String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + "%" + MainConfig.KEY_STATS_VALUE + colorValue;
        }
        else if (loreStats.equals(LoreStatsEnum.CRITICAL_DAMAGE) || loreStats.equals(LoreStatsEnum.FISHING_CHANCE)) {
            statsValue = String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + symbolMultiplier + MainConfig.KEY_STATS_VALUE + colorValue;
        }
        else if (loreStats.equals(LoreStatsEnum.BLOCK_AMOUNT) || loreStats.equals(LoreStatsEnum.BLOCK_RATE) || loreStats.equals(LoreStatsEnum.HIT_RATE) || loreStats.equals(LoreStatsEnum.DODGE_RATE) || loreStats.equals(LoreStatsEnum.PVP_DAMAGE) || loreStats.equals(LoreStatsEnum.PVE_DAMAGE) || loreStats.equals(LoreStatsEnum.PVP_DEFENSE) || loreStats.equals(LoreStatsEnum.PVE_DEFENSE) || loreStats.equals(LoreStatsEnum.FISHING_SPEED) || loreStats.equals(LoreStatsEnum.LURES_ENDURANCE)) {
            statsValue = ((value1 > 0.0) ? (String.valueOf(positiveValue) + "+" + MainConfig.KEY_STATS_VALUE + textValue1 + "%" + MainConfig.KEY_STATS_VALUE + colorValue) : (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + "%" + MainConfig.KEY_STATS_VALUE + colorValue));
        }
        else if (loreStats.equals(LoreStatsEnum.FISHING_POWER) || loreStats.equals(LoreStatsEnum.LURES_MAX_TENSION)) {
            statsValue = ((value1 > 0.0) ? (String.valueOf(positiveValue) + "+" + MainConfig.KEY_STATS_VALUE + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue) : (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue));
        }
        else if (loreStats.equals(LoreStatsEnum.DURABILITY)) {
            statsValue = ((value2 > value1) ? (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + symbolDivide + textValue2 + MainConfig.KEY_STATS_VALUE + colorValue) : (String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue2 + symbolDivide + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue));
        }
        else if (loreStats.equals(LoreStatsEnum.LEVEL)) {
            final String colorExpCurrent = mainConfig.getStatsColorExpCurrent();
            final String colorExpUp = mainConfig.getStatsColorExpUp();
            final HashMap<String, String> map = new HashMap<String, String>();
            String expValue = mainConfig.getStatsFormatExp();
            map.put("exp", String.valueOf(MainConfig.KEY_EXP_CURRENT) + colorExpCurrent + textValue2 + MainConfig.KEY_EXP_CURRENT + colorExpCurrent);
            map.put("up", String.valueOf(MainConfig.KEY_EXP_UP) + colorExpUp + this.getUpExp((int)value1) + MainConfig.KEY_EXP_UP + colorExpUp);
            expValue = TextUtil.placeholder(map, expValue, "<", ">");
            statsValue = String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue + " " + expValue;
        }
        else {
            statsValue = String.valueOf(MainConfig.KEY_STATS_VALUE) + textValue1 + MainConfig.KEY_STATS_VALUE + colorValue;
        }
        return statsValue;
    }

    public final LoreStatsEnum getLoreStats(final String lore) {
        LoreStatsEnum[] values;
        for (int length = (values = LoreStatsEnum.values()).length, i = 0; i < length; ++i) {
            final LoreStatsEnum loreStats = values[i];
            if (lore.contains(this.getKeyStats(loreStats, true))) {
                return loreStats;
            }
        }
        return null;
    }

    public final LoreStatsEnum getLoreStats(final ItemStack item, final int line) {
        if (line > 0 && EquipmentUtil.hasLore(item) && line <= EquipmentUtil.getLores(item).size()) {
            final String lore = EquipmentUtil.getLores(item).get(line - 1);
            return this.getLoreStats(lore);
        }
        return null;
    }

    public final boolean isLoreStats(final String lore) {
        final LoreStatsEnum loreStats = this.getLoreStats(lore);
        return loreStats != null;
    }

    public final boolean isLoreStats(final ItemStack item, final int line) {
        final LoreStatsEnum loreStats = this.getLoreStats(item, line);
        return loreStats != null;
    }

    public final int getLineLoreStats(ItemStack item, LoreStatsEnum stats) {
        if (item == null || item.getType() == Material.AIR || stats == null) return -1;
        List<String> lores = EquipmentUtil.getLores(item);
        if (lores == null || lores.isEmpty()) return -1;
        String identifier = org.bukkit.ChatColor.stripColor(TextUtil.colorful(stats.getText()));
        for (int i = 0; i < lores.size(); i++) {
            String plainLine = org.bukkit.ChatColor.stripColor(TextUtil.colorful(lores.get(i)));
            if (plainLine.contains(identifier)) {
                return i + 1;
            }
        }
        return -1;
    }

    public final double getUpExp(int level) {
        if (level < 1) {
            level = 1;
        }
        final double upExp = Math.pow(level, 2) * 25 + level * 50 + 100;
        return upExp;
    }

    public final boolean checkDurability(final ItemStack item) {
        double currentDura = this.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.CURRENT);
        return this.checkDurability(item, (int)currentDura);
    }

    public final boolean checkDurability(final ItemStack item, final int durability) {
        if (this.hasLoreStats(item, LoreStatsEnum.DURABILITY)) {
            return durability > 0;
        }
        return true;
    }

    public final boolean checkLevel(final ItemStack item, final int requirement) {
        final int level = (int)this.getLoreValue(item, LoreStatsEnum.LEVEL, LoreStatsOption.CURRENT);
        return this.checkLevel(item, level, requirement);
    }

    public final boolean checkLevel(final ItemStack item, final int level, final int requirement) {
        final boolean hasLoreLevel = this.hasLoreStats(item, LoreStatsEnum.LEVEL);
        return hasLoreLevel && level >= requirement;
    }

    public boolean durability(final LivingEntity livingEntity, final Slot slot, final int currentValue) {
        if (EntityUtil.isPlayer((Entity)livingEntity)) {
            final ItemStack item = Bridge.getBridgeEquipment().getEquipment(livingEntity, slot);
            return item == null || item.getType() == Material.BOW || this.durability(livingEntity, item, currentValue, false);
        }
        return true;
    }

    public final boolean durability(final LivingEntity livingEntity, final ItemStack item, final int currentValue, final boolean damage) {
        final int line = this.getLineLoreStats(item, LoreStatsEnum.DURABILITY);

        if (line != -1) {
            if (currentValue <= 0) {
                return false;
            }

            if (damage) {
                this.damageDurability(item);
            }
        }
        return true;
    }

    public final void damageDurability(final ItemStack item) {
        final int line = this.getLineLoreStats(item, LoreStatsEnum.DURABILITY);
        if (line != -1) {
            double cur = this.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.CURRENT);
            double max = this.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.MAX);

            if (cur > 0) {
                int next = (int)cur - 1;
                String newLore = this.getTextLoreStats(LoreStatsEnum.DURABILITY, (double)next, max);
                EquipmentUtil.setLore(item, line, newLore);
            }
        }
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final LivingEntity attacker) {
        return this.getLoreStatsWeapon(attacker, false);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final LivingEntity attacker, final boolean reverse) {
        return this.getLoreStatsWeapon(attacker, true, reverse);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final LivingEntity attacker, final boolean checkDurability, final boolean reverse) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final boolean isItemUniversal = mainConfig.isStatsEnableItemUniversal();
        double damage = 0.0;
        double penetration = 0.0;
        double pvpDamage = 0.0;
        double pveDamage = 0.0;
        double attackAoERadius = 0.0;
        double attackAoEDamage = 0.0;
        double criticalChance = 0.0;
        double criticalDamage = 0.0;
        double hitRate = 0.0;
        Slot[] values;
        for (int length = (values = Slot.values()).length, i = 0; i < length; ++i) {
            final Slot slot = values[i];
            if (slot.getType().equals(SlotType.WEAPON) || isItemUniversal) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(attacker, slot);
                if (item != null) {
                    final boolean itemReverse = reverse && slot.getType().equals(SlotType.WEAPON);
                    final LoreStatsWeapon statsBuild = this.getLoreStatsWeapon(item, attacker, slot, checkDurability, itemReverse);
                    damage += statsBuild.getDamage();
                    penetration += statsBuild.getPenetration();
                    pvpDamage += statsBuild.getPvPDamage();
                    pveDamage += statsBuild.getPvEDamage();
                    attackAoERadius += statsBuild.getAttackAoERadius();
                    attackAoEDamage += statsBuild.getAttackAoEDamage();
                    criticalChance += statsBuild.getCriticalChance();
                    criticalDamage += statsBuild.getCriticalDamage();
                    hitRate += statsBuild.getHitRate();
                }
            }
        }
        return new LoreStatsWeapon(damage, penetration, pvpDamage, pveDamage, attackAoERadius, attackAoEDamage, criticalChance, criticalDamage, hitRate);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final ItemStack item) {
        return this.getLoreStatsWeapon(item, null, Slot.MAINHAND, true, false);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final ItemStack item, final boolean reverse) {
        return this.getLoreStatsWeapon(item, null, Slot.MAINHAND, true, reverse);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final ItemStack item, final boolean checkDurability, final boolean reverse) {
        return this.getLoreStatsWeapon(item, null, Slot.MAINHAND, checkDurability, reverse);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final ItemStack item, final Slot slot, final boolean checkDurability, final boolean reverse) {
        return this.getLoreStatsWeapon(item, null, slot, checkDurability, reverse);
    }

    public final LoreStatsWeapon getLoreStatsWeapon(final ItemStack item, final LivingEntity holder, final Slot slot, final boolean checkDurability, final boolean reverse) {
        final GameManager gameManager = this.plugin.getGameManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        final Slot secondarySlot = reverse ? Slot.MAINHAND : Slot.OFFHAND;
        if (EquipmentUtil.loreCheck(item) && (!checkDurability || this.checkDurability(item)) && (holder == null || !(holder instanceof Player) || requirementManager.isAllowed((Player)holder, item))) {
            final double scaleValue = slot.equals(secondarySlot) ? mainConfig.getStatsScaleOffHandValue() : 1.0;
            final double damage = this.getLoreValue(item, LoreStatsEnum.DAMAGE, null) * scaleValue;
            final double penetration = this.getLoreValue(item, LoreStatsEnum.PENETRATION, null) * scaleValue;
            final double pvpDamage = this.getLoreValue(item, LoreStatsEnum.PVP_DAMAGE, null) * scaleValue;
            final double pveDamage = this.getLoreValue(item, LoreStatsEnum.PVE_DAMAGE, null) * scaleValue;
            final double attackAoERadius = this.getLoreValue(item, LoreStatsEnum.ATTACK_AOE_RADIUS, null) * scaleValue;
            final double attackAoEDamage = this.getLoreValue(item, LoreStatsEnum.ATTACK_AOE_DAMAGE, null) * scaleValue;
            final double criticalChance = this.getLoreValue(item, LoreStatsEnum.CRITICAL_CHANCE, null) * scaleValue;
            final double criticalDamage = this.getLoreValue(item, LoreStatsEnum.CRITICAL_DAMAGE, null) * scaleValue;
            final double hitRate = this.getLoreValue(item, LoreStatsEnum.HIT_RATE, null) * scaleValue;
            return new LoreStatsWeapon(damage, penetration, pvpDamage, pveDamage, attackAoERadius, attackAoEDamage, criticalChance, criticalDamage, hitRate);
        }
        return new LoreStatsWeapon(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public final LoreStatsArmor getLoreStatsArmor(final LivingEntity victims) {
        return this.getLoreStatsArmor(victims, true);
    }

    public final LoreStatsArmor getLoreStatsArmor(final LivingEntity victims, final boolean checkDurability) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final boolean isItemUniversal = mainConfig.isStatsEnableItemUniversal();
        double defense = 0.0;
        double pvpDefense = 0.0;
        double pveDefense = 0.0;
        double health = 0.0;
        double healthRegen = 0.0;
        double staminaMax = 0.0;
        double staminaRegen = 0.0;
        double blockAmount = 0.0;
        double blockRate = 0.0;
        double dodgeRate = 0.0;
        Slot[] values;
        for (int length = (values = Slot.values()).length, i = 0; i < length; ++i) {
            final Slot slot = values[i];
            final SlotType slotType = slot.getType();
            if (slotType.equals(SlotType.ARMOR) || isItemUniversal) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(victims, slot);
                if (EquipmentUtil.isSolid(item)) {
                    final LoreStatsArmor statsBuild = this.getLoreStatsArmor(item, victims, slot, checkDurability);
                    defense += statsBuild.getDefense();
                    pvpDefense += statsBuild.getPvPDefense();
                    pveDefense += statsBuild.getPvEDefense();
                    health += statsBuild.getHealth();
                    healthRegen += statsBuild.getHealthRegen();
                    staminaMax += statsBuild.getStaminaMax();
                    staminaRegen += statsBuild.getStaminaRegen();
                    blockAmount += statsBuild.getBlockAmount();
                    blockRate += statsBuild.getBlockRate();
                    dodgeRate += statsBuild.getDodgeRate();
                }
            }

        }
        return new LoreStatsArmor(defense, pvpDefense, pveDefense, health, healthRegen, staminaMax, staminaRegen, blockAmount, blockRate, dodgeRate);
    }

    public final LoreStatsArmor getLoreStatsArmor(final ItemStack item) {
        return this.getLoreStatsArmor(item, null, Slot.MAINHAND, true);
    }

    public final LoreStatsArmor getLoreStatsArmor(final ItemStack item, final boolean checkDurability) {
        return this.getLoreStatsArmor(item, null, Slot.MAINHAND, checkDurability);
    }

    public final LoreStatsArmor getLoreStatsArmor(final ItemStack item, final Slot slot, final boolean checkDurability) {
        return this.getLoreStatsArmor(item, null, slot, checkDurability);
    }

    public final LoreStatsArmor getLoreStatsArmor(final ItemStack item, final LivingEntity holder, final Slot slot, final boolean checkDurability) {
        final GameManager gameManager = this.plugin.getGameManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        if (EquipmentUtil.loreCheck(item) && (!checkDurability || this.checkDurability(item)) && (holder == null || !(holder instanceof Player) || requirementManager.isAllowed((Player)holder, item))) {
            final double scale = slot.equals(Slot.OFFHAND) ? mainConfig.getStatsScaleOffHandValue() : 1.0;
            final double defense = this.getLoreValue(item, LoreStatsEnum.DEFENSE, null) * scale;
            final double pvpDefense = this.getLoreValue(item, LoreStatsEnum.PVP_DEFENSE, null) * scale;
            final double pveDefense = this.getLoreValue(item, LoreStatsEnum.PVE_DEFENSE, null) * scale;
            final double health = this.getLoreValue(item, LoreStatsEnum.HEALTH, null) * scale;
            final double healthRegen = this.getLoreValue(item, LoreStatsEnum.HEALTH_REGEN, null) * scale;
            final double staminaMax = this.getLoreValue(item, LoreStatsEnum.STAMINA_MAX, null) * scale;
            final double staminaRegen = this.getLoreValue(item, LoreStatsEnum.STAMINA_REGEN, null) * scale;
            final double blockAmount = this.getLoreValue(item, LoreStatsEnum.BLOCK_AMOUNT, null) * scale;
            final double blockRate = this.getLoreValue(item, LoreStatsEnum.BLOCK_RATE, null) * scale;
            final double dodgeRate = this.getLoreValue(item, LoreStatsEnum.DODGE_RATE, null) * scale;
            return new LoreStatsArmor(defense, pvpDefense, pveDefense, health, healthRegen, staminaMax, staminaRegen, blockAmount, blockRate, dodgeRate);
        }
        return new LoreStatsArmor(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public final void sendBrokenCode(final LivingEntity livingEntity, final Slot slot) {
        this.sendBrokenCode(livingEntity, slot, true);
    }

    public final void sendBrokenCode(final LivingEntity livingEntity, final Slot slot, final boolean broken) {
        final PluginManager pluginManager = this.plugin.getPluginManager();
        final GameManager gameManager = this.plugin.getGameManager();
        final PassiveEffectManager passiveEffectManager = gameManager.getPassiveEffectManager();
        final LanguageManager lang = pluginManager.getLanguageManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        if (EntityUtil.isPlayer((Entity)livingEntity)) {
            final Player player = EntityUtil.parsePlayer((Entity)livingEntity);
            final ItemStack item = Bridge.getBridgeEquipment().getEquipment(player, slot);
            String message = null;
            switch (slot) {
                case MAINHAND: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_MainHand");
                    break;
                }
                case OFFHAND: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_Offhand");
                    break;
                }
                case HELMET: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_Helmet");
                    break;
                }
                case CHESTPLATE: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_Chestplate");
                    break;
                }
                case LEGGINGS: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_Leggings");
                    break;
                }
                case BOOTS: {
                    message = lang.getText((LivingEntity)player, "Item_Broken_Boots");
                    break;
                }
                default: {
                    return;
                }
            }
            SenderUtil.playSound((CommandSender)player, SoundEnum.ENTITY_BLAZE_DEATH);
            SenderUtil.sendMessage((CommandSender)player, message);
            if (broken && mainConfig.isStatsEnableItemBroken()) {
                final boolean enableGradeCalculation = mainConfig.isPassiveEnableGradeCalculation();
                final Collection<PassiveEffectEnum> buffs = passiveEffectManager.getPassiveEffects(item);
                Bridge.getBridgeEquipment().setEquipment(livingEntity, (ItemStack)null, slot);
                PlayerUtil.setMaxHealth(player);
                passiveEffectManager.reloadPassiveEffect(player, buffs, enableGradeCalculation);
            }
        }
    }

    public final String getKeyStats(final LoreStatsEnum stats) {
        return this.getKeyStats(stats, false);
    }

    public final String getKeyStats(final LoreStatsEnum stats, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String color = mainConfig.getStatsColor();
        final String text = stats.getText();
        return TextUtil.colorful(color + text);
    }
}