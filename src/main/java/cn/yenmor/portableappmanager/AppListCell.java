package cn.yenmor.portableappmanager;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;

/**
 * 自定义应用列表单元格
 */
public class AppListCell extends ListCell<AppEntry> {
    private final HBox content;
    private final ImageView iconView;
    private final Label nameLabel;
    private final Label pathLabel;
    private final Label idLabel;

    public AppListCell() {
        super();

        // 创建图标视图
        iconView = new ImageView();
        iconView.setFitWidth(32);
        iconView.setFitHeight(32);
        iconView.setPreserveRatio(true);

        // 创建标签
        nameLabel = new Label();
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        pathLabel = new Label();
        pathLabel.setFont(Font.font("System", 11));
        pathLabel.setStyle("-fx-text-fill: #7f8c8d;");

        idLabel = new Label();
        idLabel.setFont(Font.font("System", 10));
        idLabel.setStyle("-fx-text-fill: #95a5a6; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-padding: 2 6;");

        // 创建右侧面板
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 布局
        content = new HBox(10);
        content.getChildren().addAll(iconView, nameLabel, spacer, idLabel, pathLabel);
        content.setStyle("-fx-padding: 8px;");
    }

    @Override
    protected void updateItem(AppEntry app, boolean empty) {
        super.updateItem(app, empty);

        if (empty || app == null) {
            setGraphic(null);
            setText(null);
        } else {
            // 设置图标
            if (app.getIconPath() != null && !app.getIconPath().isEmpty()) {
                File iconFile = new File(app.getIconPath());
                if (iconFile.exists()) {
                    try {
                        iconView.setImage(new Image(iconFile.toURI().toString()));
                    } catch (Exception e) {
                        iconView.setImage(null);
                    }
                } else {
                    iconView.setImage(null);
                }
            } else {
                iconView.setImage(null);
            }

            // 设置标签文本
            nameLabel.setText(app.getName());
            pathLabel.setText(app.getPath());
            idLabel.setText("ID: " + app.getId());

            // 设置样式（如果文件不存在）
            if (!app.exists()) {
                content.setStyle("-fx-padding: 8px; -fx-background-color: #fff3cd;");
                nameLabel.setText(app.getName() + " [NOT FOUND]");
            } else {
                content.setStyle("-fx-padding: 8px;");
            }

            setGraphic(content);
        }
    }
}