package com.personoid.api.npc;

import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class InputEmulator {
    private final NPC npc;

    private final Map<InputAction, Boolean> inputActions = new HashMap<>();

    public InputEmulator(NPC npc) {
        this.npc = npc;
        initInputs();
    }

    public void tick() {
        if (inputActions.get(InputAction.JUMP)) {
            npc.getMoveController().jump();
        }
        Vector direction = npc.getLookController().getDirection().clone();
        Vector velocity = new Vector();
        if (inputActions.get(InputAction.MOVE_FORWARD)) {
            velocity.add(direction.multiply(0.2));
        }
        if (inputActions.get(InputAction.MOVE_BACKWARD)) {
            velocity.subtract(direction.multiply(0.2));
        }
        if (inputActions.get(InputAction.MOVE_LEFT)) {
            velocity.add(direction.crossProduct(new Vector(0, 1, 0)).multiply(0.2));
        }
        if (inputActions.get(InputAction.MOVE_RIGHT)) {
            velocity.subtract(direction.crossProduct(new Vector(0, 1, 0)).multiply(0.2));
        }
        npc.getMoveController().addVelocity(velocity);
    }

    private void initInputs() {
        for (InputAction inputAction : InputAction.values()) {
            inputActions.put(inputAction, false);
        }
    }

    public void setPressed(InputAction input, boolean pressed) {
        inputActions.put(input, pressed);
    }

    public boolean isPressed(InputAction input) {
        return inputActions.get(input);
    }
}
