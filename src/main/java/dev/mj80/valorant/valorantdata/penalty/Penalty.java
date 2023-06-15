package dev.mj80.valorant.valorantdata.penalty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mj80.valorant.valorantdata.DataUtils;
import dev.mj80.valorant.valorantdata.Messages;
import dev.mj80.valorant.valorantdata.ValorantData;
import dev.mj80.valorant.valorantdata.data.StatData;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Penalty {
    private final UUID playerUUID;
    private final UUID staffUUID;
    
    private final String playerName;
    private final String staffName;
    
    private final @Nullable OfflinePlayer player;
    private final @Nullable OfflinePlayer staff;
    
    private final String reason;
    private final PenaltyType penaltyType;
    private final long start;
    private final long duration;
    private final long end;
    private final int pID;
    
    public Penalty(UUID playerUUID, UUID staffUUID, String reason, long start, long duration, int pID) {
        this.playerUUID = playerUUID;
        this.staffUUID = staffUUID;
        
        player = ValorantData.getInstance().getServer().getPlayer(playerUUID);
        staff = ValorantData.getInstance().getServer().getPlayer(staffUUID);
        playerName = player == null ? "UNKNOWN" : player.getName();
        staffName = staff == null ? "UNKNOWN" : staff.getName();
        this.reason = reason;
        this.start = start;
        this.duration = duration;
        this.end = start + duration;
        this.pID = pID;
        this.penaltyType = PenaltyType.fromPID(pID);
    }
    
    @SuppressWarnings("unused")
    public static Penalty generate(UUID playerUUID, UUID staffUUID, PenaltyType penaltyType, String reason, long start, long duration) {
        return new Penalty(playerUUID, staffUUID, reason, start, duration, Integer.parseInt(penaltyType.getId() +
                String.valueOf(ValorantData.getInstance().getPenaltyManager().getJsonArray().size() + 1)));
    }
    
    public static Penalty of(JsonObject jsonObject) {
        String playerUUID = jsonObject.get("target").getAsString();
        String staffUUID = jsonObject.get("staff").getAsString();
        String reason = jsonObject.get("reason").getAsString();
        long start = jsonObject.get("start").getAsLong();
        long duration = jsonObject.get("duration").getAsLong();
        int pID = jsonObject.get("id").getAsInt();
        return new Penalty(UUID.fromString(playerUUID), UUID.fromString(staffUUID), reason, start, duration, pID);
    }
    public static Penalty of(int id) {
        JsonObject jsonObject = ValorantData.getInstance().getPenaltyManager().getJsonArray().asList()
                .stream().map(JsonElement::getAsJsonObject).filter(object -> object.getAsJsonObject().get("id").getAsInt() == id).findFirst().orElse(null);
        if(jsonObject != null) {
            return of(jsonObject);
        } return null;
    }
    
    public JsonObject getAsJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id", pID);
        object.addProperty("target", String.valueOf(playerUUID));
        object.addProperty("staff", String.valueOf(staffUUID));
        object.addProperty("reason", reason);
        object.addProperty("start", start);
        object.addProperty("duration", duration);
        return object;
    }
    
    @SuppressWarnings("unused")
    public void addPenalty() {
        ValorantData.getInstance().getPenaltyManager().addPenalty(this);
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.getPenalties().add(this);
        data.saveData();
    }
    
    @SuppressWarnings("unused")
    public Component sendPenaltyWarning() {
        String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
        String until = DataUtils.timeUntil(end);
        switch(penaltyType) {
            case PERMANENT_BAN ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "permanently banned", staffName, reason, "<white>never expire"));
            case TEMPORARY_BAN ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "temporarily banned", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
            case PERMANENT_MUTE ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "permanently muted", staffName, reason, "<white>never <gray>expire"));
            case TEMPORARY_MUTE ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "temporarily muted", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
            case KICK ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "kicked", staffName, reason, "<white>never <gray>expire"));
            case WARN ->
                alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "warned", staffName, reason, "<white>never <gray>expire"));
        }
        return getPenaltyMessage();
    }
    
    public Component getPenaltyMessage() {
        switch(penaltyType) {
            case PERMANENT_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_BAN_REASON.getMessage(reason, banned, pID);
            }
            case TEMPORARY_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_BAN_REASON.getMessage(reason, banned, ends, until, pID);
            }
            case PERMANENT_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_MUTE_REASON.getMessage(reason, muted, pID);
            }
            case TEMPORARY_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_MUTE_REASON.getMessage(reason, muted, ends, until, pID);
            }
            case KICK -> {
                String kicked = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.KICK_REASON.getMessage(reason, kicked, pID);
            }
            case WARN -> {
                String warned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.WARN_REASON.getMessage(reason, warned, pID);
            }
            default -> {
                return Component.text().asComponent();
            }
        }
    }
    
    @SuppressWarnings("unused")
    public boolean isActive() {
        long time = System.currentTimeMillis();
        if(penaltyType.isPermanent()) return true;
        return start <= time && time <= end;
    }
    
    private void alert(Component message) {
        List<Player> onlineStaff = ValorantData.getInstance().getServer().getOnlinePlayers().stream().map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull).filter(players -> players.hasPermission("valorant.staff")).toList();
        onlineStaff.forEach(staff ->
                staff.sendMessage(message));
        List<String> lines = List.of(MiniMessage.miniMessage().serialize(message).split("\n"));
        lines.forEach(line -> ValorantData.getInstance().getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(line)));
    }
}
