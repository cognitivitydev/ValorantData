package dev.cognitivity.valorant.valorantdata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.cognitivity.valorant.valorantdata.DataUtils;
import dev.cognitivity.valorant.valorantdata.Messages;
import dev.cognitivity.valorant.valorantdata.ValorantData;
import dev.cognitivity.valorant.valorantdata.penalty.Penalty;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;

@Getter @Setter
public class StatData {
    private final OfflinePlayer player;
    @NotNull private final PlayerData data;
    private final File file;
    
    private long kills,deaths,assists,roundsPlayed,matchesPlayed,victories,losses,discordId;
    private double damageDealt,damageReceived;
    private double particles = 1;
    private boolean tosAccepted;
    private final ArrayList<String> hashedIps = new ArrayList<>();
    private final ArrayList<Penalty> penalties = new ArrayList<>();
    private final ArrayList<String> blacklistedCommands = new ArrayList<>();
    
    public StatData(OfflinePlayer player, @NotNull PlayerData data) {
        this.player = player;
        file = new File(ValorantData.getDataPath() + File.separator + player.getUniqueId() + ".json");
        if (DataUtils.createFile(file) || DataUtils.readFile(file).isEmpty()) {
            DataUtils.writeJSONObject(file, this.create());
        }
        load();
        this.data = data;
    }
    
