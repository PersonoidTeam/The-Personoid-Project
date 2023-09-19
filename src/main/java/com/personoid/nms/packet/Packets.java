package com.personoid.nms.packet;

import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.cache.Cache;
import com.personoid.nms.NMS;
import com.personoid.nms.mappings.NMSMethod;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.personoid.nms.packet.NMSReflection.findClass;

public class Packets {
    private static final Cache CACHE = new Cache("Packets");

    static {
        CACHE.put("server_player", Package.SERVER_PLAYER_CLASS.getMappedClass());
        CACHE.put("block_position", Package.minecraft("core.BlockPos").getMappedClass());
    }

    public static Packet addPlayer(Player player, boolean tabListed) {
        Parameter playerParam = new Parameter(Package.PLAYER_CLASS.getRawClass(), NMSReflection.getHandle(player));
        Packet addPlayerPacket = NMS.createPacket("ClientboundAddPlayerPacket", playerParam);
        return Packet.merge(showPlayer(player, tabListed), addPlayerPacket);
    }

    public static Packet showPlayer(Player player, boolean tabListed) {
        Class<?> action = Package.PROTOCOL.sub("game.ClientboundPlayerInfoUpdatePacket$Action").getRawClass();
        Enum addPlayerAction = (Enum) NMSReflection.getEnum(action, "ADD_PLAYER");
        Enum updateListedAction = (Enum) NMSReflection.getEnum(action, "UPDATE_LISTED");
        EnumSet enumSet = EnumSet.of(addPlayerAction);
        if (tabListed) enumSet.add(updateListedAction);
        Parameter actionParams = new Parameter(EnumSet.class, enumSet);
        Parameter playerParam = new Parameter(Collection.class, Collections.singletonList(NMSReflection.getHandle(player)));
        Packet updatePacket = NMS.createPacket("ClientboundPlayerInfoUpdatePacket", actionParams, playerParam);
        if (!tabListed) return Packet.merge(hidePlayer(player), updatePacket);
        return updatePacket;
    }

    public static Packet updateDisplayName(Player player) {
        Class<?> action = Package.PROTOCOL.sub("game.ClientboundPlayerInfoUpdatePacket$Action").getRawClass();
        Parameter updateParam = new Parameter(action, NMSReflection.getEnum(action, "UPDATE_DISPLAY_NAME"));
        Parameter playerParam = new Parameter(Collection.class, Collections.singletonList(NMSReflection.getHandle(player)));
        return NMS.createPacket("ClientboundPlayerInfoUpdatePacket", updateParam.enumSet(), playerParam);
    }

    public static Packet hidePlayer(Player player) {
/*        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoRemovePacket$a");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER*/
        //Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Parameter playerParam = new Parameter(UUID.class, player.getUniqueId());
        return NMS.createPacket("ClientboundPlayerInfoRemovePacket", playerParam.list());
    }

    public static Packet removePlayer(Player player) {
/*        Class<?> playerInfoPacketAction = findClass(Packages.PACKETS.plus("game"),
                "ClientboundPlayerInfoRemovePacket$a");
        Parameter actionParam = new Parameter(playerInfoPacketAction, getEnum(playerInfoPacketAction, "REMOVE_PLAYER")); // REMOVE_PLAYER*/
        //Parameter playerParam = new Parameter(CACHE.getClass("entity_player"), getEntityPlayer(player));
        Parameter playerParam = new Parameter(UUID.class, player.getUniqueId());
        Parameter playerIdParam = new Parameter(int.class, player.getEntityId());
        Packet infoPacket = NMS.createPacket("ClientboundPlayerInfoRemovePacket", playerParam.list());
        Packet removeEntityPacket = NMS.createPacket("ClientboundRemoveEntitiesPacket", playerIdParam.array());
        return Packet.merge(infoPacket, removeEntityPacket);
    }

    public static Packet entityTakeItem(int itemId, int entityId, int amount) {
        return NMS.createPacket("ClientboundTakeItemEntityPacket", new Parameter(int.class, itemId),
                new Parameter(int.class, entityId), new Parameter(int.class, amount));
    }

    public static Packet blockDestruction(int breakerId, Location location, int stage) {
        try {
            Class<?> blockPosClass = CACHE.getClass("block_position");
            Object blockPos = blockPosClass.getConstructor(int.class, int.class, int.class)
                    .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return NMS.createPacket("ClientboundBlockDestructionPacket", new Parameter(int.class, breakerId),
                    new Parameter(blockPosClass, blockPos), new Parameter(int.class, stage));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet rotateEntity(Entity entity, float yaw, float pitch) {
        byte yawByte = (byte) ((yaw % 360) * 256 / 360);
        byte pitchByte = (byte) ((pitch % 360) * 256 / 360);
        Parameter entityParam = CACHE.getOrPut("entity", () -> {
            return new Parameter(findClass(Packages.ENTITY, "Entity"), NMSReflection.getHandle(entity));
        });
        Packet headPacket = NMS.createPacket("ClientboundRotateHeadPacket", entityParam, new Parameter(byte.class, yawByte));
/*            Packet entityPacket = createPacket("PacketPlayOutEntity$PacketPlayOutEntityLook", new Parameter(int.class, entity.getEntityId()),
                    new Parameter(byte.class, yawByte), new Parameter(byte.class, pitchByte), new Parameter(boolean.class, false));
            return Packet.mergePackets(headPacket, entityPacket);*/
        return headPacket;
        //ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(getEntity(entity), yawByte);
        //ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
    }

    public static Packet updateEntityData(Entity entity) {
        NMSMethod getEntityData = Package.ENTITY_CLASS.getMappedClass().getMethod("getEntityData");
        Object entityData = getEntityData.invoke(NMSReflection.getHandle(entity));

        int versionInt = NMSReflection.getVersionInt();
        int subVersionInt = NMSReflection.getSubVersionInt();
        boolean updatedVersion = versionInt > 19 || (versionInt == 19 && subVersionInt >= 2);

        if (updatedVersion) {
            NMSMethod getNonDefaultValues = Package.ENTITY_DATA_CLASS.getMappedClass().getMethod("getNonDefaultValues");
            Object nonDefaultValues = getNonDefaultValues.invoke(entityData);
            return NMS.createPacket("ClientboundSetEntityDataPacket", new Parameter(int.class, entity.getEntityId()),
                    new Parameter(List.class, nonDefaultValues));
        } else {
            return NMS.createPacket("ClientboundSetEntityDataPacket", new Parameter(int.class, entity.getEntityId()),
                    new Parameter(entityData.getClass(), entityData), new Parameter(boolean.class, false));
        }
    }

    public static Packet entityEquipment(int entityId, Map<EquipmentSlot, ItemStack> equipment) {
        Class<?> pairClass = Package.mojang("datafixers.util.Pair").getRawClass();
        List<Object> list = new ArrayList<>();
        equipment.forEach((slot, item) -> {
            try {
                Object pair = pairClass.getConstructor(Object.class, Object.class)
                        .newInstance(NMSReflection.getEquipmentSlot(slot), NMSReflection.getNMSItemStack(item));
                list.add(pair);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return NMS.createPacket("ClientboundSetEquipmentPacket", new Parameter(int.class, entityId),
                new Parameter(List.class, list));
    }
}
