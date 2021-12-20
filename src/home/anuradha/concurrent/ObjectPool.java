package home.anuradha.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ObjectPool<T> {

    private ConcurrentLinkedQueue<T> pool;

    public ObjectPool(final int minObjects) {
        pool = new ConcurrentLinkedQueue<T>();
        for (int i = 0; i < minObjects; i++) {
            pool.add(createNewObject());
        }
    }

    public T borrow() {
        T object = pool.poll();
        return object == null ? createNewObject() : object;
    }

    public void putBack(T object) {
        if (object != null) {
            this.pool.offer(object);
        }
    }

    public int size() {
        return pool.size();
    }

    protected abstract T createNewObject();
}