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
import org.hackatum.messages.PostEmployeeRequest;
import org.hackatum.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;

public class EmployeeRoute {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String route = "/api/employee";

    public static void register(Router router) {
        router.route(HttpMethod.GET, route + "/all").handler(EmployeeRoute::getAll);
        router.route(HttpMethod.GET, route + "/:id").handler(EmployeeRoute::get);
        router.route(HttpMethod.GET, route + "/:id/store").handler(EmployeeRoute::getStore);
        router.route(HttpMethod.POST, route).handler(EmployeeRoute::post);
    }

    private static void get(RoutingContext req) {
        try {
            String id = req.request().getParam("id");

            Employee employee = (Employee) DbAccess.getWithNullCheck(id, DbAccess.employee);

            Type storeType = new TypeToken<Store>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(storeType, Store.getIdSerializer()).create();
            String result = gson.toJson(employee);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (SQLException e) {
            logger.error("Failed to get user.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        } catch (ClassNotFoundException e) {
            logger.error("No user for given ID.", e);
            req.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            req.response().end(e.toString());
        }
    }

    private static void post(RoutingContext req) {
        try {
            String body = req.getBodyAsString();
            Gson gson = new GsonBuilder().create();
            PostEmployeeRequest msg = gson.fromJson(body, PostEmployeeRequest.class);

            Store store = (Store) DbAccess.getWithNullCheck(msg.storeId, DbAccess.store);

            Employee employee = new Employee(msg.name, msg.username, msg.email, msg.password, msg.role, store);
            DbAccess.employee.create(employee);

            String result = employee.getId();
            req.response().end(result);
        } catch (Exception e) {
            logger.error("Failed to create user.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getStore(RoutingContext req) {
        try {
            String id = req.request().getParam("id");

            Employee employee = (Employee) DbAccess.getWithNullCheck(id, DbAccess.employee);

            Store store = (Store) DbAccess.getWithNullCheck(employee.getStore().getId(), DbAccess.store);

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Type employeeType = new TypeToken<Employee>() {}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(employeeType, Employee.getIdSerializer())
                    .registerTypeAdapter(reservationType, Reservation.getIdSerializer())
                    .create();
            String result = gson.toJson(store);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (ClassNotFoundException e) {
            logger.error("No store for given ID.", e);
            req.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            req.response().end(e.toString());
        } catch (Exception e) {
            logger.error("Failed to get store.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getAll(RoutingContext req) {
        try {
            List<Employee> response = DbAccess.employee.queryForAll();

            Type storeType = new TypeToken<Store>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(storeType, Store.getIdSerializer()).create();
            String result = gson.toJson(response);
            req.response().putHeader("Content-Type", "application/json").end(result);

        } catch (Exception e) {
            logger.error("Failed to get all reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }
}
