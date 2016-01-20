package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.db.DbConnector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class JdbcConnector implements DbConnector {

    private SQLConnection con;

    @Override
    public void openConnection(Vertx vertx, JsonObject config,
                               Handler<AsyncResult<Void>> handler) {

        JDBCClient.createShared(vertx, config).getConnection(ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                con = ar.result();
                handler.handle(Future.succeededFuture());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        if (con == null)
            throw DbConnector.NOT_INITIALIZED_EXCEPTION;
        return (T) con;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        if (con != null) {
            con.close(handler);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }
}
