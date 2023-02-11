package dev.mj80.valorant.valorantdata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.mj80.valorant.valorantdata.JsonUtils;
import dev.mj80.valorant.valorantdata.Messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StatManager {
    public void createData(StatData data) {
        JsonObject particles = new JsonObject();
        particles.addProperty("particles", 1.0F);
        
        JsonArray preferencesArray = new JsonArray();
        preferencesArray.add(particles);
        
        JsonObject preferencesObject = new JsonObject();
        preferencesObject.add("preferences", preferencesArray);
        
        JsonObject firstLogin = new JsonObject();
        long firstPlayed = data.getPlayer().getFirstPlayed();
        if(firstPlayed == 0) firstPlayed = System.currentTimeMillis();
        firstLogin.addProperty("firstLogin", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new Date(firstPlayed)));
        
        JsonArray nameHistoryArray = new JsonArray();
        nameHistoryArray.add(data.getPlayer().getName());
        
        JsonObject nameHistory = new JsonObject();
        nameHistory.add("nameHistory", nameHistoryArray);
        
        JsonArray profileArray = new JsonArray();
        profileArray.add(preferencesObject);
        profileArray.add(firstLogin);
        profileArray.add(nameHistory);
        
        JsonObject profileObject = new JsonObject();
        profileObject.add("profile", profileArray);
        
        JsonObject kills = new JsonObject();
        kills.addProperty("kills", data.getKills());
        
        JsonObject deaths = new JsonObject();
        deaths.addProperty("deaths", data.getDeaths());
        
        JsonObject assists = new JsonObject();
        assists.addProperty("assists", data.getAssists());
        
        JsonObject roundsPlayed = new JsonObject();
        roundsPlayed.addProperty("roundsPlayed", data.getRoundsPlayed());
        
        JsonObject matchesPlayed = new JsonObject();
        matchesPlayed.addProperty("matchesPlayed", data.getMatchesPlayed());
        
        JsonObject victories = new JsonObject();
        victories.addProperty("victories", data.getVictories());
        
        JsonObject loses = new JsonObject();
        loses.addProperty("loses", data.getLoses());
        
        JsonObject damageDealt = new JsonObject();
        damageDealt.addProperty("damageDealt", data.getDamageDealt());
        
        JsonObject damageReceived = new JsonObject();
        damageReceived.addProperty("damageReceived", data.getDamageReceived());
        
        JsonArray statisticsArray = new JsonArray();
        statisticsArray.add(kills);
        statisticsArray.add(deaths);
        statisticsArray.add(assists);
        statisticsArray.add(roundsPlayed);
        statisticsArray.add(matchesPlayed);
        statisticsArray.add(victories);
        statisticsArray.add(loses);
        statisticsArray.add(damageDealt);
        statisticsArray.add(damageReceived);
        
        JsonObject statisticsObject = new JsonObject();
        statisticsObject.add("statistics", statisticsArray);
        
        JsonArray dataArray = new JsonArray();
        dataArray.add(profileObject);
        dataArray.add(statisticsObject);
        
        JsonObject dataObject = new JsonObject();
        dataObject.add("data", dataArray);
        
        updateData(data);
    }
    
    public void saveData(StatData data) {
        long start = System.currentTimeMillis();
        data.getPlayer().sendMessage(Messages.SAVING_DATA.getMessage());
        JsonObject json = JsonUtils.parseJSON(data.getFile());
        assert json != null;
        JsonArray statistics = json.getAsJsonArray("data").get(1).getAsJsonObject().getAsJsonArray("statistics");
        set(statistics, 0, "kills", data.getKills());
        set(statistics, 1, "deaths", data.getDeaths());
        set(statistics, 2, "assists", data.getAssists());
        set(statistics, 3, "roundsPlayed", data.getRoundsPlayed());
        set(statistics, 4, "matchesPlayed", data.getMatchesPlayed());
        set(statistics, 5, "victories", data.getVictories());
        set(statistics, 6, "loses", data.getLoses());
        set(statistics, 7, "damageDealt", data.getDamageDealt());
        set(statistics, 8, "damageReceived", data.getDamageReceived());
        JsonUtils.writeJSONObject(data.getFile(), json);
        data.getPlayer().sendMessage(String.format(Messages.SAVED_DATA.getMessage(), System.currentTimeMillis() - start));
    }
    
    public void updateData(StatData data) {
        JsonObject dataFile = JsonUtils.parseJSON(data.getFile());
        assert dataFile != null;
        JsonArray nameHistory = dataFile.getAsJsonArray("data").get(0).getAsJsonObject()
                .getAsJsonArray("profile").get(2).getAsJsonObject()
                .getAsJsonArray("nameHistory");
        if(!nameHistory.contains(new JsonPrimitive(data.getPlayer().getName()))) nameHistory.add(data.getPlayer().getName());
        JsonUtils.writeJSONObject(data.getFile(), dataFile);
    }
    
    private void set(JsonArray jsonArray, int index, String property, Number value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(property, value);
        jsonArray.set(index, jsonObject);
    }
}
