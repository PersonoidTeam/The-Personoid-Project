package com.personoid.api.utils.packet;

import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.Parameter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.personoid.api.utils.packet.ReflectionUtils.getField;

public class Packets {
    private static final CacheManager CACHE = new CacheManager("packets");

    static {
        CACHE.put("entity_player", ReflectionUtils.getClass(Packages.LEVEL, "EntityPlayer"));
        CACHE.put("block_position", ReflectionUtils.getClass(Packages.CORE, "BlockPosition"));
    }

    public static Packet addPlayer(Player player) {
        Class<?> playerInfoPacketAction = ReflectionUtils.getClass(Packages.PACKETS, "PacketPlayOutPlayerInfo$Action");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getField(playerInfoPacketAction, "ADD_PLAYER"));
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), ReflectionUtils.getEntityPlayer(player));
        Class<?> dataWatcherClass = ReflectionUtils.getClass(Packages.NETWORK.plus("syncher"), "DataWatcher");
        Object entityData = ReflectionUtils.getMethod(playerParam.getValue(), "getEntityData");
        try {
            Packet infoPacket = ReflectionUtils.createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam);
            Packet addPlayerPacket = ReflectionUtils.createPacket("PacketPlayOutNamedEntitySpawn", playerParam);
            Packet setEntityDataPacket = ReflectionUtils.createPacket("PacketPlayOutEntityMetadata",
                    new Parameter(dataWatcherClass, entityData), new Parameter(boolean.class, true));
            return Packet.mergePackets(infoPacket, addPlayerPacket, setEntityDataPacket);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet removePlayer(Player player) {
        Class<?> playerInfoPacketAction = ReflectionUtils.getClass(Packages.PACKETS, "PacketPlayOutPlayerInfo$Action");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getField(playerInfoPacketAction, "REMOVE_PLAYER"));
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), ReflectionUtils.getEntityPlayer(player));
        Parameter playerIdParam = new Parameter(int.class, player.getEntityId());
        try {
            Packet infoPacket = ReflectionUtils.createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam);
            Packet removeEntityPacket = ReflectionUtils.createPacket("PacketPlayOutEntityDestroy", playerIdParam);
            return Packet.mergePackets(infoPacket, removeEntityPacket);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet entityTakeItem(int itemId, int entityId, int amount) {
        try {
            return ReflectionUtils.createPacket("PacketPlayOutCollect", new Parameter(int.class, itemId),
                    new Parameter(int.class, entityId), new Parameter(int.class, amount));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet blockDestruction(int breakerId, Location location, int stage) {
        try {
            Class<?> blockPosClass = CACHE.getClass("block_position");
            Object blockPos = blockPosClass.getConstructor(int.class, int.class, int.class)
                    .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return ReflectionUtils.createPacket("PacketPlayOutBlockBreakAnimation", new Parameter(int.class, breakerId),
                    new Parameter(blockPosClass, blockPos), new Parameter(int.class, stage));
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet rotateEntity(Entity entity, float yaw, float pitch) {
        byte yawByte = (byte) ((yaw % 360) * 256 / 360);
        byte pitchByte = (byte) ((pitch % 360) * 256 / 360);
        try {
            Parameter entityParam = new Parameter(CACHE.get("entity_player"), ReflectionUtils.getNMSEntity(entity));
            return ReflectionUtils.createPacket("PacketPlayOutEntityHeadRotation", entityParam, new Parameter(byte.class, yawByte));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(ReflectionUtils.getEntity(entity), yawByte);
        //ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
    }

/*    public static Packet setEntityData(int entityId, DataWatcher data, boolean value) {
        //ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entityId, data, value);
        try {
            return ReflectionUtils.createPacket("PacketPlayOutEntityMetadata", new Parameter(int.class, entityId),
                    new Parameter(DataWatcher.class, data), new Parameter(boolean.class, value));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }*/

    public static Packet entityEquipment(int entityId, Map<EquipmentSlot, ItemStack> equipment) {
        //List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> list = new ArrayList<>();
        //equipment.forEach((slot, item) -> list.add(Pair.of(ReflectionUtils.getSlot(slot), ReflectionUtils.getItemStack(item))));
        //ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, list);
        try {
            Class<?> slotClass = ReflectionUtils.getClass(Packages.NETWORK.plus("world.entity"), "EquipmentSlot");
            Class<?> itemStackClass = ReflectionUtils.getClass(Packages.NETWORK.plus("world.item"), "ItemStack");
            Class<?> pairClass = ReflectionUtils.getClass(Packages.NETWORK.plus("util"), "Pair");
            List<Object> list = new ArrayList<>();
            equipment.forEach((slot, item) -> {
                try {
                    Object pair = pairClass.getConstructor(slotClass, itemStackClass)
                            .newInstance(ReflectionUtils.getEquipmentSlot(slot), ReflectionUtils.getItemStack(item));
                    list.add(pair);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            return ReflectionUtils.createPacket("PacketPlayOutEntityEquipment", new Parameter(int.class, entityId),
                    new Parameter(List.class, list));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
