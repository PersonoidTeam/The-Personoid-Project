package com.personoid.nms.packet;

import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.cache.Cache;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.personoid.nms.packet.ReflectionUtils.*;

public class Packets {
    private static final Cache CACHE = new Cache("packets");

    static {
        CACHE.put("entity_player", findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
        CACHE.put("block_position", findClass(Packages.CORE, "BlockPosition"));
    }

    public static Packet addPlayer(Player player, boolean tabListed) {
        try {
            Parameter playerParam = new Parameter(findClass(Packages.PLAYER, "EntityHuman"), ReflectionUtils.getEntityPlayer(player));
            Packet addPlayerPacket = createPacket("PacketPlayOutNamedEntitySpawn", playerParam);
            return Packet.mergePackets(showPlayer(player, tabListed), addPlayerPacket);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet showPlayer(Player player, boolean tabListed) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoUpdatePacket$a");
        Enum addPlayerAction = (Enum) ReflectionUtils.getEnum(playerInfoPacketAction, "ADD_PLAYER");
        Enum updateListedAction = (Enum) ReflectionUtils.getEnum(playerInfoPacketAction, "UPDATE_LISTED");
        EnumSet enumSet = EnumSet.of(addPlayerAction);
        if (tabListed) enumSet.add(updateListedAction);
        Parameter actionParams = new Parameter(EnumSet.class, enumSet);
        Parameter playerParam = new Parameter(Collection.class, Collections.singletonList(ReflectionUtils.getEntityPlayer(player)));
        try {
            Packet updatePacket = createPacket("ClientboundPlayerInfoUpdatePacket", actionParams, playerParam);
            if (!tabListed) return Packet.mergePackets(hidePlayer(player), updatePacket);
            return updatePacket;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet updateDisplayName(Player player) {
        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoUpdatePacket$a");
        Parameter updateParam = new Parameter(playerInfoPacketAction, ReflectionUtils.getEnum(playerInfoPacketAction, "UPDATE_DISPLAY_NAME"));
        Parameter playerParam = new Parameter(Collection.class, Collections.singletonList(ReflectionUtils.getEntityPlayer(player)));
        try {
            return createPacket("ClientboundPlayerInfoUpdatePacket", updateParam.enumSet(), playerParam);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet hidePlayer(Player player) {
/*        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoRemovePacket$a");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER*/
        //Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Parameter playerParam = new Parameter(UUID.class, player.getUniqueId());
        try {
            return createPacket("ClientboundPlayerInfoRemovePacket", playerParam.list());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet removePlayer(Player player) {
/*        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoRemovePacket$a");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER*/
        //Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Parameter playerParam = new Parameter(UUID.class, player.getUniqueId());
        Parameter playerIdParam = new Parameter(int.class, player.getEntityId());
        try {
            Packet infoPacket = createPacket("ClientboundPlayerInfoRemovePacket", playerParam.list());
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
            Parameter entityParam = CACHE.getOrPut("entity", () -> {
                return new Parameter(findClass(Packages.ENTITY, "Entity"), ReflectionUtils.getNMSEntity(entity));
            });
            Packet headPacket = createPacket("PacketPlayOutEntityHeadRotation", entityParam, new Parameter(byte.class, yawByte));
/*            Packet entityPacket = createPacket("PacketPlayOutEntity$PacketPlayOutEntityLook", new Parameter(int.class, entity.getEntityId()),
                    new Parameter(byte.class, yawByte), new Parameter(byte.class, pitchByte), new Parameter(boolean.class, false));
            return Packet.mergePackets(headPacket, entityPacket);*/
            return headPacket;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(getEntity(entity), yawByte);
        //ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
    }

    public static Packet updateEntityData(Entity entity) {
        try {
            Object entityData = invoke(getNMSEntity(entity), "aj"); // getEntityData
            if (ReflectionUtils.getVersionInt() >= 19 && ReflectionUtils.getSubVersionInt() <= 2) {
                return createPacket("PacketPlayOutEntityMetadata", new Parameter(int.class, entity.getEntityId()),
                        new Parameter(entityData.getClass(), entityData), new Parameter(boolean.class, false));
            } else {
                Object nonDefaultValues = entityData.getClass().getMethod("c").invoke(entityData); // getNonDefaultValues
                return createPacket("PacketPlayOutEntityMetadata", new Parameter(int.class, entity.getEntityId()),
                        new Parameter(List.class, nonDefaultValues));
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet entityEquipment(int entityId, Map<EquipmentSlot, ItemStack> equipment) {
        try {
            Class<?> pairClass = findClass("com.mojang.datafixers.util", "Pair");
            List<Object> list = new ArrayList<>();
            equipment.forEach((slot, item) -> {
                try {
                    Object pair = pairClass.getConstructor(Object.class, Object.class)
                            .newInstance(ReflectionUtils.getEquipmentSlot(slot), ReflectionUtils.getItemStack(item));
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
