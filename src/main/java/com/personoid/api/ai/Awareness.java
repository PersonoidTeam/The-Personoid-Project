package com.personoid.api.ai;

import com.personoid.api.npc.NPC;

public class Awareness {
    private final NPC npc;

    public Awareness(NPC npc) {
        this.npc = npc;
    }

    public void tick() {

    }

/*    public boolean shouldJump(Path path) {
        if (npc.getMoveController().isClimbing() || path == null || npc.isMovingInWater()) return false;
        double blockadeDist = Integer.MAX_VALUE;
        for (int i = 0; i <= 3; i++) {
            Vector lookAheadPos = path.getNPCPosAtNode(npc, path.getNextNodeIndex() + i);
            if (lookAheadPos.getY() < npc.getLocation().getY() - 2) {
                return false; // jumping here would result in the npc taking fall damage
            }
            if (lookAheadPos.toLocation(npc.getWorld()).getBlock().getType().name().contains("STAIRS")) {
                return false; // don't want to jump on stairs
            }
            if (lookAheadPos.getY() > npc.getLocation().getY() + npc.getNavigation().getOptions().getMaxStepHeight()) {
                blockadeDist = npc.getLocation().toVector().distance(lookAheadPos);
                break;
            }
        }
        if (npc.isSprinting()) {
            if (blockadeDist == 2) return false; // ONE LESS THAN SPRINTING JUMP DISTANCE
            else if (blockadeDist > 2 && npc.isJumping()) return true; // ONE LESS THAN SPRINTING JUMP DISTANCE
        }
        if (npc.isOnGround() && npc.getNavigation().getGroundTicks() >= 4) {
            if (npc.isSprinting()) {
                return blockadeDist <= 1.5F; // SPRINTING JUMP DISTANCE
            } else {
                return blockadeDist <= 1.25F; // WALKING JUMP DISTANCE
            }
        }
        return false;
    }*/
}
