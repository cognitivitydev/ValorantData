package dev.mj80.valorant.valorantdata.data;

import com.google.gson.JsonObject;
import dev.mj80.valorant.valorantdata.ValorantData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Getter @Setter
public class PlayerData {
    @NotNull private OfflinePlayer player;
    
    private final AnticheatData anticheatData;
    private final CoreData coreData;
    @NotNull private final StatData stats;
    
    public PlayerData(@NotNull OfflinePlayer player) {
        long startTime = System.currentTimeMillis();
        ValorantData.getInstance().log("&b[DATA] &7Creating data for player "+player.getName()+":");
        this.player = player;
        ValorantData.getInstance().log("&b[DATA] &7Creating Anticheat data for player "+player.getName()+"...");
        anticheatData = player.isOnline() ? new AnticheatData(player.getPlayer(), this) : null;
        if(anticheatData == null)
            ValorantData.getInstance().log("&b[DATA] &cAn error occurred or the player is offline while creating Anticheat data for "+player.getName()+"...");
        ValorantData.getInstance().log("&b[DATA] &7Creating Core data for player "+player.getName()+"...");
        coreData = player.isOnline() ? new CoreData(player.getPlayer(), this) : null;
        if(anticheatData == null)
            ValorantData.getInstance().log("&b[DATA] &cAn error occurred or the player is offline while creating Core data for "+player.getName()+"...");
        ValorantData.getInstance().log("&b[DATA] &7Creating JSON data for player "+player.getName()+"...");
        stats = new StatData(player, this);
        ValorantData.getInstance().log("&b[DATA] &7Finished creating data for player "+player.getName()+". Took "+(System.currentTimeMillis() - startTime));
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
