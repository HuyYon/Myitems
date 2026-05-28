package com.praya.myitems.builder.specialpower;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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

public class SpecialPowerFissure extends SpecialPower {
    private static final PowerSpecialEnum special = PowerSpecialEnum.FISSURE;

    public SpecialPowerFissure() {
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

        final Location loc = caster.getLocation();
        final Location horizontalLoc = new Location(loc.getWorld(), 0.0, 0.0, 0.0, loc.getYaw(), 0.0f);
        final Vector aim = horizontalLoc.getDirection().normalize();

        final int duration = getDuration();
        final double weaponDamage = statsManager.getLoreStatsWeapon(caster).getDamage();
        final double skillDamage = special.getBaseAdditionalDamage() + special.getBasePercentDamage() * weaponDamage / 100.0;

        final Set<LivingEntity> listEntity = new HashSet<>();

        // Âm thanh khởi động skill
        caster.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);

        new BukkitRunnable() {
            final int limit = 12;
            final double range = 2.0;
            int t = 0;

            @Override
            public void run() {
                if (t >= limit) {
                    cancel();
                    return;
                }

                // Hiệu ứng particle
                caster.getWorld().spawnParticle(Particle.FLAME, loc, 25, 0.15, 0.25, 0.15, 0.02);
                caster.getWorld().spawnParticle(Particle.LAVA, loc, 10, 0.2, 0.15, 0.2, 0.05);
                caster.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.8f, 1.0f);

                // Damage các entity trong phạm vi
                for (LivingEntity unit : loc.getWorld().getLivingEntities()) {
                    if (!unit.equals(caster) && unit.getLocation().distance(loc) <= range && !listEntity.contains(unit)) {
                        listEntity.add(unit);
                        unit.setFireTicks(duration);
                        unit.damage(skillDamage, caster);
                    }
                }

                loc.add(aim);
                t++;
            }
        }.runTaskTimer((Plugin) plugin, 0L, 1L);
    }
}
