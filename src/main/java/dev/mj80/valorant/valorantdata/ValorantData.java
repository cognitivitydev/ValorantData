package dev.mj80.valorant.valorantdata;

import com.github.retrooper.packetevents.PacketEvents;
import dev.mj80.valorant.valorantdata.data.AnticheatData;
import dev.mj80.valorant.valorantdata.data.CoreData;
import dev.mj80.valorant.valorantdata.data.PlayerData;
import dev.mj80.valorant.valorantdata.data.StatData;
import dev.mj80.valorant.valorantdata.listeners.JoinListener;
import dev.mj80.valorant.valorantdata.listeners.PacketListener;
import dev.mj80.valorant.valorantdata.listeners.QuitListener;
import dev.mj80.valorant.valorantdata.penalty.PenaltyManager;
import dev.mj80.valorant.valorantdata.plugins.PluginManager;
import dev.mj80.valorant.valorantdata.plugins.PluginVersion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class ValorantData extends JavaPlugin {
    @Getter private static ValorantData instance;
    @Getter private static File dataPath;
    @Getter private final ArrayList<PlayerData> dataList = new ArrayList<>();
    @Getter private PenaltyManager penaltyManager;
    @Getter private PluginManager pluginManager;
    @Getter private PluginVersion version;
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true).checkForUpdates(false).bStats(true);
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        instance = this;
        dataPath = new File(ValorantData.getInstance().getDataFolder().getAbsolutePath() + File.separator + "players");
        penaltyManager = new PenaltyManager();
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
        PacketEvents.getAPI().init();
        getServer().getOnlinePlayers().forEach(this::createData);
        getServer().getScheduler().runTaskTimer(this, this::saveAll, 6000L, 6000L);
        pluginManager = new PluginManager();
        version = new PluginVersion("data", "0.0.1", 1);
    }
    
    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(this::deleteData);
        PacketEvents.getAPI().terminate();
    }
    
    /**
     * @param player the player to create data for
     * @return the player's new data
     * If the player is offline, AnticheatData and CoreData will be null.
     */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull PlayerData createData(OfflinePlayer player) {
        long start = System.nanoTime();
        if (player.isOnline() && player.getPlayer() != null)
            Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADING_DATA.getMessage());
        if (dataList.stream().noneMatch(data -> playerEquals(data.getPlayer(), player))) {
            PlayerData data = addData(player);
            if (player.isOnline()) {
                double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
                Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(ms));
            }
            return data;
        }
        if (player.isOnline()) {
            double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
            Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(ms));
        }
        return getData(player);
    }
    
    /**
     * Deletes the specified player's cached data.
     * @param player the player to delete all cached data
     */
    public void deleteData(OfflinePlayer player) {
        // clone data list
        // forEach throws an exception when the data list changes mid-loop
        new ArrayList<>(dataList).stream()
                .filter(data -> data != null && playerEquals(data.getPlayer(), player))
                .forEach(this::removeData);
    }
    
    /**
     * Obtains the player's data, including their JSON and plugin data.
     * @param player the player (including offline players) to obtain data from
     * @return the player's data
     * @see PlayerData
     * @see AnticheatData
     * @see CoreData
     * @see StatData
     */
    public PlayerData getData(OfflinePlayer player) {
        if(dataList.stream().noneMatch(data -> playerEquals(data.getPlayer(), player))) {
            return addData(player);
        }
        PlayerData data = dataList.stream().filter(dataPlayer -> playerEquals(dataPlayer.getPlayer(), player)).findFirst().orElse(null);
        if(data != null) {
            if(data.isOnline() != player.isOnline()) {
                data.createOnlineData();
            }
        }
        return data;
    }
    
    /**
     * Saves the player's stats to their JSON file.
     * @param player the player (including offline players) to save data to
     * @see StatData#saveData()
     */
    public void saveData(Player player) {
        dataList.stream().filter(data -> playerEquals(data.getPlayer(), player)).forEach(PlayerData::saveData);
    }
    /**
     * Saves everyone's stats to their JSON file.
     * @see ValorantData#saveData(Player)
     */
    public void saveAll() {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        Collection<? extends Player> staff = players.stream().filter(player -> player.hasPermission("valorant.staff")).toList();
        staff.forEach(player ->
                player.sendMessage(Messages.ADMIN_SAVING_DATA.getMessage(players.size())));
        long start = System.nanoTime();
        players.forEach(this::saveData);
        long end = System.nanoTime();
        double ms = DataUtils.round((float) (end - start)/1000000, 2);
        staff.forEach(player ->
                player.sendMessage(Messages.ADMIN_SAVED_DATA.getMessage(players.size(), ms, DataUtils.round(ms/players.size(), 2)))
        );
    }
    
    /**
     * Force creates the specified player's data.
     * @param player the player to create data for
     * @return the player's new data
     */
    private PlayerData addData(OfflinePlayer player) {
        PlayerData data = new PlayerData(player);
        dataList.add(data);
        return data;
    }
    
    /**
     * Force deletes the specified player's data.
     * @param data the data to remove
     */
    private void removeData(PlayerData data) {
        data.saveData();
        dataList.remove(data);
    }
    
    public void log(String string) {
        getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(string));
    }
    
    private boolean playerEquals(OfflinePlayer p1, OfflinePlayer p2) {
        return p1.getUniqueId().equals(p2.getUniqueId());
    }
}
