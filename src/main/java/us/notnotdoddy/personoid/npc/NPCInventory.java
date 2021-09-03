package us.notnotdoddy.personoid.npc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;

import java.util.HashMap;

public class NPCInventory {
    private final PersonoidNPC npc;
    private final Inventory inventory;
    private ItemStack[] armor = new ItemStack[4];
    private ItemStack[] extra = new ItemStack[4];
    private final ItemStack[] hands = new ItemStack[2];

    public NPCInventory(PersonoidNPC npc) {
        this.npc = npc;
        inventory = Bukkit.createInventory(npc, 36);
    }

    public PersonoidNPC getNPC() {
        return npc;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @NotNull
    public ItemStack[] getArmorContents() {
        return armor;
    }

    @NotNull
    public ItemStack[] getExtraContents() {
        return extra;
    }

    @Nullable
    public ItemStack getHelmet() {
        return armor[0];
    }

    @Nullable
    public ItemStack getChestplate() {
        return armor[1];
    }

    @Nullable
    public ItemStack getLeggings() {
        return armor[2];
    }

    @Nullable
    public ItemStack getBoots() {
        return armor[3];
    }
    
    public int getSize() {
        return inventory.getSize();
    }
    
    public int getMaxStackSize() {
        return inventory.getMaxStackSize();
    }
    
    public void setMaxStackSize(int size) {
        inventory.setMaxStackSize(size);
    }

    @Nullable
    public ItemStack getItem(int index) {
        return inventory.getItem(index);
    }

    public void setItem(int index, @Nullable ItemStack item) {
        inventory.setItem(index, item);
    }

    @NotNull
    public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return inventory.addItem(items);
    }

    @NotNull
    public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return inventory.removeItem(items);
    }

    @NotNull
    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        inventory.setContents(items);
    }

    @NotNull
    public ItemStack[] getStorageContents() {
        return inventory.getStorageContents();
    }

    public void transferToOtherInventory(Inventory transfer, int slot, Material type, int amount){
        transfer.setItem(slot, new ItemStack(type, amount));
        removeMaterialCount(type, amount);
    }

    public void removeMaterialCount(Material material, int amountToRemove){
        int removedAmount = 0;
        ItemStack[] itemStacks = getContents().clone();
        while (removedAmount < amountToRemove){
            for (ItemStack itemStack : itemStacks) {
                if (itemStack.getType().equals(material)) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    removedAmount++;
                    break;
                }
            }
        }
    }

    public int getAmountOf(ResourceTypes resourceTypes){
        int amount = 0;
        for (ItemStack itemStack : inventory.getContents()){
            if (itemStack != null){
                if (resourceTypes.contains(itemStack.getType())){
                    amount += itemStack.getAmount();
                }
            }
        }
        return amount;
    }

    public int getAmountOf(Material material){
        int amount = 0;
        for (ItemStack itemStack : inventory.getContents()){
            if (itemStack != null){
                if (material.equals(itemStack.getType())){
                    amount += itemStack.getAmount();
                }
            }
        }
        return amount;
    }

    public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        inventory.setContents(items);
    }
    
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return inventory.contains(material);
    }

    public boolean contains(@Nullable ItemStack item) {
        return inventory.contains(item);
    }

    public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
        return inventory.contains(material, amount);
    }
    
    public boolean contains(@Nullable ItemStack item, int amount) {
        return inventory.contains(item, amount);
    }

    public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        return inventory.containsAtLeast(item, amount);
    }

    @NotNull
    public HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        return inventory.all(material);
    }

    @NotNull
    public HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        return inventory.all(item);
    }

    public int first(@NotNull Material material) throws IllegalArgumentException {
        return inventory.first(material);
    }

    public int first(@NotNull ItemStack item) {
        return inventory.first(item);
    }

    public int firstEmpty() {
        return inventory.firstEmpty();
    }

    public boolean isEmpty() {
        return inventory.isEmpty();
    }
    
    public void remove(@NotNull Material material) throws IllegalArgumentException {
        inventory.remove(material);
    }

    public void remove(@NotNull ItemStack item) {
        inventory.remove(item);
    }
    
    public void clear(int index) {
        inventory.clear(index);
    }

    public void clear() {
        inventory.clear();
    }

    @NotNull
    public InventoryType getType() {
        return InventoryType.PLAYER;
    }

    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
        switch (slot) {
            case HAND -> setItemInMainHand(item);
            case OFF_HAND -> setItemInOffHand(item);
            case FEET -> setBoots(item);
            case LEGS -> setLeggings(item);
            case CHEST -> setChestplate(item);
            case HEAD -> setHelmet(item);
        }
    }

    public ItemStack getItem(@NotNull EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> getItemInMainHand();
            case OFF_HAND -> getItemInOffHand();
            case FEET -> getBoots();
            case LEGS -> getLeggings();
            case CHEST -> getChestplate();
            case HEAD -> getHelmet();
        };
    }

    public void setArmorContents(@Nullable ItemStack[] items) {
        armor = items;
        npc.getLivingEntity().getEquipment().setArmorContents(items);
    }

    public void setExtraContents(@Nullable ItemStack[] items) {
        extra = items;
    }
    
    public void setHelmet(@Nullable ItemStack helmet) {
        armor[0] = helmet;
        npc.getLivingEntity().getEquipment().setHelmet(helmet);
    }

    public void setChestplate(@Nullable ItemStack chestplate) {
        armor[1] = chestplate;
        npc.getLivingEntity().getEquipment().setChestplate(chestplate);
    }

    public void setLeggings(@Nullable ItemStack leggings) {
        armor[2] = leggings;
        npc.getLivingEntity().getEquipment().setLeggings(leggings);
    }

    public void setBoots(@Nullable ItemStack boots) {
        armor[3] = boots;
        npc.getLivingEntity().getEquipment().setBoots(boots);
    }

    @NotNull
    public ItemStack getItemInMainHand() {
        return hands[0];
    }

    public void setItemInMainHand(@Nullable ItemStack item) {
        hands[0] = item;
        npc.getLivingEntity().getEquipment().setItemInMainHand(item);
    }

    @NotNull
    public ItemStack getItemInOffHand() {
        return hands[1];
    }

    public void setItemInOffHand(@Nullable ItemStack item) {
        hands[1] = item;
        npc.getLivingEntity().getEquipment().setItemInOffHand(item);
    }
    
    public int getHeldItemSlot() {
        return 0;
    }
    
    public void setHeldItemSlot(int slot) {

    }
}
