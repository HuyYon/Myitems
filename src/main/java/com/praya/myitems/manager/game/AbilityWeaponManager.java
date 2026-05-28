package com.praya.myitems.manager.game;

import com.praya.agarthalib.utility.TextUtil;
import api.praya.myitems.builder.ability.AbilityWeaponAttributeCastDamage;
import api.praya.myitems.builder.ability.AbilityWeaponAttributeBaseDamage;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.main.SlotType;
import com.praya.myitems.config.plugin.MainConfig;
import core.praya.agarthalib.enums.main.Slot;
import java.util.HashMap;
import org.bukkit.entity.LivingEntity;
import com.praya.agarthalib.utility.EquipmentUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import api.praya.myitems.builder.ability.AbilityWeapon;
import com.praya.myitems.manager.register.RegisterAbilityWeaponManager;
import com.praya.myitems.manager.register.RegisterManager;
import com.praya.agarthalib.utility.MathUtil;
import core.praya.agarthalib.enums.main.RomanNumber;
import api.praya.myitems.builder.ability.AbilityItemWeapon;
import api.praya.myitems.builder.ability.AbilityWeaponProperties;
import java.util.Collection;
import com.praya.myitems.MyItems;
import com.praya.myitems.config.game.AbilityWeaponConfig;
import com.praya.myitems.builder.handler.HandlerManager;
import org.bukkit.ChatColor;

public class AbilityWeaponManager extends HandlerManager {
    private final AbilityWeaponConfig abilityWeaponConfig;

    protected AbilityWeaponManager(final MyItems plugin) {
        super(plugin);
        this.abilityWeaponConfig = new AbilityWeaponConfig(plugin);
    }

    public final AbilityWeaponConfig getAbilityWeaponConfig() {
        return this.abilityWeaponConfig;
    }

    public final Collection<String> getAbilityWeaponPropertiesIDs() {
        return this.getAbilityWeaponConfig().getAbilityWeaponPropertiesIDs();
    }

    public final Collection<AbilityWeaponProperties> getAllAbilityWeaponProperties() {
        return this.getAbilityWeaponConfig().getAllAbilityWeaponProperties();
    }

    public final AbilityWeaponProperties getAbilityWeaponProperties(final String ability) {
        return this.getAbilityWeaponConfig().getAbilityWeaponProperties(ability);
    }

