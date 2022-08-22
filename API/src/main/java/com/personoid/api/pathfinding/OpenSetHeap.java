package com.personoid.api.pathfinding;

import java.util.Arrays;

public class OpenSetHeap {
    private Node[] array;
    private int size;

    public OpenSetHeap(int maxHeapSize) {
        this.array = new Node[maxHeapSize];
    }

    public void add(Node n) {
        if (isFull()) {
            int l = array.length << 1;
            array = Arrays.copyOf(array, l);
        }
        size++;
        array[size] = n;
        n.setHeapIndex(size);
        update(n);
    }

    public void update(Node n) {
        int index = n.getHeapIndex();
        double cost = n.getFinalExpense();
        int parentIndex = index >>> 1;
        Node parent = array[parentIndex];
        while (index > 1 && parent.getFinalExpense() > cost) {
            array[index] = parent;
            array[parentIndex] = n;
            n.setHeapIndex(parentIndex);
            parent.setHeapIndex(index);
            index = parentIndex;
            parentIndex = index >>> 1;
            parent = array[parentIndex];
        }
    }

    public Node poll() {
        Node node = array[1];
        Node n = array[size];
        array[1] = n;
        array[size] = null;
        n.setHeapIndex(1);
        node.setHeapIndex(-1);
        size--;
        if (size < 2) return node;
        int index = 1;
        int childIndex = 2;
        double cost = n.getFinalExpense();
        while (true) {
            Node child = array[childIndex];
            double childCost = child.getFinalExpense();
            if (childIndex < size) {
                Node rightChild = array[childIndex + 1];
                double rightChildCost = rightChild.getFinalExpense();
                if (childCost > rightChildCost) {
                    childIndex++;
                    child = rightChild;
                    childCost = rightChildCost;
                }
            }
            if (cost <= childCost) break;
            array[index] = child;
            array[childIndex] = n;
            n.setHeapIndex(childIndex);
            child.setHeapIndex(index);
            index = childIndex;
            childIndex <<= 1;
            if (childIndex > size) break;
        }
        return node;
    }

    public boolean isFull() {
        return size >= array.length - 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}

