package cn.yenmor.portableappmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 软件包元数据
 * 记录导出包的基本信息
 */
public class PackageMetadata {
    private String version = "1.0";
    private String createdDate;
    private String exportSource;
    private int appCount;

    public PackageMetadata() {
        this.createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.exportSource = System.getProperty("user.name");
        this.appCount = 0;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getExportSource() {
        return exportSource;
    }

    public void setExportSource(String exportSource) {
        this.exportSource = exportSource;
    }

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }
}
