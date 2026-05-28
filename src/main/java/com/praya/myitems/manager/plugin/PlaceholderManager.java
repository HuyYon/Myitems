package com.praya.myitems.manager.plugin;

import org.bukkit.OfflinePlayer;
import api.praya.myitems.builder.ability.AbilityWeapon;
import com.praya.myitems.manager.register.RegisterAbilityWeaponManager;
import com.praya.myitems.manager.game.RequirementManager;
import com.praya.myitems.manager.game.LoreStatsManager;
import com.praya.myitems.manager.game.PassiveEffectManager;
import com.praya.myitems.manager.game.SocketManager;
import com.praya.myitems.manager.game.ElementManager;
import com.praya.myitems.manager.game.AbilityWeaponManager;
import com.praya.myitems.manager.game.PowerSpecialManager;
import com.praya.myitems.manager.game.PowerShootManager;
import com.praya.myitems.manager.game.PowerCommandManager;
import com.praya.myitems.manager.game.PowerManager;
import com.praya.myitems.manager.register.RegisterManager;
import com.praya.myitems.manager.game.GameManager;
import com.praya.agarthalib.utility.PlayerUtil;
import api.praya.myitems.builder.power.PowerSpecialEnum;
import core.praya.agarthalib.enums.branch.ProjectileEnum;
import api.praya.myitems.builder.power.PowerClickEnum;
import api.praya.myitems.builder.power.PowerEnum;
import api.praya.myitems.builder.passive.PassiveEffectEnum;
import com.praya.agarthalib.utility.MathUtil;
import api.praya.myitems.builder.lorestats.LoreStatsEnum;

import java.util.*;
import java.util.regex.Pattern;

import api.praya.myitems.builder.lorestats.LoreStatsModifier;
import com.praya.agarthalib.utility.EquipmentUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import com.praya.agarthalib.utility.ListUtil;
import com.praya.agarthalib.utility.TextUtil;
import api.praya.agarthalib.manager.plugin.SupportManagerAPI;
import com.praya.myitems.builder.placeholder.ReplacerMVDWPlaceholderAPIBuild;
import com.praya.myitems.builder.placeholder.ReplacerPlaceholderAPIBuild;
import api.praya.agarthalib.main.AgarthaLibAPI;
import com.praya.myitems.MyItems;
import com.praya.myitems.config.plugin.PlaceholderConfig;
import com.praya.myitems.builder.handler.HandlerManager;

public class PlaceholderManager extends HandlerManager {

    private final PlaceholderConfig placeholderConfig;

    protected PlaceholderManager(final MyItems plugin) {
        super(plugin);
        this.placeholderConfig = new PlaceholderConfig(plugin);
    }

    public final PlaceholderConfig getPlaceholderConfig() {
        return this.placeholderConfig;
    }

    public final Collection<String> getPlaceholderIDs() {
        return this.getPlaceholderConfig().getPlaceholderIDs();
    }

    public final Collection<String> getPlaceholders() {
        return this.getPlaceholderConfig().getPlaceholders();
    }

    public final String getPlaceholder(final String id) {
        return this.getPlaceholderConfig().getPlaceholder(id);
    }

    public final HashMap<String, String> getPlaceholderCopy() {
        return this.getPlaceholderConfig().getPlaceholderCopy();
    }

    public final boolean isPlaceholderExists(final String id) {
        return this.getPlaceholder(id) != null;
    }

    public final void registerAll() {
        final AgarthaLibAPI agarthaLibAPI = AgarthaLibAPI.getInstance();
        final SupportManagerAPI supportManagerAPI = agarthaLibAPI.getPluginManagerAPI().getSupportManager();
        final String placeholder = this.plugin.getPluginPlaceholder();

        if (supportManagerAPI.isSupportPlaceholderAPI()) {
            new ReplacerPlaceholderAPIBuild(this.plugin, placeholder).register();
        }
        if (supportManagerAPI.isSupportMVdWPlaceholder()) {
            new ReplacerMVDWPlaceholderAPIBuild(this.plugin, placeholder).register();
        }
    }

    public final List<String> localPlaceholder(final List<String> list) {
        final String divider = "\n";
        final String builder = TextUtil.convertListToString(list, divider);
        final String text = this.localPlaceholder(builder);
        return ListUtil.convertStringToList(text, divider);
    }

    public final String localPlaceholder(final String text) {
        return TextUtil.placeholder(this.getPlaceholderCopy(), text);
    }

