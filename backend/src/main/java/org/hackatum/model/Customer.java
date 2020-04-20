package org.hackatum.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

public class Customer extends User {

    @ForeignCollectionField
    private ForeignCollection<Reservation> reservationList;

    Customer() {}

    public Customer(String name, String username, String email, String password) {
        super(name, username, email, password);
    }

    public ForeignCollection<Reservation> getReservationList() {
        return reservationList;
    }
}
