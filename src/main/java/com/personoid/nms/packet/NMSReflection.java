package com.personoid.nms.packet;

import com.personoid.api.utils.cache.Cache;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NMSReflection {
    private static final Cache CACHE = new Cache("reflection_utils");

    public static Class<?> findClass(Packages packageType, String className) {
        return findClass(packageType.getPackageName(), className);
    }

    public static Class<?> findClass(String packageName, String className) {
        try {
            if (CACHE.contains(packageName + "." + className)) {
                return CACHE.getClass(packageName + "." + className);
            }
            Class<?> clazz = Class.forName(packageName + "." + className);
            CACHE.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object construct(Class<?> clazz, Object... parameters) {
        try {
            Class<?>[] types = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                types[i] = parameters[i].getClass();
            }
            return clazz.getConstructor(types).newInstance(parameters);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invoke(Object obj, String methodName, Object... args) {
        try {
            List<Class<?>> argTypes = new ArrayList<>();
            for (Object arg : args) argTypes.add(arg.getClass());
            Method method = obj.getClass().getMethod(methodName, argTypes.toArray(new Class[0]));
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        throw new RuntimeException("Failed to invoke method for class " + obj.getClass().getName() + " ( " + methodName + ")");
    }

    public static Object getHandle(Object obj) {
        try {
            return obj.getClass().getMethod("getHandle").invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNMSItemStack(ItemStack itemStack) {
        try {
            Class<?> craftItemStackClass = findClass(Packages.CRAFT_ITEM_STACK, "CraftItemStack");
            return craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS item stack", e);
        }
    }

    public static Object getEquipmentSlot(EquipmentSlot slot) {
        String slotName = slot.name();
        if (slotName.equals("HAND")) {
            slotName = "MAINHAND";
        } else if (slotName.equals("OFF_HAND")) {
            slotName = "OFFHAND";
        }
        Class<?> itemSlotClass = findClass(Packages.ITEM_SLOT, "EnumItemSlot");
        return getEnum(itemSlotClass, slotName);
    }

    public static Object getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getEnum(Class<?> clazz, String enumName) {
        try {
            Method method = clazz.getMethod("valueOf", String.class);
            method.setAccessible(true);
            return method.invoke(null, enumName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS enum", e);
        }
    }

    public static String getVersion() {
        try {
            return Bukkit.getBukkitVersion().split("-")[0].replace(".", "_");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Server version not found!");
        }
    }

    public static int getVersionInt() {
        try {
            return Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public static int getSubVersionInt() {
        try {
            return Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[2].charAt(0) + "");
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
