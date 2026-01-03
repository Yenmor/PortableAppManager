package cn.yenmor.portableappmanager;

/**
 * 导入策略枚举
 * 定义当导入软件包时遇到重复应用名称的处理方式
 */
public enum ImportStrategy {
    /**
     * 跳过重复的应用
     * 保留现有应用，不导入同名应用
     */
    SKIP,

    /**
     * 替换现有应用
     * 删除现有应用，使用导入的应用替换
     */
    REPLACE,

    /**
     * 重命名导入的应用
     * 为导入的应用添加数字后缀以避免冲突
     * 例如：如果存在 "MyApp"，则新导入的命名为 "MyApp (2)"
     */
    RENAME
}
