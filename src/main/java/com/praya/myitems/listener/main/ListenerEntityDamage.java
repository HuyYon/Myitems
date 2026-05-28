package com.praya.myitems.listener.main;

import api.praya.myitems.builder.ability.AbilityWeapon;
import api.praya.myitems.builder.socket.SocketGemsProperties;
import api.praya.myitems.builder.lorestats.LoreStatsArmor;
import com.praya.myitems.builder.ability.weapon.AbilityWeaponAirShock;
import com.praya.myitems.builder.ability.weapon.AbilityWeaponFlame;
import com.praya.myitems.manager.game.*;
import org.bukkit.entity.LivingEntity;
import core.praya.agarthalib.utility.MathUtil;
import core.praya.agarthalib.utility.EntityUtil;
import com.praya.myitems.config.plugin.MainConfig;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Entity;
import com.praya.myitems.manager.plugin.LanguageManager;
import com.praya.myitems.manager.plugin.PluginManager;

import java.util.HashMap;
import org.bukkit.OfflinePlayer;
import core.praya.agarthalib.enums.branch.SoundEnum;
import org.bukkit.command.CommandSender;
import core.praya.agarthalib.utility.SenderUtil;
import core.praya.agarthalib.utility.TextUtil;
import core.praya.agarthalib.utility.EquipmentUtil;
import core.praya.agarthalib.bridge.main.MainBridge;
import core.praya.agarthalib.enums.main.Slot;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import com.praya.myitems.MyItems;
import org.bukkit.event.Listener;
import com.praya.myitems.builder.handler.HandlerEvent;

