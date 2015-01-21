package com.johnsimon.payback.preferences;

import com.johnsimon.payback.data.SyncedData;
import com.johnsimon.payback.util.Resource;

public class Preference<T> extends SyncedData<Preference<T>> {

    private T value;

    public Preference(T value) {
        super(System.currentTimeMillis());

        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        touch();
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Preference))return false;
        Preference other = (Preference) o;

        return Resource.nullEquals(value, other.value);
    }
}