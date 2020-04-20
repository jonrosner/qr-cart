package org.hackatum.main;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;

import io.vertx.core.Vertx;
import org.hackatum.database.DbAccess;
import org.hackatum.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hackatum.server.Server;

import java.sql.SQLException;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private final static int PORT = 1337;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        String jdbcUrl = System.getenv("HACKATUM_JDBC_URL");
        ConnectionSource connectionSource = new JdbcConnectionSource(jdbcUrl, "root", "mypass");
        DbAccess.setupDatabase(connectionSource);
        Server server = new Server(PORT);
        Vertx.vertx().deployVerticle(server);
    }
}
