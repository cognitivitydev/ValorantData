package dev.mj80.valorant.valorantdata.plugins;

import com.google.gson.JsonObject;
import dev.mj80.valorant.valorantdata.DataUtils;
import lombok.Getter;

import javax.annotation.Nullable;
import java.net.UnknownHostException;

public class PluginManager {
    @Getter private final @Nullable JsonObject pluginVersions;
    
    public PluginManager() {
        this.pluginVersions = DataUtils.parseJSON(DataUtils.getTextFromURL("https://pastebin.com/raw/PAUCdutX"));
    }
    // TODO add plugin enabling/reloading
}
