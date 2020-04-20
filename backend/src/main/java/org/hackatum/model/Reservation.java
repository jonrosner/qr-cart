package org.hackatum.model;

import com.google.gson.*;
import com.j256.ormlite.field.DatabaseField;
import org.hackatum.utils.Constants;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Reservation {

    @DatabaseField(id = true)
    private String id;

    public static final String CUSTOMER_ID_FIELD_NAME = "customer_id";
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = CUSTOMER_ID_FIELD_NAME, canBeNull = false)
    private Customer customer;

    public static final String STORE_ID_FIELD_NAME = "store_id";
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = STORE_ID_FIELD_NAME, canBeNull = false)
    private Store store;

    @DatabaseField(canBeNull =  false)
    private Date startDate;

    @DatabaseField(canBeNull =  false)
    private int duration;

    @DatabaseField
    private Date checkedInTime;

    Reservation() {}

    public Reservation(Customer customer, Store store, Date startDate, int duration) {
        this.id = UUID.randomUUID().toString();
        this.customer = customer;
        this.store = store;
        this.startDate = startDate;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Store getStore() {
        return store;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getCheckedInTime() {
        return checkedInTime;
    }

    public void setCheckedInTime(Date checkedInTime) {
        this.checkedInTime = checkedInTime;
    }

    public static JsonSerializer getFullSerializer() {
        return new JsonSerializer<Reservation>() {
            @Override
            public JsonElement serialize(Reservation src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonReservation = new JsonObject();
                DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
                jsonReservation.addProperty("id", src.getId());
                jsonReservation.addProperty("startDate", dateFormat.format(src.startDate));
                jsonReservation.addProperty("customerId", src.customer.getId());
                jsonReservation.addProperty("storeId", src.store.getId());
                jsonReservation.addProperty("duration", src.getDuration());
                if (src.checkedInTime != null) {
                    jsonReservation.addProperty("checkedInTime", dateFormat.format(src.checkedInTime));
                }

                return jsonReservation;
            }
        };
    }

    public static JsonSerializer getTimetableSerializer() {
        return new JsonSerializer<Reservation>() {
            @Override
            public JsonElement serialize(Reservation src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonReservation = new JsonObject();
                DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
                jsonReservation.addProperty("id", src.getId());
                jsonReservation.addProperty("startDate", dateFormat.format(src.startDate));
                jsonReservation.addProperty("duration", src.duration);
                jsonReservation.addProperty("storeId", src.store.getId());

                return jsonReservation;
            }
        };
    }

    public static JsonSerializer getIdSerializer() {
        return (JsonSerializer<Reservation>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getId());
    }
}
