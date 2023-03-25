package dev.mj80.valorant.valorantdata;

import com.github.retrooper.packetevents.PacketEvents;
import dev.mj80.valorant.valorantdata.data.AnticheatData;
import dev.mj80.valorant.valorantdata.data.CoreData;
import dev.mj80.valorant.valorantdata.data.PlayerData;
import dev.mj80.valorant.valorantdata.data.StatData;
import dev.mj80.valorant.valorantdata.listeners.JoinListener;
import dev.mj80.valorant.valorantdata.listeners.PacketListener;
import dev.mj80.valorant.valorantdata.listeners.QuitListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public final class ValorantData extends JavaPlugin {
    @Getter private static ValorantData instance;
    @Getter private static File dataPath;
    @Getter private final ArrayList<PlayerData> dataList = new ArrayList<>();
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(true);
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        instance = this;
        dataPath = ValorantData.getInstance().getDataFolder();
        PacketEvents.getAPI().init();
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
        getServer().getOnlinePlayers().forEach(this::createData);
        getServer().getScheduler().runTaskTimer(this, this::saveAll, 6000L, 6000L);
    }
    
    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(this::deleteData);
        PacketEvents.getAPI().terminate();
    }
    
    private PlayerData addData(OfflinePlayer player) {
        PlayerData data = new PlayerData(player);
        dataList.add(data);
        return data;
    }
    
    private void removeData(OfflinePlayer player) {
        removeData(getData(player));
    }
    private void removeData(PlayerData data) {
        data.saveData();
        dataList.remove(data);
    }
    
    // START OF API
    
    /**
     * @param player Player (including offline players) to create data for.
     * @return The player's new data.
     * If the player is offline, AnticheatData and CoreData will be null.
     */
    public @NotNull PlayerData createData(OfflinePlayer player) {
        long start = System.currentTimeMillis();
        if (player.isOnline())
            Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADING_DATA.getMessage());
        if (dataList.stream().noneMatch(data -> data.getPlayer() == player)) {
            PlayerData data = addData(player);
            if (player.isOnline())
                Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(System.currentTimeMillis() - start));
            return data;
        }
        if (player.isOnline())
            Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(System.currentTimeMillis() - start));
        return getData(player);
    }
    
    /**
     * Deletes the specified player's <strong>cached</strong> data. Does not delete the player's JSON file.
     * @param player Player (including offline players) to delete all cached data.
     */
    public void deleteData(OfflinePlayer player) {
        // clone data list
        // forEach throws an exception when the data list changes mid-loop
        new ArrayList<>(dataList).stream()
                .filter(data -> data != null && data.getPlayer() == player)
                .forEach(stats -> {
            stats.saveData();
            dataList.remove(stats);
        });
    }
    
    /**
     * @param player Player (including offline players) to obtain data from.
     * @return The player's data.
     * @see PlayerData
     * @see AnticheatData
     * @see CoreData
     * @see StatData
     */
    public PlayerData getData(OfflinePlayer player) {
        if(dataList.stream().noneMatch(data -> data.getPlayer() == player)) {
            return addData(player);
        }
        return dataList.stream().filter(dataPlayer -> dataPlayer.getPlayer() == player).findFirst().orElse(null);
    }
    
    /**
     * Saves the player's stats to their JSON file.
     * @param player The player to save data to.
     * @see StatData#saveData()
     */
    public void saveData(Player player) {
        dataList.stream().filter(data -> data.getPlayer() == player).forEach(PlayerData::saveData);
    }
    /**
     * Saves everyone's stats to their JSON file.
     * Includes debug messages to online admins.
     * @see ValorantData#saveData(Player)
     */
    public void saveAll() {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        Collection<? extends Player> staff = players.stream().filter(player -> player.hasPermission("valorant.staff")).toList();
        staff.forEach(player -> {
            player.sendMessage(Messages.ADMIN_SAVING_DATA.getMessage(players.size()));
        });
        long start = System.nanoTime();
        players.forEach(this::saveData);
        long end = System.nanoTime();
        double ms = DataUtils.round((float) (end - start)/1000000, 2);
        staff.forEach(player -> {
            player.sendMessage(Messages.ADMIN_SAVED_DATA.getMessage(players.size(), ms, DataUtils.round(ms/players.size(), 2)));
        });
    }
}
