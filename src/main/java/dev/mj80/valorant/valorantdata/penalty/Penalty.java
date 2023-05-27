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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
public class Penalty {
    private final String playerName;
    private final String staffName;
    private final String reason;
    private final PenaltyType penaltyType;
    private final long start;
    private final long duration;
    private final long end;
    private final int pID;
    
    public Penalty(String playerName, String staffName, String reason, long start, long duration, int pID) {
        this.playerName = playerName;
        this.staffName = staffName;
        this.reason = reason;
        this.start = start;
        this.duration = duration;
        this.end = start + duration;
        this.pID = pID;
        this.penaltyType = PenaltyType.fromPID(pID);
    }
    
    @SuppressWarnings("unused")
    public static Penalty generate(String playerName, String staffName, PenaltyType penaltyType, String reason, long start, long duration) {
        return new Penalty(playerName, staffName, reason, start, duration, Integer.parseInt(penaltyType.getId() +
                String.valueOf(ValorantData.getInstance().getPenaltyManager().getJsonArray().size() + 1)));
    }
    
    public static Penalty of(JsonObject jsonObject) {
        String playerName = jsonObject.get("target").getAsString();
        String staffName = jsonObject.get("staff").getAsString();
        String reason = jsonObject.get("reason").getAsString();
        long start = jsonObject.get("start").getAsLong();
        long duration = jsonObject.get("duration").getAsLong();
        int pID = jsonObject.get("id").getAsInt();
        return new Penalty(playerName, staffName, reason, start, duration, pID);
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
        object.addProperty("target", playerName);
        object.addProperty("staff", staffName);
        object.addProperty("reason", reason);
        object.addProperty("start", start);
        object.addProperty("duration", duration);
        return object;
    }
    
    @SuppressWarnings("unused")
    public void addPunishment() {
        ValorantData.getInstance().getPenaltyManager().addPenalty(this);
        OfflinePlayer player = ValorantData.getInstance().getServer().getOfflinePlayer(playerName);
        StatData data = ValorantData.getInstance().getData(player).getStats();
        data.getPenalties().add(this);
        data.saveData();
        send();
    }
    public void send() {
        OfflinePlayer player = ValorantData.getInstance().getServer().getOfflinePlayer(playerName);
        if(player.isOnline() && player.getPlayer() != null) {
            Player onlinePlayer = player.getPlayer();
            switch(penaltyType) {
                case PERMANENT_BAN -> {
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "permanently banned", staffName, reason, "<white>never expire"));
                    String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    onlinePlayer.kick(Messages.PERMANENT_BAN_REASON.getMessage(reason, banned, pID));
                }
                case BAN -> {
                    String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                    String until = DataUtils.timeUntil(end);
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "temporarily banned", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
                    onlinePlayer.kick(Messages.TEMPORARY_BAN_REASON.getMessage(reason, banned, ends, until, pID));
                }
                case PERMANENT_MUTE -> {
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "permanently muted", staffName, reason, "<white>never <gray>expire"));
                    String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    onlinePlayer.sendMessage(Messages.PERMANENT_MUTE_REASON.getMessage(reason, muted, pID));
                }
                case MUTE -> {
                    String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                    String until = DataUtils.timeUntil(end);
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "temporarily muted", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
                    onlinePlayer.sendMessage(Messages.TEMPORARY_MUTE_REASON.getMessage(reason, muted, ends, until, pID));
                }
                case KICK -> {
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "kicked", staffName, reason, "<white>never <gray>expire"));
                    String kicked = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    onlinePlayer.kick(Messages.KICK_REASON.getMessage(reason, kicked, pID));
                }
                case WARN -> {
                    alert(Messages.PENALTY_ADMINISTERED.getMessage(playerName, "warned", staffName, reason, "<white>never <gray>expire"));
                    String warned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                    onlinePlayer.sendMessage(Messages.WARN_REASON.getMessage(reason, warned, pID));
                }
            }
        }
    }
    
    @SuppressWarnings("unused")
    public boolean isActive() {
        long time = System.currentTimeMillis();
        return start <= time && time >= end;
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
