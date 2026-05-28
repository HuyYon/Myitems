package com.praya.myitems.listener.custom;

import api.praya.myitems.builder.event.PowerCommandCastEvent;
import api.praya.myitems.builder.event.PowerPreCastEvent;
import api.praya.myitems.builder.event.PowerShootCastEvent;
import api.praya.myitems.builder.event.PowerSpecialCastEvent;
import api.praya.myitems.builder.lorestats.LoreStatsEnum;
import api.praya.myitems.builder.lorestats.LoreStatsOption;
import api.praya.myitems.builder.player.PlayerPowerCooldown;
import api.praya.myitems.builder.power.PowerClickEnum;
import api.praya.myitems.builder.power.PowerEnum;
import api.praya.myitems.builder.power.PowerSpecialEnum;
import com.praya.agarthalib.utility.SenderUtil;
import com.praya.agarthalib.utility.ServerEventUtil;
import com.praya.agarthalib.utility.TimeUtil;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerEvent;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.manager.game.*;
import com.praya.myitems.manager.player.PlayerManager;
import com.praya.myitems.manager.player.PlayerPowerManager;
import com.praya.myitems.manager.plugin.LanguageManager;
import com.praya.myitems.manager.plugin.PluginManager;
import com.praya.myitems.utility.main.ProjectileUtil;
import core.praya.agarthalib.builder.message.MessageBuild;
import core.praya.agarthalib.enums.branch.ProjectileEnum;
import core.praya.agarthalib.enums.branch.SoundEnum;
import core.praya.agarthalib.enums.main.Slot;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ListenerPowerPreCast extends HandlerEvent implements Listener {

    public ListenerPowerPreCast(final MyItems plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void eventPowerPreCast(final PowerPreCastEvent event) {
        final PluginManager pluginManager = this.plugin.getPluginManager();
        final PlayerManager playerManager = this.plugin.getPlayerManager();
        final GameManager gameManager = this.plugin.getGameManager();

        final PowerManager powerManager = gameManager.getPowerManager();
        final LoreStatsManager statsManager = gameManager.getStatsManager();
        final RequirementManager requirementManager = gameManager.getRequirementManager();
        final PowerCommandManager powerCommandManager = powerManager.getPowerCommandManager();
        final PlayerPowerManager playerPowerManager = playerManager.getPlayerPowerManager();
        final LanguageManager lang = pluginManager.getLanguageManager();
        final MainConfig mainConfig = MainConfig.getInstance();

        final Player player = event.getPlayer();
        final PowerEnum power = event.getPower();
        final PowerClickEnum click = event.getClick();
        final ItemStack item = event.getItem();
        final String lore = event.getLore();

        // 1.21 Check: Đảm bảo item không null/air trước khi xử lý lore
        if (item == null || item.getType().isAir()) return;

        final int durability = (int) statsManager.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.CURRENT);
        final PlayerPowerCooldown powerCooldown = playerPowerManager.getPlayerPowerCooldown((OfflinePlayer) player);

        // Tối ưu việc split chuỗi
        final String[] cooldownList = lore.split(MainConfig.KEY_COOLDOWN);

        if (requirementManager.isAllowed(player, item) && cooldownList.length > 1) {
            final String keyCooldown = cooldownList[1].replace(mainConfig.getPowerColorCooldown(), "").trim();

            double cooldown;
            try {
                cooldown = Math.max(0.0, Double.parseDouble(keyCooldown));
            } catch (NumberFormatException e) {
                return;
            }

            if (!statsManager.checkDurability(item, durability)) {
                statsManager.sendBrokenCode(player, Slot.MAINHAND, false);
                return;
            }

            final boolean enableMessageCooldown = mainConfig.isPowerEnableMessageCooldown();

            // Xử lý logic theo từng loại Power
            if (power.equals(PowerEnum.COMMAND)) {
                handleCommandPower(player, lore, power, click, item, cooldown, powerCooldown, powerCommandManager, lang, mainConfig, enableMessageCooldown);
            }
            else if (power.equals(PowerEnum.SHOOT)) {
                handleShootPower(player, lore, power, click, item, cooldown, powerCooldown, lang, mainConfig, enableMessageCooldown);
            }
            else if (power.equals(PowerEnum.SPECIAL)) {
                handleSpecialPower(player, lore, power, click, item, cooldown, powerCooldown, lang, mainConfig, enableMessageCooldown);
            }
        }
    }

    private void handleCommandPower(Player player, String lore, PowerEnum power, PowerClickEnum click, ItemStack item, double cooldown, PlayerPowerCooldown pCooldown, PowerCommandManager manager, LanguageManager lang, MainConfig config, boolean notify) {
        String[] parts = lore.split(MainConfig.KEY_COMMAND);
        if (parts.length > 1) {
            String loreCmd = parts[1].replaceFirst(config.getPowerColorType(), "");
            String key = manager.getCommandKeyByLore(loreCmd);
            if (key != null) {
                if (pCooldown.isPowerCommandCooldown(key)) {
                    if (notify) sendCooldownMsg(player, lang, "Power_Command_Cooldown", key, pCooldown.getPowerCommandTimeLeft(key));
                } else {
                    ServerEventUtil.callEvent(new PowerCommandCastEvent(player, power, click, item, lore, key, cooldown));
                }
            }
        }
    }

    private void handleShootPower(Player player, String lore, PowerEnum power, PowerClickEnum click, ItemStack item, double cooldown, PlayerPowerCooldown pCooldown, LanguageManager lang, MainConfig config, boolean notify) {
        String[] parts = lore.split(MainConfig.KEY_SHOOT);
        if (parts.length > 1) {
            String loreProj = parts[1].replace(config.getPowerColorType(), "");
            ProjectileEnum proj = ProjectileUtil.getProjectileByLore(loreProj);
            if (proj != null) {
                if (pCooldown.isPowerShootCooldown(proj)) {
                    if (notify) sendCooldownMsg(player, lang, "Power_Shoot_Cooldown", ProjectileUtil.getText(proj), pCooldown.getPowerShootTimeLeft(proj));
                } else {
                    ServerEventUtil.callEvent(new PowerShootCastEvent(player, power, click, item, lore, proj, cooldown));
                }
            }
        }
    }

    private void handleSpecialPower(Player player, String lore, PowerEnum power, PowerClickEnum click, ItemStack item, double cooldown, PlayerPowerCooldown pCooldown, LanguageManager lang, MainConfig config, boolean notify) {
        String[] parts = lore.split(MainConfig.KEY_SPECIAL);
        if (parts.length > 1) {
            String loreSpec = parts[1].replace(config.getPowerColorType(), "");
            PowerSpecialEnum spec = PowerSpecialEnum.getSpecialByLore(loreSpec);
            if (spec != null) {
                if (pCooldown.isPowerSpecialCooldown(spec)) {
                    if (notify) sendCooldownMsg(player, lang, "Power_Special_Cooldown", spec.getText(), pCooldown.getPowerSpecialTimeLeft(spec));
                } else {
                    ServerEventUtil.callEvent(new PowerSpecialCastEvent(player, power, click, item, lore, spec, cooldown));
                }
            }
        }
    }

    private void sendCooldownMsg(Player player, LanguageManager lang, String nodes, String powerName, long timeLeft) {
        MessageBuild message = lang.getMessage(nodes);
        Map<String, String> map = new HashMap<>();
        map.put("power", powerName);
        map.put("time", TimeUtil.getTextTime(timeLeft));
        message.sendMessage(player, (HashMap<String, String>) map);
        SenderUtil.playSound(player, SoundEnum.ENTITY_BLAZE_DEATH);
    }
}