public class ListenerEntityDamage extends HandlerEvent implements Listener
{
    public ListenerEntityDamage(final MyItems plugin) {
        super(plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void checkBound(final EntityDamageEvent event) {
        final GameManager gameManager = this.plugin.getGameManager();
        final PluginManager pluginManager = this.plugin.getPluginManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final LanguageManager lang = pluginManager.getLanguageManager();
        if (!event.isCancelled()) {
            final Entity entity = event.getEntity();
            if (entity instanceof Player) {
                final Player player = (Player)entity;
                Slot[] values;
                for (int length = (values = Slot.values()).length, i = 0; i < length; ++i) {
                    final Slot slot = values[i];
                    if (slot.getID() > 1) {
                        final ItemStack item = MainBridge.getBridgeEquipment().getEquipment(player, slot);
                        if (EquipmentUtil.loreCheck(item)) {
                            if (!requirementManager.isAllowed(player, item)) {
                                final String message = TextUtil.placeholder(lang.getText("Item_Lack_Requirement"), "Item", EquipmentUtil.getDisplayName(item));
                                SenderUtil.sendMessage((CommandSender)player, message);
                                SenderUtil.playSound((CommandSender)player, SoundEnum.ENTITY_BLAZE_DEATH);
                            }
                            else {
                                final Integer lineUnbound = requirementManager.getLineRequirementSoulUnbound(item);
                                if (lineUnbound != null) {
                                    final String loreBound = requirementManager.getTextSoulBound((OfflinePlayer)player);
                                    final Integer lineOld = requirementManager.getLineRequirementSoulBound(item);
                                    final HashMap<String, String> map = new HashMap<String, String>();
                                    if (lineOld != null) {
                                        EquipmentUtil.removeLore(item, (int)lineOld);
                                    }
                                    String message2 = lang.getText("Item_Bound");
                                    map.put("item", EquipmentUtil.getDisplayName(item));
                                    map.put("player", player.getName());
                                    message2 = TextUtil.placeholder((HashMap)map, message2);
                                    requirementManager.setMetadataSoulbound((OfflinePlayer)player, item);
                                    EquipmentUtil.setLore(item, (int)lineUnbound, loreBound);
                                    SenderUtil.sendMessage((CommandSender)player, message2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void damageEvent(final EntityDamageEvent event) {
        final GameManager gameManager = this.plugin.getGameManager();
        final SocketManager socketManager = gameManager.getSocketManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final MainConfig mainConfig = MainConfig.getInstance();
        if (!event.isCancelled()) {
            final boolean customDamage = mainConfig.isModifierEnableCustomModifier();
            if (customDamage) {
                final Entity getEntity = event.getEntity();
                if (EntityUtil.isLivingEntity(getEntity) && MainBridge.getBridgeLivingEntity().isLivingEntity(getEntity)) {
                    final LivingEntity victims = EntityUtil.parseLivingEntity(getEntity);
                    boolean defenseAffect = false;
                    switch (event.getCause()) {
                        case ENTITY_EXPLOSION: {
                            defenseAffect = mainConfig.isModifierDefenseAffectEntityExplosion();
                            break;
                        }
                        case BLOCK_EXPLOSION: {
                            defenseAffect = mainConfig.isModifierDefenseAffectBlockExplosion();
                            break;
                        }
                        case CUSTOM: {
                            defenseAffect = mainConfig.isModifierDefenseAffectCustom();
                            break;
                        }
                        case CONTACT: {
                            defenseAffect = mainConfig.isModifierDefenseAffectContact();
                            break;
                        }
                        case FALL: {
                            defenseAffect = mainConfig.isModifierDefenseAffectFall();
                            break;
                        }
                        case FALLING_BLOCK: {
                            defenseAffect = mainConfig.isModifierDefenseAffectFallingBlock();
                            break;
                        }
                        case THORNS: {
                            defenseAffect = mainConfig.isModifierDefenseAffectThorn();
                            break;
                        }
                        default: {
                            defenseAffect = false;
                            break;
                        }
                    }
                    if (defenseAffect) {
                        final LoreStatsArmor loreStatsVictims = statsManager.getLoreStatsArmor(victims);
                        final SocketGemsProperties socketVictims = socketManager.getSocketProperties(victims);
                        final double scaleDefenseOverall = mainConfig.getModifierScaleDefenseOverall();
                        final double scaleDefensePhysic = mainConfig.getModifierScaleDefensePhysic();
                        double damage = event.getDamage();
                        double defense = loreStatsVictims.getDefense() + socketVictims.getAdditionalDefense() + damage * socketVictims.getPercentDefense() / 100.0;
                        defense = defense * scaleDefenseOverall * scaleDefensePhysic;
                        damage -= defense;
                        damage = MathUtil.limitDouble(damage, 1.0, damage);
                        event.setDamage(damage);
                    }
                }
            }
        }
    }

    // ====================== ABILITY WEAPON TRIGGER - FIXED ======================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAbilityWeaponTrigger(com.praya.agarthalib.event.DamageFinalEvent event) {
        if (event.isCancelled()) return;

        final Player attacker = getAttacker(event);
        if (attacker == null) return;

        final LivingEntity victim = EntityUtil.parseLivingEntity(event.getEntity());
        if (victim == null) return;

        final AbilityWeaponManager abilityManager = this.plugin.getGameManager().getAbilityWeaponManager();
        final HashMap<AbilityWeapon, Integer> abilities = abilityManager.getMapAbilityWeapon(attacker, true);

        if (abilities.isEmpty()) return;

        // DEBUG (bật để test)
        attacker.sendMessage("§b[DEBUG] Triggered " + abilities.size() + " abilities");

        for (AbilityWeapon ability : abilities.keySet()) {
            int grade = abilities.get(ability);
            double damage = event.getDamage();   // Lấy damage từ event

            try {
                // Gọi method cast chính xác của Ability
                java.lang.reflect.Method method = ability.getClass().getMethod("cast",
                        Entity.class, Entity.class, int.class, double.class);
                method.invoke(ability, attacker, victim, grade, damage);
            } catch (Exception e) {
                try {
                    // Thử method cast không có damage
                    java.lang.reflect.Method method2 = ability.getClass().getMethod("cast",
                            Entity.class, Entity.class, int.class);
                    method2.invoke(ability, attacker, victim, grade);
                } catch (Exception ignored) {
                    // attacker.sendMessage("§cAbility error: " + ability.getClass().getSimpleName());
                }
            }
        }
    }

    // ====================== GET ATTACKER - FIXED ======================
    private Player getAttacker(com.praya.agarthalib.event.DamageFinalEvent event) {
        // DamageFinalEvent có thể không có getDamager(), nên lấy từ nguyên nhân gây damage
        Entity damager = null;

        try {
            // Thử lấy damager nếu có method
            java.lang.reflect.Method m = event.getClass().getMethod("getDamager");
            damager = (Entity) m.invoke(event);
        } catch (Exception ignored) {}

        if (damager == null) {
            // Fallback: lấy từ EntityDamageEvent gốc nếu có
            if (event instanceof com.praya.agarthalib.event.DamageEvent) {
                try {
                    java.lang.reflect.Method m = event.getClass().getMethod("getDamager");
                    damager = (Entity) m.invoke(event);
                } catch (Exception ignored) {}
            }
        }

        if (damager instanceof Player) return (Player) damager;

        if (damager instanceof org.bukkit.entity.Projectile proj) {
            if (proj.getShooter() instanceof Player p) return p;
        }

        return null;
    }
}
