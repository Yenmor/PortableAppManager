package cn.yenmor.portableappmanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static cn.yenmor.portableappmanager.ConstVars.*;
import static cn.yenmor.portableappmanager.SysUtiles.*;

public class PortableAppManager extends Application {

    private ListView<AppEntry> appListView;
    private ObservableList<AppEntry> apps;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Portable App Manager");

        // 主布局
        VBox mainLayout = new VBox(15);
        mainLayout.getStyleClass().add("main-layout");
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        // 标题部分
        Label titleLabel = new Label("Portable App Manager");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Manage your portable applications easily");
        subtitleLabel.getStyleClass().add("subtitle-label");

        VBox titleBox = new VBox(5, titleLabel, subtitleLabel);
        titleBox.getStyleClass().add("title-box");

        // 按钮组
        HBox buttonRow1 = new HBox(10);
        buttonRow1.getStyleClass().add("button-row");

        HBox buttonRow2 = new HBox(10);
        buttonRow2.getStyleClass().add("button-row");

        // 第一行按钮
        Button addButton = new Button("➕ Add App");
        Button deleteButton = new Button("🗑️ Delete");

        // 第二行按钮
        Button exportButton = new Button("📦 Export to Inks");
        Button exportToStartMenuButton = new Button("🚀 Export to Start Menu");

        // 第三行按钮
        HBox buttonRow3 = new HBox(10);
        buttonRow3.getStyleClass().add("button-row");

        Button openInksFolderButton = new Button("📁 Open Inks");
        Button openStartMenuButton = new Button("📂 Open Start Menu");

        // 设置按钮样式类
        addButton.getStyleClass().add("primary-button");
        exportButton.getStyleClass().add("success-button");
        exportToStartMenuButton.getStyleClass().add("success-button");
        deleteButton.getStyleClass().add("danger-button");
        openInksFolderButton.getStyleClass().add("info-button");
        openStartMenuButton.getStyleClass().add("info-button");

        // 按钮事件
        addButton.setOnAction(e -> addApplication(primaryStage));
        exportButton.setOnAction(e -> exportShortcuts());
        exportToStartMenuButton.setOnAction(e -> exportToStartMenu());
        openStartMenuButton.setOnAction(e -> openStartMenuFolder());
        openInksFolderButton.setOnAction(e -> openInksFolder());
        deleteButton.setOnAction(e -> deleteSelectedApp());

        // 添加按钮到行
        buttonRow1.getChildren().addAll(addButton, deleteButton);
        buttonRow2.getChildren().addAll(exportButton, exportToStartMenuButton);
        buttonRow3.getChildren().addAll(openInksFolderButton, openStartMenuButton);

        // ListView - 使用 AppEntry 类型
        appListView = new ListView<>();
        appListView.getStyleClass().add("app-list");
        appListView.setMinSize(500, 300);
        appListView.setPlaceholder(new Label("No applications added yet"));

