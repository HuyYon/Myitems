package com.praya.myitems.manager.task;

import org.bukkit.scheduler.BukkitScheduler;
import com.praya.myitems.builder.task.TaskPassiveEffect;
import org.bukkit.Bukkit;
import com.praya.myitems.config.plugin.MainConfig;
import com.praya.myitems.MyItems;
import org.bukkit.scheduler.BukkitTask;
import com.praya.myitems.builder.handler.HandlerManager;

public class TaskPassiveEffectManager extends HandlerManager {
    private BukkitTask taskLoadPassiveEffect;

    protected TaskPassiveEffectManager(final MyItems plugin) {
        super(plugin);
        this.reloadTaskLoadPassiveEffect();
    }

    public final void reloadTaskLoadPassiveEffect() {
        if (this.taskLoadPassiveEffect != null) {
            this.taskLoadPassiveEffect.cancel();
        }
        this.taskLoadPassiveEffect = this.createTaskLoadPassiveEffect();
    }

    private final BukkitTask createTaskLoadPassiveEffect() {
        final MainConfig mainConfig = MainConfig.getInstance();
        final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        final Runnable runnable = new TaskPassiveEffect(this.plugin);
        final long delay = 2L;
        final long period = (long) mainConfig.getPassivePeriodEffect();

        return scheduler.runTaskTimer(this.plugin, runnable, delay, period);
    }
}