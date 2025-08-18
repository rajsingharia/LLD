package org.example;

import java.util.HashMap;
import java.util.Map;

public class Attributes {

    private Map<String, String> att;

    public Attributes() {
        att = new HashMap<>();
    }

    public void putValue(Map<String, String>att) {
        att.forEach((key , value) -> {
            this.att.put(key, value);
        });
    }

    public String getValue(String attKey) {
        return att.get(attKey);
    }


    public boolean contains(String attKey, String attValue) {
        if(att.containsKey(attKey) && att.containsValue(attValue)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Attributes{" +
                "att=" + att +
                '}';
    }
}
