package dev.mj80.valorant.valorantdata.data;

import com.google.gson.JsonObject;
import dev.mj80.valorant.valorantdata.ValorantData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Getter @Setter
public class PlayerData {
    @NotNull private OfflinePlayer player;
    
    private AnticheatData anticheatData;
    private CoreData coreData;
    boolean isOnline;
    @NotNull private final StatData stats;
    
    public PlayerData(@NotNull OfflinePlayer player) {
        long startTime = System.currentTimeMillis();
        ValorantData.getInstance().log("<aqua>[DATA] <gray>Creating data for player "+player.getName()+":");
        this.player = player;
        createOnlineData();
        ValorantData.getInstance().log("<aqua>[DATA] <gray>Creating JSON data for player "+player.getName()+"...");
        stats = new StatData(player, this);
        ValorantData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+(System.currentTimeMillis() - startTime)+" ms");
    }
    public JsonObject createData() {
        return stats.create();
    }
    
    public void saveData() {
        stats.save();
    }
    
    public void updateData() {
        stats.load();
    }
    
    public void createOnlineData() {
        ValorantData.getInstance().log("<aqua>[DATA] <gray>Creating Anticheat data for player "+player.getName()+"...");
        anticheatData = player.isOnline() ? new AnticheatData(player.getPlayer(), this) : null;
        if(anticheatData == null)
            ValorantData.getInstance().log("<aqua>[DATA] <red>An error occurred or the player is offline while creating Anticheat data for "+player.getName()+"...");
        ValorantData.getInstance().log("<aqua>[DATA] <gray>Creating Core data for player "+player.getName()+"...");
        coreData = player.isOnline() ? new CoreData(player.getPlayer(), this) : null;
        if(coreData == null)
            ValorantData.getInstance().log("<aqua>[DATA] <red>An error occurred or the player is offline while creating Core data for "+player.getName()+"...");
        isOnline = anticheatData != null || coreData != null;
    }
}
