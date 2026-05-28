// 
// Decompiled by Procyon v0.5.36
// 

package com.praya.myitems.builder.ability.weapon;

import api.praya.myitems.builder.ability.*;
import com.praya.agarthalib.utility.*;
import com.praya.myitems.MyItems;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.manager.game.AbilityWeaponManager;
import com.praya.myitems.manager.game.GameManager;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.branch.MaterialEnum;
import core.praya.agarthalib.enums.branch.ParticleEnum;
import core.praya.agarthalib.enums.branch.SoundEnum;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityWeaponVenomBlast extends AbilityWeapon implements AbilityWeaponAttributeBaseDamage, AbilityWeaponAttributeCastDamage, AbilityWeaponAttributeEffect
{
    private static final String ABILITY_ID = "Venom_Blast";
    
    private AbilityWeaponVenomBlast(final MyItems plugin, final String id) {
        super((Plugin)plugin, id);
    }
    
    public static final AbilityWeaponVenomBlast getInstance() {
        return AbilityVenomBlastHelper.instance;
    }
    
    @Override
    public String getKeyLore() {
        final MainConfig mainConfig = MainConfig.getInstance();
        return mainConfig.getAbilityWeaponIdentifierVenomBlast();
    }
    
    @Override
    public List<String> getDescription() {
        return null;
    }
    
    @Override
    public int getMaxGrade() {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        return abilityWeaponProperties.getMaxGrade();
    }
    
    @Override
    public double getBaseBonusDamage(final int grade) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        final double baseBonusDamage = grade * abilityWeaponProperties.getScaleBaseBonusDamage();
        return baseBonusDamage;
    }
    
    @Override
    public double getBasePercentDamage(final int grade) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        final double basePercentDamage = grade * abilityWeaponProperties.getScaleBasePercentDamage();
        return basePercentDamage;
    }
    
    @Override
    public double getCastBonusDamage(final int grade) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        final double castBonusDamage = grade * abilityWeaponProperties.getScaleCastBonusDamage();
        return castBonusDamage;
    }
    
    @Override
    public double getCastPercentDamage(final int grade) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        final double castPercentDamage = grade * abilityWeaponProperties.getScaleCastPercentDamage();
        return castPercentDamage;
    }
    
    @Override
    public int getEffectDuration(final int grade) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final AbilityWeaponProperties abilityWeaponProperties = abilityWeaponManager.getAbilityWeaponProperties("Venom_Blast");
        return abilityWeaponProperties.getTotalDuration(grade);
    }
    
    @Override
    public void cast(final Entity caster, final Entity target, final int grade, final double damage) {
        final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
        final MainConfig mainConfig = MainConfig.getInstance();
        LivingEntity attacker;
        if (caster instanceof Projectile) {
            final Projectile projectile = (Projectile)caster;
            final ProjectileSource projectileSource = projectile.getShooter();
            if (projectileSource == null || !(projectileSource instanceof LivingEntity)) {
                return;
            }
            attacker = (LivingEntity)projectileSource;
        }
        else {
            attacker = (LivingEntity)caster;
        }
        if (target instanceof LivingEntity) {
            final LivingEntity victims = (LivingEntity)target;
            final Location location = victims.getLocation().add(0.0, 0.5, 0.0);
            final PotionEffectType potionType = PotionUtil.getPoisonType(victims);
            final double spreadDamage = this.getCastBonusDamage(grade) + damage * (this.getCastPercentDamage(grade) / 100.0);
            final double blastDamage = spreadDamage * 2.0;
            final double decrease = 1.0;
            final int limit = 4;
            final int duration = this.getEffectDuration(grade);
            final int amplifier = potionType.equals((Object)PotionEffectType.WITHER) ? 2 : 1;
            final PotionEffect potion = PotionUtil.createPotion(potionType, duration, amplifier);
            final Set<LivingEntity> units = new HashSet<LivingEntity>();
            final Collection<Player> players = (Collection<Player>)PlayerUtil.getNearbyPlayers(location, mainConfig.getEffectRange());
            victims.addPotionEffect(potion);
            new BukkitRunnable() {
                int time = 0;
                double radius = 3.0;
                double x;
                double z;
                
                public void run() {
                    if (this.time > 4) {
                        this.cancel();
                        return;
                    }
                    if (this.time == 4) {
                        Bridge.getBridgeParticle().playParticle(players, ParticleEnum.EXPLOSION_HUGE, location, 10, 0.0, 0.2, 0.0, 0.05000000074505806);
                        Bridge.getBridgeSound().playSound(players, location, SoundEnum.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                        for (final LivingEntity unit : CombatUtil.getNearbyUnits(location, 3.0)) {
                            if (!unit.equals(attacker) && !unit.equals(victims) && !units.contains(unit)) {
                                CombatUtil.areaDamage(attacker, unit, blastDamage);
                                units.add(unit);
                            }
                        }
                    }
                    else {
                        final Material materialDandelion = MaterialEnum.DANDELION.getMaterial();
                        Bridge.getBridgeSound().playSound(players, location, SoundEnum.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);
                        for (final LivingEntity unit2 : CombatUtil.getNearbyUnits(location, this.radius + 1.0)) {
                            if (!unit2.equals(attacker) && !unit2.equals(victims) && !units.contains(unit2)) {
                                final PotionEffectType potionType = PotionUtil.getPoisonType(unit2);
                                final int amplifier = potionType.equals((Object)PotionEffectType.WITHER) ? 2 : 1;
                                final PotionEffect potion = PotionUtil.createPotion(potionType, duration, amplifier);
                                unit2.addPotionEffect(potion);
                                CombatUtil.areaDamage(attacker, unit2, spreadDamage);
                                units.add(unit2);
                            }
                        }
                        for (int i = 0; i < 360; i += 30) {
                            this.x = Math.sin(i) * this.radius;
                            this.z = Math.cos(i) * this.radius;
                            location.add(this.x, 0.0, this.z);
                            Bridge.getBridgeParticle().playParticle(players, ParticleEnum.VILLAGER_HAPPY, location, 3, 0.05, 0.05, 0.05, 0.05000000074505806);
                            final Block block = location.getBlock();
                            final Material material = block.getType();
                            if (material.equals((Object)Material.AIR)) {
                                final Location locationBlock = block.getLocation();
                                BlockUtil.set(locationBlock);
                                block.setType(materialDandelion);
                                block.setMetadata("Anti_Block_Physic", MetadataUtil.createMetadata((Object)true));
                                new BukkitRunnable() {
                                    final Location locationFlower = location.clone();
                                    
                                    public void run() {
                                        final Block blockFlower = this.locationFlower.getBlock();
                                        final Material materialFlower = blockFlower.getType();
                                        final Location locationBlockFlower = blockFlower.getLocation();
                                        BlockUtil.remove(locationBlockFlower);
                                        if (materialFlower.equals((Object)materialDandelion)) {
                                            blockFlower.setType(Material.AIR);
                                        }
                                    }
                                }.runTaskLater((Plugin)plugin, 5L);
                            }
                            location.subtract(this.x, 0.0, this.z);
                        }
                        --this.radius;
                    }
                    ++this.time;
                }
            }.runTaskTimer((Plugin)plugin, 0L, 5L);
        }
    }
    
    private static class AbilityVenomBlastHelper
    {
        private static final AbilityWeaponVenomBlast instance;
        
        static {
            final MyItems plugin = (MyItems)JavaPlugin.getPlugin((Class)MyItems.class);
            instance = new AbilityWeaponVenomBlast(plugin, "Venom_Blast");
        }
    }
}
