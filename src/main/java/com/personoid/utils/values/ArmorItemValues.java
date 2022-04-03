package com.personoid.utils.values;

public enum ArmorItemValues {
    TURTLE_SHELL(2),

    LEATHER_HELMET(1),
    LEATHER_CHESTPLATE(3),
    LEATHER_LEGGINGS(2),
    LEATHER_BOOTS(1),

    CHAINMAIL_HELMET(2),
    CHAINMAIL_CHESTPLATE(5),
    CHAINMAIL_LEGGINGS(4),
    CHAINMAIL_BOOTS(1),

    IRON_HELMET(2),
    IRON_CHESTPLATE(6),
    IRON_LEGGINGS(5),
    IRON_BOOTS(2),

    GOLDEN_HELMET(2),
    GOLDEN_CHESTPLATE(5),
    GOLDEN_LEGGINGS(3),
    GOLDEN_BOOTS(1),

    DIAMOND_HELMET(3),
    DIAMOND_CHESTPLATE(8),
    DIAMOND_LEGGINGS(6),
    DIAMOND_BOOTS(3),

    NETHERITE_HELMET(3),
    NETHERITE_CHESTPLATE(8),
    NETHERITE_LEGGINGS(6),
    NETHERITE_BOOTS(3),
    ;

    final double healthMultiplier;

    ArmorItemValues(int armorPoints){
        healthMultiplier = (((armorPoints * 0.1875)/20)*10)+1;
    }

    public double getHealthMultiplier() {
        return healthMultiplier;
    }
}
