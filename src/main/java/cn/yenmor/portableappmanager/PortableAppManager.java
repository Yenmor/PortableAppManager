package cn.yenmor.portableappmanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Button deleteButton = new Button("🗑 Delete");
        Button refreshButton = new Button("🔄 Refresh");

        // 第二行按钮
        Button exportButton = new Button("📦 Export to Inks");
        Button exportToStartMenuButton = new Button("🚀 Export to Start Menu");

        // 第三行按钮
        HBox buttonRow3 = new HBox(10);
        buttonRow3.getStyleClass().add("button-row");

        Button openInksFolderButton = new Button("📁 Open Inks");
        Button openStartMenuButton = new Button("📂 Open Start Menu");

        // 第四行按钮 - 导出/导入/合并功能
        HBox buttonRow4 = new HBox(10);
        buttonRow4.getStyleClass().add("button-row");

        Button exportPackageButton = new Button("📦 Export Package");
        Button importPackageButton = new Button("📥 Import Package");
        Button mergePackagesButton = new Button("🔀 Merge Packages");

        // 设置按钮样式类
        addButton.getStyleClass().add("primary-button");
        exportButton.getStyleClass().add("success-button");
        exportToStartMenuButton.getStyleClass().add("success-button");
        deleteButton.getStyleClass().add("danger-button");
        refreshButton.getStyleClass().add("info-button");
        openInksFolderButton.getStyleClass().add("info-button");
        openStartMenuButton.getStyleClass().add("info-button");
        exportPackageButton.getStyleClass().add("success-button");
        importPackageButton.getStyleClass().add("primary-button");
        mergePackagesButton.getStyleClass().add("info-button");

        // 按钮事件
        addButton.setOnAction(e -> addApplication(primaryStage));
        exportButton.setOnAction(e -> exportShortcuts());
        exportToStartMenuButton.setOnAction(e -> exportToStartMenu());
        openStartMenuButton.setOnAction(e -> openStartMenuFolder());
        openInksFolderButton.setOnAction(e -> openInksFolder());
        deleteButton.setOnAction(e -> deleteSelectedApp());
        exportPackageButton.setOnAction(e -> exportApplicationPackage(primaryStage));
        importPackageButton.setOnAction(e -> importApplicationPackage(primaryStage));
        mergePackagesButton.setOnAction(e -> mergeApplicationPackages(primaryStage));

        // 添加按钮到行
        buttonRow1.getChildren().addAll(addButton, deleteButton, refreshButton);
        buttonRow2.getChildren().addAll(exportButton, exportToStartMenuButton);
        buttonRow3.getChildren().addAll(openInksFolderButton, openStartMenuButton);
        buttonRow4.getChildren().addAll(exportPackageButton, importPackageButton, mergePackagesButton);

        // ListView - 使用 AppEntry 类型
        appListView = new ListView<>();
        appListView.getStyleClass().add("app-list");
        appListView.setMinSize(500, 300);
        appListView.setPlaceholder(new Label("No applications added yet"));

        // 启用多选模式（用于导出多个应用）
        appListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 设置自定义 CellFactory
        appListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<AppEntry> call(ListView<AppEntry> param) {
                return new AppListCell();
            }
        });

        // 全选和取消全选按钮
        Button selectAllButton = new Button("✓ All");
        Button deselectAllButton = new Button("✗ None");

        // 设置小按钮样式
        selectAllButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px; -fx-min-width: 60px;");
        deselectAllButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px; -fx-min-width: 60px;");

        // 按钮事件
        selectAllButton.setOnAction(e -> {
            if (apps != null && !apps.isEmpty()) {
                appListView.getSelectionModel().selectAll();
            }
        });

        deselectAllButton.setOnAction(e -> {
            appListView.getSelectionModel().clearSelection();
        });

        // 创建按钮容器并放在左上角
        HBox selectionButtons = new HBox(5, selectAllButton, deselectAllButton);
        selectionButtons.setStyle("-fx-padding: 5px; -fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 5;");

        // 使用StackPane将按钮放在ListView上方
        StackPane listViewContainer = new StackPane();
        listViewContainer.getChildren().addAll(appListView, selectionButtons);
        StackPane.setAlignment(selectionButtons, Pos.TOP_LEFT);
        StackPane.setMargin(selectionButtons, new Insets(5));

        // 加载应用列表
        loadAppList();

        // 统计标签
        Label statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");
        updateStats(statsLabel);

        // 设置刷新按钮事件（需要statsLabel）
        refreshButton.setOnAction(e -> refreshAppList(statsLabel));

        // 添加所有组件到主布局
        mainLayout.getChildren().addAll(
                titleBox,
                buttonRow1,
                buttonRow2,
                buttonRow3,
                buttonRow4,
                statsLabel,
                listViewContainer
        );

        // 创建场景并应用CSS样式
        Scene scene = new Scene(mainLayout, 600, 800);
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

    // 刷新应用列表（重新加载并更新UI）
    private void refreshAppList(Label statsLabel) {
        // 重新加载应用列表
        List<AppEntry> appEntries = ConfigManager.loadApps();

        // 清空并重新填充
        apps.clear();
        apps.addAll(appEntries);

        // 强制ListView刷新（这会触发CellFactory重新渲染，包括Missing标志）
        appListView.setItems(apps);

        // 更新统计信息
        updateStats(statsLabel);
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

    // ==================== 软件包导出/导入/合并功能 ====================

    // 导出应用包
    private void exportApplicationPackage(Stage primaryStage) {
        ObservableList<AppEntry> selectedApps = appListView.getSelectionModel().getSelectedItems();

        if (selectedApps.isEmpty()) {
            showAlert("No Selection", "Please select at least one application to export.");
            return;
        }

        // 确认导出
        boolean confirmed = showConfirmDialog(
            "Export Package",
            String.format("Export %d selected application(s)?\n\nThe entire application directory will be packaged.", selectedApps.size())
        );

        if (!confirmed) {
            return;
        }

        // 选择保存位置
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Package");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Package Files", "*.zip")
        );

        // 默认文件名
        String defaultName = "apps_" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
        fileChooser.setInitialFileName(defaultName);

        File zipFile = fileChooser.showSaveDialog(primaryStage);
        if (zipFile == null) {
            return;
        }

        // 显示进度并执行导出
        Task<Boolean> exportTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Preparing export...");

                return PackageManager.exportApps(
                    selectedApps,
                    zipFile.getAbsolutePath(),
                    msg -> {
                        updateMessage(msg);
                        // 如果是finalizing阶段，添加提示
                        if (msg.contains("Finalizing")) {
                            updateMessage("Finalizing package... (This may take a moment for large packages)");
                        }
                    }
                );
            }
        };

        showProgressDialog(primaryStage, exportTask, "Exporting Package", () -> {
            if (exportTask.getValue()) {
                showAlert("Export Complete",
                    String.format("Successfully exported %d application(s) to:\n%s",
                        selectedApps.size(), zipFile.getAbsolutePath()));
                loadAppList(); // 刷新统计
            } else {
                showAlert("Export Failed", "Failed to export applications. Please check if all files exist.");
            }
        }, () -> {
            showAlert("Export Error", "Error during export: " +
                     exportTask.getException().getMessage());
        });

        new Thread(exportTask).start();
    }

    // 导入应用包
    private void importApplicationPackage(Stage primaryStage) {
        // 选择ZIP文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Package to Import");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Package Files", "*.zip")
        );

        File zipFile = fileChooser.showOpenDialog(primaryStage);
        if (zipFile == null) {
            return;
        }

        // 预览包内容
        PackageEntry preview = PackageManager.previewPackage(zipFile.getAbsolutePath());
        if (preview == null) {
            showAlert("Invalid Package", "The selected file is not a valid package.");
            return;
        }

        // 检查重复
        List<AppEntry> currentApps = ConfigManager.loadApps();
        List<String> duplicateNames = preview.getAppEntries().stream()
            .map(AppEntry::getName)
            .filter(name -> currentApps.stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(name)))
            .collect(Collectors.toList());

        // 确定导入策略（必须是 effectively final）
        final ImportStrategy strategy;
        if (!duplicateNames.isEmpty()) {
            ImportStrategy selected = showDuplicateResolutionDialog(duplicateNames);
            if (selected == null) {
                return; // 用户取消
            }
            strategy = selected;
        } else {
            strategy = ImportStrategy.RENAME;
        }

        // 执行导入
        Task<List<AppEntry>> importTask = new Task<List<AppEntry>>() {
            @Override
            protected List<AppEntry> call() throws Exception {
                updateMessage("Importing package...");
                return PackageManager.importPackage(
                    zipFile.getAbsolutePath(),
                    System.getProperty("user.dir"),
                    strategy
                );
            }
        };

        showProgressDialog(primaryStage, importTask, "Importing Package",
            () -> {
                List<AppEntry> importedApps = importTask.getValue();

                // 刷新列表
                loadAppList();

                showAlert("Import Complete",
                    String.format("Successfully imported %d application(s):\n%s",
                        importedApps.size(),
                        importedApps.stream()
                            .map(AppEntry::getName)
                            .collect(Collectors.joining(", "))));
            },
            () -> {
                showAlert("Import Error", "Error during import: " +
                         importTask.getException().getMessage());
            }
        );

        new Thread(importTask).start();
    }

    // 合并多个应用包
    private void mergeApplicationPackages(Stage primaryStage) {
        // 允许选择多个ZIP文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Packages to Merge");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Package Files", "*.zip")
        );

        List<File> zipFiles = fileChooser.showOpenMultipleDialog(primaryStage);
        if (zipFiles == null || zipFiles.isEmpty()) {
            return;
        }

        // 确认对话框
        boolean confirmed = showConfirmDialog(
            "Confirm Merge",
            String.format("Merge %d package(s)?\n\nDuplicate apps will be automatically renamed.",
                zipFiles.size())
        );

        if (!confirmed) {
            return;
        }

        // 执行合并
        Task<List<AppEntry>> mergeTask = new Task<List<AppEntry>>() {
            @Override
            protected List<AppEntry> call() throws Exception {
                updateMessage("Merging packages...");

                List<String> paths = zipFiles.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

                return PackageManager.mergePackages(paths, System.getProperty("user.dir"));
            }
        };

        showProgressDialog(primaryStage, mergeTask, "Merging Packages",
            () -> {
                List<AppEntry> mergedApps = mergeTask.getValue();

                // 刷新列表
                loadAppList();

                showAlert("Merge Complete",
                    String.format("Successfully merged %d unique application(s).", mergedApps.size()));
            },
            () -> {
                showAlert("Merge Error", "Error during merge: " +
                         mergeTask.getException().getMessage());
            }
        );

        new Thread(mergeTask).start();
    }

    // 显示进度对话框
    private void showProgressDialog(Stage owner, Task<?> task, String title,
                                     Runnable onSuccess, Runnable onFailure) {
        Stage progressStage = new Stage();
        progressStage.initOwner(owner);
        progressStage.setTitle(title);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setPrefWidth(300);

        Label statusLabel = new Label();
        statusLabel.textProperty().bind(task.messageProperty());
        statusLabel.setPrefWidth(300);
        statusLabel.setWrapText(true);

        VBox vbox = new VBox(10, progressBar, statusLabel);
        vbox.setStyle("-fx-padding: 20px;");

        progressStage.setScene(new Scene(vbox, 320, 100));
        progressStage.setResizable(false);
        progressStage.show();

        task.setOnSucceeded(e -> {
            progressStage.close();
            if (onSuccess != null) {
                onSuccess.run();
            }
        });

        task.setOnFailed(e -> {
            progressStage.close();
            if (onFailure != null) {
                onFailure.run();
            }
        });
    }

    // 显示重复应用解决对话框
    private ImportStrategy showDuplicateResolutionDialog(List<String> duplicates) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Duplicate Applications Found");
        alert.setHeaderText("The following applications already exist:");

        TextArea textArea = new TextArea(String.join("\n", duplicates));
        textArea.setEditable(false);
        textArea.setPrefHeight(100);

        VBox content = new VBox(10,
            new Label("Choose how to handle duplicates:"), textArea);

        ButtonType skipButton = new ButtonType("Skip Duplicates");
        ButtonType replaceButton = new ButtonType("Replace Existing");
        ButtonType renameButton = new ButtonType("Rename Imported (Recommended)");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(renameButton, skipButton, replaceButton, cancelButton);
        alert.getDialogPane().setContent(content);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty()) {
            return null; // 取消
        }

        ButtonType selected = result.get();
        if (selected == skipButton) return ImportStrategy.SKIP;
        if (selected == replaceButton) return ImportStrategy.REPLACE;
        if (selected == renameButton) return ImportStrategy.RENAME;

        return null; // 取消
    }
}