package org.hackatum.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.DatabaseField;

import java.lang.reflect.Type;

public class Employee extends User {

    @DatabaseField(canBeNull = false)
    private String role;

    public static final String STORE_ID_FIELD_NAME = "store_id";
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = STORE_ID_FIELD_NAME)
    private Store store;

    Employee() {}

    public Employee(String name, String username, String email, String password, String role, Store store) {
        super(name, username, email, password);
        this.role = role;
        this.store = store;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public static JsonSerializer getFullSerializer() {
        return new JsonSerializer<Employee>() {
            @Override
            public JsonElement serialize(Employee src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonEmployee = new JsonObject();

                jsonEmployee.addProperty("id", src.getId());
                jsonEmployee.addProperty("name", src.getName());
                jsonEmployee.addProperty("email", src.getEmail());
                jsonEmployee.addProperty("username", src.getUsername());
                jsonEmployee.addProperty("store", src.getStore().getId());

                return jsonEmployee;
            }
        };
    }

    public static JsonSerializer getIdSerializer() {
        return (JsonSerializer<Employee>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getId());
    }
}
