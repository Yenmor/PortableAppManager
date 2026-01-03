package cn.yenmor.portableappmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 软件包条目
 * 封装导出包的数据结构，包含元数据和应用列表
 */
public class PackageEntry {
    private PackageMetadata metadata;
    private List<AppEntry> appEntries;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PackageEntry() {
        this.metadata = new PackageMetadata();
        this.appEntries = new ArrayList<>();
    }

    public PackageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PackageMetadata metadata) {
        this.metadata = metadata;
    }

    public List<AppEntry> getAppEntries() {
        return appEntries;
    }

    public void setAppEntries(List<AppEntry> appEntries) {
        this.appEntries = appEntries;
        if (metadata != null) {
            metadata.setAppCount(appEntries.size());
        }
    }

    /**
     * 转换为JSON字符串
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /**
     * 从JSON字符串创建PackageEntry实例
     */
    public static PackageEntry fromJson(String json) {
        return gson.fromJson(json, PackageEntry.class);
    }
}
