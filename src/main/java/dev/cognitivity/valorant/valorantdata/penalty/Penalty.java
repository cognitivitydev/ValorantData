package dev.cognitivity.valorant.valorantdata.penalty;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.cognitivity.valorant.valorantdata.data.StatData;
import dev.cognitivity.valorant.valorantdata.DataUtils;
import dev.cognitivity.valorant.valorantdata.Messages;
import dev.cognitivity.valorant.valorantdata.ValorantData;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

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
    @Deprecated(forRemoval = true) private final int pID = -1;
    private final String id;
    private boolean active;
    private boolean appealed;
    @Nullable private String extra;
    
    public Penalty(UUID playerUUID, UUID staffUUID, String reason, long start, long duration, String id, boolean active, boolean appealed, @Nullable String extra) {
        this.playerUUID = playerUUID;
        this.staffUUID = staffUUID;
        
        player = ValorantData.getInstance().getServer().getOfflinePlayer(playerUUID);
        staff = ValorantData.getInstance().getServer().getOfflinePlayer(staffUUID);
        playerName = player.getName() == null ? "UNKNOWN" : player.getName();
        staffName = staff.getName() == null ? staffUUID.equals(new UUID(0, 0))
                ? "CONSOLE" : "UNKNOWN" : staff.getName();
        this.reason = reason;
        this.start = start;
        this.duration = duration;
        this.end = start + duration;
        this.id = id;
        this.penaltyType = PenaltyType.fromID(id);
        this.active = active;
        this.appealed = appealed;
        this.extra = extra;
    }
    
    @SuppressWarnings("unused")
    public static Penalty generate(UUID playerUUID, UUID staffUUID, PenaltyType penaltyType, String reason, long start, long duration, boolean appealed) {
        return generate(playerUUID, staffUUID, penaltyType, reason, start, duration, appealed, null);
    }

    @SuppressWarnings("unused")
    public static Penalty generate(UUID playerUUID, UUID staffUUID, PenaltyType penaltyType, String reason, long start, long duration, boolean appealed, String extra) {
        return new Penalty(playerUUID, staffUUID, reason, start, duration, generateHash(penaltyType), false, appealed, extra);
    }
    private static String generateHash(PenaltyType penaltyType) {
        String input = String.valueOf(ValorantData.getInstance().getPenaltyManager().getJsonArray().size() + 1);
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder string = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    string.append('0');
                }
                string.append(hex);
            }
            String trimmed = string.substring(0, 7);
            return (penaltyType.getId()+trimmed).toUpperCase();
        } catch(Exception exception) {
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Issue generating hash for penalty #"+input+" (type "+penaltyType.getId()+")", exception);
            return null;
        }
    }

    public static Penalty of(JsonObject jsonObject) {
        String playerUUID = jsonObject.get("target").getAsString();
        String staffUUID = jsonObject.get("staff").getAsString();
        String reason = jsonObject.get("reason").getAsString();
        long start = jsonObject.get("start").getAsLong();
        long duration = jsonObject.get("duration").getAsLong();
        String id = jsonObject.get("id").getAsString();
        boolean active = jsonObject.get("active").getAsBoolean();
        boolean appealed = jsonObject.get("appealed").getAsBoolean();
        String extra = jsonObject.has("extra") && !jsonObject.get("extra").isJsonNull() ? jsonObject.get("extra").getAsString() : null;
        return new Penalty(UUID.fromString(playerUUID), UUID.fromString(staffUUID), reason, start, duration, id, active, appealed, extra);
    }
    
    @Nullable public static Penalty of(String id) {
        Penalty penalty = ValorantData.getInstance().getPenaltyManager().getPenalties().stream().filter(penalties -> penalties.getId().equals(id.toUpperCase())).findFirst().orElse(null);
        if(penalty != null) {
            return penalty;
        }
        JsonObject jsonObject = ValorantData.getInstance().getPenaltyManager().getJsonArray().asList()
                .stream().map(JsonElement::getAsJsonObject).filter(object -> object.getAsJsonObject().get("id").getAsString().equals(id.toUpperCase())).findFirst().orElse(null);
        if(jsonObject != null) {
            return of(jsonObject);
        } return null;
    }
    
    public JsonObject getAsJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("target", String.valueOf(playerUUID));
        object.addProperty("staff", String.valueOf(staffUUID));
        object.addProperty("reason", reason);
        object.addProperty("start", start);
        object.addProperty("duration", duration);
        object.addProperty("active", active);
        object.addProperty("appealed", appealed);
        object.addProperty("extra", extra);
        return object;
    }
    
    @SuppressWarnings("unused")
    public void add() {
        ValorantData.getInstance().getPenaltyManager().addPenalty(this);
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.getPenalties().add(this);
        data.save();
        setActive(true);
        if(player != null && player.isOnline() && player.getPlayer() != null) {
            if (penaltyType.isNotification()) {
                player.getPlayer().sendMessage(getPenaltyMessage());
            }
            if (penaltyType.isKick()) {
                player.getPlayer().kick(getPenaltyMessage());
            }
        }
        sendPenaltyWarning();
    }
    
    @SuppressWarnings("unused")
    public void remove(@Nullable String staff) {
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.save();
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
    
    public void sendPenaltyWarning() {
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
    }
    
    public Component getPenaltyMessage() {
        switch(penaltyType) {
            case PERMANENT_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_BAN_REASON.getMessage(reason, banned, id);
            }
            case TEMPORARY_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_BAN_REASON.getMessage(reason, banned, ends, until, id);
            }
            case PERMANENT_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_MUTE_REASON.getMessage(reason, muted, id);
            }
            case TEMPORARY_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_MUTE_REASON.getMessage(reason, muted, ends, until, id);
            }
            case KICK -> {
                String kicked = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.KICK_REASON.getMessage(reason, kicked, id);
            }
            case WARN -> {
                String warned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.WARN_REASON.getMessage(reason, warned, id);
            }
            default -> {
                return Component.text().asComponent();
            }
        }
    }

    private void alert(Component message) {
        List<Player> onlineStaff = ValorantData.getInstance().getServer().getOnlinePlayers().stream().map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull).filter(players -> players.hasPermission("valorant.staff")).toList();
        onlineStaff.forEach(staff ->
                staff.sendMessage(message));
        List<String> lines = List.of(MiniMessage.miniMessage().serialize(message).split("\n"));
        lines.forEach(line -> ValorantData.getInstance().getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(line)));
    }

    @SuppressWarnings("unused")
    public boolean isActive() {
        if(!active) return false;
        if(penaltyType.isPermanent()) return true;
        long time = System.currentTimeMillis();
        return start <= time && time <= end;
    }

    private void setActive(boolean active) {
        this.active = active;
        File penaltyFile = ValorantData.getInstance().getPenaltyManager().getPenaltyFile();
        JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
        if(penaltiesObject != null) {
            JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
            for(JsonElement penaltyElement : penaltiesArray.asList()) {
                JsonObject penalty = penaltyElement.getAsJsonObject();
                if(penalty.get("id").getAsString().equals(id)) {
                    int index = penaltiesArray.asList().indexOf(penalty);
                    penalty.addProperty("active", active);
                    penaltiesArray.set(index, penalty);
                    penaltiesObject.add("penalties", penaltiesArray);
                    DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
                }
            }
        }
    }

    public void setAppealed(boolean appealed) {
        this.appealed = appealed;
        File penaltyFile = ValorantData.getInstance().getPenaltyManager().getPenaltyFile();
        JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
        if(penaltiesObject != null) {
            JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
            for(JsonElement penaltyElement : penaltiesArray.asList()) {
                JsonObject penalty = penaltyElement.getAsJsonObject();
                if(penalty.get("id").getAsString().equals(id)) {
                    int index = penaltiesArray.asList().indexOf(penalty);
                    penalty.addProperty("appealed", appealed);
                    penaltiesArray.set(index, penalty);
                    penaltiesObject.add("penalties", penaltiesArray);
                    DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
                }
            }
        }
    }

    public void setExtra(String extra) {
        this.extra = extra;
        File penaltyFile = ValorantData.getInstance().getPenaltyManager().getPenaltyFile();
        JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
        if(penaltiesObject != null) {
            JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
            for(JsonElement penaltyElement : penaltiesArray.asList()) {
                JsonObject penalty = penaltyElement.getAsJsonObject();
                if(penalty.get("id").getAsString().equals(id)) {
                    int index = penaltiesArray.asList().indexOf(penalty);
                    penalty.addProperty("extra", extra);
                    penaltiesArray.set(index, penalty);
                    penaltiesObject.add("penalties", penaltiesArray);
                    DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Penalty[id="+id+", player="+playerName+", staff="+staffName+", type="+penaltyType+", duration="+duration+", active="+active+", appealed="+appealed+"]";
    }
}
