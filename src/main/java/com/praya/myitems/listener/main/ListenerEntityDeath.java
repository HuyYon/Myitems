package com.praya.myitems.listener.main;

import api.praya.myitems.builder.lorestats.LoreStatsEnum;
import api.praya.myitems.builder.lorestats.LoreStatsOption;
import com.praya.agarthalib.utility.EntityUtil;
import com.praya.agarthalib.utility.EquipmentUtil;
import com.praya.agarthalib.utility.MathUtil;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerEvent;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.manager.game.GameManager;
import com.praya.myitems.manager.game.LoreStatsManager;
import core.praya.agarthalib.bridge.unity.Bridge;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerEntityDeath extends HandlerEvent implements Listener {

    public ListenerEntityDeath(final MyItems plugin) {
        super(plugin);
    }

    @EventHandler
    public void onDeath(final EntityDeathEvent event) {
        final GameManager gameManager = this.plugin.getGameManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final LivingEntity victim = event.getEntity();
        final MainConfig mainConfig = MainConfig.getInstance();

        final Player player = victim.getKiller();
        if (player == null) return;

        final double expGain = EntityUtil.isPlayer(victim) ? mainConfig.getDropExpPlayer() : mainConfig.getDropExpMobs();

        for (int itemCode = 0; itemCode < 6; ++itemCode) {
            final ItemStack item = Bridge.getBridgeEquipment().getEquipment(player, itemCode);

            if (item == null || item.getType().isAir() || !EquipmentUtil.loreCheck(item)) {
                continue;
            }

            final int line = statsManager.getLineLoreStats(item, LoreStatsEnum.LEVEL);
            if (line != -1) {
                double scaleExp;
                if (itemCode == 0) scaleExp = 1.0;
                else if (itemCode == 1) scaleExp = mainConfig.getModifierScaleExpOffHand();
                else scaleExp = mainConfig.getModifierScaleExpArmor();

                final List<String> currentLores = EquipmentUtil.getLores(item);
                if (line > currentLores.size()) continue;

                final String loreLevel = currentLores.get(line - 1);
                final String[] expLores = loreLevel.split(MainConfig.KEY_EXP_CURRENT);
                final String[] upLores = loreLevel.split(MainConfig.KEY_EXP_UP);

                final String colorExpCurrent = mainConfig.getStatsColorExpCurrent();
                final String colorExpUp = mainConfig.getStatsColorExpUp();

                try {
                    final double exp = MathUtil.parseDouble(ChatColor.stripColor(expLores[1].replace(colorExpCurrent, "")));
                    final double up = MathUtil.parseDouble(ChatColor.stripColor(upLores[1].replace(colorExpUp, "")));
                    final int level = (int) statsManager.getLoreValue(item, LoreStatsEnum.LEVEL, null);
                    final int maxLevel = mainConfig.getStatsMaxLevelValue();

                    if (exp + (expGain * scaleExp) < up) {
                        if (level < maxLevel) {
                            final double newExp = MathUtil.roundNumber(exp + (expGain * scaleExp), 1);
                            final String newExpLore = expLores[0] + MainConfig.KEY_EXP_CURRENT + colorExpCurrent + newExp + MainConfig.KEY_EXP_CURRENT + expLores[2];

                            EquipmentUtil.setLore(item, line, newExpLore);
                            Bridge.getBridgeEquipment().setEquipment(player, item, itemCode);
                        }
                    }
                    else {
                        final ItemMeta meta = item.getItemMeta();
                        if (meta == null) continue;

                        final double scaleUp = mainConfig.getStatsScaleUpValue();
                        final double calculation = (1.0 + scaleUp * level) / (1.0 + scaleUp * (level - 1));

                        double nextExp = MathUtil.roundNumber(exp + (expGain * scaleExp) - up, 1);
                        if (level + 1 >= maxLevel) nextExp = 0.0;

                        final String newLoreLevel = statsManager.getTextLoreStats(LoreStatsEnum.LEVEL, level + 1, nextExp);
                        final List<String> lores = new ArrayList<>(meta.getLore() != null ? meta.getLore() : new ArrayList<>());
                        final Map<Integer, String> mapLore = new HashMap<>();
                        for (int i = 0; i < lores.size(); ++i) {
                            mapLore.put(i, lores.get(i));
                        }
                        mapLore.put(line - 1, newLoreLevel);
                        if (itemCode < 2) {
                            final int lineDamage = statsManager.getLineLoreStats(item, LoreStatsEnum.DAMAGE);
                            if (lineDamage != -1) {
                                double minDmg = statsManager.getLoreValue(item, LoreStatsEnum.DAMAGE, LoreStatsOption.MIN);
                                double maxDmg = statsManager.getLoreValue(item, LoreStatsEnum.DAMAGE, LoreStatsOption.MAX);
                                final double ratio = maxDmg / minDmg;

                                minDmg = MathUtil.roundNumber(minDmg * calculation, 2);
                                maxDmg = MathUtil.roundNumber(minDmg * ratio, 2);

                                mapLore.put(lineDamage - 1, statsManager.getTextLoreStats(LoreStatsEnum.DAMAGE, minDmg, maxDmg));
                            }
                        }
                        else {
                            final int lineDefense = statsManager.getLineLoreStats(item, LoreStatsEnum.DEFENSE);
                            if (lineDefense != -1) {
                                double defense = statsManager.getLoreValue(item, LoreStatsEnum.DEFENSE, null);
                                defense = MathUtil.roundNumber(defense * calculation, 2);
                                mapLore.put(lineDefense - 1, statsManager.getTextLoreStats(LoreStatsEnum.DEFENSE, defense));
                            }
                        }

                        // Build lại danh sách Lore mới
                        final List<String> newLoresList = new ArrayList<>();
                        for (int j = 0; j < mapLore.size(); ++j) {
                            newLoresList.add(mapLore.get(j));
                        }

                        meta.setLore(newLoresList);
                        item.setItemMeta(meta);
                        Bridge.getBridgeEquipment().setEquipment(player, item, itemCode);
                    }
                } catch (Exception ignored) {
                    // Tránh crash nếu lore định dạng sai
                }
            }
        }
    }
}