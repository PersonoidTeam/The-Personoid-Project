package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.NPCInventory;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.movement.movements.FallMovement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class FallHelper extends Helper {
    private boolean placedWaterBucket;

    public FallHelper(NPC npc, FallMovement movement) {
        super(npc, movement);
    }

    @Override
    public double getCost() {
        if (placedWaterBucket) return 0;

        if (hasToMlg()) {
            if (!canPlaceWater()) return Cost.INFINITY;

            NPCInventory inv = getNPC().getInventory();
            if (!inv.hasItem(Material.WATER_BUCKET)) return Cost.INFINITY;
        }

        return 0;
    }

    public boolean hasToMlg() {
        FallMovement movement = (FallMovement) getMovement();

        int fallDistance = movement.getFallDistance();
        if (fallDistance < 4) return false;

        BlockType type = getDestination().getType(getWorld());
        return type != BlockType.WATER;
    }

    public void tick() {
        if (getNPC().isOnGround() || !hasToMlg() || !selectWaterBucket()) return;
        getNPC().interact();
        Bukkit.broadcastMessage("Placed water from bucket");
        placedWaterBucket = true;
    }

    private boolean selectWaterBucket() {
        NPCInventory inv = getNPC().getInventory();
        ItemStack item = inv.getFirst(Material.WATER_BUCKET);
        if (item == null) return false;
        inv.select(item);
        return true;
    }

    public boolean isFinished() {
        if (!placedWaterBucket) return true;

        boolean inWater = getNPC().isInWater();
        if (!inWater) return getNPC().isOnGround();

        getNPC().interact();
        Bukkit.broadcastMessage("Collected water into bucket");
        return false;
    }

    private boolean canPlaceWater() {
        World world = getNPC().getWorld();
        return !world.isUltraWarm();
    }
}
