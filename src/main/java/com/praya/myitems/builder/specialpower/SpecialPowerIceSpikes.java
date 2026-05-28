package com.praya.myitems.builder.specialpower;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.praya.myitems.MyItems;
import com.praya.myitems.manager.game.GameManager;
import com.praya.myitems.manager.game.LoreStatsManager;
import com.praya.myitems.config.plugin.MainConfig;
import api.praya.myitems.builder.power.PowerSpecialEnum;
import com.praya.myitems.builder.abs.SpecialPower;

import java.util.HashSet;
import java.util.Set;

public class SpecialPowerIceSpikes extends SpecialPower {
    private static final PowerSpecialEnum special = PowerSpecialEnum.ICE_SPIKES;

    public SpecialPowerIceSpikes() {
        super(special);
    }

    public int getDuration() {
        return special.getDuration();
    }

    @Override
    public void cast(final LivingEntity caster) {
        final MyItems plugin = (MyItems) JavaPlugin.getProvidingPlugin(MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final MainConfig mainConfig = MainConfig.getInstance();

        final Location horizontalLoc = new Location(caster.getLocation().getWorld(), 0.0, 0.0, 0.0, caster.getLocation().getYaw(), 0.0f);
        final Vector aim = horizontalLoc.getDirection().multiply(2);

        final int duration = getDuration();
        final double weaponDamage = statsManager.getLoreStatsWeapon(caster).getDamage();
        final double skillDamage = special.getBaseAdditionalDamage() + special.getBasePercentDamage() * weaponDamage / 100.0;

        final Set<LivingEntity> listEntity = new HashSet<>();

        new BukkitRunnable() {
            final int limit = 5;
            int t = 0;
            Location loc = caster.getLocation().add(aim);

            @Override
            public void run() {
                if (t >= limit) {
                    cancel();
                    return;
                }

                caster.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 1.0f);

                for (int indexSpikes = 0; indexSpikes < 3; ++indexSpikes) {
                    loc = loc.add(0.0, indexSpikes, 0.0);

                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setType(Material.PACKED_ICE);

                        for (LivingEntity unit : loc.getWorld().getLivingEntities()) {
                            if (!unit.equals(caster) && unit.getLocation().distance(loc) <= 2.0 && !listEntity.contains(unit)) {
                                listEntity.add(unit);
                                unit.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 4));
                                unit.damage(skillDamage, caster);

                                caster.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 10, 0.2, 0.2, 0.2, 0.1);
                            }
                        }

                        // Xóa block băng sau 9 ticks
                        final Location iceLoc = loc.clone();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (iceLoc.getBlock().getType() == Material.PACKED_ICE) {
                                    iceLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }.runTaskLater(plugin, 9L);
                    }

                    loc = loc.subtract(0.0, indexSpikes, 0.0);
                }

                loc.add(aim);
                t++;
            }
        }.runTaskTimer((Plugin) plugin, 0L, 3L);
    }
}
