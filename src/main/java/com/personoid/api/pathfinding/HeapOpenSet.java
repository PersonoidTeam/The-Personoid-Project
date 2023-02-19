package com.personoid.api.pathfinding;

import java.util.Arrays;

// ALL CREDIT FOR THIS CLASS GOES TO THE MAPLE PATHFINDING PROJECT
public class HeapOpenSet {
    private PathingNode[] nodes;
    private int size;

    public HeapOpenSet(int maxHeapSize) {
        this.nodes = new PathingNode[maxHeapSize];
    }

    public void add(PathingNode node) {
        if (isFull()) {
            int length = nodes.length << 1;
            nodes = Arrays.copyOf(nodes, length);
        }
        size++;
        nodes[size] = node;
        node.setHeapIndex(size);
        update(node);
    }

    public void update(PathingNode n) {
        int index = n.getHeapIndex();
        double cost = n.getFinalExpense();
        int parentIndex = index >>> 1;
        PathingNode parent = nodes[parentIndex];
        while (index > 1 && parent.getFinalExpense() > cost) {
            nodes[index] = parent;
            nodes[parentIndex] = n;
            n.setHeapIndex(parentIndex);
            parent.setHeapIndex(index);
            index = parentIndex;
            parentIndex = index >>> 1;
            parent = nodes[parentIndex];
        }
    }

    public PathingNode poll() {
        PathingNode node = nodes[1];
        PathingNode lastNode = nodes[size];
        nodes[1] = lastNode;
        nodes[size] = null;
        lastNode.setHeapIndex(1);
        node.setHeapIndex(-1);
        size--;
        if (size < 2) return node;
        int index = 1;
        int childIndex = 2;
        double cost = lastNode.getFinalExpense();
        while (true) {
            PathingNode child = nodes[childIndex];
            double childCost = child.getFinalExpense();
            if (childIndex < size) {
                PathingNode rightChild = nodes[childIndex + 1];
                double rightChildCost = rightChild.getFinalExpense();
                if (childCost > rightChildCost) {
                    childIndex++;
                    child = rightChild;
                    childCost = rightChildCost;
                }
            }
            if (cost <= childCost) break;
            nodes[index] = child;
            nodes[childIndex] = lastNode;
            lastNode.setHeapIndex(childIndex);
            child.setHeapIndex(index);
            index = childIndex;
            childIndex <<= 1;
            if (childIndex > size) break;
        }
        return node;
    }

    public boolean isFull() {
        return size >= nodes.length - 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}

