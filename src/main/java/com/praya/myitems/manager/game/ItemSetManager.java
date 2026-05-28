package com.praya.myitems.manager.game;

import org.bukkit.inventory.InventoryView;
import java.util.ListIterator;
import org.bukkit.inventory.PlayerInventory;
import java.util.Set;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.GameMode;
import com.praya.agarthalib.utility.TextUtil;
import java.util.Collections;
import org.bukkit.entity.Player;
import java.util.HashSet;
import org.bukkit.inventory.Inventory;
import api.praya.myitems.builder.ability.AbilityItemWeapon;
import api.praya.myitems.builder.item.ItemSetBonusEffectAbilityWeapon;
import api.praya.myitems.builder.item.ItemSetBonusEffect;
import api.praya.myitems.builder.item.ItemSetBonusEffectStats;
import api.praya.myitems.builder.item.ItemSetBonus;
import api.praya.myitems.builder.ability.AbilityWeapon;
import api.praya.myitems.builder.item.ItemSetBonusEffectEntity;
import core.praya.agarthalib.bridge.unity.Bridge;
import core.praya.agarthalib.enums.main.Slot;
import java.util.HashMap;
import org.bukkit.entity.LivingEntity;
import java.util.List;
import com.praya.agarthalib.utility.EquipmentUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import com.praya.myitems.config.plugin.MainConfig;
import java.util.Iterator;
import api.praya.myitems.builder.item.ItemSetComponent;
import java.util.ArrayList;
import api.praya.myitems.builder.item.ItemSet;
import java.util.Collection;
import com.praya.myitems.MyItems;
import com.praya.myitems.config.game.ItemSetConfig;
import com.praya.myitems.builder.handler.HandlerManager;

public class ItemSetManager extends HandlerManager {
    private final ItemSetConfig itemSetConfig;

    protected ItemSetManager(final MyItems plugin) {
        super(plugin);
        this.itemSetConfig = new ItemSetConfig(plugin);
    }

    public final ItemSetConfig getItemSetConfig() {
        return this.itemSetConfig;
    }

    public final Collection<String> getItemSetIDs() {
        return this.getItemSetConfig().getItemSetIDs();
    }

    public final Collection<ItemSet> getAllItemSet() {
        return this.getItemSetConfig().getAllItemSet();
    }

    public final ItemSet getItemSet(final String id) {
        return this.getItemSetConfig().getItemSet(id);
    }

    public final boolean isExists(final String id) {
        return this.getItemSet(id) != null;
    }

    public final Collection<String> getItemComponentIDs() {
        final Collection<String> itemComponentIDs = new ArrayList<String>();
        for (final ItemSet itemSet : this.getAllItemSet()) {
            for (final ItemSetComponent itemSetComponent : itemSet.getAllItemSetComponent()) {
                final String itemComponentID = itemSetComponent.getID();
                itemComponentIDs.add(itemComponentID);
            }
        }
        return itemComponentIDs;
    }

    public final ItemSetComponent getItemComponentByKeyLore(final String keyLore) {
        if (keyLore != null) {
            for (final ItemSet itemSet : this.getAllItemSet()) {
                for (final ItemSetComponent key : itemSet.getAllItemSetComponent()) {
                    if (key.getKeyLore().equalsIgnoreCase(keyLore)) {
                        return key;
                    }
                }
            }
        }
        return null;
    }

    public final ItemSetComponent getItemComponentByLore(final String lore) {
        if (lore != null) {
            final String keySetComponentSelf = MainConfig.KEY_SET_COMPONENT_SELF;
            if (lore.contains(keySetComponentSelf)) {
                final String[] partsComponent = lore.split(keySetComponentSelf);
                if (partsComponent.length > 1) {
                    final String componentKeyLore = ChatColor.stripColor(partsComponent[1]);
                    return this.getItemComponentByKeyLore(componentKeyLore);
                }
            }
        }
        return null;
    }

