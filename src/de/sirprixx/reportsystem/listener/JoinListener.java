package de.sirprixx.reportsystem.listener;

import de.sirprixx.reportsystem.Main;
import de.sirprixx.reportsystem.manager.TicketManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class JoinListener implements Listener {
    private Main main;
    private TicketManager manager;
    private String prefix = "";


    public JoinListener(Main main, TicketManager manager) {
        this.main = main;
        this.manager = manager;
        prefix = ChatColor.translateAlternateColorCodes('&', (String) main.messageData.get("prefix"));
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if((p.hasPermission("ticket.use")) && (main.getConfig().getBoolean("adminmessages"))) {
            int open = manager.getOpenTicketsSize();
            if(open > 0) {
                p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', translateAmount((String) main.messageData.get("loginNotice"), open)));
            }
        }
    }

    private String translateAmount(String message, int amount) {
        if(message.contains("%amount")) {
            String newMessage = message.replace("%amount", String.valueOf(amount));
            return newMessage;
        }
        return message;
    }
}