        // 设置自定义 CellFactory
        appListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<AppEntry> call(ListView<AppEntry> param) {
                return new AppListCell();
            }
        });

        // 加载应用列表
        loadAppList();

        // 统计标签
        Label statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");
        updateStats(statsLabel);

        // 添加所有组件到主布局
        mainLayout.getChildren().addAll(
                titleBox,
                buttonRow1,
                buttonRow2,
                buttonRow3,
                statsLabel,
                appListView
        );

        // 创建场景并应用CSS样式
        Scene scene = new Scene(mainLayout, 500, 700);
        scene.setFill(javafx.scene.paint.Color.web("#f5f6fa"));
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 添加一个可执行文件
    private void addApplication(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();

        // 过滤出 exe bat 文件
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.exe"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.bat"));

        // 让打开的目录默认是当前工作目录
        String workingDirectory = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(workingDirectory));

        // 打开文件选择框
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            // 显示命名对话框
            String appName = AppNameDialog.show(primaryStage, selectedFile);

            if (appName != null) {
                try {
                    // 计算相对路径
                    Path appPath = selectedFile.toPath();
                    Path basePath = Paths.get(workingDirectory);
                    Path relativePath = basePath.relativize(appPath);

                    // 生成ID
                    String appId = ConfigManager.generateId();

                    // 提取图标
                    String iconPath = ICONS_DIR + appId + ".png";
                    boolean iconExtracted = IconExtractor.extractIcon(selectedFile.getAbsolutePath(), iconPath);

                    if (!iconExtracted) {
                        System.err.println("Failed to extract icon, using default");
                        iconPath = ""; // 使用默认图标
                    }

                    // 创建应用条目
                    AppEntry app = new AppEntry(appId, appName, relativePath.toString(), iconPath);

                    // 保存到配置
                    ConfigManager.addApp(app);

                    // 刷新列表
                    loadAppList();

                    showAlert("Success", "Application added successfully!");

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to add application: " + e.getMessage());
                }
            }
        }
    }

    // 从配置加载应用列表
    private void loadAppList() {
        List<AppEntry> appEntries = ConfigManager.loadApps();
        apps = FXCollections.observableArrayList(appEntries);
        appListView.setItems(apps);
    }

    // 导出快捷方式
    private void exportShortcuts() {
        int successCount = 0;
        int failCount = 0;

        for (AppEntry app : apps) {
            if (!app.exists()) {
                continue;
            }

            try {
                Path absolutePath = Paths.get(app.getPath()).toAbsolutePath();
                String targetPath = absolutePath.toString().replaceAll("\\\\", "/");
                String workingDir = absolutePath.getParent().toString().replaceAll("\\\\", "/");
                String shortcutName = app.getName() + ".lnk";

                // 使用 Windows Shell 创建快捷方式
                String command = String.format(
                        "powershell $s=(New-Object -COM WScript.Shell).CreateShortcut('%s'); $s.TargetPath='%s'; $s.WorkingDirectory='%s'; $s.Save()",
                        INKS_DIR + shortcutName, targetPath, workingDir);

                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                Process process = processBuilder.start();
                process.waitFor();

                successCount++;
                System.out.println("Exported: " + app.getName());

            } catch (Exception e) {
                failCount++;
                System.err.println("Failed to export " + app.getName() + ": " + e.getMessage());
            }
        }

        showAlert("Export Complete",
                String.format("Successfully exported: %d\nFailed: %d\n\nLocation: %s",
                        successCount, failCount, INKS_DIR));
    }

    // 导出到开始菜单
    private void exportToStartMenu() {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            showAlert("Error", "Could not find APPDATA environment variable");
            return;
        }

        String startMenuPath = appData + "\\Microsoft\\Windows\\Start Menu\\Programs";
        File startMenuDir = new File(startMenuPath);
        if (!startMenuDir.exists()) {
            showAlert("Error", "Start Menu directory does not exist: " + startMenuPath);
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (AppEntry app : apps) {
            if (!app.exists()) {
                continue;
            }

            try {
                Path absolutePath = Paths.get(app.getPath()).toAbsolutePath();
                String targetPath = absolutePath.toString().replaceAll("\\\\", "/");
                String workingDir = absolutePath.getParent().toString().replaceAll("\\\\", "/");
                String shortcutName = app.getName() + ".lnk";

                // 在开始菜单创建快捷方式
                String command = String.format(
                        "powershell $s=(New-Object -COM WScript.Shell).CreateShortcut('%s'); $s.TargetPath='%s'; $s.WorkingDirectory='%s'; $s.Save()",
                        startMenuPath + "\\" + shortcutName, targetPath, workingDir);

                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                Process process = processBuilder.start();
                process.waitFor();

                successCount++;
                System.out.println("Exported to Start Menu: " + app.getName());

            } catch (Exception e) {
                failCount++;
                System.err.println("Failed to export " + app.getName() + ": " + e.getMessage());
            }
        }

        showAlert("Export Complete",
                String.format("Successfully exported: %d\nFailed: %d\n\nLocation: %s",
                        successCount, failCount, startMenuPath));
    }

    // 删除选中的应用
    private void deleteSelectedApp() {
        AppEntry selected = appListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Please select an application to delete.");
            return;
        }

        // 确认对话框
        boolean confirmed = showConfirmDialog("Confirm Delete",
                "Are you sure you want to delete this application?\n\n" + selected.getName());

        if (!confirmed) {
            return;
        }

        try {
            // 从配置文件中删除
            ConfigManager.removeApp(selected.getId());

            // 删除图标文件
            if (selected.getIconPath() != null && !selected.getIconPath().isEmpty()) {
                deleteShortcutFile(selected.getIconPath());
            }

            // 刷新列表
            loadAppList();

            System.out.println("Deleted application: " + selected.getName());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete application: " + e.getMessage());
        }
    }

    // 更新统计信息
    private void updateStats(Label statsLabel) {
        int totalApps = apps != null ? apps.size() : 0;
        int notFoundApps = 0;

        if (apps != null) {
            for (AppEntry app : apps) {
                if (!app.exists()) {
                    notFoundApps++;
                }
            }
        }

        int validApps = totalApps - notFoundApps;
        statsLabel.setText(String.format("📊 Total: %d | ✓ Valid: %d | ✗ Missing: %d",
                totalApps, validApps, notFoundApps));
    }
}