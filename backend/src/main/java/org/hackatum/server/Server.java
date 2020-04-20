package org.hackatum.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Server extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        final Router router = Router.router(vertx);

        // parse request body data for all requests
        router.route().handler(BodyHandler.create());

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        /*
         * these methods aren't necessary for this sample,
         * but you may need them for your projects
         */
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        router.get("/access-control-with-get").handler(ctx -> {
            HttpServerResponse httpServerResponse = ctx.response();
            httpServerResponse.setChunked(true);
            MultiMap headers = ctx.request().headers();
            for (String key : headers.names()) {
                httpServerResponse.write(key + ": ");
                httpServerResponse.write(headers.get(key));
                httpServerResponse.write("<br>");
            }
            httpServerResponse.putHeader("Content-Type", "application/text").end("Success");
        });

        router.post("/access-control-with-post-preflight").handler(ctx -> {
            HttpServerResponse httpServerResponse = ctx.response();
            httpServerResponse.setChunked(true);
            MultiMap headers = ctx.request().headers();
            for (String key : headers.names()) {
                httpServerResponse.write(key + ": ");
                httpServerResponse.write(headers.get(key));
                httpServerResponse.write("<br>");
            }
            httpServerResponse.putHeader("Content-Type", "application/text").end("Success");
        });


        // simple ping end point
        router.route(HttpMethod.GET, "/ping").handler(req -> {
            req.response().end(new JsonObject().put("status", "OK").put("version", "v1").encode());
        });

        CustomerRoute.register(router);
        EmployeeRoute.register(router);
        ReservationRoute.register(router);
        StoreRoute.register(router);

        // start server
        vertx.createHttpServer().requestHandler(router).listen(this.port, result -> {
            if (result.succeeded()) {
                logger.info("Started sever on port: " + this.port);
                startPromise.complete();
            } else {
                logger.error("Failed to start server on port. ", this.port, result.cause());
                startPromise.fail(result.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}
