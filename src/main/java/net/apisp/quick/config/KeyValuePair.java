package net.apisp.quick.config;

public class KeyValuePair {
    private Object key;
    private Object value;
    public KeyValuePair() {
    }
    
    public KeyValuePair(Object key, Object value){
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
}