    public final List<String> pluginPlaceholder(final List<String> list, final String... identifiers) {
        return this.pluginPlaceholder(list, null, identifiers);
    }

    public final List<String> pluginPlaceholder(final List<String> list, final Player player, final String... identifiers) {
        final String divider = "\n";
        final String builder = TextUtil.convertListToString(list, divider);
        final String text = this.pluginPlaceholder(builder, player, identifiers);
        return ListUtil.convertStringToList(text, divider);
    }

    public final String pluginPlaceholder(final String text, final String... identifiers) {
        return this.pluginPlaceholder(text, null, identifiers);
    }

    public final String pluginPlaceholder(final String text, final Player player, final String... identifiers) {
        final HashMap<String, String> map = this.getMapPluginPlaceholder(player, identifiers);
        return TextUtil.placeholder(map, text);
    }

    public final HashMap<String, String> getMapPluginPlaceholder(final String... identifiers) {
        return this.getMapPluginPlaceholder(null, identifiers);
    }

    public final HashMap<String, String> getMapPluginPlaceholder(final Player player, final String... identifiers) {
        final String placeholder = this.plugin.getPluginPlaceholder();
        final HashMap<String, String> map = new HashMap<>();
        for (final String identifier : identifiers) {
            final String replacement = this.getReplacement(player, identifier);
            if (replacement != null) {
                final String key = placeholder + "_" + identifier;
                map.put(key, replacement);
            }
        }
        return map;
    }

    public final ItemStack parseItem(final Player player, final ItemStack item) {
        final String divider = "\n";
        if (EquipmentUtil.hasDisplayName(item)) {
            final String oldDisplayName = EquipmentUtil.getDisplayName(item);
            final String newDisplayName = this.placeholder(player, oldDisplayName);
            EquipmentUtil.setDisplayName(item, newDisplayName);
        }
        if (EquipmentUtil.hasLore(item)) {
            final List<String> oldLores = EquipmentUtil.getLores(item);
            final String oldLineLore = TextUtil.convertListToString(oldLores, divider);
            final String newLineLore = this.placeholder(player, oldLineLore);
            final String[] split = newLineLore.split(divider);
            final List<String> newLores = new ArrayList<>();
            Collections.addAll(newLores, split);
            EquipmentUtil.setLores(item, newLores);
        }
        return item;
    }

    public final List<String> placeholder(final Player player, final List<String> listText) {
        return this.placeholder(player, listText, null);
    }

    public final List<String> placeholder(final Player player, final List<String> listText, final LoreStatsModifier modifier) {
        return this.placeholder(player, listText, modifier, "{", "}");
    }

    public final List<String> placeholder(final Player player, final List<String> listText, final LoreStatsModifier modifier,
                                          final String leftKey, final String rightKey) {
        if (listText == null) {
            return new ArrayList<>();
        }
        // Fix: iterate and replace via ListIterator, then return the mutated list
        // (original returned `listText` after also building an unused `list`)
        final ListIterator<String> iteratorText = listText.listIterator();
        while (iteratorText.hasNext()) {
            final String text = iteratorText.next();
            final String textPlaceholder = this.placeholder(player, text, modifier, leftKey, rightKey);
            iteratorText.set(textPlaceholder);
        }
        return listText;
    }

    public final String placeholder(final Player player, final String text) {
        return this.placeholder(player, text, null);
    }

    public final String placeholder(final Player player, final String text, final LoreStatsModifier modifier) {
        return this.placeholder(player, text, modifier, "{", "}");
    }

    public final String placeholder(final Player player, String text, final LoreStatsModifier modifier,
                                    final String leftKey, final String rightKey) {
        final String placeholder = this.plugin.getPluginPlaceholder();
        if (text.contains(leftKey)) {
            final String[] fullPartFirst = text.split(Pattern.quote(leftKey));
            for (final String checkPartFirst : fullPartFirst) {
                if (checkPartFirst.contains(rightKey)) {
                    final String check = checkPartFirst.split(Pattern.quote(rightKey))[0];
                    if (check.contains("_")) {
                        final String[] elements = check.split("_", 2);
                        final String textholder = elements[0];
                        final String identifier = elements[1];
                        if (textholder.equalsIgnoreCase(placeholder)) {
                            final CharSequence replacement = this.getReplacement(player, identifier, modifier);
                            // Fix: removed redundant String.valueOf(String.valueOf(...)) double-wrap
                            final CharSequence sequence = leftKey + check + rightKey;
                            if (replacement != null) {
                                text = text.replace(sequence, replacement);
                            }
                        }
                    }
                }
            }
        }
        return text;
    }

