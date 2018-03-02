package de.sirprixx.reportsystem.listener;

import de.sirprixx.reportsystem.manager.Ticket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TicketListener extends Event {
    private static final HandlerList HANDLERS = new HandlerList();


    private final Ticket ticket;


    private final String action;


    public TicketListener(String action, Ticket ticket) {
        this.ticket = ticket;
        this.action = action;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public String getAction() {
        return action;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
