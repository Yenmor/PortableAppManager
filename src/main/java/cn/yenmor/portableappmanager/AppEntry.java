package cn.yenmor.portableappmanager;

import javafx.scene.image.Image;
import java.io.File;

/**
 * 应用条目数据模型
 */
public class AppEntry {
    private String id;          // 应用唯一ID
    private String name;        // 应用显示名称
    private String path;        // 应用相对路径
    private String iconPath;    // 图标文件路径

    public AppEntry(String id, String name, String path, String iconPath) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.iconPath = iconPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * 获取应用文件的绝对路径
     */
    public File getAbsoluteFile() {
        return new File(path).getAbsoluteFile();
    }

    /**
     * 检查应用文件是否存在
     */
    public boolean exists() {
        return getAbsoluteFile().exists();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", id, name, path);
    }
}