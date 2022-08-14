package com.personoid.api.utils.packet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Packets {
    public static class AddPlayer extends Packet {
        protected final Player player;

        public AddPlayer(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static class RemovePlayer extends Packet {
        protected final Player player;

        public RemovePlayer(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static class EntityTakeItem extends Packet {
        protected final int itemId;
        protected final int entityId;
        protected final int amount;

        public EntityTakeItem(int itemId, int entityId, int amount) {
            this.itemId = itemId;
            this.entityId = entityId;
            this.amount = amount;
        }

        public int getItemId() {
            return itemId;
        }

        public int getEntityId() {
            return entityId;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class BlockDestruction extends Packet {
        protected final int breakerId;
        protected final Location location;
        protected final int stage;

        public BlockDestruction(int breakerId, Location location, int stage) {
            this.breakerId = breakerId;
            this.location = location;
            this.stage = stage;
        }

        public int getItemId() {
            return breakerId;
        }

        public Location getNPCId() {
            return location;
        }

        public int getAmount() {
            return stage;
        }
    }
}
