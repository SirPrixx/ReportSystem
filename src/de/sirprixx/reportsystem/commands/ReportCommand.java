package de.sirprixx.reportsystem.commands;

import de.sirprixx.reportsystem.Main;
import de.sirprixx.reportsystem.manager.TicketManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This main was made by SirPrixx.
 */
public class ReportCommand implements CommandExecutor {
    private Main main;
    private TicketManager manager;
    private String prefix = "";


    private int id;


    public ReportCommand(Main main, TicketManager manager) {
        this.main = main;
        this.manager = manager;
        prefix = ChatColor.translateAlternateColorCodes('&', (String) main.messageData.get("prefix"));
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("report")) {
            if((sender instanceof Player)) {
                Player player = (Player) sender;
                if(player.hasPermission("report.use")) {
                    if(args.length > 0) {
                        String description = "";
                        for(int i = 1; i < args.length; i++) {
                            description = description + " " + args[i].toString();
                        }
                        manager.createTicket(sender, player.getUniqueId().toString(), args[0], description, player.getLocation(), main.getServer().getServerName(), org.bukkit.Bukkit.getOnlinePlayers().size());
                    }else {
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("reportHelp"))));
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("createHelp"))));
                    }
                }else {
                    sender.sendMessage(checkMessages((String) main.messageData.get("noPermission")));
                }
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("mustBePlayer"))));
            return false;
        }
        if(cmd.getName().equalsIgnoreCase("ticket")) {
            if((sender instanceof Player)) {
                Player player = (Player) sender;
                if(args.length > 0) {
                    if(args[0].equalsIgnoreCase("view")) {
                        if(args.length > 1) {
                            if(args[1].equalsIgnoreCase("open")) {
                                if(player.hasPermission("ticket.view.open")) {
                                    manager.printOpenTickets(sender);
                                }else {
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                                }
                            }else if(args[1].equalsIgnoreCase("assigned")) {
                                if(args.length > 2) {
                                    if(args[2].equalsIgnoreCase("all")) {
                                        if(player.hasPermission("ticket.view.assigned.all")) {
                                            manager.printAllAssignedTickets(sender);
                                        }else {
                                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                                        }
                                    }
                                }else if(player.hasPermission("ticket.view.assigned")) {
                                    manager.printAssignedTickets(sender, player.getUniqueId().toString());
                                }else {
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                                }
                            }else if(args[1].equalsIgnoreCase("closed")) {
                                if(args.length > 2) {
                                    if(args[2].equalsIgnoreCase("all")) {
                                        if(player.hasPermission("ticket.view.closed.all")) {
                                            manager.printAllClosedTickets(sender);
                                        }else {
                                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                                        }
                                    }
                                }else if(player.hasPermission("ticket.view.closed")) {
                                    manager.printClosedTickets(sender, player.getUniqueId().toString());
                                }else {
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                                }
                            }
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp1"))));
                        }
                    }else if(args[0].equalsIgnoreCase("info")) {
                        if(player.hasPermission("ticket.info")) {
                            if(args.length > 1) {
                                id = Integer.parseInt(args[1]);
                                manager.printTicketInfo(sender, id);
                            }else {
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketInfoError"))));
                            }
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }else if(args[0].equalsIgnoreCase("comment")) {
                        if((player.hasPermission("ticket.comments")) &&
                                (args.length > 1)) {
                            if(args[1].equalsIgnoreCase("add")) {
                                id = Integer.parseInt(args[2]);
                                String comment = "";
                                for(int i = 3; i < args.length; i++) {
                                    comment = comment + " " + args[i].toString();
                                }
                                manager.createComment(sender, player.getUniqueId().toString(), comment, id);
                            }else if(args[1].equalsIgnoreCase("view")) {
                                id = Integer.parseInt(args[2]);
                                manager.printComments(sender, id);
                            }
                        }
                    }else if(args[0].equalsIgnoreCase("claim")) {
                        if(player.hasPermission("ticket.admin")) {
                            if(args.length > 1) {
                                id = Integer.parseInt(args[1]);
                                manager.setTicketAssigned(sender, player.getUniqueId().toString(), id);
                            }else {
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketClaimError"))));
                            }
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }else if(args[0].equalsIgnoreCase("close")) {
                        if(player.hasPermission("ticket.admin")) {
                            if(args.length > 1) {
                                id = Integer.parseInt(args[1]);
                                manager.setTicketClosed(sender, player.getUniqueId().toString(), id);
                            }else {
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketCloseError"))));
                            }
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }else if(args[0].equalsIgnoreCase("teleport")) {
                        if(player.hasPermission("ticket.teleport")) {
                            if(args.length > 1) {
                                id = Integer.parseInt(args[1]);
                                manager.teleportPlayer(sender, id);
                            }else {
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketTeleportError"))));
                            }
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }else if(args[0].equalsIgnoreCase("stats")) {
                        if(player.hasPermission("ticket.stats")) {
                            manager.printTicketStats(sender);
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }else if(args[0].equalsIgnoreCase("unclaim")) {
                        if(player.hasPermission("ticket.admin")) {
                            manager.setUnclaimed(sender, id);
                        }
                    }else if(args[0].equalsIgnoreCase("reload")) {
                        if(player.hasPermission("ticket.reload")) {
                            main.reloadConfig();
                            main.reloadMessages();
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', (String) main.messageData.get("ticketReload")));
                        }else {
                            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("noPermission"))));
                        }
                    }
                }else {
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelpTitle"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp1"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp2"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp3"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp4"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp5"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("ticketHelp6"))));
                }
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages((String) main.messageData.get("mustBePlayer"))));
            return false;
        }

        return false;
    }

    private String checkMessages(String message) {
        if(message.contains("%id")) {
            String newMessage = message.replace("%id", String.valueOf(id));
            return newMessage;
        }
        return message;
    }
}