    public final AbilityItemWeapon getAbilityItemWeapon(final String lore) {
        if (lore == null || lore.isEmpty()) return null;
        final RegisterAbilityWeaponManager reg = this.plugin.getRegisterManager().getRegisterAbilityWeaponManager();
        final String keyAbility = this.getKeyAbility();
        final String keyChance = this.getKeyChance();
        if (!lore.contains(keyAbility) || !lore.contains(keyChance)) return null;
        try {
            String plainLore = ChatColor.stripColor(lore);
            String content = lore.split(keyAbility)[1].split(keyChance)[0].trim();
            String[] parts = content.split(" ");
            if (parts.length < 2) return null;
            String textGrade = parts[parts.length - 1];
            StringBuilder keyLoreBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) keyLoreBuilder.append(" ");
                keyLoreBuilder.append(parts[i]);
            }
            String keyLore = keyLoreBuilder.toString().trim();
            final AbilityWeapon abilityWeapon = reg.getAbilityWeaponByKeyLore(keyLore);
            final int grade = RomanNumber.romanConvert(textGrade);
            if (abilityWeapon != null && grade > 0) {
                String suffix = lore.split(keyChance)[1].trim();
                String textChance = suffix.replaceAll("[^0-9.]", "");
                double chance = MathUtil.parseDouble(textChance);
                return new AbilityItemWeapon(abilityWeapon.getID(), grade, chance);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public final boolean isAbilityItemWeapon(final String lore) {
        return this.getAbilityItemWeapon(lore) != null;
    }

    public final List<AbilityItemWeapon> getListAbilityItemWeapon(final ItemStack item) {
        final List<AbilityItemWeapon> list = new ArrayList<>();
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return list;

        final List<String> lores = EquipmentUtil.getLores(item);
        for (final String lore : lores) {
            final AbilityItemWeapon ability = this.getAbilityItemWeapon(lore);
            if (ability != null) list.add(ability);
        }
        return list;
    }

    public final boolean hasAbilityItemWeapon(final ItemStack item) {
        return !this.getListAbilityItemWeapon(item).isEmpty();
    }

    public final Integer getLineAbilityItemWeapon(final ItemStack item, final String abilityID) {
        if (item == null || abilityID == null || !item.hasItemMeta()) return null;

        final AbilityWeapon abilityWeapon = this.plugin.getRegisterManager().getRegisterAbilityWeaponManager().getAbilityWeapon(abilityID);
        if (abilityWeapon == null) return null;

        final String targetKeyLore = ChatColor.stripColor(TextUtil.colorful(abilityWeapon.getKeyLore())).toLowerCase();
        final List<String> lores = EquipmentUtil.getLores(item);

        for (int i = 0; i < lores.size(); ++i) {
            String plainLore = ChatColor.stripColor(TextUtil.colorful(lores.get(i))).toLowerCase();
            if (plainLore.contains(targetKeyLore)) return i + 1;
        }
        return null;
    }

    public final HashMap<Slot, Collection<AbilityItemWeapon>> getMapListAbilityItemWeapon(final LivingEntity entity) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final HashMap<Slot, Collection<AbilityItemWeapon>> map = new HashMap<>();
        if (entity != null) {
            final boolean enableOffHand = mainConfig.isAbilityWeaponEnableOffHand();
            for (Slot slot : Slot.values()) {
                if (slot.getType().equals(SlotType.WEAPON) && (!slot.equals(Slot.OFFHAND) || enableOffHand)) {
                    final ItemStack item = Bridge.getBridgeEquipment().getEquipment(entity, slot);
                    map.put(slot, this.getListAbilityItemWeapon(item));
                }
            }
        }
        return map;
    }

    public final HashMap<AbilityWeapon, Integer> getMapAbilityWeapon(final Collection<AbilityItemWeapon> listAbilityItemWeapon, final boolean checkChance) {
        final RegisterAbilityWeaponManager reg = this.plugin.getRegisterManager().getRegisterAbilityWeaponManager();
        final HashMap<AbilityWeapon, Integer> mapAbilityWeapon = new HashMap<>();

        if (listAbilityItemWeapon != null) {
            final HashMap<String, Integer> mapAbilityGrade = new HashMap<>();
            for (final AbilityItemWeapon aiw : listAbilityItemWeapon) {
                if (!checkChance || MathUtil.chanceOf(aiw.getChance())) {
                    mapAbilityGrade.put(aiw.getAbility(), mapAbilityGrade.getOrDefault(aiw.getAbility(), 0) + aiw.getGrade());
                }
            }
            for (final String id : mapAbilityGrade.keySet()) {
                final AbilityWeapon aw = reg.getAbilityWeapon(id);
                if (aw != null) mapAbilityWeapon.put(aw, mapAbilityGrade.get(id));
            }
        }
        return mapAbilityWeapon;
    }

    public final HashMap<AbilityWeapon, Integer> getMapAbilityWeapon(final LivingEntity entity, final boolean checkChance) {
        final HashMap<AbilityWeapon, Integer> map = new HashMap<>();
        if (entity != null) {
            final HashMap<Slot, Collection<AbilityItemWeapon>> slotMap = this.getMapListAbilityItemWeapon(entity);
            for (final Collection<AbilityItemWeapon> items : slotMap.values()) {
                final HashMap<AbilityWeapon, Integer> slotAbilities = this.getMapAbilityWeapon(items, checkChance);
                for (final AbilityWeapon aw : slotAbilities.keySet()) {
                    map.put(aw, map.getOrDefault(aw, 0) + slotAbilities.get(aw));
                }
            }
        }
        return map;
    }

    public final HashMap<AbilityWeapon, Integer> getMapAbilityWeapon(final Collection<AbilityItemWeapon> list) {
        return getMapAbilityWeapon(list, false);
    }

