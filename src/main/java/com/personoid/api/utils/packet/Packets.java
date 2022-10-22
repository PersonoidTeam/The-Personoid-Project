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

import static com.personoid.api.utils.packet.ReflectionUtils.*;

public class Packets {
    private static final CacheManager CACHE = new CacheManager("packets");

    static {
        CACHE.put("entity_player", findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
        CACHE.put("block_position", findClass(Packages.CORE, "BlockPosition"));
    }

    public static Packet addPlayer(Player player) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "ADD_PLAYER")); // ADD_PLAYER
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Class<?> dataWatcherClass = findClass(Packages.NETWORK.plus("syncher"), "DataWatcher");
        Object entityData = invoke(getEntityPlayer(player), "ai"); // getEntityData
        try {
            Packet infoPacket = createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam.array());
            Parameter playerParam2 = new Parameter(findClass(Packages.PLAYER, "EntityHuman"), getEntityPlayer(player));
            Packet addPlayerPacket = createPacket("PacketPlayOutNamedEntitySpawn", playerParam2);
            Packet setEntityDataPacket = createPacket("PacketPlayOutEntityMetadata", new Parameter(int.class, player.getEntityId()),
                    new Parameter(dataWatcherClass, entityData), new Parameter(boolean.class, false));
            return Packet.mergePackets(infoPacket, addPlayerPacket, setEntityDataPacket);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet showPlayer(Player player) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "ADD_PLAYER")); // ADD_PLAYER
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        try {
            return createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam.array());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet hidePlayer(Player player) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        try {
            return createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam.array());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet removePlayer(Player player) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER
        Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Parameter playerIdParam = new Parameter(int.class, player.getEntityId());
        try {
            Packet infoPacket = createPacket("PacketPlayOutPlayerInfo", actionParam, playerParam.array());
            Packet removeEntityPacket = createPacket("PacketPlayOutEntityDestroy", playerIdParam.array());
            return Packet.mergePackets(infoPacket, removeEntityPacket);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet entityTakeItem(int itemId, int entityId, int amount) {
        try {
            return createPacket("PacketPlayOutCollect", new Parameter(int.class, itemId),
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
            return createPacket("PacketPlayOutBlockBreakAnimation", new Parameter(int.class, breakerId),
                    new Parameter(blockPosClass, blockPos), new Parameter(int.class, stage));
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet rotateEntity(Entity entity, float yaw, float pitch) {
        byte yawByte = (byte) ((yaw % 360) * 256 / 360);
        byte pitchByte = (byte) ((pitch % 360) * 256 / 360);
        try {
            Parameter entityParam = new Parameter(findClass(Packages.ENTITY, "Entity"), getNMSEntity(entity));
            return createPacket("PacketPlayOutEntityHeadRotation", entityParam, new Parameter(byte.class, yawByte));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(getEntity(entity), yawByte);
        //ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
    }

    public static Packet updateEntityData(Entity entity) {
        try {
            Object entityData = invoke(getNMSEntity(entity), "ai"); // getEntityData
            Class<?> dataWatcherClass = findClass(Packages.NETWORK.plus("syncher"), "DataWatcher");
            return createPacket("PacketPlayOutEntityMetadata", new Parameter(int.class, entity.getEntityId()),
                    new Parameter(dataWatcherClass, entityData), new Parameter(boolean.class, false));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet entityEquipment(int entityId, Map<EquipmentSlot, ItemStack> equipment) {
        //List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> list = new ArrayList<>();
        //equipment.forEach((slot, item) -> list.add(Pair.of(getSlot(slot), getItemStack(item))));
        //ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, list);
        try {
            Class<?> pairClass = findClass("com.mojang.datafixers.util", "Pair");
            List<Object> list = new ArrayList<>();
            equipment.forEach((slot, item) -> {
                try {
                    Object pair = pairClass.getConstructor(Object.class, Object.class)
                            .newInstance(getEquipmentSlot(slot), getItemStack(item));
                    list.add(pair);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            return createPacket("PacketPlayOutEntityEquipment", new Parameter(int.class, entityId),
                    new Parameter(List.class, list));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
