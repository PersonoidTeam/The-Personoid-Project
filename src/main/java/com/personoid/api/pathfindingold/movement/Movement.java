package com.personoid.api.pathfindingold.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.MovementState;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.favouring.Favouring;
import com.personoid.api.pathfindingold.movement.helpers.*;
import org.bukkit.World;

public abstract class Movement {
    private final NPC npc;
    private final Node source;
    private final Node destination;

    // helpers
    private final StepHelper stepHelper;
    private final JumpHelper jumpHelper;
    private final BumpHelper bumpHelper;
    private final DangerHelper dangerHelper;
    private final BreakHelper breakHelper;
    private final PlaceHelper placeHelper;
    private final InteractHelper interactHelper;
    private final PositionHelper positionHelper;

    private double initialCost;
    private double timeSinceStart;

    public Movement(NPC npc, Node source, Node dest) {
        this.npc = npc;
        this.source = source;
        this.destination = dest;

        this.stepHelper = new StepHelper(npc, this);
        this.jumpHelper = new JumpHelper(npc, this);
        this.bumpHelper = new BumpHelper(npc, this);
        this.dangerHelper = new DangerHelper(npc, this);
        this.breakHelper = new BreakHelper(npc, this);
        this.placeHelper = new PlaceHelper(npc, this);
        this.interactHelper = new InteractHelper(npc, this);
        this.positionHelper = new PositionHelper(npc, this);

        updateHelpers();
    }

    public void updateHelpers() {}

    public void tick() {}

    public void internalTick(double dt) {
        timeSinceStart += dt;
        tick();
    }

    public NPC getNPC() {
        return npc;
    }

    public World getWorld() {
        return npc.getWorld();
    }

    // helpers
    public StepHelper getStepHelper() {
        return stepHelper;
    }

    public JumpHelper getJumpHelper() {
        return jumpHelper;
    }

    public BumpHelper getBumpHelper() {
        return bumpHelper;
    }

    public DangerHelper getDangerHelper() {
        return dangerHelper;
    }

    public BreakHelper getBreakHelper() {
        return breakHelper;
    }

    public PlaceHelper getPlaceHelper() {
        return placeHelper;
    }

    public InteractHelper getInteractHelper() {
        return interactHelper;
    }

    public PositionHelper getPositionHelper() {
        return positionHelper;
    }

    public double favoredCost(Favouring favoring) {
        double coef = favoring.getCoefficient(destination);
        double cost = getCost() * coef;
        if (initialCost == 0) initialCost = cost;
        return cost;
    }

    public double getCost() {
        return stepHelper.getCost() + bumpHelper.getCost() + dangerHelper.getCost();
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public boolean isVerticalOnly() {
        return source.getX() == destination.getX() && source.getZ() == destination.getZ();
    }

    public boolean isVertical() {
        return source.getY() != destination.getY();
    }

    public boolean isDownwards() {
        return destination.getY() < source.getY();
    }

    public boolean isDiagonal() {
        return source.getX() != destination.getX() && source.getZ() != destination.getZ();
    }

    public boolean isDiagonal3D() {
        int dirX = getDirectionX();
        int dirY = getDirectionY();
        int dirZ = getDirectionZ();
        int sum = Math.abs(dirX) + Math.abs(dirY) + Math.abs(dirZ);
        return sum > 1;
    }

/*    public Direction getDirection() {
        int dirX = getDirectionX();
        int dirY = getDirectionY();
        int dirZ = getDirectionZ();
        if (isVertical() && !isVerticalOnly()) dirY = 0;
        return Direction.fromVector(dirX, dirY, dirZ);
    }*/

    public int getDirectionX() {
        return Integer.compare(destination.getX(), source.getX());
    }

    public int getDirectionY() {
        return Integer.compare(destination.getY(), source.getY());
    }

    public int getDirectionZ() {
        return Integer.compare(destination.getZ(), source.getZ());
    }

    public MovementState getState() {
        boolean atDestination = getNPC().getLocation().distance(destination.toLocation(getWorld())) < 0.5;
        return atDestination ? MovementState.DONE : MovementState.PROCEEDING;
    }
}
