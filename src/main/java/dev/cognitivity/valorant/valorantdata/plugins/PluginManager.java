package dev.cognitivity.valorant.valorantdata.plugins;

import com.google.gson.JsonObject;
import dev.cognitivity.valorant.valorantdata.DataUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class PluginManager {
    @Getter private final @Nullable JsonObject pluginVersions;
    
    public PluginManager() {
        this.pluginVersions = DataUtils.parseJSON(DataUtils.getTextFromURL("https://pastebin.com/raw/PAUCdutX"));
    }
    // TODO add plugin enabling/reloading
}
