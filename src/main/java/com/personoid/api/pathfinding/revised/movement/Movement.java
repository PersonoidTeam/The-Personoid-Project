package com.personoid.api.pathfinding.revised.movement;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.favoring.Favoring;
import com.personoid.api.pathfinding.revised.movement.helper.*;
import com.personoid.api.utils.TimeUtil;
import com.personoid.api.utils.bukkit.BlockPos;
import com.personoid.api.utils.bukkit.Direction;
import de.stylextv.maple.context.PlayerContext;
import de.stylextv.maple.event.events.RenderWorldEvent;
import de.stylextv.maple.input.InputAction;
import de.stylextv.maple.input.controller.InputController;
import de.stylextv.maple.input.controller.ViewController;
import de.stylextv.maple.pathing.movement.helper.*;
import de.stylextv.maple.render.ShapeRenderer;
import de.stylextv.maple.scheme.Color;

public abstract class Movement {
	
	private static final long EXECUTION_TIME_BUFFER = 5000;
	private static final double MAX_COST_INCREASE = 10;
	
	private final Node source;
	private final Node destination;
	
	private final StepHelper stepHelper = new StepHelper(this);
	private final JumpHelper jumpHelper = new JumpHelper(this);
	private final BumpHelper bumpHelper = new BumpHelper(this);
	private final DangerHelper dangerHelper = new DangerHelper(this);
	private final BreakHelper breakHelper = new BreakHelper(this);
	private final PlaceHelper placeHelper = new PlaceHelper(this);
	private final InteractHelper interactHelper = new InteractHelper(this);
	private final PositionHelper positionHelper = new PositionHelper(this);
	
	private double initialCost;
	private double timeSinceStart;
	
	public Movement(Node source, Node destination) {
		this.source = source;
		this.destination = destination;

		updateHelpers();
	}
	
	public void updateHelpers() {}
	
	public double favoredCost() {
		return favoredCost(Favoring.getDefault());
	}
	
	public double favoredCost(Favoring favoring) {
		double coef = favoring.getCoefficient(destination);
		double cost = cost() * coef;
		if (initialCost == 0) initialCost = cost;
		return cost;
	}
	
	public double cost() {
		return stepHelper.cost() + bumpHelper.cost() + dangerHelper.cost();
	}
	
	public abstract void onRenderTick();
	
	protected void setPressed(InputAction i, boolean pressed) {
		InputController.setPressed(i, pressed);
	}
	
	protected void lookAt(BlockPos pos) {
		ViewController.lookAt(pos);
	}
	
	protected void lookAt(Node n) {
		ViewController.lookAt(n);
	}
	
	protected void lookAt(Node n, boolean lookDown) {
		ViewController.lookAt(n, lookDown);
	}
	
	public MovementState getState() {
		BlockPos pos1 = PlayerContext.blockPosition();
		BlockPos pos2 = PlayerContext.feetPosition();
		boolean atDestination = destination.equals(pos1) || destination.equals(pos2);
		return atDestination ? MovementState.DONE : MovementState.PROCEEDING;
	}
	
	public void tick(double dt) {
		timeSinceStart += dt;
	}
	
/*	public void render(RenderWorldEvent event, Color color) {
		ShapeRenderer.drawNodeConnection(event, destination, color, 2);
		
		breakHelper.render(event, color);
		placeHelper.render(event, color);
	}*/
	
	public double squaredDistanceTo(BlockPos pos) {
		int dis1 = source.squaredDistanceTo(pos);
		int dis2 = destination.squaredDistanceTo(pos);
		return Math.min(dis1, dis2);
	}
	
	public boolean ranOutOfTime() {
		double cost = favoredCost();
		long allowedTime = TimeUtil.ticksToMS(cost) + EXECUTION_TIME_BUFFER;
		return timeSinceStart > allowedTime;
	}
	
	public boolean isImpossible() {
		double cost = favoredCost() - initialCost;
		return cost > MAX_COST_INCREASE;
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
	
	public Direction getDirection() {
		int dirX = getDirectionX();
		int dirY = getDirectionY();
		int dirZ = getDirectionZ();
		if (isVertical() && !isVerticalOnly()) dirY = 0;
		return Direction.fromVector(dirX, dirY, dirZ);
	}
	
	public int getDirectionX() {
		return Integer.compare(destination.getX(), source.getX());
	}
	
	public int getDirectionY() {
		return Integer.compare(destination.getY(), source.getY());
	}
	
	public int getDirectionZ() {
		return Integer.compare(destination.getZ(), source.getZ());
	}
	
	public Node getSource() {
		return source;
	}
	
	public Node getDestination() {
		return destination;
	}
	
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

}
