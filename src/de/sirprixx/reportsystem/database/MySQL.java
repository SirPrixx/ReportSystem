package de.sirprixx.reportsystem.database;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MySQL
        extends Database {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    public MySQL(Plugin plugin, String hostname, String port, String database, String username, String password) {
        super(plugin);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        user = username;
        this.password = password;
    }


    public Connection openConnection()
            throws SQLException, ClassNotFoundException {
        if(checkConnection()) {
            return connection;
        }
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" +
                        hostname + ":" + port + "/" + database,
                user, password);
        return connection;
    }
}
