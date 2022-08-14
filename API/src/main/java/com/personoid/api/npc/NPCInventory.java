package com.personoid.api.npc;

import com.personoid.api.events.NPCPickupItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NPCInventory {
    private static final int MAX_SIZE = 36;

    private final NPC npc;
    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] armorContents = new ItemStack[4];
    private final ItemStack[] hotbar = new ItemStack[9];
    private ItemStack offhand;
    private int selectedSlot = 0;

    public NPCInventory(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        handleItemPickup();
        npc.getEntity().setItemInHand(hotbar[selectedSlot]);
    }

    private void handleItemPickup() {
        for (Entity entity : npc.getEntity().getNearbyEntities(2, 1, 2)) {
            if (entity instanceof Item item && getContents().size() < MAX_SIZE) {
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
                    //Bukkit.broadcastMessage("diff: " + diff);
                    if (diff != item.getItemStack().getAmount()) {
                        new Packets.EntityTakeItem(item.getEntityId(), npc.getEntity().getEntityId(), item.getItemStack().getAmount()).send(npc);
                        if (diff == 0) {
                            item.remove();
                        } else {
                            item.getItemStack().setAmount(item.getItemStack().getAmount() - diff);
                        }
                    }
                }
            }
        }
    }

    public void select(int slot) {
        if (slot > 8 || slot < 0) {
            throw new IndexOutOfBoundsException("Slot index must be between than 0-8 (was " + slot + ")");
        }
        selectedSlot = slot;
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
    }

    // armour OR armor :)

    public void setHelmet(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.HELMET.index] = clone;
        npc.getEntity().getInventory().setHelmet(itemStack);
    }

    public void setChestplate(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.CHESTPLATE.index] = itemStack.clone();
        npc.getEntity().getInventory().setChestplate(itemStack);
    }

    public void setLeggings(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.LEGGINGS.index] = itemStack.clone();
        npc.getEntity().getInventory().setLeggings(itemStack);
    }

    public void setBoots(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.BOOTS.index] = itemStack.clone();
        npc.getEntity().getInventory().setBoots(itemStack);
    }

    public void setOffhand(ItemStack itemStack) {
        removeItem(itemStack);
        offhand = itemStack.clone();
        npc.getEntity().getInventory().setItemInOffHand(itemStack);
    }

    // contents

    public int addItem(ItemStack itemStack) {
        int diff = addItem(hotbar, itemStack, new HashSet<>());
        return diff == itemStack.getAmount() ? addItem(contents, itemStack, new HashSet<>()) : diff;
    }

    private int addItem(ItemStack[] contents, ItemStack itemStack, Set<Integer> excludedSlots) {
        // FIXME: keeps on picking up items when full
        for (int i = 0; i < contents.length; i++) {
            if (excludedSlots.contains(i)) continue;
            if (contents[i] != null && contents[i].getType() == itemStack.getType()) {
                int diff = (contents[i].getAmount() + itemStack.getAmount()) - contents[i].getMaxStackSize();
                contents[i].setAmount(Math.min(contents[i].getAmount() + itemStack.getAmount(), itemStack.getMaxStackSize()));
                if (diff == 0 || i == contents.length - 1) {
                    return diff;
                } else if (diff > 0) {
                    excludedSlots.add(i);
                    return addItem(contents, new ItemStack(itemStack.getType(), diff), excludedSlots);
                }
            } else if (contents[i] == null) {
                ItemStack stack = itemStack.clone();
                stack.setAmount(Math.min(itemStack.getAmount(), itemStack.getMaxStackSize()));
                contents[i] = stack;
                return 0;
            }
        }
        return itemStack.getAmount();
    }

    public void removeItem(ItemStack itemStack) {
        for (int i = 0; i < hotbar.length; i++) {
            if (equals(hotbar[i], itemStack)) {
                hotbar[i] = null;
                return;
            }
        }
        for (int i = 0; i < contents.length; i++) {
            if (equals(hotbar[i], itemStack)) {
                hotbar[i] = null;
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

    private boolean equals(ItemStack a, ItemStack b) {
        ItemStack sameCount = a.clone();
        sameCount.setAmount(b.getAmount());
        return sameCount.equals(b);
    }

    public List<ItemStack> getContents() {
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < this.contents.length - 1; i++) {
            if (this.contents[i] != null) {
                contents.add(this.contents[i]);
            }
        }
        return contents;
    }

    public enum SlotIndex {
        HELMET(0),
        CHESTPLATE(1),
        LEGGINGS(2),
        BOOTS(3)

        ;

        final int index;

        SlotIndex(int index){
            this.index = index;
        }
    }
}
