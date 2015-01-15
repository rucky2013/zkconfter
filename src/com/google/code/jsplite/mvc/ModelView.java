package com.google.code.jsplite.mvc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 从controller传回view端的数据
 *
 * @author lpn
 */
public class ModelView implements Iterable<Map.Entry<String, Object>> {
    private Map<String, Object> stack;

    public ModelView() {
        this.stack = new HashMap<String, Object>();
    }

    public <T> T getObject(String attributeName) {
        @SuppressWarnings("unchecked")
        T value = (T) this.stack.get(attributeName);
        return value;
    }

    public Map<String, Object> getAllObjects() {
        return this.stack;
    }

    public <T> void addObject(String attributeName, T attributeValue) {
        stack.put(attributeName, attributeValue);
    }


    public void addAllObjects(Map<String, Object> map) {
        stack.putAll(map);
    }

    public Object removeObject(String attributeName) {
        return stack.remove(attributeName);
    }

    public void removeAllObjects() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }

    @Override
    public Iterator<Entry<String, Object>> iterator() {
        return this.stack.entrySet().iterator();
    }
}
