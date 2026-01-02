package cn.yenmor.portableappmanager;//package cn.yenmor.portableappmanager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;

public class PortableAppManager extends Application {

    private ListView<String> appListView;
    private static final String CONFIG_FILE = "./PortableAppManager/config.txt";
    private static final String INKS_DIR = "./PortableAppManager/Inks/";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Portable App Manager");

        // 创建布局
        VBox layout = new VBox(10);
        layout.getStyleClass().add("vbox");  // 添加vbox的CSS样式

        // 按钮
        Button addButton = new Button("Add Application");
        Button exportButton = new Button("Export Shortcuts");

        // ListView 相关操作
        appListView = new ListView<>();
        appListView.setMinSize(200, 200);
        appListView.setPlaceholder(new Label("No applications added yet"));
        appListView.setSelectionModel(null);
        appListView.setFocusTraversable(false);
        loadAppList();

        addButton.setOnAction(e -> addApplication(primaryStage));
        exportButton.setOnAction(e -> exportShortcuts());


        layout.getChildren().addAll(addButton, exportButton, appListView);

        // 创建场景并应用CSS样式
        Scene scene = new Scene(layout, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());  // 应用CSS文件

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 添加一个可执行文件
    private void addApplication(Stage primaryStage) {

        FileChooser fileChooser = new FileChooser();

        //过滤出 exe bat 文件
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.exe"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.bat"));

        //让打开的目录默认是当前工作目录
        String workingDirectory = System.getProperty("user.dir"); // 获取当前工作目录（项目根目录）
        fileChooser.setInitialDirectory(new File(workingDirectory));

        // 打开文件选择框
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            Path appPath = selectedFile.toPath();
            Path basePath = Paths.get(workingDirectory);
            Path relativePath = basePath.relativize(appPath);
            System.out.println("Relative path: " + relativePath);
            saveAppConfig(relativePath.toString());
            loadAppList();
        }
    }

    // 添加 config 到 config.txt
    private void saveAppConfig(String relativePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE, true))) {
            writer.write(relativePath);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从 config.txt 加载应用列表
    private void loadAppList() {
        appListView.getItems().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appListView.getItems().add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 导出快捷方式
    private void exportShortcuts() {
        for (String appPath : appListView.getItems()) {
            Path path = Paths.get(appPath);
            String shortcutName = String.valueOf(path.getFileName())+".lnk";
            Path absolutePath = path.toAbsolutePath();
            String s = absolutePath.toString().replaceAll("\\\\", "/");
            try {

                // 使用 Windows Shell 创建快捷方式
                String command = String.format("powershell $s=(New-Object -COM WScript.Shell).CreateShortcut('%s'); $s.TargetPath='%s'; $s.Save()",
                        INKS_DIR + shortcutName, s);
                System.out.println(command);
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                processBuilder.inheritIO();
                Process process = processBuilder.start();

                // 等待命令执行完成
                process.waitFor();  // 阻塞直到命令执行完毕
                System.out.println("Shortcut created successfully!");

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println("Shortcuts exported to " + INKS_DIR);
    }
}

