package org.example;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.*;

// interfaces
interface CacheStorage<K, V> {
    V get(K key);
    V put(K key, V value);
    V remove(K key);
    boolean containsKey(K key);
    int size();
    Set<K> keySet();
}

interface DBStorage<K, V> {
    V read(K key);
    void write(K key, V value);
    void delete(K key);
}

interface WritePolicy<K, V> {
    void onWrite(K key, V value, CacheStorage<K, V> cache, DBStorage<K, V> db);
}

interface EvictionPolicy<K> {
    void putKey(K key);
    void onKeyAccess(K key);
    K evictCandidate();
    boolean hasSpace();
}

// concrete classes
class InMemoryCacheStorage<K, V> implements CacheStorage<K, V> {
    private final ConcurrentHashMap<K, V> cacheStorage = new ConcurrentHashMap<>();
    @Override public V get(K key) {
        try {
            System.out.println("Reading from cache " + key);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return cacheStorage.get(key);
    }
    @Override public V put(K key, V value) {
        try {
            System.out.println("Writing to cache " + key + value);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return cacheStorage.put(key, value);
    }
    @Override public V remove(K key) { return cacheStorage.remove(key); }
    @Override public boolean containsKey(K key) { return cacheStorage.containsKey(key); }
    @Override public int size() { return cacheStorage.size(); }
    @Override public Set<K> keySet() { return cacheStorage.keySet(); }
}

class SimpleDBStorage<K, V> implements DBStorage<K, V> {
    private final ConcurrentHashMap<K, V> dbStorage = new ConcurrentHashMap<>();
    @Override public V read(K key) {
        try {
            System.out.println("Reading from DB " + key);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return dbStorage.get(key);
    }
    @Override public void write(K key, V value) {
        try {
            System.out.println("Writing to DB " + key + value);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        dbStorage.put(key, value);
    }
    @Override public void delete(K key) { dbStorage.remove(key); }
}

class WriteThroughPolicy<K, V> implements WritePolicy<K, V> {
    @Override
    public void onWrite(K key, V value, CacheStorage<K, V> cache, DBStorage<K, V> db) {
        db.write(key, value);
        cache.put(key, value);
    }
}

class LRUEvictionPolicy<K> implements EvictionPolicy<K> {
    private final int capacity;
    private final LinkedHashMap<K, Boolean> map;
    private final Object lock = new Object();

    LRUEvictionPolicy(int capacity) {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    @Override
    public void putKey(K key) {
        synchronized (lock) {
            map.put(key, Boolean.TRUE);
        }
    }

    @Override
    public void onKeyAccess(K key) {
        synchronized (lock) {
            map.get(key);
        }
    }

    @Override
    public K evictCandidate() {
        synchronized (lock) {
            if (map.isEmpty()) return null;
            K eldest = map.keySet().iterator().next();
            map.remove(eldest);
            return eldest;
        }
    }

    @Override
    public boolean hasSpace() {
        synchronized (lock) {
            return map.size() < capacity;
        }
    }
}

class KeyBasedExecutor<K, V> {
    private final int size;
    private final ExecutorService[] executors;

    KeyBasedExecutor(int size) {
        this.size = size;
        executors = new ExecutorService[size];
        for (int i=0;i<size;i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
    }

    private int getIndex(K key) {
        // some logic for key -> index
        int h = (key == null) ? 0 : key.hashCode();
        return Math.abs(h % size);
    }

    public Future<V> submitTask(K key, Callable<V> task) {
        int index = getIndex(key);
        ExecutorService executorService = executors[index];
        return executorService.submit(task);
    }

    public void shutDownAll() {
        for (int i=0;i<size;i++) {
            executors[i].shutdown();
        }
    }

}

class Cache<K, V> {
    private final CacheStorage<K,V> cacheStorage;
    private final DBStorage<K, V> dbStorage;
    private final WritePolicy<K, V> writePolicy;
    private final EvictionPolicy<K> evictionPolicy;
    private final KeyBasedExecutor<K, V> executor;


    Cache(CacheStorage<K, V> cacheStorage, DBStorage<K, V> dbStorage, WritePolicy<K, V> writePolicy, EvictionPolicy<K> evictionPolicy, KeyBasedExecutor<K, V> executor) {
        this.cacheStorage = cacheStorage;
        this.dbStorage = dbStorage;
        this.writePolicy = writePolicy;
        this.evictionPolicy = evictionPolicy;
        this.executor = executor;
    }

    public Future<V> accessData(K key) {
        return executor.submitTask(key, () -> {
            if (cacheStorage.containsKey(key)) {
                // if key is in cache
                evictionPolicy.onKeyAccess(key); // key accessed
                return cacheStorage.get(key); // return value
            } else {
                if(evictionPolicy.hasSpace()) {
                    // has cacheSize
                    evictionPolicy.putKey(key);
                } else {
                    // if key is not in cache
                    K keyEvicted = evictionPolicy.evictCandidate();
                    cacheStorage.remove(keyEvicted);
                    evictionPolicy.putKey(key);
                }
                V dataFromDb = dbStorage.read(key);
                cacheStorage.put(key, dataFromDb);
                return dataFromDb;
            }
        });
    }

    public Future<V> updateData(K key, V value) {
        return executor.submitTask(key, () -> {
            writePolicy.onWrite(key, value, cacheStorage, dbStorage);
            evictionPolicy.putKey(key);
            return value;
        });
    }

    public void shutDownAll() {
        this.executor.shutDownAll();
    }

}


public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        int cacheSize = 10;

        InMemoryCacheStorage<String, Integer> inMemoryCacheStorage = new InMemoryCacheStorage();
        SimpleDBStorage<String, Integer> simpleDBStorage = new SimpleDBStorage<>();
        WriteThroughPolicy<String, Integer> writeThroughPolicy = new WriteThroughPolicy<>();
        LRUEvictionPolicy<String> lruEvictionPolicy = new LRUEvictionPolicy<>(cacheSize);
        KeyBasedExecutor<String, Integer> executor = new KeyBasedExecutor<>(cacheSize);
        Cache<String, Integer> cache = new Cache<>(
                inMemoryCacheStorage,
                simpleDBStorage,
                writeThroughPolicy,
                lruEvictionPolicy,
                executor
        );

        // putting data in DB
        simpleDBStorage.write("Raj", 100);
        simpleDBStorage.write("Priyal", 200);

        Thread.sleep(5000); //waiting for DB update

        //putting data in cache
        inMemoryCacheStorage.put("Priyal", 200);


        System.out.println("Accessing Cache system....");

        System.out.println(cache.accessData("Raj").get());

        System.out.println(cache.accessData("Priyal").get());

        cache.updateData("Priyal", 500).get();

        System.out.println(cache.accessData("Priyal").get());

        cache.shutDownAll();
    }
}