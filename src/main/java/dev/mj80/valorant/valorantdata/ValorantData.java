package dev.mj80.valorant.valorantdata;

import dev.mj80.valorant.valorantdata.data.PlayerData;
import dev.mj80.valorant.valorantdata.listeners.JoinListener;
import dev.mj80.valorant.valorantdata.listeners.QuitListener;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public final class ValorantData extends JavaPlugin {
    @Getter private static ValorantData instance;
    @Getter private static File dataPath;
    @Getter private final ArrayList<PlayerData> dataList = new ArrayList<>();
    
    @Override
    public void onEnable() {
        instance = this;
        dataPath = ValorantData.getInstance().getDataFolder();
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        
        getServer().getOnlinePlayers().forEach(this::createData);
    
        getServer().getScheduler().runTaskTimer(this, this::saveAll, 6000L, 6000L);
    }
    
    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(this::deleteData);
    }
    
    public PlayerData createData(OfflinePlayer player) {
        long start = System.currentTimeMillis();
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADING_DATA.getMessage());
        if(dataList.stream().noneMatch(data -> data.getPlayer() == player)) {
            PlayerData data = new PlayerData(player);
            dataList.add(data);
            if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(System.currentTimeMillis()-start));
            return data;
        }
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.LOADED_DATA.getMessage(System.currentTimeMillis()-start));
        return getData(player);
    }
    
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
    
    public PlayerData getData(OfflinePlayer player) {
        PlayerData data = dataList.stream().filter(dataPlayer -> dataPlayer.getPlayer() == player).findFirst().orElse(null);
        if(data == null) {
            return createData(player);
        }
        return data;
    }
    
    public void saveData(Player player) {
        dataList.stream().filter(data -> data.getPlayer() == player).forEach(PlayerData::saveData);
    }
    
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