    public final String getReplacement(final Player player, final String identifier) {
        return this.getReplacement(player, identifier, null);
    }

    public final String getReplacement(final Player player, final String identifier, final LoreStatsModifier statsModifier) {
        final GameManager gameManager = this.plugin.getGameManager();
        final RegisterManager registerManager = this.plugin.getRegisterManager();
        final PowerManager powerManager = gameManager.getPowerManager();
        final PowerCommandManager powerCommandManager = powerManager.getPowerCommandManager();
        final PowerShootManager powerShootManager = powerManager.getPowerShootManager();
        final PowerSpecialManager powerSpecialManager = powerManager.getPowerSpecialManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final ElementManager elementManager = gameManager.getElementManager();
        final SocketManager socketManager = gameManager.getSocketManager();
        final PassiveEffectManager passiveEffectManager = gameManager.getPassiveEffectManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final RegisterAbilityWeaponManager registerAbilityWeaponManager = registerManager.getRegisterAbilityWeaponManager();

        final String[] parts = identifier.split(":");
        final int length = parts.length;

        if (length == 0) {
            return null;
        }

        final String key = parts[0];

        if (key.equalsIgnoreCase("text_lorestats")) {
            if (length >= 3) {
                final LoreStatsEnum loreStats = LoreStatsEnum.get(parts[1]);
                if (loreStats != null) {
                    final String textMinValue = parts[2];
                    final double modifier = (statsModifier != null) ? statsModifier.getModifier(loreStats) : 1.0;
                    double minValue;
                    if (textMinValue.contains("~")) {
                        final String[] comp = textMinValue.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        minValue = MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                    } else {
                        if (!MathUtil.isNumber(textMinValue)) return null;
                        minValue = MathUtil.roundNumber(MathUtil.parseDouble(textMinValue));
                    }
                    double maxValue;
                    if (length == 3) {
                        maxValue = minValue;
                    } else {
                        final String textMaxValue = parts[3];
                        if (textMaxValue.contains("~")) {
                            final String[] comp = textMaxValue.split("~");
                            if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                            maxValue = MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                        } else {
                            if (!MathUtil.isNumber(textMaxValue)) return null;
                            maxValue = MathUtil.roundNumber(MathUtil.parseDouble(textMaxValue));
                        }
                    }
                    return statsManager.getTextLoreStats(loreStats,
                            MathUtil.roundNumber(minValue * modifier, 2),
                            MathUtil.roundNumber(maxValue * modifier, 2));
                }
            }

        } else if (key.equalsIgnoreCase("text_ability")) {
            if (length >= 4) {
                final AbilityWeapon abilityWeapon = registerAbilityWeaponManager.getAbilityWeapon(parts[1]);
                if (abilityWeapon != null) {
                    final String textGrade = parts[2];
                    final String textChance = parts[3];
                    int grade;
                    if (textGrade.contains("~")) {
                        final String[] comp = textGrade.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        final int raw = (int) MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                        grade = MathUtil.limitInteger(raw, 1, raw);
                    } else {
                        if (!MathUtil.isNumber(textGrade)) return null;
                        final int raw = MathUtil.parseInteger(textGrade);
                        grade = MathUtil.limitInteger(raw, 1, raw);
                    }
                    double chance;
                    if (textChance.contains("~")) {
                        final String[] comp = textChance.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        chance = MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                    } else {
                        if (!MathUtil.isNumber(textChance)) return null;
                        chance = MathUtil.roundNumber(MathUtil.parseDouble(textChance));
                    }
                    return abilityWeaponManager.getTextAbility(parts[1], grade, chance);
                }
            }

        } else if (key.equalsIgnoreCase("text_buff")) {
            if (length >= 3) {
                final PassiveEffectEnum effect = PassiveEffectEnum.get(parts[1]);
                if (effect != null) {
                    final String textGrade = parts[2];
                    int grade;
                    if (textGrade.contains("~")) {
                        final String[] comp = textGrade.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        grade = (int) MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                    } else {
                        if (!MathUtil.isNumber(textGrade)) return null;
                        grade = MathUtil.parseInteger(textGrade);
                    }
                    return passiveEffectManager.getTextPassiveEffect(effect, grade);
                }
            }

        } else if (key.equalsIgnoreCase("text_power")) {
            if (length >= 4) {
                final PowerEnum power = PowerEnum.get(parts[1]);
                if (power != null) {
                    final PowerClickEnum click = PowerClickEnum.get(parts[2]);
                    if (click != null) {
                        final String textType = parts[3];
                        double cooldown;
                        if (length == 4) {
                            cooldown = 0.0;
                        } else {
                            final String textCooldown = parts[4];
                            if (textCooldown.contains("~")) {
                                final String[] comp = textCooldown.split("~");
                                if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                                cooldown = MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                            } else {
                                if (!MathUtil.isNumber(textCooldown)) return null;
                                cooldown = MathUtil.roundNumber(MathUtil.parseDouble(textCooldown));
                            }
                        }
                        if (power.equals(PowerEnum.COMMAND)) {
                            final String type = powerCommandManager.getCommandKey(textType);
                            if (type != null) return powerCommandManager.getTextPowerCommand(click, type, cooldown);
                        } else if (power.equals(PowerEnum.SHOOT)) {
                            final ProjectileEnum type = ProjectileEnum.getProjectileEnum(textType);
                            if (type != null) return powerShootManager.getTextPowerShoot(click, type, cooldown);
                        } else if (power.equals(PowerEnum.SPECIAL)) {
                            final PowerSpecialEnum type = PowerSpecialEnum.get(textType);
                            if (type != null) return powerSpecialManager.getTextPowerSpecial(click, type, cooldown);
                        }
                    }
                }
            }

        } else if (key.equalsIgnoreCase("text_socket_empty")) {
            return socketManager.getTextSocketSlotEmpty();

        } else if (key.equalsIgnoreCase("text_socket_fill")) {
            if (length >= 3) {
                final String gems = parts[1];
                if (socketManager.isExist(gems)) {
                    final String textGrade = parts[2];
                    int grade;
                    if (textGrade.contains("~")) {
                        final String[] comp = textGrade.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        final int raw = (int) MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                        grade = MathUtil.limitInteger(raw, 1, raw);
                    } else {
                        if (!MathUtil.isNumber(textGrade)) return null;
                        grade = MathUtil.parseInteger(textGrade);
                    }
                    return socketManager.getTextSocketGemsLore(gems, grade);
                }
            }

        } else if (key.equalsIgnoreCase("text_element")) {
            if (length >= 3) {
                final String textElement = parts[1];
                if (elementManager.isExists(textElement)) {
                    final String textValue = parts[2];
                    double value;
                    if (textValue.contains("~")) {
                        final String[] comp = textValue.split("~");
                        if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                        value = MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                    } else {
                        if (!MathUtil.isNumber(textValue)) return null;
                        value = MathUtil.roundNumber(MathUtil.parseDouble(textValue));
                    }
                    return elementManager.getTextElement(textElement, value);
                }
            }

        } else if (key.equalsIgnoreCase("text_requirement_unbound")) {
            return requirementManager.getTextSoulUnbound();

        } else if (key.equalsIgnoreCase("text_requirement_bound")) {
            if (length >= 2) {
                final OfflinePlayer bound = PlayerUtil.getPlayer(parts[1]);
                if (bound != null) return bound.getName();
            }

        } else if (key.equalsIgnoreCase("text_requirement_level")) {
            if (length >= 2) {
                final String textLevel = parts[1];
                int level;
                if (textLevel.contains("~")) {
                    final String[] comp = textLevel.split("~");
                    if (!MathUtil.isNumber(comp[0]) || !MathUtil.isNumber(comp[1])) return null;
                    final int raw = (int) MathUtil.valueBetween(MathUtil.parseDouble(comp[0]), MathUtil.parseDouble(comp[1]));
                    level = MathUtil.limitInteger(raw, 0, raw);
                } else {
                    if (!MathUtil.isNumber(textLevel)) return null;
                    level = MathUtil.parseInteger(textLevel);
                    level = MathUtil.limitInteger(level, 0, level);
                }
                return requirementManager.getTextLevel(level);
            }

        } else if (key.equalsIgnoreCase("text_requirement_permission")) {
            if (length >= 2) return requirementManager.getTextPermission(parts[1]);

        } else if (key.equalsIgnoreCase("text_requirement_class")) {
            if (length >= 2) return requirementManager.getTextClass(parts[1]);
        }

        return null;
    }
}