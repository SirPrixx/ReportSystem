package de.sirprixx.reportsystem.data;

import de.sirprixx.reportsystem.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * This plugin was made by SirPrixx.
 */
public class Messages {
    public HashMap<String, String> messageData;
    private Main main;
    private String prefix = "&7";

    public Messages(Main main) {
        this.main = main;
        messageData = new HashMap();
    }


    public HashMap<String, String> getMessageData() {
        File file = new File(main.getDataFolder() + "/messages.yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
                saveMessages();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        return loadMessages();
    }


    public HashMap<String, String> loadMessages() {
        File file = new File(main.getDataFolder() + "/messages.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for(String message : cfg.getConfigurationSection("").getKeys(false)) {
            messageData.put(message, cfg.getString(message));
        }
        return messageData;
    }

    private void saveMessages() {
        setMessage("prefix", "&cReportSystem &8> &7");
        setMessage("createTicket", prefix + "Ticket has successfully been created for you with the ticket id &c%id&7. Use this id when adding comments.");
        setMessage("createComment", prefix + "Comment has successfully been added to ticket &c%id");
        setMessage("adminUpdate", prefix + "A new report has been submitted! To view report do &c/ticket info %id");
        setMessage("reportHelp", prefix + "The following commands are available:");
        setMessage("createHelp", prefix + "/report <reason> <description> To open a report");
        setMessage("ticketHelpTitle", prefix + "The following commands are available");
        setMessage("ticketHelp1", prefix + "/ticket view <open | asssigned | closed> <all>  &7To view which tickets are open");
        setMessage("ticketHelp2", prefix + "/ticket info <ticket id> To view more information about a report");
        setMessage("ticketHelp3", prefix + "/ticket claim <ticket id> Claim a ticket for you to resolve");
        setMessage("ticketHelp4", prefix + "/ticket close <ticket id> Close a ticket once you resolved the report");
        setMessage("ticketHelp5", prefix + "/ticket teleport <ticket id> Teleport to the location of the report");
        setMessage("ticketHelp6", prefix + "/ticket stats &7View how many reports are open, assigned or closed");
        setMessage("noId", prefix + "No ticket with that ID");
        setMessage("claimTicket", prefix + "You have successfully claimed this ticket");
        setMessage("alreadyClaimed", prefix + "This ticket has already been claimed");
        setMessage("alreadyClosed", prefix + "This ticket has already been closed");
        setMessage("alreadyUnclaimed", prefix + "This ticket can't be unclaimed");
        setMessage("closeTicket", prefix + "You have successfully closed this ticket");
        setMessage("unclaimTicket", prefix + "You have successfully unclaimed this ticket");
        setMessage("teleport", prefix + "You have been teleported to the location of the report");
        setMessage("closeNotice", prefix + "Ticket &c%id&7 has just been closed by &c%player");
        setMessage("ticketTitle", prefix + "==============&cReportSystem&7==============");
        setMessage("ticketId", prefix + "Ticket ID: &c");
        setMessage("ticketStatus", prefix + "Status of ticket: &c");
        setMessage("ticketAssignedTo", prefix + "Assigned To: &c");
        setMessage("ticketClosedBy", prefix + "Closed By: &c");
        setMessage("ticketClosedDate", prefix + "Closed Date: &c");
        setMessage("ticketReportingPlayer", prefix + "Reporting Player: &c");
        setMessage("ticketReason", prefix + "Reason: &c");
        setMessage("ticketDescription", prefix + "Description: &c");
        setMessage("ticketAmount", prefix + "Amount of Players online: &c");
        setMessage("ticketServer", prefix + "Server: &c");
        setMessage("ticketDateCreated", prefix + "Date of report: &c");
        setMessage("ticketLocation", prefix + "Location of report: &c");
        setMessage("ticketComments", prefix + "Comments: &c%amount §7comment(s)");
        setMessage("ticketFooter", prefix + "=====================================");
        setMessage("statsTitle", prefix + "==============&cStats&7==============");
        setMessage("statsOpen", prefix + "Open: &c");
        setMessage("statsAssigned", prefix + "Assigned: &c");
        setMessage("statsClosed", prefix + "Closed: &c");
        setMessage("statsFooter", prefix + "================================");
        setMessage("openTitle", prefix + "==============&cOpen Tickets&7==============");
        setMessage("openId", prefix + "Ticket ID: &6");
        setMessage("openInfo", prefix + "Do /ticket info <number> To view more info on a report");
        setMessage("openClaim", prefix + "Do /ticket claim <number> To assign the ticket to yourself");
        setMessage("openFooter", prefix + "=======================================");
        setMessage("assignedTitle", prefix + "==============&cAssigned Tickets&7==============");
        setMessage("assignedId", prefix + "Ticket ID: &c");
        setMessage("assignedInfo", prefix + "Do /ticket info <number> To view more info on a report");
        setMessage("assignedFooter", prefix + "=======================================");
        setMessage("allAssignedTitle", prefix + "==============&cAll Assigned Tickets&7==============");
        setMessage("allAssignedId", prefix + "Ticket ID: &c%id &7is assigned to &c%player");
        setMessage("allAssignedInfo", prefix + "Do /ticket info <number> To view more info on a report");
        setMessage("allAssignedFooter", prefix + "=======================================");
        setMessage("closedTitle", prefix + "==============&cClosed Tickets&7==============");
        setMessage("closedId", prefix + "Ticket ID: &c");
        setMessage("closedInfo", prefix + "Do /ticket info <number> To view more info on a report");
        setMessage("closedFooter", prefix + "=======================================");
        setMessage("allClosedTitle", prefix + "==============&cAll Closed Tickets&7==============");
        setMessage("allClosedId", prefix + "Ticket ID: &c%id &7was closed by &c%player");
        setMessage("allClosedInfo", prefix + "Do /ticket info <number> To view more info on a report");
        setMessage("allClosedFooter", prefix + "=======================================");
        setMessage("commentsTitle", prefix + "==============&cComments &7for Ticket &c%id&7==============");
        setMessage("comment", prefix + "%player: &c%comment");
        setMessage("commentsFooter", prefix + "==============================================");
        setMessage("openNoTickets", prefix + "No Open tickets");
        setMessage("assignedNoTickets", prefix + "You have no assigned tickets");
        setMessage("allAssignedNoTickets", prefix + "No assigned tickets");
        setMessage("closedNoTickets", prefix + "You have no closed tickets");
        setMessage("allClosedNoTickets", prefix + "No closed tickets");
        setMessage("noComments", prefix + "No comments for this ticket");
        setMessage("loginNotice", prefix + "There are currently %amount open tickets that need assigning!");
        setMessage("createFailed", prefix + "Failed to create ticket. Please contact a Admin if this problem continues");
        setMessage("failClaimTicket", prefix + "Failed to claim this ticket, please contact a admin if this continues to happen!");
        setMessage("failCloseTicket", prefix + "Failed to close this ticket, please contact a admin if this continues to happen!");
        setMessage("ticketInfoError", prefix + "&cERROR! You must enter /ticket info <id>");
        setMessage("ticketClaimError", prefix + "&cERROR! You must enter /ticket claim <id>");
        setMessage("ticketCloseError", prefix + "&cERROR! You must enter /ticket close <id>");
        setMessage("ticketTeleportError", prefix + "&cERROR! You must enter /ticket teleport <id>");
        setMessage("ticketReload", prefix + "You have successfully reloaded the configs!");
        setMessage("noPermission", prefix + "You do not have permission to use this command!");
        setMessage("mustBePlayer", prefix + "You must be a player to use this command");
        setMessage("maxTickets", prefix + "&cYou have reached the max amount of tickets you can claim");
    }

    private void setMessage(String name, String message) {
        File file = new File(main.getDataFolder() + "/messages.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if(!cfg.isSet(name)) {
            cfg.set(name, message);
            try {
                cfg.save(file);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
