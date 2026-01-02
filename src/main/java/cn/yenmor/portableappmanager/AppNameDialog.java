package cn.yenmor.portableappmanager;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

/**
 * 应用命名对话框
 */
public class AppNameDialog {

    /**
     * 显示应用命名对话框
     * @param primaryStage 主窗口
     * @param selectedFile 选中的文件
     * @return 用户输入的应用名，如果取消则返回 null
     */
    public static String show(Stage primaryStage, File selectedFile) {
        // 获取默认名称（文件名去掉扩展名）
        String defaultName = selectedFile.getName();
        int dotIndex = defaultName.lastIndexOf('.');
        if (dotIndex > 0) {
            defaultName = defaultName.substring(0, dotIndex);
        }

        // 创建对话框
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Application");
        dialog.setHeaderText("Enter a name for this application");

        // 设置按钮
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 创建输入框
        TextField nameField = new TextField(defaultName);
        nameField.setPromptText("Application name");

        // 显示文件信息
        Label fileInfo = new Label("File: " + selectedFile.getPath());
        fileInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        // 布局
        VBox content = new VBox(10, fileInfo, new Label("Application Name:"), nameField);
        content.setStyle("-fx-padding: 10px;");

        dialog.getDialogPane().setContent(content);

        // 聚焦到输入框并选中所有文本
        Platform.runLater(() -> {
            nameField.requestFocus();
            nameField.selectAll();
        });

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    return name;
                }
            }
            return null;
        });

        // 显示对话框并等待结果
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}