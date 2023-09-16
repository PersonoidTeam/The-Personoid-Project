package com.personoid.api.npc;

import com.personoid.api.events.NPCPickupItemEvent;
import com.personoid.nms.packet.Packets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;

public class NPCInventory {
    private static final int MAX_SIZE = 36;

    private final NPC npc;
    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] armorContents = new ItemStack[4];
    private final ItemStack[] hotbar = new ItemStack[9];
    private ItemStack offhand;
    private int selectedSlot = 0;
    private final Map<String, Consumer<ItemStack>> itemPickupListeners = new HashMap<>();

    public NPCInventory(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        handleItemPickup();
    }

    private void handleItemPickup() {
        for (Entity entity : npc.getEntity().getNearbyEntities(2, 1, 2)) {
            if (entity instanceof Item && getContents().length < MAX_SIZE) {
                Item item = (Item) entity;
                if (item.getPickupDelay() == 0) {
                    if (item.getThrower() != null && Bukkit.getEntity(item.getThrower()) != null) {
                        LivingEntity thrower = (LivingEntity) Bukkit.getEntity(item.getThrower());
                        NPCPickupItemEvent event = new NPCPickupItemEvent(npc, item, thrower);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) return;
                    } else {
                        NPCPickupItemEvent event = new NPCPickupItemEvent(npc, item);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) return;
                    }
                    int diff = this.addItem(item.getItemStack());
                    if (diff != item.getItemStack().getAmount()) {
                        Packets.entityTakeItem(item.getEntityId(), npc.getEntity().getEntityId(), item.getItemStack().getAmount()).send();
                        if (diff == 0) {
                            item.remove();
                        } else {
                            item.getItemStack().setAmount(item.getItemStack().getAmount() - diff);
                        }
                    }
                    for (Consumer<ItemStack> listener : itemPickupListeners.values()) {
                        listener.accept(item.getItemStack());
                    }
                }
            }
        }
    }

    public void addPickupListener(String id, Consumer<ItemStack> listener) {
        itemPickupListeners.put(id, listener);
    }

    public void removePickupListener(String id) {
        itemPickupListeners.remove(id);
    }

    public void updateVisuals() {
        Packets.entityEquipment(npc.getEntityId(), new HashMap<EquipmentSlot, ItemStack>() {{
            put(EquipmentSlot.HAND, hotbar[selectedSlot]);
            put(EquipmentSlot.OFF_HAND, offhand);
            put(EquipmentSlot.HEAD, armorContents[SlotIndex.HELMET.index]);
            put(EquipmentSlot.CHEST, armorContents[SlotIndex.CHESTPLATE.index]);
            put(EquipmentSlot.LEGS, armorContents[SlotIndex.LEGGINGS.index]);
            put(EquipmentSlot.FEET, armorContents[SlotIndex.BOOTS.index]);
        }}).send();
    }

    public void select(int slot) {
        if (slot > 8 || slot < 0) {
            throw new IndexOutOfBoundsException("Slot index must be between than 0-8 (was " + slot + ")");
        }
        selectedSlot = slot;
        npc.getEntity().getInventory().setItemInMainHand(hotbar[selectedSlot]);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.HAND, hotbar[slot])).send();
    }

    public void drop() {
        drop(selectedSlot);
    }

    // TODO: make also work for not-hotbar slots
    public void drop(int slot) {
        // drop item and remove from inventory
        ItemStack itemStack = hotbar[slot];
        if (itemStack != null) {
            Item item = npc.getEntity().getWorld().dropItem(npc.getEntity().getEyeLocation(), itemStack);
            item.setPickupDelay(40);
            Vector vel = npc.getLocation().getDirection().normalize().multiply(0.3).add(new Vector(0F, 0.1F, 0F));
            item.setVelocity(vel);
            removeItem(itemStack);
        }
        npc.getEntity().getInventory().setItemInMainHand(hotbar[selectedSlot]);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.HAND, hotbar[slot])).send();
    }

    public void setHelmet(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.HELMET.index] = clone;
        npc.getEntity().getInventory().setHelmet(itemStack);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.HEAD, itemStack)).send();
    }

    public void setChestplate(ItemStack itemStack) {
        removeItem(itemStack);
        armorContents[SlotIndex.CHESTPLATE.index] = itemStack.clone();
        npc.getEntity().getInventory().setChestplate(itemStack);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.CHEST, itemStack)).send();
    }

    public void setLeggings(ItemStack itemStack) {
        removeItem(itemStack);
        armorContents[SlotIndex.LEGGINGS.index] = itemStack.clone();
        npc.getEntity().getInventory().setLeggings(itemStack);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.LEGS, itemStack)).send();
    }

    public void setBoots(ItemStack itemStack) {
        removeItem(itemStack);
        armorContents[SlotIndex.BOOTS.index] = itemStack.clone();
        npc.getEntity().getInventory().setBoots(itemStack);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.FEET, itemStack)).send();
    }

    public void setOffhand(ItemStack itemStack) {
        removeItem(itemStack);
        offhand = itemStack.clone();
        npc.getEntity().getInventory().setItemInOffHand(itemStack);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.OFF_HAND, itemStack)).send();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public ItemStack getSelectedItem() {
        return hotbar[selectedSlot];
    }

    public ItemStack getOffhandItem() {
        return offhand;
    }

    public int addItem(ItemStack itemStack) {
        int remainingAmount = addItemToInventory(hotbar, itemStack);

        if (remainingAmount > 0) {
            remainingAmount = addItemToInventory(contents, new ItemStack(itemStack.getType(), remainingAmount));
        }

        npc.getEntity().getInventory().setItemInMainHand(hotbar[selectedSlot]);
        Packets.entityEquipment(npc.getEntityId(), Collections.singletonMap(EquipmentSlot.HAND, hotbar[selectedSlot])).send();

        return remainingAmount;
    }

    private int addItemToInventory(ItemStack[] inventory, ItemStack itemStack) {
        int remainingAmount = itemStack.getAmount();

        for (int i = 0; i < inventory.length && remainingAmount > 0; i++) {
            if (inventory[i] == null) {
                int amountToAdd = Math.min(remainingAmount, itemStack.getMaxStackSize());
                inventory[i] = new ItemStack(itemStack.getType(), amountToAdd);
                remainingAmount -= amountToAdd;
            } else if (inventory[i].getType() == itemStack.getType()) {
                int spaceAvailable = itemStack.getMaxStackSize() - inventory[i].getAmount();
                if (spaceAvailable > 0) {
                    int amountToAdd = Math.min(remainingAmount, spaceAvailable);
                    inventory[i].setAmount(inventory[i].getAmount() + amountToAdd);
                    remainingAmount -= amountToAdd;
                }
            }
        }

        return remainingAmount;
    }

    public void removeItem(ItemStack itemStack) {
        for (int i = 0; i < hotbar.length; i++) {
            if (equals(hotbar[i], itemStack)) {
                hotbar[i] = null;
                return;
            }
        }
        for (int i = 0; i < contents.length; i++) {
            if (equals(contents[i], itemStack)) {
                contents[i] = null;
                return;
            }
        }
    }

    public boolean contains(ItemStack itemStack) {
        for (ItemStack item : hotbar) {
            if (equals(item, itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Material material) {
        for (ItemStack item : hotbar) {
            if (item != null && item.getType() == material) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getFirst(Material material) {
        for (ItemStack item : hotbar) {
            if (item != null && item.getType() == material) {
                return item;
            }
        }
        return null;
    }

    private boolean equals(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        ItemStack sameCount = a.clone();
        sameCount.setAmount(b.getAmount());
        return sameCount.equals(b);
    }

/*    public List<ItemStack> getContents() {
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < this.contents.length - 1; i++) {
            if (this.contents[i] != null) {
                contents.add(this.contents[i]);
            }
        }
        return contents;
    }

    public List<ItemStack> getHotbar() {
        List<ItemStack> hotbar = new ArrayList<>();
        for (int i = 0; i < this.hotbar.length - 1; i++) {
            if (this.hotbar[i] != null) {
                hotbar.add(this.hotbar[i]);
            }
        }
        return hotbar;
    }*/

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getHotbar() {
        return hotbar;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    public ItemStack[] getInventoryContents() {
        ItemStack[] inventoryContents = new ItemStack[hotbar.length + contents.length];
        System.arraycopy(hotbar, 0, inventoryContents, 0, hotbar.length);
        System.arraycopy(contents, 0, inventoryContents, hotbar.length, contents.length);
        return inventoryContents;
    }

    public enum SlotIndex {
        HELMET(0),
        CHESTPLATE(1),
        LEGGINGS(2),
        BOOTS(3);

        final int index;

        SlotIndex(int index){
            this.index = index;
        }
    }
}
