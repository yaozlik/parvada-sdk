package com.alephri.alephsdk.data.react;

import java.util.HashMap;
import java.util.Map;

public class ReadableMap extends HashMap<String, Object> {

    public ReadableMap() {
        super();
    }

    public ReadableMap(Map<String, Object> map) {
        super();
        putAll(map);
    }

    public boolean hasKey(String key) {
        return containsKey(key) && get(key) != null;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public Integer getInt(String key) {
        return (Integer) get(key);
    }

    @SuppressWarnings("unchecked")
    public ReadableMap getMap(String key) {
        if (get(key) instanceof Map) {
            return new ReadableMap((Map<String, Object>) get(key));
        }
        return (ReadableMap) get(key);
    }
}