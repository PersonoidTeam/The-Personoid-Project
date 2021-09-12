package us.notnotdoddy.personoid.types;

import org.bukkit.Material;
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
}
