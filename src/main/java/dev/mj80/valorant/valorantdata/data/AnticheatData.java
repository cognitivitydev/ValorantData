package dev.mj80.valorant.valorantdata.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Getter @Setter
public class AnticheatData {
    private final Player player;
    private final PlayerData data;
    
    public AnticheatData(Player player, PlayerData data) {
        this.player = player;
        this.data = data;
        reset();
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
    
    public void reset() {
        lastSpeed = 0;
        lastJump = 0;
        lastFlightOff = 0;
        lastFlightOn = 0;
        lastSprintOff = 0;
        lastSprintOn = 0;
        lastServerGround = 0;
        lastClientGround = 0;
        lastKnockback = 0;
        lastTeleport = 0;
        lastDamage = 0;
        lastAttack = 0;
        lastGhostBlock = 0;
        lastOnSlime = 0;
        lastElytraBoost = 0;
        lastSafeLocation = player.getLocation();
        lastLocation = player.getLocation();
        lastLastEyeLocation = player.getEyeLocation();
        lastEyeLocation = player.getEyeLocation();
        currentLocation = player.getLocation();
        currentEyeLocation = player.getEyeLocation();
        accelerationBufferB = 0;
        jesusBufferB = 0;
        nofallBuffer = 0;
        packetsBuffer = 0;
        speedBufferA = 0;
        buffer = 0;
        verbose = 0;
        alerts = 0;
        deltaX = 0;
        deltaZ = 0;
        deltaXZ = 0;
        deltaY = 0;
        lastDeltaXZ = 0;
        deltaYaw = 0;
        deltaPitch = 0;
        alertsEnabled = player.hasPermission("anticheat.alerts");
        verboseEnabled = false;
        isPunished = false;
    }
}
