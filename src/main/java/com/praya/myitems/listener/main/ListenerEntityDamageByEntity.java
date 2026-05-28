package com.praya.myitems.listener.main;

import api.praya.myitems.builder.item.ItemSetBonusEffectEntity;
import api.praya.myitems.builder.element.ElementBoostStats;
import api.praya.myitems.builder.socket.SocketGemsProperties;
import api.praya.myitems.builder.lorestats.LoreStatsArmor;
import api.praya.myitems.builder.lorestats.LoreStatsWeapon;
import api.praya.myitems.builder.ability.AbilityWeapon;
import api.praya.myitems.builder.event.CombatCriticalDamageEvent;
import api.praya.myitems.builder.event.CombatPreCriticalEvent;
import com.praya.agarthalib.utility.*;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerEvent;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.manager.game.*;
import com.praya.myitems.manager.plugin.LanguageManager;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.branch.SoundEnum;
import core.praya.agarthalib.enums.main.Slot;
import core.praya.agarthalib.enums.main.SlotType;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class ListenerEntityDamageByEntity extends HandlerEvent implements Listener {

    public ListenerEntityDamageByEntity(final MyItems plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void checkBoundAttacker(final EntityDamageByEntityEvent event) {
        final GameManager gameManager = this.plugin.getGameManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final LanguageManager lang = this.plugin.getPluginManager().getLanguageManager();

        if (event.getDamager() instanceof Player) {
            final Player player = (Player) event.getDamager();
            for (Slot slot : Slot.values()) {
                if (slot.getID() < 2) {
                    final ItemStack item = Bridge.getBridgeEquipment().getEquipment(player, slot);
                    if (item != null && EquipmentUtil.loreCheck(item)) {
                        if (!requirementManager.isAllowed(player, item)) {
                            final String message = TextUtil.placeholder(lang.getText("Item_Lack_Requirement"), "Item", EquipmentUtil.getDisplayName(item));
                            event.setCancelled(true);
                            SenderUtil.sendMessage(player, message);
                            SenderUtil.playSound(player, SoundEnum.ENTITY_BLAZE_DEATH);
                            return;
                        } else {
                            final Integer lineUnbound = requirementManager.getLineRequirementSoulUnbound(item);
                            if (lineUnbound != null) {
                                final String loreBound = requirementManager.getTextSoulBound(player);
                                final Integer lineOld = requirementManager.getLineRequirementSoulBound(item);
                                if (lineOld != null) {
                                    EquipmentUtil.removeLore(item, lineOld);
                                }
                                String messageBound = lang.getText("Item_Bound");
                                final Map<String, String> map = new HashMap<>();
                                map.put("item", EquipmentUtil.getDisplayName(item));
                                map.put("player", player.getName());
                                messageBound = TextUtil.placeholder((HashMap<String, String>) map, messageBound);

                                requirementManager.setMetadataSoulbound(player, item);
                                EquipmentUtil.setLore(item, lineUnbound, loreBound);
                                SenderUtil.sendMessage(player, messageBound);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        final GameManager gameManager = this.plugin.getGameManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final SocketManager socketManager = gameManager.getSocketManager();
        final ElementManager elementManager = gameManager.getElementManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final ItemSetManager itemSetManager = gameManager.getItemSetManager();
        final LanguageManager lang = this.plugin.getPluginManager().getLanguageManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        final Entity entityAttacker = event.getDamager();
        final Entity entityVictims = event.getEntity();
        LivingEntity attacker = null;
        boolean reverse = false;
        if (entityAttacker instanceof Projectile) {
            final Projectile projectile = (Projectile) entityAttacker;
            final ProjectileSource source = projectile.getShooter();
            if (!(source instanceof LivingEntity)) return;
            attacker = (LivingEntity) source;
            reverse = (EquipmentUtil.holdBow(attacker) && Slot.OFFHAND.equals(EquipmentUtil.getActiveSlotBow(attacker)));
        } else if (entityAttacker instanceof LivingEntity) {
            attacker = (LivingEntity) entityAttacker;
        }

        if (attacker == null || !(entityVictims instanceof LivingEntity)) return;
        final LivingEntity victims = (LivingEntity) entityVictims;

        final boolean isSkillDamage = CombatUtil.isSkillDamage(victims);
        final boolean isAreaDamage = CombatUtil.isAreaDamage(victims);
        if (CombatUtil.hasMetadataInstantDamage(victims)) {
            CombatUtil.removeMetadataInstantDamage(victims);
            return;
        }

        final ItemStack itemMainHand = Bridge.getBridgeEquipment().getEquipment(attacker, Slot.MAINHAND);
        final ItemStack itemOffHand = Bridge.getBridgeEquipment().getEquipment(attacker, Slot.OFFHAND);

        double baseDamage = event.getDamage();
        if (!ServerUtil.isLegacy()) {
            try {
                baseDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
            } catch (Exception ignored) {}
        }

        double damage = event.getDamage() - baseDamage;
        boolean customDamage = attacker instanceof Player ? mainConfig.isModifierEnableCustomModifier() : mainConfig.isModifierEnableCustomMobDamage();

        if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE && itemMainHand != null && itemMainHand.getType() == Material.BOW) {
            if (!isSkillDamage && !isAreaDamage) customDamage = false;
        }

        if (mainConfig.isModifierEnableVanillaDamage()) {
            damage += (mainConfig.getModifierScaleDamageVanilla() * baseDamage);
        } else {
            damage -= 1.0;
        }

        if (customDamage) {
            final LoreStatsWeapon loreStatsAttacker = statsManager.getLoreStatsWeapon(attacker, reverse);
            final LoreStatsArmor loreStatsVictims = statsManager.getLoreStatsArmor(victims);
            final SocketGemsProperties socketAttacker = socketManager.getSocketProperties(attacker);
            final SocketGemsProperties socketVictims = socketManager.getSocketProperties(victims);

            final Map<String, Double> elementAttacker = elementManager.getMapElement(attacker, SlotType.WEAPON);
            final Map<String, Double> elementVictims = elementManager.getMapElement(victims, SlotType.ARMOR);
            final Map<String, Double> mapElement = elementManager.getElementCalculation((HashMap<String, Double>)elementAttacker, (HashMap<String, Double>)elementVictims);

            final ElementBoostStats elementBoostStatsAttacker = elementManager.getElementBoostStats((HashMap<String, Double>)mapElement);
            final Map<AbilityWeapon, Integer> mapAbilityWeapon = abilityWeaponManager.getMapAbilityWeapon(attacker, true);

            final ItemSetBonusEffectEntity setAttacker = itemSetManager.getItemSetBonusEffectEntity(attacker);
            final ItemSetBonusEffectEntity setVictims = itemSetManager.getItemSetBonusEffectEntity(victims);

            double statsDamage = loreStatsAttacker.getDamage();
            double attributeDamage = (statsDamage + socketAttacker.getAdditionalDamage() + (statsDamage * socketAttacker.getPercentDamage() / 100.0));
            attributeDamage += (abilityWeaponManager.getTotalBaseBonusDamage((HashMap<AbilityWeapon, Integer>)mapAbilityWeapon) + (statsDamage * abilityWeaponManager.getTotalBasePercentDamage((HashMap<AbilityWeapon, Integer>)mapAbilityWeapon) / 100.0));
            attributeDamage += (elementBoostStatsAttacker.getBaseAdditionalDamage() + (statsDamage * elementBoostStatsAttacker.getBasePercentDamage() / 100.0));
            attributeDamage += (setAttacker.getEffectStats().getAdditionalDamage() + (statsDamage * setAttacker.getEffectStats().getPercentDamage() / 100.0));

            damage += (attributeDamage * mainConfig.getModifierScaleDamageCustom());

            double accuration = 100.0 + (loreStatsAttacker.getHitRate() + socketAttacker.getHitRate()) - (loreStatsVictims.getDodgeRate() + socketVictims.getDodgeRate());
            if (MathUtil.chanceOf(100.0 - accuration)) {
                sendCombatMessage(attacker, lang.getText("Attack_Miss"), SoundEnum.ENTITY_BAT_TAKEOFF);
                sendCombatMessage(victims, lang.getText("Attack_Dodge"), SoundEnum.ENTITY_BAT_TAKEOFF);
                event.setCancelled(true);
                return;
            }

            double blockRate = loreStatsVictims.getBlockRate() + socketVictims.getBlockRate() + setVictims.getEffectStats().getBlockRate();
            if (MathUtil.chanceOf(blockRate)) {
                double blockAmount = MathUtil.limitDouble(25.0 + loreStatsVictims.getBlockAmount(), 0.0, 100.0);
                damage *= (100.0 - blockAmount) / 100.0;
                sendCombatMessage(attacker, lang.getText("Attack_Block"), SoundEnum.BLOCK_ANVIL_PLACE);
                sendCombatMessage(victims, lang.getText("Attack_Blocked"), SoundEnum.BLOCK_ANVIL_PLACE);
            }

            // --- Logic CRITICAL ---
            if (mainConfig.isModifierEnableCustomCritical()) {
                double cc = loreStatsAttacker.getCriticalChance() + socketAttacker.getCriticalChance();
                CombatPreCriticalEvent preCrit = new CombatPreCriticalEvent(attacker, victims, cc);
                ServerEventUtil.callEvent(preCrit);
                if (!preCrit.isCancelled() && preCrit.isCritical()) {
                    double cd = 1.0 + (loreStatsAttacker.getCriticalDamage() / 100.0);
                    CombatCriticalDamageEvent critEvent = new CombatCriticalDamageEvent(attacker, victims, damage, cd, 0.0);
                    ServerEventUtil.callEvent(critEvent);
                    damage = critEvent.getCalculationDamage();
                }
            }

            // Độ bền (Durability)
            handleDurability(attacker, victims, statsManager, mainConfig);

            // Hiệu ứng Nguyên tố & Kỹ năng
            if (!isAreaDamage) {
                elementManager.applyElementPotion(attacker, victims, (HashMap<String, Double>)mapElement);
                for (Map.Entry<AbilityWeapon, Integer> entry : mapAbilityWeapon.entrySet()) {
                    entry.getKey().cast(entityAttacker, entityVictims, entry.getValue(), damage);
                }
            }

            // --- Tính toán DEFENSE & PENETRATION ---
            double penetration = MathUtil.limitDouble(loreStatsAttacker.getPenetration() + socketAttacker.getPenetration(), 0.0, 100.0);
            double defense = (loreStatsVictims.getDefense() + socketVictims.getAdditionalDefense()) * mainConfig.getModifierScaleDefenseOverall();
            defense *= (100.0 - penetration) / 100.0;

            damage -= defense;
            damage = Math.max(0, damage);
        }

        // Áp dụng Scale Damage Mob
        if (!(victims instanceof Player)) {
            damage *= mainConfig.getModifierScaleMobDamageReceive();
        }

        // Xử lý Potion Effects (Strength/Resistance)
        damage = applyVanillaPotionModifiers(attacker, victims, damage);

        event.setDamage(damage);
    }

    private void sendCombatMessage(LivingEntity entity, String message, SoundEnum sound) {
        if (entity instanceof Player) {
            Player p = (Player) entity;
            SenderUtil.sendMessage(p, message);
            SenderUtil.playSound(p, sound);
        }
    }

    private void handleDurability(LivingEntity attacker, LivingEntity victims, LoreStatsManager statsManager, MainConfig config) {
        for (Slot slot : Slot.values()) {
            LivingEntity holder = slot.getType() == SlotType.WEAPON ? attacker : victims;
            ItemStack item = Bridge.getBridgeEquipment().getEquipment(holder, slot);
            if (item != null && !item.getType().isAir() && item.getType() != Material.BOW) {
                statsManager.damageDurability(item);
                if (config.isStatsEnableItemBrokenMessage() && !statsManager.checkDurability(item)) {
                    statsManager.sendBrokenCode(holder, slot);
                }
            }
        }
    }

    private double applyVanillaPotionModifiers(LivingEntity attacker, LivingEntity victims, double damage) {
        // Strength
        if (attacker.hasPotionEffect(PotionEffectType.STRENGTH)) {
            PotionEffect eff = attacker.getPotionEffect(PotionEffectType.STRENGTH);
            if (eff != null) damage += (damage * (eff.getAmplifier() + 1) * 0.1);
        }
        // Resistance
        if (victims.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            PotionEffect eff = victims.getPotionEffect(PotionEffectType.RESISTANCE);
            if (eff != null) damage -= (damage * (eff.getAmplifier() + 1) * 0.05);
        }
        return Math.max(0, damage);
    }
}