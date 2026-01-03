package cn.yenmor.portableappmanager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 软件包管理器
 * 处理应用的导出、导入和合并功能
 */
public class PackageManager {
    private static final Logger logger = Logger.getLogger(PackageManager.class.getName());
    private static final int BUFFER_SIZE = 8192;

    /**
     * 导出单个应用到ZIP文件
     * @param app 应用条目
     * @param zipOutputPath ZIP文件输出路径
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static boolean exportApp(AppEntry app, String zipOutputPath, Consumer<String> progressCallback) {
        return exportApps(List.of(app), zipOutputPath, progressCallback);
    }

    /**
     * 导出多个应用到ZIP文件
     * @param apps 应用列表
     * @param zipOutputPath ZIP文件输出路径
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static boolean exportApps(List<AppEntry> apps, String zipOutputPath, Consumer<String> progressCallback) {
        if (apps == null || apps.isEmpty()) {
            logger.severe("No apps to export");
            return false;
        }

        // 验证所有应用存在
        for (AppEntry app : apps) {
            if (!app.exists()) {
                logger.severe("Application not found: " + app.getName());
                return false;
            }
        }

        try {
            // 创建PackageEntry
            PackageEntry packageEntry = new PackageEntry();
            packageEntry.setAppEntries(apps);

            // 创建ZIP文件
            try (ZipOutputStream zos = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(zipOutputPath)))) {

                // 添加manifest.json
                if (progressCallback != null) {
                    progressCallback.accept("Adding manifest...");
                }
                addManifestToZip(zos, packageEntry);

                // 对每个应用添加文件
                for (int i = 0; i < apps.size(); i++) {
                    AppEntry app = apps.get(i);
                    if (progressCallback != null) {
                        progressCallback.accept(String.format("Exporting %d/%d: %s", i + 1, apps.size(), app.getName()));
                    }

                    // 获取应用文件和所在目录（使用配置中的相对路径）
                    String appPath = app.getPath();
                    File appFile = app.getAbsoluteFile();
                    File appDir = appFile.getParentFile();

                    // 计算相对路径中的目录部分（例如："MyDir/app.exe" -> "MyDir/"）
                    Path appFilePath = Paths.get(appPath);
                    Path appDirPath = appFilePath.getParent();
                    String basePath = appDirPath != null ? appDirPath.toString() + "/" : "";

                    // 添加应用目录到ZIP（从应用所在目录开始打包）
                    addDirectoryToZip(zos, appDir, basePath, progressCallback);

                    // 添加图标（如果存在）
                    if (app.getIconPath() != null && !app.getIconPath().isEmpty()) {
                        File iconFile = new File(app.getIconPath());
                        if (iconFile.exists()) {
                            addFileToZip(zos, iconFile, "icons/" + iconFile.getName());
                        }
                    }
                }

                if (progressCallback != null) {
                    progressCallback.accept("Finalizing package...");
                }

                // 显式刷新以确保所有数据写入
                zos.flush();

                // ZipOutputStream关闭时会自动完成（try-with-resources）
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Export failed", e);
            // 删除不完整的ZIP文件
            new File(zipOutputPath).delete();
            return false;
        }
    }

    /**
     * 从ZIP文件导入应用
     * @param zipPath ZIP文件路径
     * @param targetBasePath 目标基础路径
     * @param strategy 导入策略
     * @return 导入的应用列表
     */
    public static List<AppEntry> importPackage(String zipPath, String targetBasePath, ImportStrategy strategy) {
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("Package file not found: " + zipPath);
        }

        List<AppEntry> importedApps = new ArrayList<>();
        PackageEntry packageEntry = null;

        try {
            // 第一遍：读取manifest.json
            packageEntry = readManifestFromZip(String.valueOf(zipFile));
            if (packageEntry == null) {
                throw new IOException("Invalid package: missing or invalid manifest.json");
            }

            // 检查重复应用
            List<AppEntry> currentApps = ConfigManager.loadApps();
            Set<String> currentNames = currentApps.stream()
                    .map(AppEntry::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            // 处理名称冲突
            Map<String, String> nameMapping = new HashMap<>();
            for (AppEntry app : packageEntry.getAppEntries()) {
                String originalName = app.getName();
                String finalName = originalName;

                if (currentNames.contains(originalName.toLowerCase())) {
                    switch (strategy) {
                        case SKIP:
                            continue; // 跳过此应用
                        case REPLACE:
                            // 删除现有应用
                            AppEntry existingApp = currentApps.stream()
                                    .filter(a -> a.getName().equalsIgnoreCase(originalName))
                                    .findFirst()
                                    .orElse(null);
                            if (existingApp != null) {
                                ConfigManager.removeApp(existingApp.getId());
                            }
                            finalName = originalName;
                            break;
                        case RENAME:
                            // 添加数字后缀
                            int suffix = 2;
                            do {
                                finalName = originalName + " (" + suffix + ")";
                                suffix++;
                            } while (currentNames.contains(finalName.toLowerCase()));
                            break;
                    }
                }

                nameMapping.put(originalName, finalName);
                currentNames.add(finalName.toLowerCase());
            }

            // 第二遍：解压文件
            try (ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(zipFile)))) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    // 安全检查：防止路径遍历攻击
                    if (entryName.contains("..")) {
                        logger.warning("Skipping potentially malicious entry: " + entryName);
                        continue;
                    }

