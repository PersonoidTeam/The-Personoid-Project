package com.personoid.npc;

import com.personoid.npc.components.NPCComponent;
import org.bukkit.inventory.ItemStack;

public class NPCInventory extends NPCComponent {
    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] armorContents = new ItemStack[4];
    private final ItemStack[] hotbar = new ItemStack[9];

    private int mainHandIndex = 0;
    private int offHandIndex = -1;

    public NPCInventory(NPC npc) {
        super(npc);
    }

    // armour OR armor :)

    public void setHelmet(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.HELMET.index] = clone;
    }

    public void setChestplate(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.CHESTPLATE.index] = clone;
    }

    public void setLeggings(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.LEGGINGS.index] = clone;
    }

    public void setBoots(ItemStack itemStack){
        ItemStack clone = itemStack.clone();
        removeItem(itemStack);
        armorContents[SlotIndex.BOOTS.index] = clone;
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
