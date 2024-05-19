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
    private final PlayerData data;
    
    public CoreData(Player player, PlayerData data) {
        this.player = player;
        this.data = data;
        reset();
    }
    
    private boolean inGame,inQueue,staff,scoped,planting,defusing,onGround,packEnabled,vanished;
    @Deprecated private double deltaX,deltaY,deltaZ,deltaXZ,deltaXYZ;
    private int streak,ultimatePoints;
    private long lastUsedAbility3,lastUsedAbility2,lastUsedAbility1,lastUsedUltimate,lastScopeIn,lastAir,lastJump,lastMovement;
    private Object agent,team,match;
    private ArrayList<Object> cheats = new ArrayList<>();
    private BoundingBox box0,box1,box2,box3;
    private Location lastSafeLocation;
    private Player spectating = null;
    
    @SuppressWarnings("unused")
    public long getTime(long value) {
        return System.currentTimeMillis() - value;
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
