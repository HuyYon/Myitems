package com.praya.myitems.builder.specialpower;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import com.praya.myitems.MyItems;
import com.praya.myitems.manager.game.GameManager;
import com.praya.myitems.manager.game.LoreStatsManager;
import com.praya.myitems.config.plugin.MainConfig;
import api.praya.myitems.builder.power.PowerSpecialEnum;
import com.praya.myitems.builder.abs.SpecialPower;

import java.util.HashSet;
import java.util.Set;

public class SpecialPowerAmaterasu extends SpecialPower {
    private static final PowerSpecialEnum special = PowerSpecialEnum.AMATERASU;

    public SpecialPowerAmaterasu() {
        super(special);
    }

    public int getDuration() {
        return special.getDuration();
    }

    public int getLimit() {
        return getDuration() / 2;
    }

    public double getRange() {
        return 3.0;
    }

    @Override
    public void cast(final LivingEntity caster) {
        final MyItems plugin = (MyItems) JavaPlugin.getProvidingPlugin(MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final MainConfig mainConfig = MainConfig.getInstance();

        final Location loc = caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(2));
        final int limit = getLimit();
        final double range = getRange();
        final double weaponDamage = statsManager.getLoreStatsWeapon(caster).getDamage();
        final double skillDamage = special.getBaseAdditionalDamage() + special.getBasePercentDamage() * weaponDamage / 100.0;

        final Set<LivingEntity> listEntity = new HashSet<>();
        final Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());

        // Âm thanh khởi động skill
        caster.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 5.0f, 1.0f);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= limit) {
                    cancel();
                    return;
                }

                // Hiệu ứng chính
                caster.getWorld().spawnParticle(Particle.FLAME, loc, 25, 1.5, 0.75, 1.5, 0.0);
                caster.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 5.0f, 1.0f);

                // Tìm victim trong phạm vi
                for (LivingEntity unit : loc.getWorld().getLivingEntities()) {
                    if (!unit.equals(caster) && unit.getLocation().distance(loc) <= range) {
                        listEntity.add(unit);
                    }
                }

                // Gây damage định kỳ
                for (LivingEntity victim : listEntity) {
                    if (!victim.isDead()) {
                        Location victimLoc = victim.getLocation().add(0.0, 0.5, 0.0);
                        victim.getWorld().spawnParticle(Particle.FLAME, victimLoc, 12, 0.25, 0.5, 0.25, 0.0);
                        victim.getWorld().playSound(victimLoc, Sound.BLOCK_FIRE_AMBIENT, 0.75f, 1.0f);

                        if (t % 10 == 0) {
                            victim.damage(skillDamage, caster);
                        }
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
