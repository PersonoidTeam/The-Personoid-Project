package com.personoid.api.npc;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NPCFields {
    private Object base;
    private NPC npc;

    public void setBase(Object base) {
        this.base = base;
    }

    // region UTILS

    public void invoke(String methodName, Object... args) {
        try {
            Method method = getClass().getSuperclass().getMethod(methodName);
            method.invoke(this, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T invoke(Class<T> type, String methodName, Object... args) {
        try {
            Method method = getClass().getSuperclass().getMethod(methodName);
            return (T) method.invoke(this, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getField(Class<T> type, String fieldName, Object... args) {
        try {
            Field field = getClass().getSuperclass().getField(fieldName);
            field.setAccessible(true);
            return (T) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void setField(String fieldName, T value) {
        try {
            Field field = getClass().getSuperclass().getField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void modInt(String fieldName, int modifier) {
        try {
            Field field = getClass().getSuperclass().getField(fieldName);
            field.setAccessible(true);
            field.set(this, getField(int.class, fieldName) + modifier);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion

    // NPC METHODS + VARIABLES
    private double lastYIncrease;

    private void tick() {
        Bukkit.broadcastMessage("tick");
        invoke("loadChunks");
        invoke("tick");
        if (!invoke(Boolean.class, "isAlive")) return;
        int aliveTicks = getField(int.class, "aliveTicks");
        setField("aliveTicks", aliveTicks + 1);

        double yPos = invoke(int.class, "getY");

        if (aliveTicks == 1) {
            lastYIncrease = yPos;
        }

        if (getField(int.class, "hurtTime") > 0) modInt("hurtTime", -1);

        if (invoke(boolean.class, "checkGround")) {
            if (getField(int.class, "groundTicks") < Integer.MAX_VALUE) {
                modInt("groundTicks", 1);
            }
        } else setField("groundTicks", 0);

        float health = invoke(float.class, "getHealth");
        float maxHealth = invoke(float.class, "getMaxHealth");
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth; // 0.1F = natual regen speed (full saturation)

        invoke("setHealth", amount);

        if (yPos < -64) {
            invoke("outOfWorld");
        }

        //fallDamageCheck();

        // FIXME: swimming not working

/*        if (inWater() && targetLoc.getY() - 1F < getLocation().getY()) {
            if (!isSwimming()) setSwimming(true);
        } else if (isSwimming()) {
            setSwimming(false);
        }*/

/*        for (Material material : itemCooldowns.keySet()) {
            int cooldown = itemCooldowns.get(material);
            if (cooldown > 0) itemCooldowns.put(material, cooldown - 1);
            else itemCooldowns.remove(material);
        }

        tickComponents();
        updatePose();*/
    }

/*    private void spawn() {
        npc.getClass().getMethod("setPos").invoke()
        nmsNPC.setPos(location.getX(), location.getY(), location.getZ());
        Packets.addPlayer(nmsNPC.getEntity()).send();
        nmsNPC.getLevel().addNewPlayer(nmsNPC);
    }*/
}
