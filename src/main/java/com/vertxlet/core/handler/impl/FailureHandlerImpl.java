package com.vertxlet.core.handler.impl;

import com.vertxlet.core.Constants;
import com.vertxlet.core.db.DatabaseConnection;
import com.vertxlet.core.handler.FailureHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FailureHandlerImpl implements FailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(FailureHandlerImpl.class);

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(RoutingContext rc) {
        logger.error("Unexpected error occur", rc.failure());
        Map<String, DatabaseConnection<?>> connectionMap = rc.get(Constants.DATABASE_KEY);

        rc.response().putHeader("Content-Type", "text/html")
                .setStatusCode(500)
                .end("<html><h1>Server internal error</h1></html>");

        List<Future> futures = connectionMap.entrySet().stream()
                .map(entry -> {
                    logger.info("Close database connection " + entry.getKey());
                    return entry.getValue().close();
                })
                .collect(Collectors.toList());
        CompositeFuture.all(futures).onComplete(ar -> logger.info("All database resources cleaned"));
    }
}
