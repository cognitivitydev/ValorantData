package dev.cognitivity.valorant.valorantdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.cognitivity.valorant.valorantdata.data.PlayerData;

import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;

public class DataUtils {
    public static boolean createFile(File file) {
        try {
            boolean mkdir = file.getParentFile().mkdirs();
            boolean created = file.createNewFile();
            return mkdir || created;
        } catch (IOException exception) {
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't create file "+file.getPath(), exception);
            return false;
        }
    }
    
    @Nullable public static JsonObject parseJSON(File file) {
        try {
            return (JsonObject) JsonParser.parseReader(new FileReader(file.getCanonicalPath()));
        } catch(Exception exception){
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't parse Json from file "+file.getPath(), exception);
            return null;
        }
    }
    @Nullable public static JsonObject parseJSON(String json) {
        if(json == null) return null;
        try {
            return (JsonObject) JsonParser.parseString(json);
        } catch (Exception exception) {
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't parse Json from "+json, exception);
            return null;
        }
    }
    public static void writeJSONObject(File file, JsonObject jsonObject) {
        try {
            PrintWriter printWriter = new PrintWriter(file.getCanonicalPath());
            printWriter.write(jsonObject.toString());
            printWriter.flush();
            printWriter.close();
        } catch(Exception exception) {
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't write Json to file "+file.getPath(), exception);
        }
    }
    
    @SuppressWarnings("unused")
    public static void updateData(PlayerData data) {
        JsonObject dataFile = parseJSON(data.getStats().getFile());
        assert dataFile != null;
        JsonArray nameHistory = dataFile.getAsJsonArray("data").get(0).getAsJsonObject()
                .getAsJsonArray("profile").get(2).getAsJsonObject()
                .getAsJsonArray("nameHistory");
        if(data.getPlayer().getName() != null && !nameHistory.contains(new JsonPrimitive(data.getPlayer().getName()))) nameHistory.add(data.getPlayer().getName());
        writeJSONObject(data.getStats().getFile(), dataFile);
    }
    
    public static String readFile(File file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            createFile(file);
            StringBuilder text = new StringBuilder();
            while(scanner.hasNextLine()) {
                text.append(scanner.nextLine()).append("\n");
            }
            if (text.toString().isBlank()) {
                scanner.close();
                return "";
            }
            scanner.close();
            return text.toString();
        } catch(Exception exception) {
            if(scanner != null) scanner.close();
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't read "+file.getPath(), exception);
            return "";
        }
    }
    
    public static double round(float n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static double round(double n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static String timeUntil(long end) {
        return timeUntil(System.currentTimeMillis(), end);
    }
    public static String timeUntil(long start, long end) {
        return timeLength(end - start);
    }
    public static String timeLength(long time) {
        if(time <= 0) {
            return "0 ms";
        }
        long years = (long) Math.floor((double) time / 31536000000L);
        time %= 31536000000L;
        long days = (long) Math.floor((double) time / 86400000);
        time %= 86400000;
        long hours = (long) Math.floor((double) time / 3600000);
        time %= 3600000;
        long minutes = (long) Math.floor((double) time / 60000);
        time %= 60000;
        long seconds = (long) Math.floor((double) time / 1000);
        long milliseconds = time % 1000;
        StringBuilder stringBuilder = new StringBuilder();
        if(years != 0) {
            stringBuilder.append(years).append("y ");
        }
        if(days != 0) {
            stringBuilder.append(days).append("d ");
        }
        if(hours != 0) {
            stringBuilder.append(hours).append("h ");
        }
        if(minutes != 0) {
            stringBuilder.append(minutes).append("m ");
        }
        if(seconds != 0) {
            stringBuilder.append(seconds).append("s ");
        }
        if(years == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            stringBuilder.append(milliseconds).append("ms");
        }
        return stringBuilder.toString().trim();
    }
    public static String getTextFromURL(String input) {
        try {
            URL url = new URI(input).toURL();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String text;
            while ((text = bufferedReader.readLine()) != null) {
                stringBuilder.append(text);
                stringBuilder.append(System.lineSeparator());
            }
            bufferedReader.close();
            return stringBuilder.toString().trim();
        } catch(UnknownHostException exception) {
            ValorantData.getInstance().log("<dark_red>[DATA] <red>Cannot read from \""+input+"\": No internet connection.");
        } catch(Exception exception) {
            ValorantData.getInstance().getLogger().log(Level.SEVERE, "Couldn't read from URL "+input, exception);
        }
        return null;
    }
}
