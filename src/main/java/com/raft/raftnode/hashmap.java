package com.raft.raftnode;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class hashmap {

    private final ConcurrentHashMap<String, String> store
            = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        store.put(key, value);
    }

    public String get(String key) {
        return store.getOrDefault(key, "Key not found");
    }

    public String del(String key) {
        if (store.containsKey(key)) {
            store.remove(key);
            return "Deleted " + key;
        }
        return "Key not found";
    }
}


