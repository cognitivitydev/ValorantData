package dev.mj80.valorant.valorantdata.penalty;

import lombok.Getter;

import java.util.Arrays;

@SuppressWarnings("unused")
public enum PenaltyType {
    
    PERMANENT_BAN(6), BAN(5), PERMANENT_MUTE(4), MUTE(3), KICK(2), WARN(1), NONE(9);
    
    @Getter private final int id;
    
    PenaltyType(int id) {
        this.id = id;
    }
    
    public static PenaltyType fromPID(int pID) {
        return Arrays.stream(values()).filter(type -> type.getId() == Integer.parseInt(Integer.toString(Math.abs(pID)).substring(0, 1))).findFirst().orElse(NONE);
    }
}
