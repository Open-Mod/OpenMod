package dev.openmod.project.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.openmod.project.Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Map;

public class Util {
    public static <K,V> Map<K, V> readFileAsJSON(String path) {
        String fileName = Paths.get("").toAbsolutePath().resolve(path).toString();
        Map<K, V> jsonData = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<K,V>>() {}.getType();
            jsonData =  gson.fromJson(br, type);
        } catch(IOException e) {
            Project.LOGGER.error(e);
        }
        return jsonData;
    }
}
