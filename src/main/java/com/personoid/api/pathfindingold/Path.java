package com.personoid.api.pathfindingold;

import java.util.List;

public class Path {
    private final List<PathSegment> segments;
    private int index;

    public Path(List<PathSegment> segments) {
        this.segments = segments;
    }

    public void next() {
        if (isFinished()) return;
        PathSegment segment = getCurrentSegment();
        segment.next();
        if (segment.isFinished()) index++;
    }

    public boolean isFinished() {
        return index >= segments.size();
    }

    public PathSegment getCurrentSegment() {
        return segments.get(index);
    }

    public void add(PathSegment segment) {
        segments.add(segment);
    }

    public void clear() {
        segments.clear();
    }
}
