package com.praya.myitems.builder.specialpower;

import org.bukkit.Location;
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

public class SpecialPowerNeroBeam extends SpecialPower {
    private static final PowerSpecialEnum special = PowerSpecialEnum.NERO_BEAM;

    public SpecialPowerNeroBeam() {
        super(special);
    }

    public int getDuration() {
        return special.getDuration();
    }

    public int getLimit() {
        return 15;
    }

    public double getBaseRange() {
        return 1.5;
    }

    public double getScaleRange() {
        return 0.05;
    }

    public double getStartRadius() {
        return 0.2;
    }

    public double getScaleRadius() {
        return 0.05;
    }

    @Override
    public void cast(final LivingEntity caster) {
        final MyItems plugin = (MyItems) JavaPlugin.getProvidingPlugin(MyItems.class);
        final GameManager gameManager = plugin.getGameManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final MainConfig mainConfig = MainConfig.getInstance();

        final Location loc = caster.getEyeLocation();
        final Location leftLoc = new Location(loc.getWorld(), 0.0, 0.0, 0.0, loc.getYaw() - 90.0f, loc.getPitch());
        final Location upLoc = new Location(loc.getWorld(), 0.0, 0.0, 0.0, loc.getYaw(), loc.getPitch() - 90.0f);

        final Vector left = leftLoc.getDirection();
        final Vector up = upLoc.getDirection();
        final Vector vector = loc.getDirection();

        final int duration = getDuration();
        final double weaponDamage = statsManager.getLoreStatsWeapon(caster).getDamage();
        final double skillDamage = special.getBaseAdditionalDamage() + special.getBasePercentDamage() * weaponDamage / 100.0;

        final Set<LivingEntity> listEntity = new HashSet<>();

        new BukkitRunnable() {
            final int limit = getLimit();
            double range = getBaseRange();
            double startRadius = getStartRadius();
            double radius = 0.2;
            int t = 0;
            double degree;

            @Override
            public void run() {
                if (t >= limit) {
                    cancel();
                    return;
                }

                Location partLoc = loc.clone();
                degree = Math.PI / (2.0 * (radius / startRadius));

                // Vẽ vòng tròn particle
                for (double math = 0.0; math <= 2 * Math.PI; math += degree) {
                    double calcHorizontal = Math.sin(math) * radius;
                    double calcVertical = Math.cos(math) * radius;

                    partLoc.add(left.getX() * calcHorizontal, left.getY() * calcHorizontal, left.getZ() * calcHorizontal);
                    partLoc.add(up.getX() * calcVertical, up.getY() * calcVertical, up.getZ() * calcVertical);

                    caster.getWorld().spawnParticle(Particle.WITCH, partLoc, 1, 0, 0, 0, 0);

                    partLoc.subtract(left.getX() * calcHorizontal, left.getY() * calcHorizontal, left.getZ() * calcHorizontal);
                    partLoc.subtract(up.getX() * calcVertical, up.getY() * calcVertical, up.getZ() * calcVertical);
                }

                caster.getWorld().playSound(loc, Sound.ENTITY_WITHER_SHOOT, 5.0f, 1.0f);

                // Damage các entity trong phạm vi
                for (LivingEntity unit : loc.getWorld().getLivingEntities()) {
                    if (!unit.equals(caster) && unit.getLocation().distance(loc) <= range && !listEntity.contains(unit)) {
                        listEntity.add(unit);
                        unit.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 10));
                        unit.damage(skillDamage, caster);
                    }
                }

                radius += getScaleRadius();
                range += getScaleRange();
                loc.add(vector);
                t++;
            }
        }.runTaskTimer((Plugin) plugin, 0L, 1L);
    }
}
