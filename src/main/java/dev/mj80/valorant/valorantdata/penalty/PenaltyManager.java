package dev.mj80.valorant.valorantdata.penalty;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mj80.valorant.valorantdata.DataUtils;
import dev.mj80.valorant.valorantdata.ValorantData;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;

public class PenaltyManager {
    @Getter private final File penaltyFile = new File(ValorantData.getInstance().getDataFolder() + File.separator + "penalties.json");
    @Getter private final ArrayList<Penalty> penalties = new ArrayList<>();
    
    public PenaltyManager() {
        try {
            if(DataUtils.createFile(penaltyFile)) {
                JsonObject penaltiesObject = new JsonObject();
                JsonArray penaltiesArray = new JsonArray();
                penaltiesObject.add("penalties", penaltiesArray);
                DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
            }
            JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
            assert penaltiesObject != null;
            JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
            for(JsonElement element : penaltiesArray) {
                Penalty penalty = Penalty.of(element.getAsJsonObject());
                penalties.add(penalty);
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }
    public void addPenalty(Penalty penalty) {
        penalties.add(penalty);
        JsonObject penaltiesObject = DataUtils.parseJSON(penaltyFile);
        assert penaltiesObject != null;
        JsonArray penaltiesArray = penaltiesObject.get("penalties").getAsJsonArray();
        penaltiesArray.add(penalty.getAsJson());
        penaltiesObject.add("penalties", penaltiesArray);
        DataUtils.writeJSONObject(penaltyFile, penaltiesObject);
    }
    public void removePenalty(Penalty penalty) {
        penalties.remove(penalty);
    }
    public JsonObject getJsonObject() {
        return DataUtils.parseJSON(penaltyFile);
    }
    public JsonArray getJsonArray() {
        return getJsonObject().get("penalties").getAsJsonArray();
    }
}
