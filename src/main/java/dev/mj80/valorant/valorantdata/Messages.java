package dev.mj80.valorant.valorantdata;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public enum Messages {
    SAVING_DATA("&7Saving data..."),
    ADMIN_SAVING_DATA("&bSaving data for %s players..."),
    SAVED_DATA("&7Data saved in %s ms."),
    ADMIN_SAVED_DATA("&bSaved data for %s players in %s ms. &7(avg %s)"),
    
    ERROR_CREATING_FILE("""
                &r
                &cThere was an error while creating or loading your data!
                &c&lPlease contact an admin.
                &7INFO:
                &8%s,%s,%s
                &r"""),
    ;
    
    
    private final String message;
    
    Messages(@NotNull String message) {
        this.message = message;
    }
    
    public @NotNull String getMessage() {
        return formatMessage(message);
    }
    
    public @NotNull String getMessage(Object... args) {
        return formatMessage(String.format(message, args));
    }
    
    private static String formatMessage(String message) {
        message = message.replace("<RED>", "&#fd303a")
                .replace("<ORANGE>", "&#fcba03").replace("<YELLOW>", "&#fbfe3b")
                .replace("<DARKGREEN>", "&#2bba7c").replace("<GREEN>", "&#3afba7")
                .replace("<BLUE>", "&#31afec").replace("<PURPLE>", "&#a452e3")
                .replace("<MAGENTA>", "&#ea4adf").replace("<PINK>", "&#f6adc6")
                .replace("<GRAY>", "&#a4c1c2").replace("<DARKGRAY>", "&#787667");
        char[] chars = message.toCharArray();
        StringBuilder builder = new StringBuilder();
        String colorHex = "";
        boolean isHex = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i < chars.length - 1 && chars[i+1] == '#') {
                colorHex = "";
                isHex = true;
            } else if (isHex) {
                colorHex += chars[i];
                isHex = colorHex.length() < 7;
                if (!isHex)
                    builder.append(ChatColor.of(colorHex));
            } else
                builder.append(chars[i]);
        }
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }
}
