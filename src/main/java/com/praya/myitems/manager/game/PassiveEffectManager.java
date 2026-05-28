package com.praya.myitems.manager.game;

import com.praya.myitems.builder.abs.PassiveEffect;
import com.praya.agarthalib.utility.MathUtil;
import com.praya.myitems.builder.passive.buff.BuffHealthBoost;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;
import com.praya.agarthalib.utility.ServerUtil;
import core.praya.agarthalib.enums.main.VersionNMS;
import com.praya.agarthalib.utility.PlayerUtil;
import java.util.*;
import org.bukkit.entity.Player;
import api.praya.myitems.builder.player.PlayerPassiveEffectCooldown;
import com.praya.myitems.manager.player.PlayerPassiveEffectManager;
import org.bukkit.OfflinePlayer;
import core.praya.agarthalib.enums.main.RomanNumber;
import org.bukkit.inventory.ItemStack;
import com.praya.agarthalib.utility.EquipmentUtil;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.main.Slot;
import org.bukkit.entity.LivingEntity;
import com.praya.agarthalib.utility.TextUtil;
import api.praya.myitems.builder.passive.PassiveTypeEnum;
import com.praya.myitems.config.plugin.MainConfig;
import api.praya.myitems.builder.passive.PassiveEffectEnum;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerManager;

public class PassiveEffectManager extends HandlerManager {
    protected PassiveEffectManager(final MyItems plugin) {
        super(plugin);
    }

    public final String getTextPassiveEffect(final PassiveEffectEnum effect, final int grade) {
        final MainConfig mainConfig = MainConfig.getInstance();
        // Cập nhật Generic Type cho HashMap
        final HashMap<String, String> map = new HashMap<String, String>();
        String format = effect.getType().equals(PassiveTypeEnum.BUFF) ? mainConfig.getPassiveBuffFormat() : mainConfig.getPassiveDebuffFormat();

        String keyValue = this.getKeyPassiveEffect(effect, grade);
        map.put("buff", keyValue);
        map.put("buffs", keyValue);
        map.put("debuff", keyValue);
        map.put("debuffs", keyValue);

        // Sửa lỗi ép kiểu và Placeholder
        format = TextUtil.placeholder((HashMap<String, String>) map, format, "<", ">");
        return format;
    }

