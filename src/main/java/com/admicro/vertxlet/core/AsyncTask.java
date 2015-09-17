package com.admicro.vertxlet.core;

/**
 * @param <T> the type of the result
 */
public interface AsyncTask<T> {

    T doTask() throws Exception;
}