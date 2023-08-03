package com.personoid.api.pathfinding.calc.pathfinder;

import com.personoid.api.pathfinding.calc.node.evaluator.ClimbNodeEvaluator;
import com.personoid.api.pathfinding.calc.node.evaluator.FallNodeEvaluator;
import com.personoid.api.pathfinding.calc.node.evaluator.JumpNodeEvaluator;
import com.personoid.api.pathfinding.calc.node.evaluator.WalkNodeEvaluator;

public class LongRangePathFinder extends PathFinder {
    @Override
    protected void registerEvaluators() {
        this.evaluators.add(new WalkNodeEvaluator());
        this.evaluators.add(new JumpNodeEvaluator());
        this.evaluators.add(new FallNodeEvaluator());
        this.evaluators.add(new ClimbNodeEvaluator());
    }

    @Override
    protected int getTimeout() {
        return 1000;
    }

    @Override
    protected int getChunkingRadius() {
        return -1;
    }

    @Override
    protected boolean shouldSoftFail() {
        return false;
    }
}