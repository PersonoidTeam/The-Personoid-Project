package com.personoid.api.npc;

import com.personoid.api.events.NPCPickupItemEvent;
import com.personoid.api.utils.packet.Packets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class NewInventory implements PlayerInventory {
    private final NPC npc;
    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] armorContents = new ItemStack[4];
    private final ItemStack[] hotbar = new ItemStack[9];
    private int selectedSlot;

    public NewInventory(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        handleItemPickup();
        npc.getEntity().setItemInHand(hotbar[selectedSlot]);
    }

    private void handleItemPickup() {
        for (Entity entity : npc.getEntity().getNearbyEntities(2, 1, 2)) {
            if (entity instanceof Item item && getContents().length < 27) { //27 == MAX_SIZE
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
                    int diff = addItem(item.getItemStack()).size();
                    //Bukkit.broadcastMessage("diff: " + diff);
                    if (diff != item.getItemStack().getAmount()) {
/*                        PacketUtils.send(new ClientboundTakeItemEntityPacket(item.getEntityId(),
                                npc.getBukkitEntity().getEntityId(), item.getItemStack().getAmount()));*/
                        new Packets.EntityTakeItem(item.getEntityId(), npc.getEntity().getEntityId(), item.getItemStack().getAmount()).send();
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

    public void drop(int slot) {
        // drop item and remove from inventory
        ItemStack itemStack = getContents()[slot];
        if (itemStack != null) {
            Item item = npc.getEntity().getWorld().dropItem(npc.getEntity().getEyeLocation(), itemStack);
            item.setPickupDelay(40);
            Vector vel = npc.getLocation().getDirection().normalize().multiply(0.3).add(new Vector(0F, 0.1F, 0F));
            item.setVelocity(vel);
            removeItem(itemStack);
        }
    }

    @NotNull
    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[0];
    }

    @NotNull
    @Override
    public ItemStack[] getExtraContents() {
        return new ItemStack[0];
    }

    @Nullable
    @Override
    public ItemStack getHelmet() {
        return null;
    }

    @Nullable
    @Override
    public ItemStack getChestplate() {
        return null;
    }

    @Nullable
    @Override
    public ItemStack getLeggings() {
        return null;
    }

    @Nullable
    @Override
    public ItemStack getBoots() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Nullable
    @Override
    public ItemStack getItem(int index) {
        return null;
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item) {

    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public ItemStack[] getContents() {
        return new ItemStack[0];
    }

    @Override
    public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {

    }

    @NotNull
    @Override
    public ItemStack[] getStorageContents() {
        return new ItemStack[0];
    }

    @Override
    public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item) {
        return false;
    }

    @Override
    public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        return false;
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        return null;
    }

    @Override
    public int first(@NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void remove(@NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(@NotNull ItemStack item) {

    }

    @Override
    public void clear(int index) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public List<HumanEntity> getViewers() {
        return null;
    }

    @NotNull
    @Override
    public InventoryType getType() {
        return null;
    }

    @Override
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {

    }

    @Nullable
    @Override
    public ItemStack getItem(@NotNull EquipmentSlot slot) {
        return null;
    }

    @Override
    public void setArmorContents(@Nullable ItemStack[] items) {

    }

    @Override
    public void setExtraContents(@Nullable ItemStack[] items) {

    }

    @Override
    public void setHelmet(@Nullable ItemStack helmet) {

    }

    @Override
    public void setChestplate(@Nullable ItemStack chestplate) {

    }

    @Override
    public void setLeggings(@Nullable ItemStack leggings) {

    }

    @Override
    public void setBoots(@Nullable ItemStack boots) {

    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return null;
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack item) {

    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return null;
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack item) {

    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return null;
    }

    @Override
    public void setItemInHand(@Nullable ItemStack stack) {

    }

    @Override
    public int getHeldItemSlot() {
        return 0;
    }

    @Override
    public void setHeldItemSlot(int slot) {

    }

    @Nullable
    @Override
    public HumanEntity getHolder() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator(int index) {
        return null;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return null;
    }
}
