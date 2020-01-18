package com.frostflames.lighten.serialization;

import java.io.Serializable;

public class ByteSerializationWrapper <T> implements Serializable {

    T object;

    public ByteSerializationWrapper(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
