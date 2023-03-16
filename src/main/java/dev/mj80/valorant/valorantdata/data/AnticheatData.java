package dev.mj80.valorant.valorantdata.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Getter @Setter
public class AnticheatData {
    private final Player player;
    private final StatData stats;
    
    public AnticheatData(Player player, StatData stats) {
        this.player = player;
        this.stats = stats;
    }
    
    public boolean serverGround,nearGround,clientGround,lServerGround,lNearGround,lClientGround,inLiquid,onLiquid,inPowderedSnow,onIce,underBlock,onStairs,onSlab,onSlime,onClimbable,
            onSoul,onSoulSand,sprint,lSprint,alertsEnabled,verboseEnabled,isPunished,inWebs;
    public int buffer,verbose,alerts,groundTime,airTime,onLiquidTime,inLiquidTime,soulSandTime;
    public long lastMovement,lastJump,lastSprintOn,lastSprintOff,lastFlightOff,lastFlightOn,lastServerGround,lastClientGround,lastNearGround,lastInLiquid,lastOnLiquid,
            lastKnockback,lastTeleport,lastDamage,lastAttack,lastOnIce,lastUnderBlock,lastOnStairs,lastOnSlab,lastOnClimbable,lastOnSoul,lastOnSoulSand,lastOnSlime,
            lastSpeed,lastGhostBlock,lastElytraBoost,lastElytraToggleOn,lastElytraToggleOff,lastGliding,lastArmAnimation,lastKeepAliveReceived,lastKeepAliveSent;
    public float deltaX,deltaZ,deltaXZ,deltaY,deltaYaw,deltaPitch,lastDeltaX,lastDeltaZ,lastDeltaXZ,lastDeltaY,lastDeltaYaw,lastDeltaPitch;
    public Location lastLocation,currentLocation,currentEyeLocation,lastSafeLocation,lastEyeLocation,lastLastEyeLocation;
    public double attributeSpeed,lAttributeSpeed,llAttributeSpeed,lllAttributeSpeed,llllAttributeSpeed;
    public ArrayList<Location> pastLocations = new ArrayList<>();
    
    /* ACCELERATION */
    
    public int accelerationBufferA;
    public int accelerationBufferB;
    
    /* JESUS */
    
    public int jesusBufferB;
    public int jesusBufferC;
    
    /* NOFALL */
    
    public int nofallBuffer;
    
    /* PACKETS */
    
    public int packetsBuffer;
    
    /* SPEED */
    
    public int speedBufferA;
    public int speedBufferB;
    
    /* SPRINT */
    
    public int sprintBufferC;
    
    public long getTime(long ms) {
        return System.currentTimeMillis() - ms;
    }
}
