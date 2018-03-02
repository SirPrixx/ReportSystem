package de.sirprixx.reportsystem;

import de.sirprixx.reportsystem.commands.ReportCommand;
import de.sirprixx.reportsystem.data.Messages;
import de.sirprixx.reportsystem.database.MySQL;
import de.sirprixx.reportsystem.database.Queries;
import de.sirprixx.reportsystem.database.SQLite;
import de.sirprixx.reportsystem.listener.JoinListener;
import de.sirprixx.reportsystem.manager.TicketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * This plugin was made by SirPrixx.
 */
public class Main extends JavaPlugin {
    public HashMap<String, String> messageData = new HashMap();
    private Connection connection = null;
    private Messages messages;
    private TicketManager manager;
    public String prefix = "";

    public static void logger(String text) {
        String string = ChatColor.translateAlternateColorCodes('&', text);
        Bukkit.getConsoleSender().sendMessage(string);
    }

    public void onEnable() {

        saveDefaultConfig();

        messages = new Messages(this);
        messageData = messages.getMessageData();

        if(getConfig().getBoolean("database")) {
            String hostname = getConfig().getString("hostname");
            String port = getConfig().getString("port");
            String database = getConfig().getString("database");
            String username = getConfig().getString("username");
            String password = getConfig().getString("password");
            MySQL MySQL = new MySQL(this, hostname, port, database, username, password);
            try {
                connection = MySQL.openConnection();
                createMySQLTables();
            }catch(ClassNotFoundException e) {
                e.printStackTrace();
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }else {
            SQLite sqlite = new SQLite(this, "/tickets.db");
            try {
                connection = sqlite.openConnection();
                createSQLiteTables();
            }catch(ClassNotFoundException e) {
                e.printStackTrace();
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }

        manager = new TicketManager(this, new Queries(connection), getConfig().getInt("maxtickets"));
        try {
            manager.loadInTickets();
        }catch(SQLException e) {
            logger("There was a error loading in the tickets from the database!");
            e.printStackTrace();
        }
        getCommand("report").setExecutor(new ReportCommand(this, manager));
        getCommand("ticket").setExecutor(new ReportCommand(this, manager));
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new JoinListener(this, manager), this);
        manager.startTask();

        prefix = ChatColor.translateAlternateColorCodes('&', (String) this.messageData.get("prefix"));
        logger("§cReportSystem §8> §7WHOOOOSH. ReportSystem is running on this server!");
    }

    public void onDisable() {
        manager.onDisableUpdate();
        try {
            connection.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }
        logger("§cReportSystem §8> §7WHOOOOSH. ReportSystem is §cnot§7 running on this server!");
    }

    public void reloadMessages() {
        messages.loadMessages();
    }

    private void createMySQLTables() {
        Queries query = new Queries(connection);
        boolean created = query.createMySQLTable();
        if(!created) {
            logger("Error while creating MySQL database table. Do you have the correct database details in the config?");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void createSQLiteTables() {
        Queries query = new Queries(connection);
        boolean created = query.createSQLiteTable();
        if(!created) {
            logger("Error while creating SQLite database table. Please report this to the plugin developer");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
