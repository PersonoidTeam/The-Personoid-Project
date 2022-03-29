package com.personoid.npc;

import com.personoid.events.NPCPickupItemEvent;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NPCInventory extends NPCTickingComponent {
    private static final int MAX_SIZE = 36;

    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] armorContents = new ItemStack[4];
    private final ItemStack[] hotbar = new ItemStack[9];

    private int mainHandIndex = 0;
    private int offHandIndex = -1;

    public NPCInventory(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        handleItemPickup();
    }

    private void handleItemPickup() {
        for (Entity entity : getNPC().getBukkitEntity().getNearbyEntities(2, 1, 2)) {
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
                    PacketUtils.send(new ClientboundTakeItemEntityPacket(item.getEntityId(),
                            npc.getBukkitEntity().getEntityId(), item.getItemStack().getAmount()));
                    this.addItem(item.getItemStack());
                    item.remove();
                }
            }
        }
    }

    // armour OR armor :)

    public void setHelmet(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.HELMET.index] = clone;
    }

    public void setChestplate(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.CHESTPLATE.index] = itemStack.clone();
    }

    public void setLeggings(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.LEGGINGS.index] = itemStack.clone();
    }

    public void setBoots(ItemStack itemStack){
        removeItem(itemStack);
        armorContents[SlotIndex.BOOTS.index] = itemStack.clone();
    }

    // contents

    public void addItem(ItemStack itemStack) {
        for (int i = 0; i < hotbar.length; i++) {
            if (hotbar[i] == null) {
                hotbar[i] = itemStack;
                return;
            }
        }
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                contents[i] = itemStack;
                return;
            }
        }
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
