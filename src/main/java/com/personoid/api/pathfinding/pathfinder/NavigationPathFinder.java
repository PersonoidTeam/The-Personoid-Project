package com.personoid.api.pathfinding.pathfinder;

import com.personoid.api.pathfinding.node.evaluator.ClimbNodeEvaluator;
import com.personoid.api.pathfinding.node.evaluator.FallNodeEvaluator;
import com.personoid.api.pathfinding.node.evaluator.JumpNodeEvaluator;
import com.personoid.api.pathfinding.node.evaluator.WalkNodeEvaluator;

public class NavigationPathFinder extends PathFinder {
    @Override
    protected void registerEvaluators() {
        this.evaluators.add(new WalkNodeEvaluator());
        this.evaluators.add(new JumpNodeEvaluator());
        this.evaluators.add(new FallNodeEvaluator());
        this.evaluators.add(new ClimbNodeEvaluator());
    }

    @Override
    protected int getTimeout() {
        return 50;
    }

    @Override
    protected int getChunkingRadius() {
        return 32;
    }

    @Override
    protected boolean shouldSoftFail() {
        return true;
    }
}
