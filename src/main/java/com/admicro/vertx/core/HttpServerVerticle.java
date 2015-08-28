package com.admicro.vertx.core;

import com.admicro.vertx.utils.ServerOptions;
import com.admicro.vertx.utils.ServerOptionsFactory;
import com.admicro.vertx.utils.SimpleClassLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class HttpServerVerticle extends AbstractVerticle {

    static final String ROOT_PATH = "/*";

    private final Map<String, Vertxlet> mappingUrls = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerOptions options;

    public HttpServerVerticle(ServerOptions options) {
        this.options = options;
    }

    public HttpServerVerticle() {
        this.options = ServerOptionsFactory.defaultServerOption(vertx);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route(ROOT_PATH).handler(routingContext -> {
            routingContext.addHeadersEndHandler(Future::complete);
            routingContext.next();
        }).failureHandler(routingContext -> {
            SQLConnection con = routingContext.get("db");
            if (con != null) {
                con.close(v -> {
                });
            }
            routingContext.response().putHeader("content-type", "text/html")
                    .setStatusCode(500).end("<html><h1>Server internal error</h1></html>");
        });

        try {
            scanForMappingUrl(router);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            logger.error(e.getMessage(), e);
            startFuture.fail(e);
            return;
        }

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(options.getPort(), options.getAddress(), result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
    }

    private void scanForMappingUrl(Router router) throws Exception {
        final Reflections reflections = new Reflections("");

        for (Class<?> clazz : reflections.getSubTypesOf(Vertxlet.class)) {
            if (!clazz.isAnnotationPresent(VertxServlet.class)) continue;

            for (String url : clazz.getAnnotation(VertxServlet.class).url()) {
                Vertxlet servlet;
                if (!mappingUrls.containsKey(url)) {
                    servlet = (Vertxlet) SimpleClassLoader.loadClass(clazz);
                    servlet.setContext(vertx, this);

                    logger.info(String.format("Mapping url %s with class %s", url, clazz.getName()));
                    mappingUrls.put(url, servlet);
                } else {
                    servlet = mappingUrls.get(url);
                }

                router.route(url).handler(servlet::handle);
            }
        }
    }
}