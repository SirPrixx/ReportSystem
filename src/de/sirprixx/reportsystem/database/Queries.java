package de.sirprixx.reportsystem.database;

import java.sql.*;

public class Queries {
    private Connection connection = null;

    private String mysqlSQL = "CREATE TABLE IF NOT EXISTS tickets ( ticket_id INT (6) NOT NULL AUTO_INCREMENT, uuid VARCHAR (40), status VARCHAR (20) NOT NULL DEFAULT 'OPEN', reason VARCHAR (20) NOT NULL, description VARCHAR (50) NOT NULL, server_name VARCHAR (50) NOT NULL, player_amount smallint NOT NULL, world VARCHAR (30), created_date bigint NOT NULL , location VARCHAR (50), assigned_to VARCHAR (50) NULL, closed_by VARCHAR (50) NULL, closed_date bigint NULL, PRIMARY KEY (ticket_id));";
    private String sqliteSQL = "CREATE TABLE IF NOT EXISTS tickets ( ticket_id INTEGER PRIMARY KEY, uuid VARCHAR (40), status VARCHAR (20) NOT NULL DEFAULT 'OPEN', reason VARCHAR (20) NOT NULL, description VARCHAR (50) NOT NULL, server_name VARCHAR (50) NOT NULL, player_amount smallint NOT NULL, world VARCHAR (30), created_date bigint NOT NULL, location VARCHAR (50), assigned_to VARCHAR (50) NULL, closed_by VARCHAR (50) NULL, closed_date bigint NULL);";

    private String commentsMysqlSQL = "CREATE TABLE IF NOT EXISTS comments (comment_id INT (6) NOT NULL AUTO_INCREMENT, ticket_id INT (6) NOT NULL, uuid VARCHAR(40) NOT NULL, comment VARCHAR(255) NOT NULL, PRIMARY KEY (comment_id));";
    private String commentsSqliteSQL = "CREATE TABLE IF NOT EXISTS comments (comment_id INTEGER PRIMARY KEY, ticket_id INTEGER NOT NULL, uuid VARCHAR(40) NOT NULL, comment VARCHAR(255) NOT NULL);";


    public Queries(Connection connection) {
        this.connection = connection;
    }


    public boolean createMySQLTable() {
        boolean successful = false;
        if((executeUpdate(mysqlSQL)) && (executeUpdate(commentsMysqlSQL))) {
            successful = true;
        }
        return successful;
    }


    public boolean createSQLiteTable() {
        boolean successful = false;
        if((executeUpdate(sqliteSQL)) && (executeUpdate(commentsSqliteSQL))) {
            successful = true;
        }
        return successful;
    }


    public boolean executeUpdate(String sql) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            return true;
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public ResultSet executeQuery(String sql) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean insertTicket(String uuid, String reason, String description, int playerAmount, String world, long date, String location, String server) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO tickets (uuid, reason, description, server_name, player_amount, world, created_date, location) VALUES ('" + uuid + "', ?, ?, ?, '" + playerAmount + "', '" + world + "', '" + date + "', '" + location + "');");
            ps.setString(1, reason);
            ps.setString(2, description);
            ps.setString(3, server);
            ps.executeUpdate();
            return true;
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean insertComment(String player, String comment, int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO comments (uuid, ticket_id, comment) VALUES (?, ?, ?);");
            ps.setString(1, player);
            ps.setInt(2, id);
            ps.setString(3, comment);
            ps.executeUpdate();
            return true;
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean setAssigned(String player, int id) {
        String sql = "UPDATE tickets SET status='ASSIGNED', assigned_to='" + player + "' WHERE ticket_id='" + id + "';";
        return executeUpdate(sql);
    }


    public boolean setClosed(String player, String assignedTo, int id, long date) {
        String sql = "UPDATE tickets SET status='CLOSED', assigned_to='" + assignedTo + "', closed_by='" + player + "', closed_date='" + date + "' WHERE ticket_id='" + id + "';";
        return executeUpdate(sql);
    }


    public boolean setUnclaimed(int id) {
        String sql = "UPDATE tickets SET status='OPEN', assigned_to='NULL' WHERE ticket_id='" + id + "';";
        return executeUpdate(sql);
    }


    public void keepConnectionAlive() {
        String sql = "SELECT COUNT(status) AS Amount FROM tickets WHERE status = 'OPEN';";
        executeQuery(sql);
    }


    public ResultSet loadAllTickets() {
        String sql = "SELECT * FROM tickets;";
        return executeQuery(sql);
    }


    public ResultSet loadAllComments(int id) {
        String sql = "SELECT * FROM comments WHERE ticket_id = '" + id + "' ORDER BY comment_id;";
        return executeQuery(sql);
    }


    public boolean checkIfTicketExists(int id)
            throws SQLException {
        String sql = "SELECT 1 FROM tickets WHERE ticket_id='" + id + "';";
        ResultSet rs = executeQuery(sql);
        return rs.next();
    }


    public boolean checkIfCommentExists(String player, String comment, int id)
            throws SQLException {
        String sql = "SELECT 1 FROM comments WHERE uuid = '" + player + "' AND ticket_id = '" + id + "' AND comment = '" + comment + "';";
        ResultSet rs = executeQuery(sql);
        return rs.next();
    }


    public void closeConnection()
            throws SQLException {
        connection.close();
    }
}
