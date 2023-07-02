package dev.mj80.valorant.valorantdata.penalty;

import com.google.gson.JsonArray;
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

import java.io.File;
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
    private boolean active;
    
    public Penalty(UUID playerUUID, UUID staffUUID, String reason, long start, long duration, int pID, boolean active) {
        this.playerUUID = playerUUID;
        this.staffUUID = staffUUID;
        
        player = ValorantData.getInstance().getServer().getOfflinePlayer(playerUUID);
        staff = ValorantData.getInstance().getServer().getOfflinePlayer(staffUUID);
        playerName = player.getName() == null ? "UNKNOWN" : player.getName();
        staffName = staff.getName() == null ? staffUUID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                ? "CONSOLE" : "UNKNOWN" : staff.getName();
        this.reason = reason;
        this.start = start;
        this.duration = duration;
        this.end = start + duration;
        this.pID = pID;
        this.penaltyType = PenaltyType.fromPID(pID);
        this.active = active;
    }
    
    @SuppressWarnings("unused")
    public static Penalty generate(UUID playerUUID, UUID staffUUID, PenaltyType penaltyType, String reason, long start, long duration) {
        return new Penalty(playerUUID, staffUUID, reason, start, duration, Integer.parseInt(penaltyType.getId() +
                String.valueOf(ValorantData.getInstance().getPenaltyManager().getJsonArray().size() + 1)), false);
    }
    
    public static Penalty of(JsonObject jsonObject) {
        String playerUUID = jsonObject.get("target").getAsString();
        String staffUUID = jsonObject.get("staff").getAsString();
        String reason = jsonObject.get("reason").getAsString();
        long start = jsonObject.get("start").getAsLong();
        long duration = jsonObject.get("duration").getAsLong();
        int pID = jsonObject.get("id").getAsInt();
        boolean active = jsonObject.get("active").getAsBoolean();
        return new Penalty(UUID.fromString(playerUUID), UUID.fromString(staffUUID), reason, start, duration, pID, active);
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
        object.addProperty("active", active);
        return object;
    }
    
    @SuppressWarnings("unused")
    public void add() {
        ValorantData.getInstance().getPenaltyManager().addPenalty(this);
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.getPenalties().add(this);
        data.saveData();
        setActive(true);
    }
    
    @SuppressWarnings("unused")
    public void remove(@Nullable String staff) {
        ValorantData.getInstance().getPenaltyManager().removePenalty(this);
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.saveData();
        setActive(false);
        String length = DataUtils.timeLength(this.duration);
        String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
        String until = DataUtils.timeUntil(end);
        if (staff != null) {
            switch (penaltyType) {
                case PERMANENT_BAN -> alert(Messages.PENALTY_REMOVED.getMessage(playerName, "permanently unbanned", staff, "banned", staffName, reason, "Permanent"));
                case TEMPORARY_BAN ->
                        alert(Messages.PENALTY_REMOVED.getMessage(playerName, "temporarily unbanned", staff, "banned", staffName, reason, length + " (" + until + " // " + ends + ")"));
                case PERMANENT_MUTE -> alert(Messages.PENALTY_REMOVED.getMessage(playerName, "permanently unmuted", staff, "muted", staffName, reason, "Permanent"));
                case TEMPORARY_MUTE ->
                        alert(Messages.PENALTY_REMOVED.getMessage(playerName, "temporarily unmuted", staff, "muted", staffName, reason, length + " (" + until + " // " + ends + ")"));
            }
        }
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

    public void alert(Component message) {
        List<Player> onlineStaff = ValorantData.getInstance().getServer().getOnlinePlayers().stream().map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull).filter(players -> players.hasPermission("valorant.staff")).toList();
        onlineStaff.forEach(staff ->
                staff.sendMessage(message));
        List<String> lines = List.of(MiniMessage.miniMessage().serialize(message).split("\n"));
        lines.forEach(line -> ValorantData.getInstance().getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(line)));
    }

    @SuppressWarnings("unused")
    public boolean isActive() {
        long time = System.currentTimeMillis();
        if(penaltyType.isPermanent() && active) return true;
        return active && start <= time && time <= end;
    }

    private void setActive(boolean active) {
        this.active = active;
        File penaltyFile = ValorantData.getInstance().getPenaltyManager().getPenaltyFile();
        JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
        if(penaltiesObject != null) {
            JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
            for(JsonElement penaltyElement : penaltiesArray.asList()) {
                JsonObject penalty = penaltyElement.getAsJsonObject();
                if(penalty.get("id").getAsInt() == pID) {
                    int index = penaltiesArray.asList().indexOf(penalty);
                    penalty.addProperty("active", active);
                    penaltiesArray.set(index, penalty);
                    penaltiesObject.add("penalties", penaltiesArray);
                    DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
                }
            }
        }
    }
}
