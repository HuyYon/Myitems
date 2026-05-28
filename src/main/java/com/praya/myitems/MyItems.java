package com.praya.myitems;

import com.praya.myitems.utility.main.AntiBugUtil;
import org.bukkit.event.Listener;
import com.praya.myitems.listener.support.ListenerPlayerStaminaRegenChange;
import com.praya.myitems.listener.support.ListenerPlayerStaminaMaxChange;
import com.praya.myitems.listener.support.ListenerPlayerHealthRegenChange;
import com.praya.myitems.listener.support.ListenerMythicMobDeath;
import com.praya.myitems.listener.support.ListenerMythicMobSpawn;
import com.praya.myitems.listener.support.ListenerPlayerLevelUp;
import com.praya.agarthalib.utility.PluginUtil;
import com.praya.myitems.listener.main.ListenerPlayerSwapHandItems;
import com.praya.myitems.listener.main.ListenerBlockExplode;
import org.bukkit.plugin.Plugin;
import com.praya.agarthalib.utility.ServerEventUtil;
import com.praya.myitems.listener.support.ListenerPlayerHealthMaxChange;
import com.praya.myitems.listener.custom.ListenerPowerSpecialCast;
import com.praya.myitems.listener.custom.ListenerPowerShootCast;
import com.praya.myitems.listener.custom.ListenerPowerPreCast;
import com.praya.myitems.listener.custom.ListenerPowerCommandCast;
import com.praya.myitems.listener.custom.ListenerMenuOpen;
import com.praya.myitems.listener.custom.ListenerMenuClose;
import com.praya.myitems.listener.custom.ListenerCombatCriticalDamage;
import com.praya.myitems.listener.main.ListenerProjectileHit;
import com.praya.myitems.listener.main.ListenerEntityShootBow;
import com.praya.myitems.listener.main.ListenerPlayerRespawn;
import com.praya.myitems.listener.main.ListenerPlayerJoin;
import com.praya.myitems.listener.main.ListenerPlayerInteractEntity;
import com.praya.myitems.listener.main.ListenerPlayerInteract;
import com.praya.myitems.listener.main.ListenerPlayerItemDamage;
import com.praya.myitems.listener.main.ListenerInventoryOpen;
import com.praya.myitems.listener.main.ListenerInventoryDrag;
import com.praya.myitems.listener.main.ListenerInventoryClick;
import com.praya.myitems.listener.main.ListenerHeldItem;
import com.praya.myitems.listener.main.ListenerEntityRegainHealth;
import com.praya.myitems.listener.main.ListenerEntityDeath;
import com.praya.myitems.listener.main.ListenerEntityDamageByEntity;
import com.praya.myitems.listener.main.ListenerEntityDamage;
import com.praya.myitems.listener.main.ListenerPlayerDropItem;
import com.praya.myitems.listener.main.ListenerCommand;
import com.praya.myitems.listener.main.ListenerBlockPhysic;
import com.praya.myitems.listener.main.ListenerBlockBreak;
import org.bukkit.command.TabCompleter;
import com.praya.myitems.tabcompleter.TabCompleterMyItems;
import com.praya.agarthalib.utility.ServerUtil;
import core.praya.agarthalib.enums.main.VersionNMS;
import com.praya.myitems.command.CommandMyItems;
import java.util.List;
import com.praya.myitems.manager.register.RegisterManager;
import com.praya.myitems.manager.task.TaskManager;
import com.praya.myitems.manager.game.GameManager;
import com.praya.myitems.manager.player.PlayerManager;
import com.praya.myitems.manager.plugin.PluginManager;
import core.praya.agarthalib.builder.face.Agartha;
import org.bukkit.plugin.java.JavaPlugin;

public class MyItems extends JavaPlugin implements Agartha
{
    private final String type = "Premium";
    private final String placeholder = "myitems";
    private PluginManager pluginManager;
    private PlayerManager playerManager;
    private GameManager gameManager;
    private TaskManager taskManager;
    private RegisterManager registerManager;
    
    public String getPluginName() {
        return this.getName();
    }
    
    public String getPluginType() {
        return "Premium";
    }
    
    public String getPluginVersion() {
        return this.getDescription().getVersion();
    }
    
    public String getPluginPlaceholder() {
        return "myitems";
    }
    
    public String getPluginWebsite() {
        return this.getPluginManager().getPluginPropertiesManager().getWebsite();
    }
    
    public String getPluginLatest() {
        return this.getPluginManager().getPluginPropertiesManager().getPluginTypeVersion(this.getPluginType());
    }
    
    public List<String> getPluginDevelopers() {
        return this.getPluginManager().getPluginPropertiesManager().getDevelopers();
    }
    
