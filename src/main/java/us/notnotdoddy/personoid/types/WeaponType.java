package us.notnotdoddy.personoid.types;

import org.bukkit.Material;

public enum WeaponType {
/*    TURTLE_HELMET(Material.TURTLE_HELMET, 2),

    LEATHER_HELMET(Material.TURTLE_HELMET, 1),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, 3),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, 2),
    LEATHER_BOOTS(Material.LEATHER_BOOTS, 1),

    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, 2),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, 5),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, 4),
    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, 1),

    IRON_HELMET(Material.IRON_HELMET, 2),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, 6),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, 5),
    IRON_BOOTS(Material.IRON_BOOTS, 2),

    GOLDEN_HELMET(Material.GOLDEN_HELMET, 2),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, 5),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, 3),
    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, 1),

    DIAMOND_HELMET(Material.DIAMOND_HELMET, 3),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, 8),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, 6),
    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, 3),

    NETHERITE_HELMET(Material.NETHERITE_HELMET, 3),
    NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, 8),
    NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, 6),
    NETHERITE_BOOTS(Material.NETHERITE_BOOTS, 3),*/
    ;

    final Material material;
    final double hitPoints;

    WeaponType(Material material, int hitPoints){
        this.material = material;
        this.hitPoints = (((hitPoints * 0.1875)/20)*10)+1;
    }

    public double getHitPoints() {
        return hitPoints;
    }
}
