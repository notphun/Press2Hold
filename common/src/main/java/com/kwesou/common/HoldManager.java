package com.kwesou.common;

import java.util.HashSet;
import java.util.Set;

public class HoldManager {

    private boolean latched = false;

    private final Set<String> latchedKeys = new HashSet<>();

    public boolean toggle() {
        latched = !latched;
        return latched;
    }

    public boolean isLatched() {
        return latched;
    }

    public void setKeys(Set<String> keys) {
        latchedKeys.clear();
        latchedKeys.addAll(keys);
    }

    public Set<String> getKeys() {
        return latchedKeys;
    }

    public void clear() {
        latchedKeys.clear();
    }

    public boolean isEmpty() {
        return latchedKeys.isEmpty();
    }

    public String formatKeys() {
        return String.join(", ", latchedKeys);
    }
}