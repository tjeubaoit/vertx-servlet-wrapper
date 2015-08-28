package com.admicro.vertx.core;

/**
 * @param <T> the type of the result
 */
public interface AsyncTask<T> {

    T run() throws Exception;
}