                    if (entryName.equals("manifest.json")) {
                        // 跳过manifest.json，已经在第一遍读取过了
                        zis.closeEntry();
                        continue;
                    } else if (entryName.startsWith("icons/")) {
                        // 提取图标文件
                        File iconDir = new File(ConstVars.ICONS_DIR);
                        if (!iconDir.exists()) {
                            iconDir.mkdirs();
                        }
                        File targetFile = new File(ConstVars.ICONS_DIR + new File(entryName).getName());
                        extractZipEntry(zis, entry, targetFile);
                    } else {
                        // 提取应用文件到项目根目录，保持原有目录结构
                        File targetFile = new File(targetBasePath, entryName);
                        extractZipEntry(zis, entry, targetFile);
                    }

                    zis.closeEntry();
                }
            }

            // 创建AppEntry对象（可能需要重命名）
            for (AppEntry originalApp : packageEntry.getAppEntries()) {
                String originalName = originalApp.getName();
                String finalName = nameMapping.getOrDefault(originalName, originalName);

                // 如果被跳过，不添加到列表
                if (finalName.equals(originalName) && strategy == ImportStrategy.SKIP
                        && currentApps.stream().anyMatch(a -> a.getName().equalsIgnoreCase(originalName))) {
                    continue;
                }

                // 创建新的AppEntry
                String newId = ConfigManager.generateId();
                String iconPath = "";
                if (originalApp.getIconPath() != null && !originalApp.getIconPath().isEmpty()) {
                    String iconFileName = new File(originalApp.getIconPath()).getName();
                    iconPath = ConstVars.ICONS_DIR + iconFileName;
                }

                AppEntry newApp = new AppEntry(newId, finalName, originalApp.getPath(), iconPath);
                importedApps.add(newApp);

                // 保存到配置
                ConfigManager.addApp(newApp);
            }

            return importedApps;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Import failed", e);
            throw new RuntimeException("Failed to import package: " + e.getMessage(), e);
        }
    }

    /**
     * 合并多个ZIP包
     * @param zipPaths ZIP文件路径列表
     * @param targetBasePath 目标基础路径
     * @return 所有导入的应用（去重后）
     */
    public static List<AppEntry> mergePackages(List<String> zipPaths, String targetBasePath) {
        if (zipPaths == null || zipPaths.isEmpty()) {
            throw new IllegalArgumentException("No packages to merge");
        }

        List<AppEntry> allApps = new ArrayList<>();
        Map<String, AppEntry> appMap = new LinkedHashMap<>(); // 保持插入顺序

        for (String zipPath : zipPaths) {
            try {
                // 使用RENAME策略避免冲突
                List<AppEntry> importedApps = importPackage(zipPath, targetBasePath, ImportStrategy.RENAME);

                // 根据应用名称去重（保留首次出现的）
                for (AppEntry app : importedApps) {
                    String key = app.getName().toLowerCase();
                    if (!appMap.containsKey(key)) {
                        appMap.put(key, app);
                    }
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to merge package: " + zipPath, e);
                // 继续处理其他包
            }
        }

        return new ArrayList<>(appMap.values());
    }

    /**
     * 预览ZIP包内容（不实际导入）
     * @param zipPath ZIP文件路径
     * @return PackageEntry 包含包信息
     */
    public static PackageEntry previewPackage(String zipPath) {
        try {
            return readManifestFromZip(zipPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to preview package", e);
            return null;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 添加manifest.json到ZIP
     */
    private static void addManifestToZip(ZipOutputStream zos, PackageEntry packageEntry) throws IOException {
        ZipEntry entry = new ZipEntry("manifest.json");
        zos.putNextEntry(entry);

        byte[] manifestBytes = packageEntry.toJson().getBytes(StandardCharsets.UTF_8);
        zos.write(manifestBytes);

        zos.closeEntry();
    }

    /**
     * 从ZIP读取manifest.json
     */
    private static PackageEntry readManifestFromZip(String zipPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipPath)))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("manifest.json".equals(entry.getName())) {
                    byte[] bytes = zis.readAllBytes();
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    return PackageEntry.fromJson(json);
                }
                zis.closeEntry();
            }
        }

        return null;
    }

    /**
     * 递归添加目录到ZIP
     */
    private static void addDirectoryToZip(ZipOutputStream zos, File dir, String basePath,
                                          Consumer<String> callback) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        // 获取基础路径（应用的可执行文件所在目录）
        File baseDir = dir;
        int fileCount = 0;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                addDirectoryToZip(zos, file, basePath + file.getName() + "/", callback);
            } else {
                // 计算相对路径
                String relativePath = basePath + getRelativePath(file, baseDir);
                addFileToZip(zos, file, relativePath);

                // 每10个文件报告一次进度，避免UI更新过频
                fileCount++;
                if (callback != null && fileCount % 10 == 0) {
                    callback.accept("  Added " + fileCount + " files...");
                }
            }
        }

        // 完成目录时报告
        if (callback != null && fileCount > 0) {
            callback.accept("  Completed: " + baseDir.getName() + " (" + fileCount + " files)");
        }
    }

    /**
     * 计算文件相对于基础目录的路径
     */
    private static String getRelativePath(File file, File baseDir) {
        try {
            Path basePath = baseDir.toPath();
            Path filePath = file.toPath();
            return basePath.relativize(filePath).toString();
        } catch (IllegalArgumentException e) {
            // 如果无法计算相对路径，返回文件名
            return file.getName();
        }
    }

    /**
     * 添加单个文件到ZIP
     */
    private static void addFileToZip(ZipOutputStream zos, File file, String entryPath) throws IOException {
        ZipEntry entry = new ZipEntry(entryPath);
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }

        zos.closeEntry();
    }

    /**
     * 解压ZIP条目到目标位置
     */
    private static void extractZipEntry(ZipInputStream zis, ZipEntry entry, File targetFile) throws IOException {
        if (entry.isDirectory()) {
            targetFile.mkdirs();
            return;
        }

        // 确保父目录存在
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = zis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
    }
}
