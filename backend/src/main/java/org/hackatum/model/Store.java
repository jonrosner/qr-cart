package org.hackatum.model;

import com.google.gson.*;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class Store {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(canBeNull = false)
    private float locationLat;

    @DatabaseField(canBeNull = false)
    private float locationLng;

    public static final String NAME_FIELD_NAME = "display_name";
    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String address;

    @DatabaseField(canBeNull = false)
    private String zip;

    @DatabaseField(canBeNull = false)
    private String city;

    @DatabaseField(canBeNull = false)
    private int maximumCapacity;

    @DatabaseField(canBeNull = false)
    private float reservationPercentage;

    @DatabaseField(canBeNull = false)
    private int checkInTimeDuration;

    @DatabaseField(canBeNull = false)
    private int minSlotSize;

    @DatabaseField(canBeNull = false)
    private int maxSlotSize;

    @DatabaseField(canBeNull = false)
    private int errorMargin;

    @DatabaseField
    private int currentNoPeopleInStore;

    @ForeignCollectionField
    private ForeignCollection<Employee> employeeList;

    @ForeignCollectionField
    private ForeignCollection<Reservation> reservationList;

    // these attributes are calculated on the fly when the entity is requested
    public int numUpcomingReservations;
    public int numRemainingReservations;

    public Store() {}

    public Store(float locationLat, float locationLng, String name, String address, String zip, String city, int maximumCapacity, float reservationPercentage, int checkInTimeDuration, int minSlotSize, int maxSlotSize, int errorMargin) {
        this.id = UUID.randomUUID().toString();
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.name = name;
        this.address = address;
        this.zip = zip;
        this.city = city;
        this.maximumCapacity = maximumCapacity;
        this.reservationPercentage = reservationPercentage;
        this.checkInTimeDuration = checkInTimeDuration;
        this.minSlotSize = minSlotSize;
        this.maxSlotSize = maxSlotSize;
        this.errorMargin = errorMargin;
    }

    public String getId() {
        return id;
    }

    public float getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(float locationLat) {
        this.locationLat = locationLat;
    }

    public float getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(float locationLng) {
        this.locationLng = locationLng;
    }

    public static String getNameFieldName() {
        return NAME_FIELD_NAME;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public float getReservationPercentage() {
        return reservationPercentage;
    }

    public void setReservationPercentage(float reservationPercentage) {
        this.reservationPercentage = reservationPercentage;
    }

    public int getCheckInTimeDuration() {
        return checkInTimeDuration;
    }

    public void setCheckInTimeDuration(int checkInTimeDuration) {
        this.checkInTimeDuration = checkInTimeDuration;
    }

    public int getMinSlotSize() {
        return minSlotSize;
    }

    public void setMinSlotSize(int minSlotSize) {
        this.minSlotSize = minSlotSize;
    }

    public int getMaxSlotSize() {
        return maxSlotSize;
    }

    public void setMaxSlotSize(int maxSlotSize) {
        this.maxSlotSize = maxSlotSize;
    }

    public int getErrorMargin() {
        return errorMargin;
    }

    public void setErrorMargin(int errorMargin) {
        this.errorMargin = errorMargin;
    }

    public int getCurrentNoPeopleInStore() {
        return currentNoPeopleInStore;
    }

    public void setCurrentNoPeopleInStore(int currentNoPeopleInStore) {
        this.currentNoPeopleInStore = currentNoPeopleInStore;
    }

    public ForeignCollection<Employee> getEmployeeList() {
        return employeeList;
    }

    public ForeignCollection<Reservation> getReservationList() {
        return reservationList;
    }

    public static JsonSerializer getIdSerializer() {
        return (JsonSerializer<Store>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getId());
    }
}
