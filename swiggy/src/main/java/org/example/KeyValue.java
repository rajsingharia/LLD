package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValue {

    private Map<String, Attributes> keyStore;

    public KeyValue() {
        keyStore = new HashMap<>();
    }

    public void put(String key, Map<String, String>attKeyValue) {
        Attributes attributes = new Attributes();
        attributes.putValue(attKeyValue);
        keyStore.put(key, attributes);
    }

    public Attributes get(String key) {
        return keyStore.get(key);
    }

    public void delete(String key) {
        keyStore.remove(key);
    }

    public List<String> search(String attKey, String attValue) {
        List<String> searchKeys = new ArrayList<>();
        keyStore.forEach( (key, attributes) -> {
            if(attributes.contains(attKey, attValue)) {
                searchKeys.add(key);
            }
        });
        return  searchKeys;
    }

}
