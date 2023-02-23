package dev.mj80.valorant.valorantdata;

import dev.mj80.valorant.valorantdata.data.StatData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ValorantData extends JavaPlugin {
    @Getter private static ValorantData instance;
    @Getter private static File dataPath;
    @Getter private final Set<StatData> dataList = new HashSet<>();
    
    @Override
    public void onEnable() {
        instance = this;
        dataPath = ValorantData.getInstance().getDataFolder();
    }
    
    public StatData createData(Player player) {
        if(getData(player) == null) {
            StatData data = new StatData(player);
            dataList.add(data);
            return data;
        } else {
            return getData(player);
        }
    }
    
    public void deleteData(Player player) {
        dataList.stream().filter(dataPlayer -> dataPlayer.getPlayer() == player).forEach(stats -> {
            stats.saveData();
            dataList.remove(stats);
        });
    }
    
    public StatData getData(Player player) {
        return dataList.stream().filter(dataPlayer -> dataPlayer.getPlayer() == player).findFirst().orElse(null);
    }
    
    public void saveData(Player player) {
        dataList.stream().filter(data -> data.getPlayer() == player).forEach(StatData::saveData);
    }
    
    public void saveAll() {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        Collection<? extends Player> staff = players.stream().filter(player -> player.hasPermission("valorant.staff")).toList();
        players.forEach(player -> {
            player.sendMessage(String.format(Messages.ADMIN_SAVING_DATA.getMessage(), players.size()));
        });
        long start = System.nanoTime();
        players.forEach(this::saveData);
        long end = System.nanoTime();
        double ms = DataUtils.round((float) (end - start)/1000000, 2);
        staff.forEach(player -> {
            player.sendMessage(String.format(Messages.ADMIN_SAVED_DATA.getMessage(), players.size(), ms, DataUtils.round(ms/players.size(), 2)));
        });
    }
}
