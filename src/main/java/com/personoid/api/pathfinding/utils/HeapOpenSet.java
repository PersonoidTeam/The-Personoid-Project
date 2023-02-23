package com.personoid.api.pathfinding.utils;

import com.personoid.api.pathfinding.node.Node;

import java.util.Arrays;

// ALL CREDIT FOR THIS CLASS GOES TO THE MAPLE PATHFINDING PROJECT
public class HeapOpenSet {
    private static final int DEFAULT_CAPACITY = 1024;

    private Node[] nodes;
    private int size;

    public HeapOpenSet() {
        this(DEFAULT_CAPACITY);
    }

    public HeapOpenSet(int maxHeapSize) {
        this.nodes = new Node[maxHeapSize];
    }

    public void add(Node node) {
        if (isFull()) {
            int length = nodes.length << 1;
            nodes = Arrays.copyOf(nodes, length);
        }
        size++;
        nodes[size] = node;
        node.setHeapIndex(size);
        update(node);
    }

    public void update(Node n) {
        int index = n.getHeapIndex();
        double cost = n.getFCost();
        int parentIndex = index >>> 1;
        Node parent = nodes[parentIndex];
        while (index > 1 && parent.getFCost() > cost) {
            nodes[index] = parent;
            nodes[parentIndex] = n;
            n.setHeapIndex(parentIndex);
            parent.setHeapIndex(index);
            index = parentIndex;
            parentIndex = index >>> 1;
            parent = nodes[parentIndex];
        }
    }

    public Node poll() {
        Node node = nodes[1];
        Node lastNode = nodes[size];
        nodes[1] = lastNode;
        nodes[size] = null;
        lastNode.setHeapIndex(1);
        node.setHeapIndex(-1);
        size--;
        if (size < 2) return node;
        int index = 1;
        int childIndex = 2;
        double cost = lastNode.getFCost();
        while (true) {
            Node child = nodes[childIndex];
            double childCost = child.getFCost();
            if (childIndex < size) {
                Node rightChild = nodes[childIndex + 1];
                double rightChildCost = rightChild.getFCost();
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

