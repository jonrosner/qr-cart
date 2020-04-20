package org.hackatum.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.hackatum.model.Customer;
import org.hackatum.model.Employee;
import org.hackatum.model.Reservation;
import org.hackatum.model.Store;

import java.sql.SQLException;

public class DbAccess {
    public static Dao<Customer, String> customer;
    public static Dao<Store, String> store;
    public static Dao<Reservation, String> reservation;
    public static Dao<Employee, String> employee;

    public static void setupDatabase(ConnectionSource connectionSource) throws SQLException {
        customer = DaoManager.createDao(connectionSource, Customer.class);
        store = DaoManager.createDao(connectionSource, Store.class);
        reservation = DaoManager.createDao(connectionSource, Reservation.class);
        employee = DaoManager.createDao(connectionSource, Employee.class);

        TableUtils.createTableIfNotExists(connectionSource, Customer.class);
        TableUtils.createTableIfNotExists(connectionSource, Store.class);
        TableUtils.createTableIfNotExists(connectionSource, Reservation.class);
        TableUtils.createTableIfNotExists(connectionSource, Employee.class);
    }

    public static Object getWithNullCheck(String id, Dao dao) throws ClassNotFoundException, SQLException {
        if (id == null) {
            throw new IllegalArgumentException("No id provided.");
        }
        Object obj = dao.queryForId(id);
        if (obj == null) {
            throw new ClassNotFoundException("Failed to get from the database." + dao.toString() + ", id: " + id);
        }
        return obj;
    }
}
