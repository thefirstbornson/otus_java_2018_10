package dbService;

public interface DBCache{
     <K,V> void put(K key, V value);
     <K,V> V get(K key);
     <K>void remove(K key);
     int size();
     <K> void cleanup();
}
