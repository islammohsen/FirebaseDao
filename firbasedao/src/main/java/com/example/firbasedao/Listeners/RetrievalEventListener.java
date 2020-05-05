package com.example.firbasedao.Listeners;

public abstract class RetrievalEventListener<T> extends AbstractEventListener {
    public abstract void OnDataRetrieved(T t);
}