    public JsonObject create() {
        try {
            JsonObject dataObject = new JsonObject();
            dataObject.addProperty("version", DataUpdater.getDataVersion());

            /* PROFILE */
            JsonObject profileObject = new JsonObject();

            JsonObject settingsObject = new JsonObject();
            settingsObject.addProperty("particles", this.particles);
            profileObject.add("settings", settingsObject);

            profileObject.add("addresses", new JsonArray());

            long firstPlayed = player.getFirstPlayed();
            if(firstPlayed == 0) firstPlayed = System.currentTimeMillis();
            profileObject.addProperty("firstLogin", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new Date(firstPlayed)));

            JsonArray nameHistoryArray = new JsonArray();
            nameHistoryArray.add(player.getName());
            profileObject.add("nameHistory", nameHistoryArray);

            profileObject.addProperty("tos", this.tosAccepted);

            dataObject.add("profile", profileObject);

            /* STATISTICS */
            JsonObject statisticsObject = new JsonObject();

            statisticsObject.addProperty("kills", this.kills);
            statisticsObject.addProperty("deaths", this.deaths);
            statisticsObject.addProperty("assists", this.assists);
            statisticsObject.addProperty("roundsPlayed", this.roundsPlayed);
            statisticsObject.addProperty("matchesPlayed", this.matchesPlayed);
            statisticsObject.addProperty("victories", this.victories);
            statisticsObject.addProperty("losses", this.losses);
            statisticsObject.addProperty("damageDealt", this.damageDealt);
            statisticsObject.addProperty("damageReceived", this.damageReceived);

            dataObject.add("statistics", statisticsObject);

            /* DISCORD */
            JsonObject discordObject = new JsonObject();

            discordObject.addProperty("id", this.discordId);

            dataObject.add("discord", discordObject);

            /* PENALTIES */
            dataObject.add("penalties", new JsonArray());

            /* BLACKLISTS */
            JsonObject blacklistedObject = new JsonObject();

            blacklistedObject.add("commands", new JsonArray());

            dataObject.add("blacklists", blacklistedObject);

            JsonArray dataArray = new JsonArray();
            dataArray.add(profileObject);
            dataArray.add(statisticsObject);
            dataArray.add(discordObject);
            dataArray.add(blacklistedObject);
            return dataObject;
        } catch(Exception exception) {
            if(player.getPlayer() != null) {
                player.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<red>Failed to load your player data! <white>(ID 1)\n" +
                                "\n" +
                                "<gray><b>ERROR INFO</b>\n" +
                                "<white>Error while creating data!\n" +
                                exception + "\n" +
                                "\n" +
                                "<red>Please report this on our Discord: <blue><u>discord.gg/example</u><red>."
                ));
            }
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Failed to create player data! (ID 1)", exception);
            return null;
        }
    }
    
    public void save() {
        try {
            ValorantData.getInstance().log("<aqua>[DATA] <gray>Saving data for "+player.getName()+"...");
            long start = System.nanoTime();
            if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.SAVING_DATA.getMessage());
            JsonObject dataObject = DataUtils.parseJSON(file);
            assert dataObject != null;

            /* PROFILE */
            JsonObject profileObject = dataObject.get("profile").getAsJsonObject();

            JsonObject settingsObject = profileObject.get("settings").getAsJsonObject();
            settingsObject.addProperty("particles", this.particles);

            profileObject.addProperty("tos", this.tosAccepted);

            /* STATISTICS */
            JsonObject statisticsObject = dataObject.get("statistics").getAsJsonObject();

            statisticsObject.addProperty("kills", this.kills);
            statisticsObject.addProperty("deaths", this.deaths);
            statisticsObject.addProperty("assists", this.assists);
            statisticsObject.addProperty("roundsPlayed", this.roundsPlayed);
            statisticsObject.addProperty("matchesPlayed", this.matchesPlayed);
            statisticsObject.addProperty("victories", this.victories);
            statisticsObject.addProperty("losses", this.losses);
            statisticsObject.addProperty("damageDealt", this.damageDealt);
            statisticsObject.addProperty("damageReceived", this.damageReceived);

            /* DISCORD */
            JsonObject discordObject = dataObject.get("discord").getAsJsonObject();

            discordObject.addProperty("id", this.discordId);

            /* PENALTIES */
            JsonArray penaltiesArray = dataObject.get("penalties").getAsJsonArray();
            this.penalties.stream().filter(Objects::nonNull).map(Penalty::getId).filter(id -> !penaltiesArray.asList().stream().map(JsonElement::getAsString).toList().contains(id))
                    .forEach(penaltiesArray::add);

            /* BLACKLISTS */
            JsonObject blacklistedObject = dataObject.get("blacklists").getAsJsonObject();

            JsonArray commands = blacklistedObject.get("commands").getAsJsonArray();
            this.blacklistedCommands.stream().filter(Objects::nonNull).forEach(commands::add);

            DataUtils.writeJSONObject(file, dataObject);
            double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
            if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.SAVED_DATA.getMessage(ms));
            ValorantData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+ms+" ms.");
        } catch(Exception exception) {
            if(player.getPlayer() != null) {
                player.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<red>Failed to save your player data! <white>(ID 2)\n" +
                                "\n" +
                                "<gray><b>ERROR INFO</b>\n" +
                                "<white>Error while saving data!\n" +
                                exception + "\n" +
                                "\n" +
                                "<red>Please report this on our Discord: <blue><u>discord.gg/example</u><red>."
                ));
            }
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Failed to save player data! (ID 2)", exception);
        }
    }
    
    public void load() {
        try {
            long start = System.nanoTime();
            if (player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.LOADING_DATA.getMessage());

            JsonObject dataObject = DataUtils.parseJSON(file);
            assert dataObject != null;

            /* PROFILE */
            JsonObject profile = dataObject.get("profile").getAsJsonObject();

            JsonObject settings = profile.get("settings").getAsJsonObject();
            this.particles = settings.get("particles").getAsDouble();

            JsonArray nameHistory = profile.get("nameHistory").getAsJsonArray();
            if (player.getName() != null && !nameHistory.contains(new JsonPrimitive(player.getName()))) nameHistory.add(player.getName());

            this.tosAccepted = profile.get("tos").getAsBoolean();

            /* STATISTICS */
            JsonObject statistics = dataObject.get("statistics").getAsJsonObject();

            this.kills = statistics.get("kills").getAsLong();
            this.deaths = statistics.get("deaths").getAsLong();
            this.assists = statistics.get("assists").getAsLong();
            this.roundsPlayed = statistics.get("roundsPlayed").getAsLong();
            this.matchesPlayed = statistics.get("matchesPlayed").getAsLong();
            this.victories = statistics.get("victories").getAsLong();
            this.losses = statistics.get("losses").getAsLong();
            this.damageDealt = statistics.get("damageDealt").getAsDouble();
            this.damageReceived = statistics.get("damageReceived").getAsDouble();

            /* DISCORD */
            JsonObject discord = dataObject.get("discord").getAsJsonObject();

            discordId = discord.get("id").getAsLong();

            /* PENALTIES */
            JsonArray penaltiesArray = dataObject.get("penalties").getAsJsonArray();

            penalties.clear();
            penalties.addAll(penaltiesArray.asList().stream().map(penalty -> Penalty.of(penalty.getAsString())).toList());

            /* BLACKLISTS */
            JsonObject blacklistedObject = dataObject.get("blacklists").getAsJsonObject();

            JsonArray commands = blacklistedObject.get("commands").getAsJsonArray();
            blacklistedCommands.clear();
            blacklistedCommands.addAll(commands.asList().stream().map(JsonElement::getAsString).toList());

            double ms = DataUtils.round((float) (System.nanoTime() - start) / 1000000, 2);
            if (player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.LOADED_DATA.getMessage(ms));
        } catch(Exception exception) {
            if(player.getPlayer() != null) {
                player.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<red>Failed to load your player data! <white>(ID 3)\n" +
                                "\n" +
                                "<gray><b>ERROR INFO</b>\n" +
                                "<white>Error while loading data!\n" +
                                exception + "\n" +
                                "\n" +
                                "<red>Please report this on our Discord: <blue><u>discord.gg/example</u><red>."
                ));
            }
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Failed to load player data! (ID 3)", exception);
        }
    }
    public void update() {
        JsonObject data = DataUtils.parseJSON(file);
        assert data != null;
        int version = data.get("version").getAsInt();
        int latestVersion = DataUpdater.getDataVersion();
        if(version < latestVersion) {
            data = DataUpdater.update(data);
        } else if(version > latestVersion) {
            if(player.getPlayer() != null) {
                player.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<red>Failed to load your player data! <white>(ID 4)\n" +
                                "\n" +
                                "<gray><b>ERROR INFO</b>\n" +
                                "<white>Invalid version!\n" +
                                version + " > " + latestVersion+"\n" +
                                "\n" +
                                "<red>Please report this on our Discord: <blue><u>discord.gg/example</u><red>."
                ));
            }
        }
        JsonObject profile = data.get("profile").getAsJsonObject();

        JsonArray nameHistory = profile.get("nameHistory").getAsJsonArray();
        if(player.getName() != null && !nameHistory.contains(new JsonPrimitive(player.getName()))) nameHistory.add(player.getName());

        DataUtils.writeJSONObject(file, data);

    }
}
