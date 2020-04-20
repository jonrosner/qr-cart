package org.hackatum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hackatum.database.DbAccess;
import org.hackatum.messages.PostStoreRequest;
import org.hackatum.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.BadAttributeValueExpException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StoreRoute {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String route = "/api/store";

    public static void register(Router router) {
        router.route(HttpMethod.GET, route + "/all").handler(StoreRoute::getAll);
        router.route(HttpMethod.GET, route + "/:id").handler(StoreRoute::get);
        router.route(HttpMethod.POST, route).handler(StoreRoute::post);
        router.route(HttpMethod.GET, route + "/:id/employees").handler(StoreRoute::getEmployees);
        router.route(HttpMethod.GET, route + "/:id/reservations/:day").handler(StoreRoute::getReservations);
        router.route(HttpMethod.PUT, route + "/:id/incCurrentNo")
                .handler((RoutingContext req) -> StoreRoute.changeCurrentNo(req, 1));
        router.route(HttpMethod.PUT, route + "/:id/decCurrentNo")
                .handler((RoutingContext req) -> StoreRoute.changeCurrentNo(req, -1));
    }

    private static void get(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            Store store = (Store) DbAccess.getWithNullCheck(id, DbAccess.store);

            List<Reservation> reservationList = new ArrayList<>(store.getReservationList());

            long now = Date.from(Instant.now()).getTime();
            store.numRemainingReservations = (int) reservationList.stream().filter(reservation -> {
                if (reservation.getCheckedInTime() != null) {
                    // customer already walked in
                    return false;
                }
                return reservation.getStartDate().getTime() <= now && now <= reservation.getStartDate().getTime() + reservation.getStore().getCheckInTimeDuration() * 60 * 1000;
            }).count();

            store.numUpcomingReservations = (int) reservationList.stream().filter(reservation -> {
                // starttime is between start of upcoming slot -> + maxSlotSize
                long start = ((long) Math.ceil(now / (reservation.getStore().getMinSlotSize() * 60000))) * (reservation.getStore().getMinSlotSize() * 60000) + reservation.getStore().getMinSlotSize() * 60000;
                long end = start + TimeUnit.MINUTES.toMillis(reservation.getStore().getMaxSlotSize());
                return start <= reservation.getStartDate().getTime() && reservation.getStartDate().getTime() <= end;
            }).collect(Collectors.toList()).size();

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Type employeeType = new TypeToken<Employee>() {}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(employeeType, Employee.getIdSerializer())
                    .registerTypeAdapter(reservationType, Reservation.getIdSerializer())
                    .create();
            String result = gson.toJson(store);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (ClassNotFoundException e) {
            logger.error("No user for given ID.", e);
            req.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            req.response().end(e.toString());
        } catch (Exception e) {
            logger.error("Failed to get store.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void post(RoutingContext req) {
        try {
            String body = req.getBodyAsString();
            Gson gson = new GsonBuilder().create();
            PostStoreRequest msg = gson.fromJson(body, PostStoreRequest.class);

            Store store = new Store(msg.locationLat, msg.locationLng, msg.name, msg.address, msg.zip, msg.city,
                    msg.maximumCapacity, msg.reservationPercentage, msg.checkInTimeDuration, msg.minSlotSize,
                    msg.maxSlotSize, msg.errorMargin);

            DbAccess.store.create(store);
            String result = store.getId();
            req.response().end(result);
        } catch (Exception e) {
            logger.error("Failed to create user.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getEmployees(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            Store store = (Store) DbAccess.getWithNullCheck(id, DbAccess.store);
            List<Employee> response = new ArrayList<>(store.getEmployeeList());

            Type employeeType = new TypeToken<Employee>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(employeeType, Employee.getFullSerializer()).create();
            String result = gson.toJson(response);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (Exception e) {
            logger.error("Failed to get employees.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getReservations(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            int day = Integer.parseInt(req.request().getParam("day"));

            if (day == 0) {
                throw new IllegalArgumentException("Bad day provided.");
            }
            Store store = (Store) DbAccess.getWithNullCheck(id, DbAccess.store);

            // get reservations for today
            List<Reservation> reservationList = new ArrayList<>(store.getReservationList()).stream().filter(reservation -> {
                // starttime is between start of day until end of day
                long startOfDay = day * TimeUnit.DAYS.toMillis(1);
                long endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1);
                return startOfDay <= reservation.getStartDate().getTime() && reservation.getStartDate().getTime() <= endOfDay;
            }).collect(Collectors.toList());

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(reservationType,
                    Reservation.getTimetableSerializer()).create();
            String result = gson.toJson(reservationList);
            req.response().putHeader("Content-Type", "application/json").end(result);

        } catch (Exception e) {
            logger.error("Failed to get reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void changeCurrentNo(RoutingContext req, int i) {
        try {
            String id = req.request().getParam("id");
            Store store = (Store) DbAccess.getWithNullCheck(id, DbAccess.store);

            if (i < 0 && store.getCurrentNoPeopleInStore() == 0) {
                throw new BadAttributeValueExpException("Cannot further decrease.");
            }

            if (i > 0 && store.getCurrentNoPeopleInStore() == store.getMaximumCapacity()) {
                throw new BadAttributeValueExpException("Cannot further increase.");
            }

            store.setCurrentNoPeopleInStore(store.getCurrentNoPeopleInStore() + i);

            // TODO: is this update atomic? is the entity locked for other queries during this update?
            DbAccess.store.update(store);

            req.response().end(Integer.toString(store.getCurrentNoPeopleInStore()));
        } catch(BadAttributeValueExpException e) {
            logger.error("Bad request.", e);
            req.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            req.response().end(e.toString());
        } catch (Exception e) {
            logger.error("Failed to get all reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void getAll(RoutingContext req) {
        try {

            List<Store> response = DbAccess.store.queryForAll();

            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Type employeeType = new TypeToken<Employee>() {}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(reservationType, Reservation.getIdSerializer())
                    .registerTypeAdapter(employeeType, Employee.getIdSerializer())
                    .create();
            String result = gson.toJson(response);

            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (Exception e) {
            logger.error("Failed to get all reservations.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }
}
