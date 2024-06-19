package dev.cognitivity.valorant.valorantdata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public enum Messages {
    SAVING_DATA("<gray>Saving data..."),
    SAVED_DATA("<gray>Data saved in %s ms."),
    
    ADMIN_SAVING_DATA("<aqua>Saving data for %s players..."),
    ADMIN_SAVED_DATA("<aqua>Saved data for %s players in %s ms. <gray>(avg %s)"),
    
    LOADING_DATA("<gray>Loading data..."),
    LOADED_DATA("<gray>Loaded data in %s ms."),
    
    PERMANENT_BAN_REASON("""
            <red>You have been permanently banned from ValorantFPS.
            
            <gray>Reason: <white>%s
            <gray>Banned on <white>%s<gray>.
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>You may appeal this penalty at <aqua><u>discord.gg/example</u>
            <dark_gray>Reconnect to generate a link code for appeals.
            """),
    TEMPORARY_BAN_REASON("""
            <red>You have been temporarily banned from ValorantFPS.
            
            <gray>Reason: <white>%s
            <gray>Banned on <white>%s<gray>.
            <gray>Expires on <white>%s. <gray>(<white>%s<gray>)
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>You may appeal this penalty at <aqua><u>discord.gg/example</u>
            <dark_gray>Reconnect to generate a link code for appeals.
            """),
    PERMANENT_MUTE_REASON("""
            
            <red>You are permanently muted from ValorantFPS.
            
            <gray>Reason: <white>%s
            <gray>Muted on <white>%s<gray>.
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>You may appeal this penalty at <aqua><u>discord.gg/example</u>
            <dark_gray>Reconnect to generate a link code for appeals.
            """),
    TEMPORARY_MUTE_REASON("""
            
            <red>You are temporarily muted from ValorantFPS.
            
            <gray>Reason: <white>%s
            <gray>Muted on <white>%s<gray>.
            <gray>Expires on <white>%s. <gray>(<white>%s<gray>)
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>You may appeal this penalty at <aqua><u>discord.gg/example</u>
            <dark_gray>Reconnect to generate a link code for appeals.
            """),
    KICK_REASON("""
            <red>You have been kicked from ValorantFPS.
            
            <gray>Reason: <white>%s
            <gray>Kicked on <white>%s<gray>.
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>This penalty cannot be appealed.
            """),
    WARN_REASON("""
            
            <red>You have been warned.
            
            <gray>Reason: <white>%s
            <gray>Warned on <white>%s<gray>.
            <gray>Penalty ID: <gold>#%s
            
            <dark_gray>This penalty cannot be appealed.
            """),
    PENALTY_ADMINISTERED("""
            <aqua>[PENALTY] <white>%s <gray>has been %s by <white>%s<gray>.
              <gray>Reason: <white>"%s"
              <gray>This will %s<gray>."""),
    PENALTY_REMOVED("""
            <aqua>[PENALTY] <white>%s <gray>has been %s by <white>%s<gray>.
              <gray>Originally %s by: <white>%s
              <gray>Original reason: <white>"%s"
              <gray>Original penalty length: <white>%s""")
    ;
    
    
    private final String message;
    
    Messages(@NotNull String message) {
        this.message = message;
    }
    
    public @NotNull Component getMessage() {
        return MiniMessage.miniMessage().deserialize(message);
    }
    
    public @NotNull Component getMessage(Object... args) {
        return MiniMessage.miniMessage().deserialize(String.format(message, args));
    }
}
