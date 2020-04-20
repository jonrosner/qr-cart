package org.hackatum.messages;

public class PostStoreRequest {
    public float locationLat;
    public float locationLng;
    public String name;
    public String address;
    public String zip;
    public String city;
    public int maximumCapacity;
    public float reservationPercentage;
    public int checkInTimeDuration;
    public int minSlotSize;
    public int maxSlotSize;
    public int errorMargin;
}
