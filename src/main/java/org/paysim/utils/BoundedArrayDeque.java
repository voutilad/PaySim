package org.paysim.utils;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * A naive sliding buffer based on an ArrayDeque.
 * <p>
 * Sets a finite max size and enforces it by dropping the last item if size maxes out.
 *
 * @param <T>
 */
public class BoundedArrayDeque<T> extends ArrayDeque<T> {
    public final static int DEFAULT_SIZE = 50;

    public final int depth;

    public BoundedArrayDeque() {
        this(DEFAULT_SIZE);
    }

    public BoundedArrayDeque(int numElements) {
        super(numElements);
        depth = numElements;
    }

    private void removeLastIfTooBig() {
        if (size() >= depth) {
            this.removeLast();
        }
    }

    @Override
    public void addFirst(T o) {
        removeLastIfTooBig();
        super.addFirst(o);
    }

    @Override
    public void addLast(T o) {
        removeLastIfTooBig();
        super.addLast(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(item -> addFirst(item));

        // TODO: this is violating the addAll definition as it just assumes mutation occurred
        return true;
    }

    @Override
    public boolean offerFirst(T o) {
        removeLastIfTooBig();
        return super.offerFirst(o);
    }

    @Override
    public boolean offerLast(T o) {
        removeLastIfTooBig();
        return super.offerLast(o);
    }

    @Override
    public boolean add(T o) {
        removeLastIfTooBig();
        return super.add(o);
    }

    @Override
    public boolean offer(T o) {
        removeLastIfTooBig();
        return super.offer(o);
    }

    @Override
    public void push(T o) {
        removeLastIfTooBig();
        super.push(o);
    }
}