    public final ItemSetComponent getItemComponent(final ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            final List<String> lores = EquipmentUtil.getLores(item);
            for (final String lore : lores) {
                final ItemSetComponent itemSetComponent = this.getItemComponentByLore(lore);
                if (itemSetComponent != null) {
                    return itemSetComponent;
                }
            }
        }
        return null;
    }

    public final ItemSet getItemSetByComponentID(final String componentID) {
        if (componentID != null) {
            for (final ItemSet key : this.getAllItemSet()) {
                for (final ItemSetComponent itemSetComponent : key.getAllItemSetComponent()) {
                    if (itemSetComponent.getID().equalsIgnoreCase(componentID)) {
                        return key;
                    }
                }
            }
        }
        return null;
    }

    public final ItemSet getItemSetByKeyLore(final String keyLore) {
        if (keyLore != null) {
            for (final ItemSet key : this.getAllItemSet()) {
                for (final ItemSetComponent itemSetComponent : key.getAllItemSetComponent()) {
                    if (itemSetComponent.getKeyLore().equalsIgnoreCase(keyLore)) {
                        return key;
                    }
                }
            }
        }
        return null;
    }

    public final ItemSet getItemSetByLore(final String lore) {
        if (lore != null) {
            final String keySetComponentSelf = MainConfig.KEY_SET_COMPONENT_SELF;
            if (lore.contains(keySetComponentSelf)) {
                final String[] partsComponent = lore.split(keySetComponentSelf);
                if (partsComponent.length > 1) {
                    final String componentKeyLore = ChatColor.stripColor(partsComponent[1]);
                    return this.getItemSetByKeyLore(componentKeyLore);
                }
            }
        }
        return null;
    }

    public final ItemSet getItemSet(final ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            final List<String> lores = EquipmentUtil.getLores(item);
            for (final String lore : lores) {
                final ItemSet itemSet = this.getItemSetByLore(lore);
                if (itemSet != null) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    public final boolean isItemSet(final ItemStack item) {
        return this.getItemSet(item) != null;
    }

    public final HashMap<Slot, ItemSetComponent> getMapItemComponent(final LivingEntity entity) {
        return this.getMapItemComponent(entity, true);
    }

    public final HashMap<Slot, ItemSetComponent> getMapItemComponent(final LivingEntity entity, final boolean checkSlot) {
        final HashMap<Slot, ItemSetComponent> mapItemSetComponent = new HashMap<Slot, ItemSetComponent>();
        if (entity != null) {
            for (final Slot slot : Slot.values()) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(entity, slot);
                if (item != null) {
                    final ItemSetComponent itemSetComponent = this.getItemComponent(item);
                    if (itemSetComponent != null && itemSetComponent.isMatchSlot(slot)) {
                        mapItemSetComponent.put(slot, itemSetComponent);
                    }
                }
            }
        }
        return mapItemSetComponent;
    }

    public final HashMap<Slot, ItemSet> getMapItemSet(final LivingEntity entity) {
        return this.getMapItemSet(entity, true);
    }

    public final HashMap<Slot, ItemSet> getMapItemSet(final LivingEntity entity, final boolean checkSlot) {
        final HashMap<Slot, ItemSet> mapItemSet = new HashMap<Slot, ItemSet>();
        if (entity != null) {
            final HashMap<Slot, ItemSetComponent> mapItemSetComponent = this.getMapItemComponent(entity, checkSlot);
            for (final Slot slot : mapItemSetComponent.keySet()) {
                final ItemSetComponent itemSetComponent = mapItemSetComponent.get(slot);
                final ItemSet itemSet = itemSetComponent.getItemSet();
                if (itemSet != null) {
                    mapItemSet.put(slot, itemSet);
                }
            }
        }
        return mapItemSet;
    }

    public final HashMap<ItemSet, Integer> getMapItemSetTotal(final LivingEntity entity) {
        return this.getMapItemSetTotal(entity, true);
    }

    public final HashMap<ItemSet, Integer> getMapItemSetTotal(final LivingEntity entity, final boolean checkSlot) {
        final HashMap<ItemSet, Integer> mapItemSetTotal = new HashMap<ItemSet, Integer>();
        if (entity != null) {
            final HashMap<Slot, ItemSet> mapItemSet = this.getMapItemSet(entity, checkSlot);
            for (final Slot slot : mapItemSet.keySet()) {
                final ItemSet itemSet = mapItemSet.get(slot);
                if (mapItemSetTotal.containsKey(itemSet)) {
                    final int total = mapItemSetTotal.get(itemSet) + 1;
                    mapItemSetTotal.put(itemSet, total);
                } else {
                    mapItemSetTotal.put(itemSet, 1);
                }
            }
        }
        return mapItemSetTotal;
    }

    public final ItemSetBonusEffectEntity getItemSetBonusEffectEntity(final LivingEntity entity) {
        return this.getItemSetBonusEffectEntity(entity, true);
    }

    public final ItemSetBonusEffectEntity getItemSetBonusEffectEntity(final LivingEntity entity, final boolean checkSlot) {
        return this.getItemSetBonusEffectEntity(entity, checkSlot, true);
    }

    public final ItemSetBonusEffectEntity getItemSetBonusEffectEntity(final LivingEntity entity, final boolean checkSlot, final boolean checkChance) {
        final GameManager gameManager = this.plugin.getGameManager();
        final AbilityWeaponManager abilityWeaponManager = gameManager.getAbilityWeaponManager();
        final HashMap<AbilityWeapon, Integer> mapAbilityWeapon = new HashMap<AbilityWeapon, Integer>();
        double additionalDamage = 0.0, percentDamage = 0.0, penetration = 0.0, pvpDamage = 0.0, pveDamage = 0.0;
        double additionalDefense = 0.0, percentDefense = 0.0, health = 0.0, healthRegen = 0.0, staminaMax = 0.0, staminaRegen = 0.0;
        double attackAoERadius = 0.0, attackAoEDamage = 0.0, pvpDefense = 0.0, pveDefense = 0.0, criticalChance = 0.0, criticalDamage = 0.0;
        double blockAmount = 0.0, blockRate = 0.0, hitRate = 0.0, dodgeRate = 0.0;

        if (entity != null) {
            final HashMap<ItemSet, Integer> mapItemSetTotal = this.getMapItemSetTotal(entity, checkSlot);
            for (final ItemSet itemSet : mapItemSetTotal.keySet()) {
                final int total = mapItemSetTotal.get(itemSet);
                for (final ItemSetBonus itemSetBonus : itemSet.getAllItemSetBonus()) {
                    if (total >= itemSetBonus.getAmountID()) {
                        final ItemSetBonusEffect itemSetBonusEffect = itemSetBonus.getEffect();
                        final ItemSetBonusEffectStats stats = itemSetBonusEffect.getEffectStats();
                        final ItemSetBonusEffectAbilityWeapon bonusAbility = itemSetBonusEffect.getEffectAbilityWeapon();
                        final HashMap<AbilityWeapon, Integer> mapAbilityWeaponBonus = abilityWeaponManager.getMapAbilityWeapon(bonusAbility.getAllAbilityItemWeapon(), checkChance);

                        additionalDamage += stats.getAdditionalDamage();
                        percentDamage += stats.getPercentDamage();
                        penetration += stats.getPenetration();
                        pvpDamage += stats.getPvPDamage();
                        pveDamage += stats.getPvEDamage();
                        additionalDefense += stats.getAdditionalDefense();
                        percentDefense += stats.getPercentDefense();
                        health += stats.getHealth();
                        healthRegen += stats.getHealthRegen();
                        staminaMax += stats.getStaminaMax();
                        staminaRegen += stats.getStaminaRegen();
                        attackAoERadius += stats.getAttackAoERadius();
                        attackAoEDamage += stats.getAttackAoEDamage();
                        pvpDefense += stats.getPvPDefense();
                        pveDefense += stats.getPvEDefense();
                        criticalChance += stats.getCriticalChance();
                        criticalDamage += stats.getCriticalDamage();
                        blockAmount += stats.getBlockAmount();
                        blockRate += stats.getBlockRate();
                        hitRate += stats.getHitRate();
                        dodgeRate += stats.getDodgeRate();

                        for (final AbilityWeapon abilityWeapon : mapAbilityWeaponBonus.keySet()) {
                            final int grade = mapAbilityWeaponBonus.get(abilityWeapon);
                            mapAbilityWeapon.put(abilityWeapon, mapAbilityWeapon.getOrDefault(abilityWeapon, 0) + grade);
                        }
                    }
                }
            }
        }
        final ItemSetBonusEffectStats effectStats = new ItemSetBonusEffectStats(additionalDamage, percentDamage, penetration, pvpDamage, pveDamage, additionalDefense, percentDefense, health, healthRegen, staminaMax, staminaRegen, attackAoERadius, attackAoEDamage, pvpDefense, pveDefense, criticalChance, criticalDamage, blockAmount, blockRate, hitRate, dodgeRate);
        return new ItemSetBonusEffectEntity(effectStats, mapAbilityWeapon);
    }

    public final void updateItemSet(final LivingEntity entity) {
        this.updateItemSet(entity, true);
    }

    public final void updateItemSet(final LivingEntity entity, final boolean checkPlayerInventory) {
        this.updateItemSet(entity, checkPlayerInventory, null);
    }

    public final void updateItemSet(final LivingEntity entity, final boolean checkPlayerInventory, final Inventory inventory) {
        final MainConfig mainConfig = MainConfig.getInstance();
        if (entity != null) {
            final String keyLine = MainConfig.KEY_SET_LINE;
            final String keySetComponentSelf = MainConfig.KEY_SET_COMPONENT_SELF;
            final String keySetComponentOther = MainConfig.KEY_SET_COMPONENT_OTHER;
            final String loreBonusActive = mainConfig.getSetLoreBonusActive();
            final String loreBonusInactive = mainConfig.getSetLoreBonusInactive();
            final String loreComponentActive = mainConfig.getSetLoreComponentActive();
            final String loreComponentInactive = mainConfig.getSetLoreComponentInactive();
            final HashMap<Slot, ItemSetComponent> mapItemSetComponent = this.getMapItemComponent(entity);
            final HashMap<ItemSet, Integer> mapItemSetTotal = new HashMap<ItemSet, Integer>();
            final Collection<ItemSetComponent> allItemSetComponent = mapItemSetComponent.values();
            final Set<ItemStack> contents = new HashSet<ItemStack>();

            for (final Slot slot : Slot.values()) {
                final ItemStack item = Bridge.getBridgeEquipment().getEquipment(entity, slot);
                if (item != null) {
                    contents.add(item);
                    if (mapItemSetComponent.containsKey(slot)) {
                        final ItemSet itemSet = mapItemSetComponent.get(slot).getItemSet();
                        if (itemSet != null) {
                            mapItemSetTotal.put(itemSet, mapItemSetTotal.getOrDefault(itemSet, 0) + 1);
                        }
                    }
                }
            }

            if (entity instanceof Player) {
                final Player player = (Player) entity;
                if (checkPlayerInventory) {
                    final PlayerInventory playerInventory = player.getInventory();
                    final ItemStack itemCursor = player.getItemOnCursor();
                    if (itemCursor != null && itemCursor.getType() != org.bukkit.Material.AIR) contents.add(itemCursor);
                    for (final ItemStack content : playerInventory.getContents()) {
                        if (content != null && content.getType() != org.bukkit.Material.AIR) contents.add(content);
                    }
                }
                if (inventory != null) {
                    for (final ItemStack content : inventory.getContents()) {
                        if (content != null && content.getType() != org.bukkit.Material.AIR) contents.add(content);
                    }
                }
            }

            for (final ItemStack item2 : contents) {
                final ItemSetComponent itemSetComponent2 = this.getItemComponent(item2);
                if (itemSetComponent2 != null) {
                    final ItemSet itemSet2 = itemSetComponent2.getItemSet();
                    if (itemSet2 == null) continue;

                    final String name = itemSet2.getName();
                    final int total2 = mapItemSetTotal.getOrDefault(itemSet2, 0);
                    final int maxComponent = itemSet2.getTotalComponent();
                    final List<String> lores = EquipmentUtil.getLores(item2);
                    final List<String> loresBonus = new ArrayList<String>();
                    final List<String> loresComponent = new ArrayList<String>();
                    final List<Integer> bonusAmountIDs = new ArrayList<Integer>(itemSet2.getBonusAmountIDs());
                    final HashMap<String, String> mapPlaceholder = new HashMap<String, String>();
                    List<String> loresSet = mainConfig.getSetFormat();
                    Collections.sort(bonusAmountIDs);

                    lores.removeIf(lore -> lore.contains(keyLine));

                    for (final ItemSetComponent partComponent : itemSet2.getAllItemSetComponent()) {
                        String formatComponent = mainConfig.getSetFormatComponent();
                        String replacementKeyLore;
                        boolean isActive = allItemSetComponent.contains(partComponent);
                        String statusLore = isActive ? loreComponentActive : loreComponentInactive;

                        formatComponent = statusLore + formatComponent;
                        String prefix = partComponent.equals(itemSetComponent2) ? keySetComponentSelf : keySetComponentOther;
                        replacementKeyLore = prefix + statusLore + partComponent.getKeyLore() + prefix + statusLore;

                        mapPlaceholder.clear();
                        mapPlaceholder.put("item_set_component_id", partComponent.getID());
                        mapPlaceholder.put("item_set_component_keylore", replacementKeyLore);
                        loresComponent.add(TextUtil.placeholder(mapPlaceholder, formatComponent, "<", ">"));
                    }

                    for (final int bonusAmountID : bonusAmountIDs) {
                        final ItemSetBonus partBonus = itemSet2.getItemSetBonus(bonusAmountID);
                        for (final String description : partBonus.getDescription()) {
                            String formatBonus = (total2 >= bonusAmountID ? loreBonusActive : loreBonusInactive) + mainConfig.getSetFormatBonus();
                            mapPlaceholder.clear();
                            mapPlaceholder.put("item_set_bonus_amount", String.valueOf(bonusAmountID));
                            mapPlaceholder.put("item_set_bonus_description", description);
                            loresBonus.add(TextUtil.placeholder(mapPlaceholder, formatBonus, "<", ">"));
                        }
                    }

                    mapPlaceholder.clear();
                    mapPlaceholder.put("item_set_name", name);
                    mapPlaceholder.put("item_set_total", String.valueOf(total2));
                    mapPlaceholder.put("item_set_max", String.valueOf(maxComponent));
                    mapPlaceholder.put("list_item_set_component", TextUtil.convertListToString(loresComponent, "\n"));
                    mapPlaceholder.put("list_item_set_bonus", TextUtil.convertListToString(loresBonus, "\n"));

                    loresSet = TextUtil.placeholder(mapPlaceholder, loresSet, "<", ">");
                    loresSet = TextUtil.expandList(loresSet, "\n");
                    for (String s : loresSet) {
                        lores.add(keyLine + s);
                    }
                    EquipmentUtil.setLores(item2, lores);
                }
            }

            if (entity instanceof Player) {
                final Player player = (Player) entity;
                // 1.21.4: getOpenInventory() returns AbstractContainerMenu; getType() available via InventoryView cast
                final org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
                if (!player.getGameMode().equals(GameMode.CREATIVE) || openView.getType() != InventoryType.CREATIVE) {
                    player.updateInventory();
                }
            }
        }
    }
}