    public final HashMap<AbilityWeapon, Integer> getMapAbilityWeapon(final LivingEntity entity) {
        return getMapAbilityWeapon(entity, false);
    }

    public final double getTotalBaseBonusDamage(final HashMap<AbilityWeapon, Integer> map) {
        double total = 0.0;
        if (map != null) {
            for (AbilityWeapon aw : map.keySet()) {
                if (aw instanceof AbilityWeaponAttributeBaseDamage) {
                    total += ((AbilityWeaponAttributeBaseDamage) aw).getBaseBonusDamage(map.get(aw));
                }
            }
        }
        return total;
    }

    public final double getTotalBasePercentDamage(final HashMap<AbilityWeapon, Integer> map) {
        double total = 0.0;
        if (map != null) {
            for (AbilityWeapon aw : map.keySet()) {
                if (aw instanceof AbilityWeaponAttributeBaseDamage) {
                    total += ((AbilityWeaponAttributeBaseDamage) aw).getBasePercentDamage(map.get(aw));
                }
            }
        }
        return total;
    }

    public final double getTotalCastBonusDamage(final HashMap<AbilityWeapon, Integer> map) {
        double total = 0.0;
        if (map != null) {
            for (AbilityWeapon aw : map.keySet()) {
                if (aw instanceof AbilityWeaponAttributeCastDamage) {
                    total += ((AbilityWeaponAttributeCastDamage) aw).getCastBonusDamage(map.get(aw));
                }
            }
        }
        return total;
    }

    public final double getTotalCastPercentDamage(final HashMap<AbilityWeapon, Integer> map) {
        double total = 0.0;
        if (map != null) {
            for (AbilityWeapon aw : map.keySet()) {
                if (aw instanceof AbilityWeaponAttributeCastDamage) {
                    total += ((AbilityWeaponAttributeCastDamage) aw).getCastPercentDamage(map.get(aw));
                }
            }
        }
        return total;
    }


    public final String getTextAbility(final String ability, final int grade) {
        return this.getTextAbility(ability, grade, 100.0);
    }

    public final String getTextAbility(final String ability, final int grade, final double chance) {
        final MainConfig config = MainConfig.getInstance();
        final String keyAbility = this.getKeyAbility(ability, grade);
        final String keyChance = this.getKeyChance(chance);

        if (keyAbility == null) return "";

        String format = config.getAbilityFormat();
        HashMap<String, String> map = new HashMap<>();
        map.put("ability", keyAbility);
        map.put("chance", keyChance);

        return TextUtil.placeholder(map, format, "<", ">");
    }

    private final String getKeyAbility(final String ability, final Integer grade) {
        final RegisterAbilityWeaponManager reg = this.plugin.getRegisterManager().getRegisterAbilityWeaponManager();
        final MainConfig config = MainConfig.getInstance();
        final String key = MainConfig.KEY_ABILITY_WEAPON;
        final String color = config.getAbilityColor();

        if (ability == null) return key + color;

        final AbilityWeapon aw = reg.getAbilityWeapon(ability);
        if (aw == null) return null;

        final String keyLore = TextUtil.colorful(aw.getKeyLore());
        if (grade != null) {
            final int fixedGrade = MathUtil.limitInteger(grade, 1, aw.getMaxGrade());
            return key + color + keyLore + " " + RomanNumber.getRomanNumber(fixedGrade) + key + color;
        }
        return key + color + keyLore;
    }

    private final String getKeyChance(final Double chance) {
        final MainConfig config = MainConfig.getInstance();
        final String key = MainConfig.KEY_ABILITY_PERCENT;
        final String color = config.getAbilityColorPercent();

        double val = (chance != null) ? MathUtil.limitDouble(chance, 0.0, 100.0) : 0.0;
        return key + color + val + key + color + "%";
    }

    private final String getKeyAbility() { return this.getKeyAbility(null); }
    private final String getKeyAbility(final String ability) { return this.getKeyAbility(ability, null); }
    private final String getKeyChance() { return this.getKeyChance(null); }
}