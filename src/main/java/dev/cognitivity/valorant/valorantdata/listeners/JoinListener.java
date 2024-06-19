package dev.cognitivity.valorant.valorantdata.listeners;

import dev.cognitivity.valorant.valorantdata.ValorantData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ValorantData.getInstance().createData(player);
    }
}
