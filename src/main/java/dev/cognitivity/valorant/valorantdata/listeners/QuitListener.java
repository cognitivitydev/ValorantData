package dev.cognitivity.valorant.valorantdata.listeners;

import dev.cognitivity.valorant.valorantdata.ValorantData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ValorantData.getInstance().deleteData(player);
    }
}