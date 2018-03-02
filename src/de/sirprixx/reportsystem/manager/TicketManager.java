package de.sirprixx.reportsystem.manager;

import de.sirprixx.reportsystem.Main;
import de.sirprixx.reportsystem.database.Queries;
import de.sirprixx.reportsystem.enums.TicketStates;
import de.sirprixx.reportsystem.listener.TicketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class TicketManager {
    private Main main;
    private Queries query;
    private int currentTicketID = 1;
    private String prefix;
    private int ticketID;
    private int maxTickets;
    private ArrayList<Ticket> allTickets;
    private ArrayList<Ticket> ticketsToUpdate;
    private ArrayList<Ticket> commentsToUpdate;
    private HashMap<String, Integer> claimedAmount;


    public TicketManager(Main main, Queries query, int maxTickets) {
        this.main = main;
        this.query = query;
        this.maxTickets = maxTickets;
        allTickets = new ArrayList();
        ticketsToUpdate = new ArrayList();
        commentsToUpdate = new ArrayList();
        claimedAmount = new HashMap();

        prefix = ChatColor.translateAlternateColorCodes('&', (String) main.messageData.get("prefix"));
    }


    public void createTicket(CommandSender sender, String player, String reason, String description, Location l, String server, int playerAmount) {
        String world = l.getWorld().toString();
        String[] tempWorld = world.split("=");
        world = tempWorld[1].substring(0, tempWorld[1].length() - 1);

        long x = Math.round(l.getX());
        long y = Math.round(l.getY());
        long z = Math.round(l.getZ());
        String location = "X:" + x + " Y:" + y + " Z:" + z;

        Ticket newTicket = new Ticket(currentTicketID, player, reason, description, location, world, server, playerAmount);
        ticketID = newTicket.getTicketId();

        allTickets.add(newTicket);
        ticketsToUpdate.add(newTicket);
        currentTicketID += 1;
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("createTicket"))));

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("ticket.use")) {
                p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("adminUpdate"))));
            }
        }
        main.getServer().getPluginManager().callEvent(new TicketListener("create", newTicket));
    }


    public Ticket createTicket(String player, TicketStates ticketState, String reason, String description, String location, Date createdDate, String world, String server, int playerAmount) {
        Ticket newTicket = new Ticket(currentTicketID, player, reason, description, location, world, server, playerAmount);
        newTicket.setState(ticketState);
        newTicket.setDateCreated(createdDate);

        allTickets.add(newTicket);
        currentTicketID += 1;
        return newTicket;
    }


    public void createComment(CommandSender sender, String player, String comment, int id) {
        Ticket ticket = getTicket(id);
        if(ticket != null) {
            ticket.addComment(player, comment);
            commentsToUpdate.add(ticket);
            ticketID = ticket.getTicketId();
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("createComment"))));
            main.getServer().getPluginManager().callEvent(new TicketListener("comment", ticket));
        }
    }


    public void startTask() {
        long delay = main.getConfig().getLong("updateTime") * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(main, new BukkitRunnable() {
            public void run() {
                boolean successful = false;
                int noneUpdated = 0;
                if(!ticketsToUpdate.isEmpty()) {
                    Iterator<Ticket> it = ticketsToUpdate.iterator();
                    while(it.hasNext()) {
                        Ticket ticket = (Ticket) it.next();
                        if(ticket.getState().equals(TicketStates.OPEN)) {
                            try {
                                if(!query.checkIfTicketExists(ticket.getTicketId())) {
                                    successful = query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
                                }else {
                                    successful = query.setUnclaimed(ticket.getTicketId());
                                }
                            }catch(SQLException e) {
                                e.printStackTrace();
                            }
                        }else if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                            successful = query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
                        }else if(ticket.getState().equals(TicketStates.CLOSED)) {
                            successful = query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
                        }
                        if(!successful) {
                            Bukkit.getLogger().severe("§cReportSystem §8> §7There was a problem with updating 1 ticket to the database");
                        }
                        it.remove();
                        noneUpdated = 1;
                    }
                }
                if(!commentsToUpdate.isEmpty()) {
                    Iterator<Ticket> it = commentsToUpdate.iterator();
                    while(it.hasNext()) {
                        Ticket ticket = (Ticket) it.next();
                        HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
                        Iterator localIterator2;
                        for(Iterator localIterator1 = commentsMap.entrySet().iterator(); localIterator1.hasNext();

                            localIterator2.hasNext()) {
                            Map.Entry<Integer, HashMap<String, String>> entryComments = (Map.Entry) localIterator1.next();
                            HashMap<String, String> comments = (HashMap) entryComments.getValue();
                            localIterator2 = comments.entrySet().iterator();
                            //continue;
                            Map.Entry<String, String> entry = (Map.Entry) localIterator2.next();
                            try {
                                if(!query.checkIfCommentExists((String) entry.getValue(), (String) entry.getKey(), ticket.getTicketId())) {
                                    query.insertComment((String) entry.getValue(), (String) entry.getKey(), ticket.getTicketId());
                                    noneUpdated = 1;
                                }
                            }catch(SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        it.remove();
                    }
                }
                if(main.getConfig().getBoolean("database")) {
                    int currentTickets = allTickets.size();
                    allTickets.clear();
                    try {
                        loadInTickets();
                    }catch(SQLException e) {
                        e.printStackTrace();
                    }
                    int newTickets = allTickets.size();
                    if(newTickets > currentTickets) {
                        noneUpdated = 2;
                    }
                }
                if(noneUpdated == 0) {
                    query.keepConnectionAlive();
                    main.logger("§cReportSystem §8> §7Database connection has been kept alive");
                }else if(noneUpdated == 1) {
                    main.logger("§cReportSystem §8> §7Database has sucessfully updated");
                }else {
                    main.logger("§cReportSystem §8> §7New tickets/comments have been added to the server");
                }
            }
        }, 0L, delay);
    }


    public void setTicketAssigned(CommandSender sender, String player, int id) {
        Ticket ticket = getTicket(id);
        ticketID = id;
        int amount = 0;
        if(claimedAmount.containsKey(player)) {
            amount = ((Integer) claimedAmount.get(player)).intValue();
        }
        if(ticket != null) {
            if(ticket.getState().equals(TicketStates.OPEN)) {
                if(amount < maxTickets) {
                    ticket.setAssignedTo(player);
                    ticket.setState(TicketStates.ASSIGNED);
                    ticketsToUpdate.add(ticket);
                    increaseClaimedAmount(player);
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("claimTicket"))));
                    main.getServer().getPluginManager().callEvent(new TicketListener("claim", ticket));
                }else {
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("maxTickets"))));
                }
            }else {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("alreadyClaimed"))));
            }
        }else {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noId"))));
        }
    }


    public void setTicketClosed(CommandSender sender, String player, int id) {
        Ticket ticket = getTicket(id);
        ticketID = id;
        if(ticket != null) {
            if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                boolean ableToClose = false;
                if((!main.getConfig().getBoolean("allowotherstoclose")) &&
                        (ticket.getAssignedTo().equals(player))) {
                    ableToClose = true;
                }

                if(ableToClose) {
                    ticket.setClosedBy(player);
                    ticket.setClosedDate(new Date());
                    ticket.setState(TicketStates.CLOSED);
                    ticketsToUpdate.add(ticket);
                    decreaseClaimedAmount(ticket.getAssignedTo());
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closeTicket"))));
                    if(main.getConfig().getBoolean("closenotice")) {
                        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                            if(p.hasPermission("ticket.admin")) {
                                p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages((String) main.messageData.get("closeNotice")), player)));
                                main.getServer().getPluginManager().callEvent(new TicketListener("close", ticket));
                            }
                        }
                    }
                }
            }else {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("alreadyClosed"))));
            }
        }else {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noId"))));
        }
    }


    public void setUnclaimed(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        ticketID = id;
        if(ticket != null) {
            if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                decreaseClaimedAmount(ticket.getAssignedTo());
                ticket.setAssignedTo(null);
                ticket.setState(TicketStates.OPEN);
                ticketsToUpdate.add(ticket);
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("unclaimTicket"))));
                main.getServer().getPluginManager().callEvent(new TicketListener("unclaim", ticket));
            }else {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("alreadyUnclaimed"))));
            }
        }else {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noId"))));
        }
    }


    public void printTicketInfo(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        ticketID = id;
        if(ticket != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketTitle"))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketId"))) + ticket.getTicketId());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketStatus"))) + ticket.getState().toString());

            if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
            }else if(ticket.getState().equals(TicketStates.CLOSED)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketClosedBy"))) + getName(ticket.getClosedBy()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketClosedDate"))) + ticket.getClosedDate().toString());
            }
            sender.sendMessage("");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketReportingPlayer"))) + getName(ticket.getReportingPlayer()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketReason"))) + ticket.getReason());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketDescription"))) + ticket.getDescription());
            if(main.getConfig().getBoolean("players")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketAmount"))) + ticket.getPlayerAmount());
            }
            if(main.getConfig().getBoolean("bungeecord")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketServer"))) + ticket.getServer());
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketDateCreated"))) + ticket.getDateCreated().toString());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketLocation"))) + ticket.getLocation() + " World: " + ticket.getWorld());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkSize(checkMessages((String) main.messageData.get("ticketComments")), ticket.getComments().size())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketFooter"))));
        }else {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noId"))));
        }
    }


    public void teleportPlayer(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        if(ticket != null) {
            Player p = (Player) sender;
            String location = ticket.getLocation();
            String[] temp = location.split(" ");
            String[] tempx = temp[0].split(":");
            String[] tempy = temp[1].split(":");
            String[] tempz = temp[2].split(":");
            Double x = Double.valueOf(Double.parseDouble(tempx[1].trim()));
            Double y = Double.valueOf(Double.parseDouble(tempy[1].trim()));
            Double z = Double.valueOf(Double.parseDouble(tempz[1].trim()));
            Location l = new Location(Bukkit.getWorld(ticket.getWorld()), x.doubleValue(), y.doubleValue(), z.doubleValue());
            p.teleport(l);
            p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("teleport"))));
        }
    }


    public void printTicketStats(CommandSender sender) {
        int open = 0;
        int assigned = 0;
        int closed = 0;
        for(Ticket ticket : allTickets) {
            if(ticket.getState().equals(TicketStates.OPEN)) {
                open++;
            }else if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                assigned++;
            }else if(ticket.getState().equals(TicketStates.CLOSED)) {
                closed++;
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("statsTitle"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("statsOpen"))) + open);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("statsAssigned"))) + assigned);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("statsClosed"))) + closed);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("statsFooter"))));
    }


    public void printOpenTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsOpen();
        String view = "";
        if(!tickets.isEmpty()) {
            for(Ticket ticket : tickets) {
                view = view + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openId"))) + ticket.getTicketId() + "\n";
            }
        }else {
            view = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openNoTickets")));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openTitle"))));
        sender.sendMessage(view);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openClaim"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("openFooter"))));
    }


    public void printAssignedTickets(CommandSender sender, String player) {
        ArrayList<Ticket> tickets = getTicketsAssignedForPlayer(player);
        String view = "";
        if(!tickets.isEmpty()) {
            for(Ticket ticket : tickets) {
                view = view + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("assignedId"))) + ticket.getTicketId() + "\n";
            }
        }else {
            view = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("assignedNoTickets")));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("assignedTitle"))));
        sender.sendMessage(view);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("assignedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("assignedFooter"))));
    }

    public void printAllAssignedTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsAssignedForAllPlayers();
        String view = "";
        if(!tickets.isEmpty()) {
            for(Ticket ticket : tickets) {
                ticketID = ticket.getTicketId();
                view = view + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages((String) main.messageData.get("allAssignedId")), ticket.getAssignedTo())) + "\n";
            }
        }else {
            view = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allAssignedNoTickets")));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allAssignedTitle"))));
        sender.sendMessage(view);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allAssignedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allAssignedFooter"))));
    }


    public void printClosedTickets(CommandSender sender, String player) {
        ArrayList<Ticket> tickets = getTicketsClosedForPlayer(player);
        String view = "";
        if(!tickets.isEmpty()) {
            for(Ticket ticket : tickets) {
                view = view + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closedId"))) + ticket.getTicketId() + "\n";
            }
        }else {
            view = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closedNoTickets")));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closedTitle"))));
        sender.sendMessage(view);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("closedFooter"))));
    }


    public void printAllClosedTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsClosedForAllPlayers();
        String view = "";
        if(!tickets.isEmpty()) {
            for(Ticket ticket : tickets) {
                ticketID = ticket.getTicketId();
                view = view + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages((String) main.messageData.get("allClosedId")), ticket.getClosedBy())) + "\n";
            }
        }else {
            view = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allClosedNoTickets")));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allClosedTitle"))));
        sender.sendMessage(view);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allClosedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("allClosedFooter"))));
    }


    public void printComments(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        if(ticket != null) {
            String commentsList = "";
            ticketID = ticket.getTicketId();
            HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
            if(!commentsMap.isEmpty()) {
                for(int i = 0; i < commentsMap.size(); i++) {
                    HashMap<String, String> comments = (HashMap) commentsMap.get(Integer.valueOf(i));
                    for(Map.Entry<String, String> entry : comments.entrySet()) {
                        String uuid = (String) entry.getValue();
                        String comment = (String) entry.getKey();
                        commentsList = commentsList + ChatColor.translateAlternateColorCodes('&', checkComment(checkPlayer(checkMessages((String) main.messageData.get("comment")), uuid), comment)) + "\n";
                    }
                }
            }else {
                commentsList = ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noComments")));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("commentsTitle"))));
            sender.sendMessage(commentsList);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("commentsFooter"))));
        }else {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noId"))));
        }
    }


    public int getOpenTicketsSize() {
        ArrayList<Ticket> tickets = getTicketsOpen();
        return tickets.size();
    }


    public void loadInTickets()
            throws SQLException {
        ResultSet rs = query.loadAllTickets();
        if(rs != null) {
            while(rs.next()) {
                String id = rs.getString("ticket_id");
                String player = rs.getString("uuid");
                TicketStates ticketState = TicketStates.valueOf(rs.getString("status"));
                String reason = rs.getString("reason");
                String description = rs.getString("description");
                Date createdDate = new Date(rs.getLong("created_date"));
                String location = rs.getString("location");
                String world = rs.getString("world");
                String server = rs.getString("server_name");
                int playerAmount = rs.getInt("player_amount");

                currentTicketID = Integer.parseInt(id);

                Ticket ticket = createTicket(player, ticketState, reason, description, location, createdDate, world, server, playerAmount);

                ResultSet set = query.loadAllComments(ticket.getTicketId());
                if(set != null) {
                    while(set.next()) {
                        String uuid = set.getString("uuid");
                        String comment = set.getString("comment");
                        ticket.addComment(uuid, comment);
                    }
                }

                if(ticketState.equals(TicketStates.ASSIGNED)) {
                    String assignedTo = rs.getString("assigned_to");
                    ticket.setAssignedTo(assignedTo);
                    increaseClaimedAmount(assignedTo);
                }else if(ticketState.equals(TicketStates.CLOSED)) {
                    String closedBy = rs.getString("closed_by");
                    Date closedDate = rs.getDate("closed_date");
                    ticket.setClosedBy(closedBy);
                    ticket.setClosedDate(closedDate);
                }
            }
        }
    }


    public void onDisableUpdate() {
        if(ticketsToUpdate.size() > 0) {
            for(Ticket ticket : ticketsToUpdate) {
                if(ticket.getState().equals(TicketStates.OPEN)) {
                    try {
                        if(query.checkIfTicketExists(ticket.getTicketId())) {
                            query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
                        }else {
                            query.setUnclaimed(ticket.getTicketId());
                        }
                    }catch(SQLException e) {
                        e.printStackTrace();
                        main.logger("There was a problem with updating 1 ticket to the database");
                    }
                }else if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                    query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
                }else if(ticket.getState().equals(TicketStates.CLOSED)) {
                    query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
                }
            }
        }
        if(!commentsToUpdate.isEmpty()) {
            Iterator<Ticket> it = commentsToUpdate.iterator();
            while(it.hasNext()) {
                Ticket ticket = (Ticket) it.next();
                HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
                Iterator localIterator3;
                for(Iterator localIterator2 = commentsMap.entrySet().iterator(); localIterator2.hasNext();

                    localIterator3.hasNext()) {
                    Map.Entry<Integer, HashMap<String, String>> entryComments = (Map.Entry) localIterator2.next();
                    HashMap<String, String> comments = (HashMap) entryComments.getValue();
                    localIterator3 = comments.entrySet().iterator();
                    Map.Entry<String, String> entry = (Map.Entry) localIterator3.next();
                    try {
                        if(!query.checkIfCommentExists((String) entry.getValue(), (String) entry.getKey(), ticket.getTicketId())) {
                            query.insertComment((String) entry.getValue(), (String) entry.getKey(), ticket.getTicketId());
                        }
                    }catch(SQLException e) {
                        e.printStackTrace();
                    }
                }

                it.remove();
            }
        }
        main.logger("§cReportSystem §8> §7Database has been successfully updated");
    }

    private Ticket getTicket(int id) {
        Ticket selectedTicket = null;
        for(Ticket ticket : allTickets) {
            if(ticket.getTicketId() == id) {
                selectedTicket = ticket;
                break;
            }
        }
        return selectedTicket;
    }

    private ArrayList<Ticket> getTicketsOpen() {
        ArrayList<Ticket> tickets = new ArrayList();
        for(Ticket ticket : allTickets) {
            if(ticket.getState().equals(TicketStates.OPEN)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsAssignedForPlayer(String player) {
        ArrayList<Ticket> tickets = new ArrayList();
        for(Ticket ticket : allTickets) {
            if((ticket.getState().equals(TicketStates.ASSIGNED)) &&
                    (ticket.getAssignedTo().equals(player))) {
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    private ArrayList<Ticket> getTicketsAssignedForAllPlayers() {
        ArrayList<Ticket> tickets = new ArrayList();
        for(Ticket ticket : allTickets) {
            if(ticket.getState().equals(TicketStates.ASSIGNED)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsClosedForPlayer(String player) {
        ArrayList<Ticket> tickets = new ArrayList();
        for(Ticket ticket : allTickets) {
            if((ticket.getState().equals(TicketStates.CLOSED)) &&
                    (ticket.getClosedBy().equals(player))) {
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    private ArrayList<Ticket> getTicketsClosedForAllPlayers() {
        ArrayList<Ticket> tickets = new ArrayList();
        for(Ticket ticket : allTickets) {
            if(ticket.getState().equals(TicketStates.CLOSED)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private String checkMessages(String message) {
        if(message.contains("%id")) {
            String newMessage = message.replace("%id", String.valueOf(ticketID));
            return newMessage;
        }
        return message;
    }

    private String checkSize(String message, int size) {
        if(message.contains("%amount")) {
            String newMessage = message.replace("%amount", String.valueOf(size));
            return newMessage;
        }
        return message;
    }

    private String checkPlayer(String message, String player) {
        if(message.contains("%player")) {
            String newMessage = message.replace("%player", getName(player));
            return newMessage;
        }
        return message;
    }

    private String checkComment(String message, String comment) {
        if(message.contains("%comment")) {
            String newMessage = message.replace("%comment", comment);
            return newMessage;
        }
        return message;
    }

    private void increaseClaimedAmount(String uuid) {
        if(claimedAmount.containsKey(uuid)) {
            Integer amount = (Integer) claimedAmount.get(uuid);
            amount = Integer.valueOf(amount.intValue() + 1);
            claimedAmount.put(uuid, amount);
        }else {
            claimedAmount.put(uuid, Integer.valueOf(1));
        }
    }

    private void decreaseClaimedAmount(String uuid) {
        if(claimedAmount.containsKey(uuid)) {
            Integer amount = (Integer) claimedAmount.get(uuid);
            amount = Integer.valueOf(amount.intValue() - 1);
            claimedAmount.put(uuid, amount);
        }
    }

    private String getName(String uuid) {
        return Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
    }
}