    public final int getHighestGradePassiveEffect(final PassiveEffectEnum effect, final LivingEntity livingEntity) {
        int grade = 0;
        for (final Slot slot : Slot.values()) {
            if (this.checkAllowedSlot(slot)) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(livingEntity, slot);
                // 1.21.4: Kiểm tra item null/không khí trước khi loreCheck
                if (item != null && !item.getType().isAir() && EquipmentUtil.loreCheck(item)) {
                    final int passiveEffectGrade = this.passiveEffectGrade(item, effect);
                    if (passiveEffectGrade > grade) {
                        grade = passiveEffectGrade;
                    }
                }
            }
        }
        return grade;
    }

    public final int getTotalGradePassiveEffect(final PassiveEffectEnum effect, final LivingEntity livingEntity) {
        int grade = 0;
        for (final Slot slot : Slot.values()) {
            if (this.checkAllowedSlot(slot)) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(livingEntity, slot);
                if (item != null && !item.getType().isAir() && EquipmentUtil.loreCheck(item)) {
                    grade += this.passiveEffectGrade(item, effect);
                }
            }
        }
        return grade;
    }

    public final PassiveEffectEnum getPassiveEffect(final String lore) {
        if (lore == null) return null;
        final String plainLore = ChatColor.stripColor(lore).toLowerCase();
        for (final PassiveEffectEnum passiveEffect : PassiveEffectEnum.values()) {
            final String effectText = passiveEffect.getText().toLowerCase();
            if (plainLore.contains(effectText)) {
                return passiveEffect;
            }
        }
        return null;
    }

    public final PassiveEffectEnum getPassiveEffect(final ItemStack item, final int line) {
        if (item != null && line > 0 && EquipmentUtil.hasLore(item)) {
            final List<String> lores = EquipmentUtil.getLores(item);
            if (line <= lores.size()) {
                final String lore = lores.get(line - 1);
                return this.getPassiveEffect(lore);
            }
        }
        return null;
    }

    public final boolean isPassiveEffect(final String lore) {
        return this.getPassiveEffect(lore) != null;
    }

    public final boolean isPassiveEffect(final ItemStack item, final int line) {
        return this.getPassiveEffect(item, line) != null;
    }

    public final int passiveEffectGrade(final LivingEntity livingEntity, final PassiveEffectEnum effect, final Slot slot) {
        return this.passiveEffectGrade(Bridge.getBridgeEquipment().getEquipment(livingEntity, slot), effect);
    }

    public final int passiveEffectGrade(final ItemStack item, final PassiveEffectEnum effect) {
        final int line = this.getLinePassiveEffect(item, effect);
        return (line != -1) ? this.passiveEffectGrade(item, effect, line) : 0;
    }

    public final int passiveEffectGrade(final ItemStack item, final PassiveEffectEnum effect, final int line) {
        List<String> lores = EquipmentUtil.getLores(item);
        if (line > 0 && line <= lores.size()) {
            return this.passiveEffectGrade(effect, lores.get(line - 1));
        }
        return 0;
    }

    public final int passiveEffectGrade(final PassiveEffectEnum effect, final String lore) {
        if (lore == null) return 0;
        final String plainLore = ChatColor.stripColor(lore);
        final String effectText = effect.getText();

        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                java.util.regex.Pattern.quote(effectText) + "\\s+([0-9IVXLCDM]+)",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        final java.util.regex.Matcher matcher = pattern.matcher(plainLore);

        if (matcher.find()) {
            String val = matcher.group(1);
            if (val.matches("[0-9]+")) {
                return Integer.parseInt(val);
            }
            return RomanNumber.romanConvert(val);
        }

        if (plainLore.toLowerCase().contains(effectText.toLowerCase())) {
            return 1;
        }

        return 0;
    }

    public final int getLinePassiveEffect(final ItemStack item, final PassiveEffectEnum effect) {
        if (item == null || item.getType().isAir() || effect == null || !EquipmentUtil.hasLore(item)) {
            return -1;
        }
        final String effectTextRaw = effect.getText();
        if (effectTextRaw == null) {
            return -1;
        }
        final String effectText = effectTextRaw.toLowerCase();
        final List<String> lores = EquipmentUtil.getLores(item);
        for (int i = 0; i < lores.size(); i++) {
            String lore = lores.get(i);
            if (lore != null) {
                String plainLore = ChatColor.stripColor(lore).toLowerCase();
                if (plainLore.contains(effectText)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public Collection<PassiveEffectEnum> getPassiveEffects(final ItemStack item) {
        final Collection<PassiveEffectEnum> listEffect = new ArrayList<PassiveEffectEnum>();
        if (item != null && !item.getType().isAir() && EquipmentUtil.loreCheck(item)) {
            for (final PassiveEffectEnum effect : PassiveEffectEnum.values()) {
                if (this.getLinePassiveEffect(item, effect) != -1) {
                    listEffect.add(effect);
                }
            }
        }
        return listEffect;
    }

    public final void setPassiveEffectCooldown(final PassiveEffectEnum effect, final OfflinePlayer player, final long cooldown) {
        final PlayerPassiveEffectManager playerPassiveEffectManager = this.plugin.getPlayerManager().getPlayerPassiveEffectManager();
        final PlayerPassiveEffectCooldown playerPassiveEffectCooldown = playerPassiveEffectManager.getPlayerPassiveEffectCooldown(player);
        playerPassiveEffectCooldown.setPassiveEffectCooldown(effect, cooldown);
    }

    public final boolean isPassiveEffectCooldown(final PassiveEffectEnum effect, final OfflinePlayer player) {
        final PlayerPassiveEffectManager playerPassiveEffectManager = this.plugin.getPlayerManager().getPlayerPassiveEffectManager();
        final PlayerPassiveEffectCooldown playerPassiveEffectCooldown = playerPassiveEffectManager.getPlayerPassiveEffectCooldown(player);
        return playerPassiveEffectCooldown.isPassiveEffectCooldown(effect);
    }

    public final void reloadPassiveEffect(final Player player, final ItemStack item, final boolean sum) {
        this.reloadPassiveEffect(player, this.getPassiveEffects(item), sum);
    }

    public final void reloadPassiveEffect(final Player player, final Collection<PassiveEffectEnum> effects, final boolean sum) {
        for (final PassiveEffectEnum effect : effects) {
            this.runPassiveEffect(player, effect, true, sum);
        }
    }

    public final void loadPassiveEffect(final boolean sum) {
        for (final Player player : PlayerUtil.getOnlinePlayers()) {
            this.runAllPassiveEffect(player, sum);
        }
    }

    public final void runAllPassiveEffect(final Player player, final boolean sum) {
        this.runAllPassiveEffect(player, false, sum);
    }

    public final void runAllPassiveEffect(final Player player, final boolean reset, final boolean sum) {
        for (final PassiveEffectEnum effect : PassiveEffectEnum.values()) {
            this.runPassiveEffect(player, effect, reset, sum);
        }
    }

    public final void runPassiveEffect(final Player player, final PassiveEffectEnum effect, final boolean sum) {
        this.runPassiveEffect(player, effect, false, sum);
    }

    public final void runPassiveEffect(final Player player, final PassiveEffectEnum effect, final boolean reset, final boolean sum) {
        final int grade = sum ? this.getTotalGradePassiveEffect(effect, (LivingEntity) player) : this.getHighestGradePassiveEffect(effect, (LivingEntity) player);
        this.applyPassiveEffect(player, effect, grade, reset);
    }

    public final void applyPassiveEffect(final Player player, final PassiveEffectEnum effect, final int grade) {
        this.applyPassiveEffect(player, effect, grade, false);
    }

    public final void applyPassiveEffect(final Player player, final PassiveEffectEnum effect, int grade, final boolean reset) {
        PotionEffectType potion = effect.getPotion();
        if (potion != null) {
            if (player.hasPotionEffect(potion)) {
                org.bukkit.potion.PotionEffect active = player.getPotionEffect(potion);
                if (active != null && active.getAmplifier() == (grade - 1) && active.getDuration() > 200) {
                    return;
                }
            }
            if (reset) {
                if (potion.equals(PotionEffectType.HEALTH_BOOST)) {
                    new BuffHealthBoost().reset(player);
                } else {
                    player.removePotionEffect(potion);
                }
            }
        }

        int maxGrade = effect.getMaxGrade();
        if (grade > maxGrade) {
            grade = maxGrade;
        }

        if (grade != 0) {
            final PassiveEffect passiveEffect = PassiveEffect.getPassiveEffect(effect, grade);
            if (passiveEffect != null) {
                passiveEffect.cast(player);
            }
        }
    }

    public final String getKeyPassiveEffect(final PassiveEffectEnum effect, final boolean justCheck) {
        return this.getKeyPassiveEffect(effect, 1, justCheck);
    }

    public final String getKeyPassiveEffect(final PassiveEffectEnum effect, final int grade) {
        return this.getKeyPassiveEffect(effect, grade, false);
    }

    public final String getKeyPassiveEffect(final PassiveEffectEnum effect, final int grade, final boolean justCheck) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final String key = MainConfig.KEY_PASSIVE_EFFECT;
        final String color = effect.getType().equals(PassiveTypeEnum.BUFF) ? mainConfig.getPassiveBuffColor() : mainConfig.getPassiveDebuffColor();
        final String text = effect.getText();
        final String roman = (grade > 10) ? String.valueOf(grade) : RomanNumber.getRomanNumber(grade);

        // Tối ưu hóa nối chuỗi cho Java 21
        if (justCheck) {
            return key + color + text;
        }
        return key + color + text + " " + roman + key + color;
    }

    public final boolean checkAllowedSlot(final Slot slot) {
        final MainConfig mainConfig = MainConfig.getInstance();
        return mainConfig.isPassiveEnableHand() || (!slot.equals(Slot.MAINHAND) && !slot.equals(Slot.OFFHAND));
    }
}