    public final PluginManager getPluginManager() {
        return this.pluginManager;
    }
    
    public final GameManager getGameManager() {
        return this.gameManager;
    }
    
    public final PlayerManager getPlayerManager() {
        return this.playerManager;
    }
    
    public final TaskManager getTaskManager() {
        return this.taskManager;
    }
    
    public final RegisterManager getRegisterManager() {
        return this.registerManager;
    }
    
    public void onEnable() {
        this.setPluginManager();
        this.setPlayerManager();
        this.setGameManager();
        this.setTaskManager();
        this.setRegisterManager();
        this.getPluginManager().getDependencyManager().getDependencyConfig().setup();
        this.getPluginManager().getHookManager().getHookConfig().setup();
        this.setup();
        this.registerCommand();
        this.registerTabComplete();
        this.registerEvent();
        this.registerPlaceholder();
    }
    
    private final void setPluginManager() {
        (this.pluginManager = new PluginManager(this)).initialize();
    }
    
    private final void setGameManager() {
        this.gameManager = new GameManager(this);
    }
    
    private final void setPlayerManager() {
        this.playerManager = new PlayerManager(this);
    }
    
    private final void setTaskManager() {
        this.taskManager = new TaskManager(this);
    }
    
    private final void setRegisterManager() {
        this.registerManager = new RegisterManager(this);
    }
    
    private final void setup() {
        this.gameManager.getAbilityWeaponManager().getAbilityWeaponConfig().setup();
        this.gameManager.getElementManager().getElementConfig().setup();
        this.gameManager.getPowerManager().getPowerCommandManager().getPowerCommandConfig().setup();
        this.gameManager.getPowerManager().getPowerSpecialManager().getPowerSpecialConfig().setup();
        this.gameManager.getSocketManager().getSocketConfig().setup();
        this.gameManager.getItemManager().getItemConfig().setup();
        this.gameManager.getItemTypeManager().getItemTypeConfig().setup();
        this.gameManager.getItemTierManager().getItemTierConfig().setup();
        this.gameManager.getItemGeneratorManager().getItemGeneratorConfig().setup();
        this.gameManager.getItemSetManager().getItemSetConfig().setup();
    }
    
    private final void registerPlaceholder() {
        this.getPluginManager().getPlaceholderManager().registerAll();
    }

    private final void registerCommand() {
        final CommandMyItems commandMyItems = new CommandMyItems(this);
        this.getCommand("MyItems").setExecutor(commandMyItems);
    }

    private final void registerTabComplete() {
        final TabCompleter tabCompleterMyItems = new TabCompleterMyItems(this);

        this.getCommand("MyItems").setTabCompleter(tabCompleterMyItems);
    }
    
