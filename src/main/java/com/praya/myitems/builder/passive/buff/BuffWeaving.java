// 
// Decompiled by Procyon v0.5.36
// 

package com.praya.myitems.builder.passive.buff;

import api.praya.myitems.builder.passive.PassiveEffectEnum;
import com.praya.agarthalib.utility.PotionUtil;
import com.praya.myitems.builder.abs.PassiveEffect;
import com.praya.myitems.config.plugin.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BuffWeaving extends PassiveEffect
{
    private static final PassiveEffectEnum buff;

    static {
        buff = PassiveEffectEnum.WATER_BREATHING;
    }

    public BuffWeaving() {
        super(BuffWeaving.buff, 1);
    }

    public BuffWeaving(final int grade) {
        super(BuffWeaving.buff, grade);
    }
    
    @Override
    public final void cast(final Player player) {
        final MainConfig mainConfig = MainConfig.getInstance();
        final PotionEffectType potionType = this.getPotion();
        final boolean isEnableParticle = mainConfig.isMiscEnableParticlePotion();
        final PotionEffect potion = PotionUtil.createPotion(potionType, PotionEffect.INFINITE_DURATION, this.grade, true, isEnableParticle);
        player.addPotionEffect(potion);
    }
}
