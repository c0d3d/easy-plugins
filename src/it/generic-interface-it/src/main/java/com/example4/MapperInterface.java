package com.example4;

public interface MapperInterface<T, I, Z> {
    public Z map(T t, I i);
}
