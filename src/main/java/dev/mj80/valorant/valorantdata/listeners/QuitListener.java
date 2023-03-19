package dev.mj80.valorant.valorantdata.listeners;

import dev.mj80.valorant.valorantdata.ValorantData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // delete data next tick, prevents errors from core/anticheat from the quit event
        ValorantData.getInstance().getServer().getScheduler().runTask(ValorantData.getInstance(), () -> ValorantData.getInstance().deleteData(player));
    }
}