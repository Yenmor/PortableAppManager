package cn.yenmor.portableappmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 配置管理器 - 负责读写应用的JSON配置
 */
public class ConfigManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type APP_LIST_TYPE = new TypeToken<List<AppEntry>>(){}.getType();

    /**
     * 读取所有应用配置
     */
    public static List<AppEntry> loadApps() {
        File configFile = new File(ConstVars.CONFIG_FILE);

        if (!configFile.exists()) {
            // 尝试从旧的配置文件迁移
            return migrateFromOldConfig();
        }

        try {
            String json = new String(Files.readAllBytes(Paths.get(ConstVars.CONFIG_FILE)));
            List<AppEntry> apps = gson.fromJson(json, APP_LIST_TYPE);
            return apps != null ? apps : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 保存所有应用配置
     */
    public static void saveApps(List<AppEntry> apps) {
        try {
            String json = gson.toJson(apps);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ConstVars.CONFIG_FILE))) {
                writer.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加新应用
     */
    public static void addApp(AppEntry app) {
        List<AppEntry> apps = loadApps();
        apps.add(app);
        saveApps(apps);
    }

    /**
     * 删除应用
     */
    public static void removeApp(String appId) {
        List<AppEntry> apps = loadApps();
        apps.removeIf(app -> app.getId().equals(appId));
        saveApps(apps);
    }

    /**
     * 生成唯一ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 从旧的配置文件迁移数据
     */
    private static List<AppEntry> migrateFromOldConfig() {
        List<AppEntry> apps = new ArrayList<>();
        File oldConfigFile = new File(ConstVars.OLD_CONFIG_FILE);

        if (!oldConfigFile.exists()) {
            return apps;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(oldConfigFile))) {
            String line;
            int idCounter = 1;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String id = String.format("%03d", idCounter++);
                    String fileName = new File(line).getName();
                    // 移除扩展名作为默认名称
                    String name = fileName.contains(".") ?
                        fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

                    AppEntry entry = new AppEntry(id, name, line, "");
                    apps.add(entry);
                }
            }
            // 保存为新的JSON格式
            saveApps(apps);
            System.out.println("Migrated " + apps.size() + " apps from old config");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apps;
    }
}