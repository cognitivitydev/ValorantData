package dev.cognitivity.valorant.valorantdata.plugins;

import com.google.gson.JsonObject;
import dev.cognitivity.valorant.valorantdata.ValorantData;
import lombok.Getter;

public class PluginVersion {
    @Getter private final String plugin;
    @Getter private final String currentName;
    @Getter private String updatedName;
    @Getter private final int currentId;
    @Getter private int updatedId;
    
    public PluginVersion(String plugin, String currentName, int currentId) {
        this.plugin = plugin;
        this.currentName = currentName;
        this.currentId = currentId;
        verifyVersions();
    }
    public void verifyVersions() {
        JsonObject versions = ValorantData.getInstance().getPluginManager().getPluginVersions();
        if (versions == null) {
                ValorantData.getInstance().log("<red>[PLUGINS] <gray>Couldn't check version for the plugin id \""+plugin+"\".");
            return;
        }
        JsonObject jsonObject = ValorantData.getInstance().getPluginManager().getPluginVersions().get(plugin).getAsJsonObject();
        updatedName = jsonObject.get("version").getAsString();
        updatedId = jsonObject.get("id").getAsInt();
        if(currentId > updatedId) {
            ValorantData.getInstance().log("<gold>[PLUGINS] <gray>Plugin with the id \""+plugin+"\" has an unknown version. ("+currentId+" > "+updatedId+")");
        }
        if(currentId == updatedId) {
            ValorantData.getInstance().log("<green>[PLUGINS] <gray>Plugin with the id \""+plugin+"\" is up to date.");
            if(!currentName.equals(updatedName)) {
                ValorantData.getInstance().log("<gold>[PLUGINS] <gray>Plugin with the id \"" + plugin + "\" has mismatched version names. ("+currentName+" != "+updatedName+")");
            }
        }
        if(currentId < updatedId) {
            ValorantData.getInstance().log("<red>[PLUGINS] <gray>Plugin with the id \""+plugin+"\" is outdated by " +
                    (updatedId-currentId)+" versions. ("+currentId+" < "+updatedId+")");
        }
    }
}
