package dev.mj80.valorant.valorantdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.mj80.valorant.valorantdata.data.PlayerData;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class DataUtils {
    public static JsonObject parseJSON(File file) {
        try {
            return (JsonObject) JsonParser.parseReader(new FileReader(file.getCanonicalPath()));
        } catch(Exception exception){
            exception.printStackTrace();
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
            exception.printStackTrace();
        }
    }
    
    public static void updateData(PlayerData data) {
        JsonObject dataFile = parseJSON(data.getStats().getFile());
        assert dataFile != null;
        JsonArray nameHistory = dataFile.getAsJsonArray("data").get(0).getAsJsonObject()
                .getAsJsonArray("profile").get(2).getAsJsonObject()
                .getAsJsonArray("nameHistory");
        if(!nameHistory.contains(new JsonPrimitive(data.getPlayer().getName()))) nameHistory.add(data.getPlayer().getName());
        writeJSONObject(data.getStats().getFile(), dataFile);
    }
    
    public static String readFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            StringBuilder text = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                text.append(scanner.nextLine());
            }
            if (text.toString().isBlank()) {
                scanner.close();
                return "-";
            }
            scanner.close();
            return text.toString();
        } catch(Exception exception) {
            exception.printStackTrace();
            return "-";
        }
    }
    
    public static double round(float n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static double round(double n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
}
