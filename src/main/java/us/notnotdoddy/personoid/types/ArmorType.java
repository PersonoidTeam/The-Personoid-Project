package us.notnotdoddy.personoid.types;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public enum ArmorType {
    TURTLE_HELMET(Material.TURTLE_HELMET, EquipmentSlot.HEAD, 2),

    LEATHER_HELMET(Material.TURTLE_HELMET, EquipmentSlot.HEAD, 1),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, EquipmentSlot.CHEST, 3),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, EquipmentSlot.LEGS, 2),
    LEATHER_BOOTS(Material.LEATHER_BOOTS, EquipmentSlot.FEET, 1),

    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, EquipmentSlot.HEAD, 2),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST, 5),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS, 4),
    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, EquipmentSlot.FEET, 1),

    IRON_HELMET(Material.IRON_HELMET, EquipmentSlot.HEAD, 2),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, EquipmentSlot.CHEST, 6),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, EquipmentSlot.LEGS, 5),
    IRON_BOOTS(Material.IRON_BOOTS, EquipmentSlot.FEET, 2),

    GOLDEN_HELMET(Material.GOLDEN_HELMET, EquipmentSlot.HEAD, 2),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST, 5),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, EquipmentSlot.LEGS, 3),
    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, EquipmentSlot.FEET, 1),

    DIAMOND_HELMET(Material.DIAMOND_HELMET, EquipmentSlot.HEAD, 3),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST, 8),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, EquipmentSlot.LEGS, 6),
    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, EquipmentSlot.FEET, 3),

    NETHERITE_HELMET(Material.NETHERITE_HELMET, EquipmentSlot.HEAD, 3),
    NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, EquipmentSlot.CHEST, 8),
    NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, EquipmentSlot.LEGS, 6),
    NETHERITE_BOOTS(Material.NETHERITE_BOOTS, EquipmentSlot.FEET, 3),
    ;

    final Material material;
    final EquipmentSlot slot;
    final double healthMultiplier;

    ArmorType(Material material, EquipmentSlot slot, int armorPoints){
        this.material = material;
        this.slot = slot;
        healthMultiplier = (((armorPoints * 0.1875)/20)*10)+1;
    }

    public double getHealthMultiplier() {
        return healthMultiplier;
    }
}
