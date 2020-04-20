package org.hackatum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hackatum.database.DbAccess;
import org.hackatum.messages.PostCustomerRequest;
import org.hackatum.model.Customer;
import org.hackatum.model.Reservation;
import org.hackatum.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerRoute {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String route = "/api/customer";

    public static void register(Router router) {
        router.route(HttpMethod.GET, route + "/all").handler(CustomerRoute::getAll);
        router.route(HttpMethod.GET, route + "/:id").handler(CustomerRoute::get);
        router.route(HttpMethod.POST, route).handler(CustomerRoute::post);
        router.route(HttpMethod.GET, route + "/:id/reservations").handler(CustomerRoute::getAllReservations);
    }

    private static void get(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            if (id == null) {
                throw new IllegalArgumentException("No task id provided.");
            }
            User customer = DbAccess.customer.queryForId(id);
            if (customer == null) {
                throw new ClassNotFoundException("Failed to get user from the database.");
            }
            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(reservationType, Reservation.getIdSerializer()).create();
            String result = gson.toJson(customer);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (SQLException e) {
            logger.error("Failed to get user.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        } catch (ClassNotFoundException e) {
            logger.error("No user for given ID.", e);
            req.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            req.response().end(e.toString());
        } catch (IllegalArgumentException e) {
            logger.error("No user id provided.", e);
            req.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            req.response().end(e.toString());
        }
    }

    private static void post(RoutingContext req) {
        try {
            String body = req.getBodyAsString();
            Gson gson = new GsonBuilder().create();
            PostCustomerRequest msg = gson.fromJson(body, PostCustomerRequest.class);
            Customer customer = new Customer(msg.name, msg.username, msg.email, msg.password);
            DbAccess.customer.create(customer);
            String result = gson.toJson(customer);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (Exception e) {
            logger.error("Failed to create customer.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getAllReservations(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            Customer customer = (Customer) DbAccess.getWithNullCheck(id, DbAccess.customer);

            List<Reservation> response = new ArrayList<>(customer.getReservationList());

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(reservationType, Reservation.getFullSerializer()).create();
            String result = gson.toJson(response);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (Exception e) {
            logger.error("Failed to get all reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getAll(RoutingContext req) {
        try {

            List<Customer> response = DbAccess.customer.queryForAll();

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(reservationType, Reservation.getIdSerializer()).create();
            String result = gson.toJson(response);
            req.response().putHeader("Content-Type", "application/json").end(result);

        } catch (Exception e) {
            logger.error("Failed to get all reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }
}
