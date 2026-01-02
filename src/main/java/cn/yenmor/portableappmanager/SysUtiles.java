package cn.yenmor.portableappmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import cn.yenmor.portableappmanager.PortableAppManager;
import cn.yenmor.portableappmanager.ConstVars;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import static cn.yenmor.portableappmanager.ConstVars.INKS_DIR;

public class SysUtiles {
    // 删除快捷方式文件
    public static void deleteShortcutFile(String appPath) {
        try {
            Path path = Paths.get(appPath);
            String shortcutName = path.getFileName().toString() + ".lnk";
            File shortcutFile = new File(INKS_DIR + shortcutName);

            if (shortcutFile.exists()) {
                if (shortcutFile.delete()) {
                    System.out.println("Deleted shortcut: " + shortcutName);
                } else {
                    System.err.println("Failed to delete shortcut: " + shortcutName);
                }
            } else {
                System.out.println("Shortcut file does not exist: " + shortcutName);
            }
        } catch (Exception e) {
            System.err.println("Error deleting shortcut: " + e.getMessage());
        }
    }

    // 打开开始菜单程序文件夹
    public static void openStartMenuFolder() {
        try {
            // 获取开始菜单程序文件夹路径
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                String startMenuPath = appData + "\\Microsoft\\Windows\\Start Menu\\Programs";

                // 使用 explorer 打开文件夹
                ProcessBuilder pb = new ProcessBuilder("explorer", startMenuPath);
                pb.start();
                System.out.println("Opened Start Menu folder: " + startMenuPath);
            } else {
                System.err.println("Could not find APPDATA environment variable");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open Start Menu folder");
        }
    }

    // 打开 Inks 文件夹
    public static void openInksFolder() {
        try {
            File inksDir = new File(INKS_DIR);

            // 如果目录不存在，创建它
            if (!inksDir.exists()) {
                inksDir.mkdirs();
            }

            // 使用 explorer 打开文件夹
            ProcessBuilder pb = new ProcessBuilder("explorer", inksDir.getAbsolutePath());
            pb.start();
            System.out.println("Opened Inks folder: " + inksDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Inks folder: " + e.getMessage());
        }
    }

    // 显示信息对话框
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 显示确认对话框
    public static boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}
