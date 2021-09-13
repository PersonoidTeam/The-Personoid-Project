package us.notnotdoddy.personoid.types;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;

public class Types {
    public static boolean isArmor(Material material) {
        for (ArmorType type : ArmorType.values()) {
            if (type.material.equals(material)) {
                return true;
            }
        }
        return false;
    }

    public static EquipmentSlot getArmorSlotType(Material material) {
        for (ArmorType type : ArmorType.values()) {
            if (type.material.equals(material)) {
                return type.slot;
            }
        }
        return null;
    }

    public static boolean isWeapon(Material material) {
        for (WeaponType type : WeaponType.values()) {
            if (type.material.equals(material)) {
                return true;
            }
        }
        return false;
    }

    public static Sound getArmorEquipSound(Material material) {
        String key = material.getKey().getKey();
        if (key.contains("leather")) {
            return Sound.ITEM_ARMOR_EQUIP_LEATHER;
        } else if (key.contains("iron")) {
            return Sound.ITEM_ARMOR_EQUIP_IRON;
        } else if (key.contains("chainmail")) {
            return Sound.ITEM_ARMOR_EQUIP_CHAIN;
        } else if (key.contains("gold")) {
            return Sound.ITEM_ARMOR_EQUIP_GOLD;
        } else if (key.contains("diamond")) {
            return Sound.ITEM_ARMOR_EQUIP_DIAMOND;
        } else if (key.contains("netherite")) {
            return Sound.ITEM_ARMOR_EQUIP_NETHERITE;
        } else if (key.contains("elytra")) {
            return Sound.ITEM_ARMOR_EQUIP_ELYTRA;
        }
        return null;
    }
}