    private final void registerEvent() {
        final Listener listenerBlockBreak = (Listener)new ListenerBlockBreak(this);
        final Listener listenerBlockPhysic = (Listener)new ListenerBlockPhysic(this);
        final Listener listenerCommand = (Listener)new ListenerCommand(this);
        final Listener listenerPlayerDropItem = (Listener)new ListenerPlayerDropItem(this);
        final Listener listenerEntityDamage = (Listener)new ListenerEntityDamage(this);
        final Listener listenerEntityDamageByEntity = (Listener)new ListenerEntityDamageByEntity(this);
        final Listener listenerEntityDeath = (Listener)new ListenerEntityDeath(this);
        final Listener listenerEntityRegainHealth = (Listener)new ListenerEntityRegainHealth(this);
        final Listener listenerHeldItem = (Listener)new ListenerHeldItem(this);
        final Listener listenerInventoryClick = (Listener)new ListenerInventoryClick(this);
        final Listener listenerInventoryDrag = (Listener)new ListenerInventoryDrag(this);
        final Listener listenerInventoryOpen = (Listener)new ListenerInventoryOpen(this);
        final Listener listenerPlayerItemDamage = (Listener)new ListenerPlayerItemDamage(this);
        final Listener listenerPlayerInteract = (Listener)new ListenerPlayerInteract(this);
        final Listener listenerPlayerInteractEntity = (Listener)new ListenerPlayerInteractEntity(this);
        final Listener listenerPlayerJoin = (Listener)new ListenerPlayerJoin(this);
        final Listener listenerPlayerRespawn = (Listener)new ListenerPlayerRespawn(this);
        final Listener listenerEntityShootBowEvent = (Listener)new ListenerEntityShootBow(this);
        final Listener listenerProjectileHit = (Listener)new ListenerProjectileHit(this);
        final Listener listenerCombatCriticalDamage = (Listener)new ListenerCombatCriticalDamage(this);
        final Listener listenerMenuClose = (Listener)new ListenerMenuClose(this);
        final Listener listenerMenuOpen = (Listener)new ListenerMenuOpen(this);
        final Listener listenerPowerCommandCast = (Listener)new ListenerPowerCommandCast(this);
        final Listener listenerPowerPreCast = (Listener)new ListenerPowerPreCast(this);
        final Listener listenerPowerShootCast = (Listener)new ListenerPowerShootCast(this);
        final Listener listenerPowerSpecialCast = (Listener)new ListenerPowerSpecialCast(this);
        final Listener listenerPlayerHealthMaxChange = (Listener)new ListenerPlayerHealthMaxChange(this);
        ServerEventUtil.registerEvent((Plugin)this, listenerBlockBreak);
        ServerEventUtil.registerEvent((Plugin)this, listenerBlockPhysic);
        ServerEventUtil.registerEvent((Plugin)this, listenerCommand);
        ServerEventUtil.registerEvent((Plugin)this, listenerEntityDamage);
        ServerEventUtil.registerEvent((Plugin)this, listenerEntityDamageByEntity);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerDropItem);
        ServerEventUtil.registerEvent((Plugin)this, listenerEntityDeath);
        ServerEventUtil.registerEvent((Plugin)this, listenerEntityRegainHealth);
        ServerEventUtil.registerEvent((Plugin)this, listenerHeldItem);
        ServerEventUtil.registerEvent((Plugin)this, listenerInventoryClick);
        ServerEventUtil.registerEvent((Plugin)this, listenerInventoryDrag);
        ServerEventUtil.registerEvent((Plugin)this, listenerInventoryOpen);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerItemDamage);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerInteract);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerInteractEntity);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerJoin);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerRespawn);
        ServerEventUtil.registerEvent((Plugin)this, listenerEntityShootBowEvent);
        ServerEventUtil.registerEvent((Plugin)this, listenerProjectileHit);
        ServerEventUtil.registerEvent((Plugin)this, listenerCombatCriticalDamage);
        ServerEventUtil.registerEvent((Plugin)this, listenerMenuClose);
        ServerEventUtil.registerEvent((Plugin)this, listenerMenuOpen);
        ServerEventUtil.registerEvent((Plugin)this, listenerPowerCommandCast);
        ServerEventUtil.registerEvent((Plugin)this, listenerPowerPreCast);
        ServerEventUtil.registerEvent((Plugin)this, listenerPowerShootCast);
        ServerEventUtil.registerEvent((Plugin)this, listenerPowerSpecialCast);
        ServerEventUtil.registerEvent((Plugin)this, listenerPlayerHealthMaxChange);
        if (ServerUtil.isCompatible(VersionNMS.V1_21_R3)) {
            final Listener listenerBlockExplode = (Listener)new ListenerBlockExplode(this);
            final Listener listenerPlayerSwapHandItems = (Listener)new ListenerPlayerSwapHandItems(this);
            ServerEventUtil.registerEvent((Plugin)this, listenerBlockExplode);
            ServerEventUtil.registerEvent((Plugin)this, listenerPlayerSwapHandItems);
        }
        if (PluginUtil.isPluginInstalled("SkillAPI")) {
            final Listener listenerPlayerLevelUp = (Listener)new ListenerPlayerLevelUp(this);
            ServerEventUtil.registerEvent((Plugin)this, listenerPlayerLevelUp);
        }
        if (PluginUtil.isPluginInstalled("MythicMobs")) {
            final Listener listenerMythicMobSpawn = (Listener)new ListenerMythicMobSpawn(this);
            final Listener listenerMythicMobDeath = (Listener)new ListenerMythicMobDeath(this);
            ServerEventUtil.registerEvent((Plugin)this, listenerMythicMobSpawn);
            ServerEventUtil.registerEvent((Plugin)this, listenerMythicMobDeath);
        }
        if (PluginUtil.isPluginInstalled("LifeEssence")) {
            final Listener listenerPlayerHealthRegenChange = (Listener)new ListenerPlayerHealthRegenChange(this);
            ServerEventUtil.registerEvent((Plugin)this, listenerPlayerHealthRegenChange);
        }
        if (PluginUtil.isPluginInstalled("CombatStamina")) {
            final Listener listenerPlayerStaminaMaxChange = (Listener)new ListenerPlayerStaminaMaxChange(this);
            final Listener listenerPlayerStaminaRegenChange = (Listener)new ListenerPlayerStaminaRegenChange(this);
            ServerEventUtil.registerEvent((Plugin)this, listenerPlayerStaminaMaxChange);
            ServerEventUtil.registerEvent((Plugin)this, listenerPlayerStaminaRegenChange);
        }
    }
    
    public void onDisable() {
        AntiBugUtil.antiBugCustomStats();
    }
}
