package com.praya.myitems.manager.task;

import org.bukkit.scheduler.BukkitScheduler;
import com.praya.myitems.builder.task.TaskCustomEffect;
import org.bukkit.Bukkit;
import com.praya.myitems.MyItems;
import org.bukkit.scheduler.BukkitTask;
import com.praya.myitems.builder.handler.HandlerManager;

public class TaskCustomEffectManager extends HandlerManager {
    private BukkitTask taskCustomEffect;

    protected TaskCustomEffectManager(final MyItems plugin) {
        super(plugin);
        this.reloadTaskCustomEffect();
    }

    public final void reloadTaskCustomEffect() {
        if (this.taskCustomEffect != null) {
            this.taskCustomEffect.cancel();
        }
        this.taskCustomEffect = this.createTaskCustomEffect();
    }

    private final BukkitTask createTaskCustomEffect() {
        final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        final Runnable runnable = new TaskCustomEffect(this.plugin);
        final long delay = 2L;
        final long period = 1L;
        return scheduler.runTaskTimer(this.plugin, runnable, delay, period);
    }
}