package dev.mj80.valorant.valorantdata.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;

@Getter @Setter
public class CoreData {
    private final Player player;
    private final StatData stats;
    
    public CoreData(Player player, StatData stats) {
        this.player = player;
        this.stats = stats;
        reset();
    }
    
    private boolean inGame,inQueue,staff,scoped,planting,onGround,packEnabled;
    private double deltaX,deltaY,deltaZ,deltaXZ,deltaXYZ;
    private int streak;
    private long lastUsedAbility3,lastUsedAbility2,lastUsedAbility1,lastUsedUltimate,lastScopeIn,lastAir,lastJump,lastMovement;
    private Object agent;
    private ArrayList<Object> cheats = new ArrayList<>();
    private BoundingBox box0,box1,box2,box3;
    private Location lastSafeLocation;
    
    public long getTime(long value) {
        return System.currentTimeMillis()-value;
    }
    
    public long getDiscordId() {
        return stats.getDiscordId();
    }
    
    public void reset() {
        inGame = false;
        staff = player.hasPermission("valorant.staff");
        onGround = true;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("inGame");
        if(team == null) {
            team = scoreboard.registerNewTeam("inGame");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        team.removeEntry(player.getName());
    }
}
