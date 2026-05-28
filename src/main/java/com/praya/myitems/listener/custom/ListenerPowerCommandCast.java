package com.praya.myitems.listener.custom;

import api.praya.myitems.builder.event.PowerCommandCastEvent;
import api.praya.myitems.builder.lorestats.LoreStatsEnum;
import api.praya.myitems.builder.lorestats.LoreStatsOption;
import api.praya.myitems.builder.player.PlayerPowerCooldown;
import api.praya.myitems.builder.power.PowerCommandProperties;
import com.praya.agarthalib.utility.CommandUtil;
import com.praya.agarthalib.utility.EquipmentUtil;
import com.praya.agarthalib.utility.MathUtil;
import com.praya.agarthalib.utility.TextUtil;
import com.praya.myitems.MyItems;
import com.praya.myitems.builder.handler.HandlerEvent;
import com.praya.myitems.manager.game.LoreStatsManager;
import com.praya.myitems.manager.game.PowerCommandManager;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.main.Slot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerPowerCommandCast extends HandlerEvent implements Listener {

    public ListenerPowerCommandCast(final MyItems plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void eventPowerCommandCast(final PowerCommandCastEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        final String keyCommand = event.getKeyCommand();

        // Managers
        final PowerCommandManager powerCommandManager = this.plugin.getGameManager().getPowerManager().getPowerCommandManager();
        final LoreStatsManager statsManager = this.plugin.getGameManager().getStatsManager();
        final PlayerPowerCooldown powerCooldown = this.plugin.getPlayerManager().getPlayerPowerManager().getPlayerPowerCooldown(player);

        final PowerCommandProperties properties = powerCommandManager.getPowerCommandProperties(keyCommand);
        if (properties == null) return;

        // 1. Xử lý Placeholders
        final Map<String, String> map = new HashMap<>();
        map.put("player", player.getName());

        // 2. Thực thi lệnh (OP & Console)
        this.executeCommands(player, properties.getCommandOP(), map, true);
        this.executeCommands(player, properties.getCommandConsole(), map, false);

        // 3. Xử lý Cooldown
        long timeCooldown = MathUtil.convertSecondsToMilis(event.getCooldown());
        if (timeCooldown > 0) {
            powerCooldown.setPowerCommandCooldown(keyCommand, timeCooldown);
        }

        // 4. Xử lý Tiêu hao hoặc Độ bền
        if (properties.isConsume()) {
            this.handleConsume(player, item);
        } else {
            this.handleDurability(player, item, statsManager);
        }
    }

    private void executeCommands(Player player, List<String> commands, Map<String, String> map, boolean isSudo) {
        if (commands == null || commands.isEmpty()) return;

        for (String cmd : commands) {
            // Apply placeholders
            String processedCmd = TextUtil.placeholder(new HashMap<>(map), cmd, "<", ">");
            processedCmd = TextUtil.hookPlaceholderAPI(player, processedCmd);

            if (isSudo) {
                CommandUtil.sudoCommand(player, processedCmd, true);
            } else {
                CommandUtil.consoleCommand(processedCmd);
            }
        }
    }

    private void handleConsume(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            // Sử dụng Bridge để xóa item ở Mainhand an toàn trong 1.21.R3
            Bridge.getBridgeEquipment().setEquipment(player, null, Slot.MAINHAND);
        }
    }

    private void handleDurability(Player player, ItemStack item, LoreStatsManager statsManager) {
        if (item == null || item.getType() == Material.AIR) return;

        int currentDura = (int) statsManager.getLoreValue(item, LoreStatsEnum.DURABILITY, LoreStatsOption.CURRENT);

        // Trừ độ bền và kiểm tra xem item có bị hỏng (Broken) không
        if (!statsManager.durability(player, item, currentDura, true)) {
            statsManager.sendBrokenCode(player, Slot.MAINHAND);
        }
    }
}