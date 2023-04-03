package dev.mj80.valorant.valorantdata.data;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class PlayerData {
    @NotNull private OfflinePlayer player;
    
    private final AnticheatData anticheatData;
    private final CoreData coreData;
    @NotNull private final StatData stats;
    
    public PlayerData(@NotNull OfflinePlayer player) {
        this.player = player;
        anticheatData = player.isOnline() ? new AnticheatData(player.getPlayer(), this) : null;
        coreData = player.isOnline() ? new CoreData(player.getPlayer(), this) : null;
        stats = new StatData(player, this);
    }
    public JsonObject createData() {
        return stats.createData();
    }
    
    public void saveData() {
        stats.saveData();
    }
    
    public void updateData() {
        stats.updateData();
    }
}
