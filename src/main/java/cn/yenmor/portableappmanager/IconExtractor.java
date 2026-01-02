package cn.yenmor.portableappmanager;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * 图标提取工具类
 */
public class IconExtractor {

    /**
     * 从可执行文件提取图标并保存到指定路径
     * @param exePath 可执行文件路径
     * @param outputPath 输出图标路径（PNG格式）
     * @return 是否成功
     */
    public static boolean extractIcon(String exePath, String outputPath) {
        try {
            File file = new File(exePath);
            if (!file.exists()) {
                return false;
            }

            // 使用 Swing 获取系统图标
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);

            if (icon == null) {
                // 如果无法获取系统图标，使用默认图标
                return createDefaultIcon(outputPath);
            }

            // 将 Icon 转换为 BufferedImage
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();

            // 确保输出目录存在
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 保存为 PNG
            ImageIO.write(image, "PNG", outputFile);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to extract icon: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建默认图标（当无法提取图标时使用）
     */
    private static boolean createDefaultIcon(String outputPath) {
        try {
            // 创建一个简单的默认图标
            BufferedImage image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制圆角矩形背景
            g.setColor(new Color(100, 149, 237)); // 矢车菊蓝
            g.fillRoundRect(4, 4, 40, 40, 8, 8);

            // 绘制简单的应用图标形状
            g.setColor(Color.WHITE);
            g.fillRect(14, 14, 20, 20);

            g.dispose();

            // 确保输出目录存在
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            ImageIO.write(image, "PNG", outputFile);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to create default icon: " + e.getMessage());
            return false;
        }
    }
}