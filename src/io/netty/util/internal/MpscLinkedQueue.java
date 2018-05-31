/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscLinkedQueueNode;
import io.netty.util.internal.MpscLinkedQueueTailRef;
import io.netty.util.internal.ReadOnlyIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

final class MpscLinkedQueue<E>
extends MpscLinkedQueueTailRef<E>
implements Queue<E> {
    private static final long serialVersionUID = -1878402552271506449L;
    long p00;
    long p01;
    long p02;
    long p03;
    long p04;
    long p05;
    long p06;
    long p07;
    long p30;
    long p31;
    long p32;
    long p33;
    long p34;
    long p35;
    long p36;
    long p37;

    MpscLinkedQueue() {
        DefaultNode<Object> tombstone = new DefaultNode<Object>(null);
        this.setHeadRef(tombstone);
        this.setTailRef(tombstone);
    }

    private MpscLinkedQueueNode<E> peekNode() {
        MpscLinkedQueueNode head = this.headRef();
        MpscLinkedQueueNode next = head.next();
        if (next == null && head != this.tailRef()) {
            while ((next = head.next()) == null) {
            }
        }
        return next;
    }

    @Override
    public boolean offer(E value) {
        DefaultNode<E> newTail;
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (value instanceof MpscLinkedQueueNode) {
            newTail = (DefaultNode<E>)value;
            newTail.setNext(null);
        } else {
            newTail = new DefaultNode<E>(value);
        }
        MpscLinkedQueueNode<E> oldTail = this.getAndSetTailRef(newTail);
        oldTail.setNext(newTail);
        return true;
    }

    @Override
    public E poll() {
        MpscLinkedQueueNode<E> next = this.peekNode();
        if (next == null) {
            return null;
        }
        MpscLinkedQueueNode oldHead = this.headRef();
        this.lazySetHeadRef(next);
        oldHead.unlink();
        return next.clearMaybe();
    }

    @Override
    public E peek() {
        MpscLinkedQueueNode<E> next = this.peekNode();
        if (next == null) {
            return null;
        }
        return next.value();
    }

    @Override
    public int size() {
        MpscLinkedQueueNode<E> next;
        int count = 0;
        MpscLinkedQueueNode<E> n = this.peekNode();
        while (n != null && n.value() != null && n != (next = n.next())) {
            n = next;
            if (++count != Integer.MAX_VALUE) continue;
            break;
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return this.peekNode() == null;
    }

    @Override
    public boolean contains(Object o) {
        MpscLinkedQueueNode<E> n = this.peekNode();
        while (n != null) {
            E value = n.value();
            if (value == null) {
                return false;
            }
            if (value == o) {
                return true;
            }
            MpscLinkedQueueNode<E> next = n.next();
            if (n == next) break;
            n = next;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIterator<E>(this.toList().iterator());
    }

    @Override
    public boolean add(E e) {
        if (this.offer(e)) {
            return true;
        }
        throw new IllegalStateException("queue full");
    }

    @Override
    public E remove() {
        E e = this.poll();
        if (e != null) {
            return e;
        }
        throw new NoSuchElementException();
    }

    @Override
    public E element() {
        E e = this.peek();
        if (e != null) {
            return e;
        }
        throw new NoSuchElementException();
    }

    private List<E> toList(int initialCapacity) {
        return this.toList(new ArrayList(initialCapacity));
    }

    private List<E> toList() {
        return this.toList(new ArrayList());
    }

    private List<E> toList(List<E> elements) {
        E value;
        MpscLinkedQueueNode<E> next;
        MpscLinkedQueueNode<E> n = this.peekNode();
        while (n != null && (value = n.value()) != null && elements.add(value) && n != (next = n.next())) {
            n = next;
        }
        return elements;
    }

    @Override
    public Object[] toArray() {
        return this.toList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.toList(a.length).toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (this.contains(e)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException("c");
        }
        if (c == this) {
            throw new IllegalArgumentException("c == this");
        }
        boolean modified = false;
        for (E e : c) {
            this.add(e);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        while (this.poll() != null) {
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        for (E e : this) {
            out.writeObject(e);
        }
        out.writeObject(null);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object e;
        in.defaultReadObject();
        DefaultNode<Object> tombstone = new DefaultNode<Object>(null);
        this.setHeadRef(tombstone);
        this.setTailRef(tombstone);
        while ((e = in.readObject()) != null) {
            this.add(e);
        }
    }

    private static final class DefaultNode<T>
    extends MpscLinkedQueueNode<T> {
        private T value;

        DefaultNode(T value) {
            this.value = value;
        }

        @Override
        public T value() {
            return this.value;
        }

        @Override
        protected T clearMaybe() {
            T value = this.value;
            this.value = null;
            return value;
        }
    }

}

