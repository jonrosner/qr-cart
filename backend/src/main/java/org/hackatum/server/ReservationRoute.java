package org.hackatum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.mail.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hackatum.database.DbAccess;
import org.hackatum.messages.PutCheckinRequest;
import org.hackatum.messages.PostReservationRequest;
import org.hackatum.model.Customer;
import org.hackatum.model.Reservation;
import org.hackatum.model.Store;
import org.hackatum.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ReservationRoute {
    private static final Logger logger = LoggerFactory.getLogger(ReservationRoute.class);

    private static final String route = "/api/reservation";

    public static void register(Router router) {
        router.route(HttpMethod.GET, route + "/:id").handler(ReservationRoute::get);
        router.route(HttpMethod.PUT, route + "/:id/checkin").handler(ReservationRoute::doCheckIn);
        router.route(HttpMethod.POST, route).handler(ReservationRoute::post);
    }

    private static void get(RoutingContext req) {
        try {
            String id = req.request().getParam("id");
            Reservation reservation = (Reservation) DbAccess.getWithNullCheck(id, DbAccess.reservation);
            Type reservationType = new TypeToken<Reservation>() {}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(reservationType, Reservation.getFullSerializer()).create();
            String result = gson.toJson(reservation);
            req.response().putHeader("Content-Type", "application/json").end(result);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            req.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            req.response().end(e.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void post(RoutingContext req) {
        try {
            String body = req.getBodyAsString();
            Gson gson = new GsonBuilder().setDateFormat(Constants.DATE_FORMAT).create();
            PostReservationRequest msg = gson.fromJson(body, PostReservationRequest.class);

            Customer customer = (Customer) DbAccess.getWithNullCheck(msg.customerId, DbAccess.customer);

            Store store = (Store) DbAccess.getWithNullCheck(msg.storeId, DbAccess.store);

            Reservation reservation = new Reservation(customer, store, msg.startDate, msg.duration);

            DbAccess.reservation.create(reservation);

            createMailForReservation(Vertx.vertx(), reservation);

            String result = reservation.getId();
            req.response().end(result);
        } catch (Exception e) {
            logger.error("Failed to create reservation.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }

    private static void createMailForReservation(Vertx vertx, Reservation reservation) {
        try {
            MailConfig config = new MailConfig();
            config.setHostname("mail.infomaniak.com");
            config.setPort(587);
            config.setStarttls(StartTLSOptions.REQUIRED);
            config.setUsername("qrcart@ibpg.eu");
            config.setPassword("R8@JBEs-UR*sZ*AXF8QH");

            DateFormat dateFormatter = new SimpleDateFormat( "dd.MM.yyyy" );
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            MailClient mailClient = MailClient.create(vertx, config);
            MailMessage message = new MailMessage();
            message.setFrom("qrcart@goma-cms.org");
            message.setTo("igruber@me.com");
            message.setSubject("Your reservation at " + reservation.getStore().getName());
            message.setHtml("<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<title>Your reservation at " + reservation.getStore().getName() + "</title>" +
                    "<style>" +
                    "html, body {" +
                    "font-family: sans-serif;" +
                    "font-size: 1em;" +
                    "margin: 0;" +
                    "padding: 1em;" +
                    "color: #333;" +
                    "}" +
                    "a {" +
                    "color: #a3bf74;" +
                    "}" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<h2>Your reservation at " + reservation.getStore().getName() + " on the " + dateFormatter.format(reservation.getStartDate()) + ".</h2>" +
                    "<p>Checkin is from " + timeFormat.format(reservation.getStartDate()) +
                    " until " +
                    timeFormat.format(
                            new Date(
                                    reservation.getStartDate().getTime() +
                                            reservation.getStore().getCheckInTimeDuration() * 60 * 1000 - 1
                            )
                    ) + ". Please be on time, otherwise your reservation will expire.</p>" +
                    "<p align=\"center\" style=\"text-align: center;\"><img src=\"cid:qrcode\" alt=\"QR-Code\" /></p>" +
                    "<p>Address: "+reservation.getStore().getAddress()+" in "+reservation.getStore().getZip()+" "+reservation.getStore().getCity()+"</p>" +
                    "<p>See you soon and stay healthy!</p>" +
                    "<p>QR Cart</p>" +
                    "</body>" +
                    "</html>");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(reservation.getId(), BarcodeFormat.QR_CODE, 400, 400);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            MailAttachment attachment = new MailAttachment();
            attachment.setContentType("image/png");
            attachment.setName("qrcode.png");
            attachment.setContentId("qrcode");
            attachment.setData(Buffer.buffer(pngData));

            message.setAttachment(attachment);

            mailClient.sendMail(message, result -> {
                if (result.succeeded()) {
                    System.out.println(result.result());
                } else {
                    result.cause().printStackTrace();
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doCheckIn(RoutingContext req) {
        try {
            String id = req.request().getParam("id");

            String body = req.getBodyAsString();
            Gson gson = new GsonBuilder().create();
            PutCheckinRequest putCheckinRequest = gson.fromJson(body, PutCheckinRequest.class);

            Reservation reservation = (Reservation) DbAccess.getWithNullCheck(id, DbAccess.reservation);

            logger.info("IDS", reservation.getStore().getId(), putCheckinRequest.storeId);

            if (!reservation.getStore().getId().equals(putCheckinRequest.storeId)) {
                throw new IllegalArgumentException("Wrong store for this reservation.");
            }
            if (reservation.getCheckedInTime() != null) {
                throw new IllegalArgumentException("Reservation already checked in");
            }

            long checkInStart = reservation.getStartDate().getTime();
            long checkInEnd = reservation.getStartDate().getTime() + TimeUnit.MINUTES.toMillis(reservation.getStore().getCheckInTimeDuration());
            long now = Date.from(Instant.now()).getTime();

            /* TODO: Integrate for Production
            if (now < checkInStart || now > checkInEnd) {
                throw new OperationNotSupportedException("Not in check in time.");
            }*/

            reservation.setCheckedInTime(Date.from(Instant.now()));

            DbAccess.reservation.update(reservation);
            req.response().end(reservation.getId());
        } catch (IllegalArgumentException e) {
            logger.error("Failed to check in.", e);
            req.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            req.response().end(e.toString());
        } catch (Exception e) {
            logger.error("Failed to check in.", e);
            req.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            req.response().end(e.toString());
        }
    }
}
