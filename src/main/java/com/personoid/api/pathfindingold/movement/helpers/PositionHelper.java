package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.Pose;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.movements.ParkourMovement;
import org.bukkit.util.Vector;

public class PositionHelper extends Helper {
    private static final double MAX_DISTANCE_FROM_CENTER = 0.016;
    private static final double MAX_SIDEWAYS_DISTANCE = 0.12;

    public PositionHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public boolean prepareParkourJump() {
        Node source = getSource();

        double sourceX = source.getX() + 0.5;
        double sourceZ = source.getZ() + 0.5;

        ParkourMovement movement = (ParkourMovement) getMovement();

        int dirX = movement.getDirectionX();
        int dirZ = movement.getDirectionZ();

        Vector pos = getNPC().getBlockPos().toVector();

        double x = pos.getX();
        double z = pos.getZ();

        double dx = x - sourceX;
        double dz = z - sourceZ;

        double forwards = dx * dirX;
        double sideways = dz;

        if (dirX == 0) {
            forwards = dz * dirZ;
            sideways = dx;
        }

        double sidewaysDis = Math.abs(sideways);

        if (sidewaysDis > MAX_SIDEWAYS_DISTANCE) {
            double targetX = sourceX - dirX * 0.5;
            double targetZ = sourceZ - dirZ * 0.5;
            moveTo(targetX, targetZ);
            return false;
        }

        Node dest = movement.getDestination();
        getNPC().face(dest.toLocation(getNPC().getWorld()));
        getNPC().getMoveController().moveForward(true);

        return forwards > 0.7 && forwards < 1;
    }

    public boolean centerOnSource() {
        Node source = getSource();

        double sourceX = source.getX() + 0.5;
        double sourceZ = source.getZ() + 0.5;

        return moveTo(sourceX, sourceZ);
    }

    private boolean moveTo(double x, double z) {
        double dis = squaredDistanceTo(x, z);
        boolean reached = dis < MAX_DISTANCE_FROM_CENTER;

        if (reached) {
            double speed = getNPC().getMoveController().getVelocity().getY(); // horizontal speed
            return speed == 0;
        }

        getNPC().getMoveController().moveForward(true);
        getNPC().setPose(Pose.SNEAKING);

        double y = getNPC().getEntity().getLocation().getY();
        getNPC().face(x, y, z);

        return false;
    }

    private double squaredDistanceTo(double x, double z) {
        Vector pos = getNPC().getBlockPos().toVector();
        double dx = pos.getX() - x;
        double dz = pos.getZ() - z;
        return dx * dx + dz * dz;
    }
}
