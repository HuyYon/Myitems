package com.praya.myitems.builder.specialpower;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.praya.myitems.MyItems;
import com.praya.myitems.config.plugin.MainConfig;
import api.praya.myitems.builder.power.PowerSpecialEnum;
import com.praya.myitems.builder.abs.SpecialPower;

import java.util.Collection;

public class SpecialPowerBlink extends SpecialPower {
    private static final PowerSpecialEnum special = PowerSpecialEnum.BLINK;

    public SpecialPowerBlink() {
        super(special);
    }

    @Override
    public void cast(final LivingEntity caster) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final Location locationCasterEye = caster.getEyeLocation();

        // Lấy vị trí blink cách 20 block phía trước
        Location locationBlink = locationCasterEye.clone().add(locationCasterEye.getDirection().multiply(20));

        final double height = caster.getEyeHeight();
        Collection<? extends Player> players = caster.getWorld().getPlayers();

        // Giữ hướng nhìn
        locationBlink.setYaw(locationCasterEye.getYaw());
        locationBlink.setPitch(locationCasterEye.getPitch());
        locationBlink.subtract(0.0, height, 0.0);

        // Nếu block tại vị trí blink là solid thì dịch lên
        if (locationBlink.getBlock().getType().isSolid()) {
            locationBlink.add(0.0, height, 0.0);
        }

        // Teleport caster
        caster.teleport(locationBlink);

        // Hiệu ứng particle + sound
        caster.getWorld().spawnParticle(Particle.PORTAL, locationBlink, 25, 0.5, 0.25, 0.5, 0.0);
        caster.getWorld().playSound(locationBlink, Sound.BLOCK_PORTAL_TRAVEL, 0.6f, 1.0f);
    }
}
