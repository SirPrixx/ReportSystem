package de.sirprixx.reportsystem.manager;


import de.sirprixx.reportsystem.enums.TicketStates;

import java.util.Date;
import java.util.HashMap;


public class Ticket {
    private int ticketId;
    private TicketStates state;
    private String assignedTo;
    private String closedBy;
    private String reportingPlayer;
    private String reason;
    private String description;
    private Date dateCreated;
    private Date closedDate;
    private String location;
    private String world;
    private String server;
    private int playerAmount;
    private int commentId;
    private HashMap<Integer, HashMap<String, String>> comments;

    public Ticket(int ticketId, String reportingPlayer, String reason, String description, String location, String world, String server, int playerAmount) {
        this.ticketId = ticketId;
        state = TicketStates.OPEN;
        this.reportingPlayer = reportingPlayer;
        this.reason = reason;
        this.description = description;
        dateCreated = new Date();
        this.location = location;
        this.world = world;
        this.server = server;
        this.playerAmount = playerAmount;

        comments = new HashMap();
    }


    public int getTicketId() {
        return ticketId;
    }


    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }


    public TicketStates getState() {
        return state;
    }


    public void setState(TicketStates state) {
        this.state = state;
    }


    public String getAssignedTo() {
        return assignedTo;
    }


    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }


    public String getClosedBy() {
        return closedBy;
    }


    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }


    public String getReportingPlayer() {
        return reportingPlayer;
    }


    public void setReportingPlayer(String reportingPlayer) {
        this.reportingPlayer = reportingPlayer;
    }


    public String getReason() {
        return reason;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Date getDateCreated() {
        return dateCreated;
    }


    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    public Date getClosedDate() {
        return closedDate;
    }


    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }


    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    public HashMap<Integer, HashMap<String, String>> getComments() {
        return comments;
    }


    public void setComments(HashMap<Integer, HashMap<String, String>> comments) {
        this.comments = comments;
    }


    public String getServer() {
        return server;
    }


    public void setServer(String server) {
        this.server = server;
    }


    public int getPlayerAmount() {
        return playerAmount;
    }


    public void setPlayerAmount(int playerAmount) {
        this.playerAmount = playerAmount;
    }


    public String getWorld() {
        return world;
    }


    public void setWorld(String world) {
        this.world = world;
    }


    public void addComment(String player, String comment) {
        HashMap<String, String> commentMap = new HashMap();
        commentMap.put(comment, player);
        comments.put(Integer.valueOf(commentId), commentMap);
        commentId += 1;
    }
}
