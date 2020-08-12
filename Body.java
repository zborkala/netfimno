package com.example.myapp;

import java.util.HashMap;
import java.util.Map;

public class Body {
    Map<String,String> map;

    public Body() {
       map = new HashMap<>();
    }
    public Body put(String key, String value) {
        map.put(key, value);
        return this;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
