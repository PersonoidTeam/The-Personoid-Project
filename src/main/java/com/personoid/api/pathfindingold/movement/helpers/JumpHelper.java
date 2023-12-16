package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class JumpHelper extends Helper {
    private static final BoundingBox COLLISION_BOX = new BoundingBox(-0.5, 0.6, -0.5, 0.5, 1.5, 0.5);

    public JumpHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public boolean shouldJump() {
        if (!canJump()) return false;
        return isCollisionAhead();
    }

    private boolean isCollisionAhead() {
        Node source = getSource();
        Node destination = getDestination();

        double x = (source.getX() + destination.getX() + 1) * 0.5;
        double z = (source.getZ() + destination.getZ() + 1) * 0.5;

        Vector pos = getNPC().getLocation().toVector();

        double y = Math.max(pos.getY(), source.getY());
        if (y >= destination.getY()) return false;

        BoundingBox box = COLLISION_BOX.shift(x, y, z);

        // Check if the box collides with any blocks
        World world = getNPC().getEntity().getWorld();
        for (int ix = Math.min(source.getX(), destination.getX()); ix <= Math.max(source.getX(), destination.getX()); ix++) {
            for (int iy = Math.min(source.getY(), destination.getY()); iy <= Math.max(source.getY(), destination.getY()); iy++) {
                for (int iz = Math.min(source.getZ(), destination.getZ()); iz <= Math.max(source.getZ(), destination.getZ()); iz++) {
                    if (world.getBlockAt(ix, iy, iz).getType().isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean canJump() {
        return true; // AwarenessController.canJump();
    }
}
