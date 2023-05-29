package dev.mj80.valorant.valorantdata.penalty;

import lombok.Getter;

import java.util.Arrays;

@SuppressWarnings("unused")
public enum PenaltyType {
    
    PERMANENT_BAN(6, "pban", "permanentban", "permanent_ban", "perm_ban", "permban"),
    TEMPORARY_BAN(5, "tban", "temporaryban", "temporary_ban", "temp_ban", "tempban"),
    PERMANENT_MUTE(4, "pmute", "permanentmute", "permanent_mute", "perm_mute", "permmute"),
    TEMPORARY_MUTE(3, "tmute", "temporarymute", "temporary_mute", "temp_mute", "tempmute"),
    KICK(2, "kick"),
    WARN(1, "warn"),
    NONE(9);
    
    @Getter private final int id;
    @Getter private final String[] names;
    
    PenaltyType(int id, String... names) {
        this.id = id;
        this.names = names;
    }
    
    public boolean isPermanent() { return this == PERMANENT_BAN || this == PERMANENT_MUTE; }
    public boolean isTemporary() { return this == TEMPORARY_BAN || this == TEMPORARY_MUTE; }
    public boolean isInstant() { return this == WARN || this == KICK; }
    public boolean isMute() { return this == TEMPORARY_MUTE || this == PERMANENT_MUTE; }
    public boolean isBan() { return this == TEMPORARY_BAN || this == PERMANENT_BAN; }
    /**
     * @return whether the player is kicked when the penalty active.
     */
    public boolean isKick() { return this == KICK || this == TEMPORARY_BAN || this == PERMANENT_BAN; }
    /**
     * @return whether the player is notified when the penalty active.
     */
    public boolean isNotification() { return this == WARN || this == TEMPORARY_MUTE || this == PERMANENT_MUTE; }
    
    public static PenaltyType fromPID(int pID) {
        return Arrays.stream(values()).filter(type -> type.getId() == Integer.parseInt(Integer.toString(Math.abs(pID)).substring(0, 1))).findFirst().orElse(NONE);
    }
    public static PenaltyType fromString(String name) {
        return Arrays.stream(values()).filter(type -> Arrays.stream(type.getNames()).anyMatch(typeName -> typeName.equalsIgnoreCase(name))).findFirst().orElse(NONE);
    }
}