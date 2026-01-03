# Portable App Manager

一个用 JavaFX 开发的 Windows 便携式应用程序管理器，帮助你轻松管理便携应用，快速创建快捷方式，并支持应用的导出、导入和合并。

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-red)

## ✨ 功能特性

### 📦 应用管理
- **添加应用**：支持添加 `.exe` 和 `.bat` 可执行文件
- **自动图标提取**：自动从可执行文件提取图标，或使用默认图标
- **应用验证**：实时检测应用文件是否存在，标记缺失的应用
- **多选支持**：支持批量选择和操作

### 🚀 快捷方式导出
- **导出到 Inks 目录**：在项目目录下创建自定义快捷方式
- **导出到开始菜单**：直接添加到 Windows 开始菜单，方便系统级访问
- **一键操作**：快速批量导出所有应用的快捷方式

### 📦 软件包管理
- **导出软件包**：将应用及其完整目录打包为 ZIP 文件
  - 保持原目录结构
  - 包含应用图标和配置信息
  - 支持多选批量导出
- **导入软件包**：从 ZIP 包导入应用
  - 三种冲突处理策略：跳过、替换、重命名
  - 自动解压到项目根目录
  - 保持原有目录结构
- **合并软件包**：支持多个 ZIP 包合并
  - 自动去重（按应用名）
  - 一次性导入多个应用包

### 🔄 其他功能
- **刷新列表**：手动刷新应用列表，更新状态
- **全选/取消全选**：快速选择或清除所有应用
- **统计信息**：显示应用总数、有效数和缺失数
- **文件夹快速访问**：快速打开 Inks 目录和开始菜单目录

## 🛠️ 技术栈

- **Java 21** - 现代 Java 特性
- **JavaFX 21.0.6** -桌面 UI 框架
- **Gson 2.10.1** - JSON 序列化
- **Maven** - 项目构建和依赖管理
- **ControlsFX, Ikonli** - JavaFX UI 组件库

## 📋 系统要求

- **操作系统**：Windows 10/11
- **Java 版本**：JDK 21 或更高版本
- **Maven 版本**：3.8+ （用于构建）

## 🚀 快速开始

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/yourusername/PortableAppManager.git
cd PortableAppManager

# 编译项目
mvn clean compile

# 运行应用
mvn clean javafx:run

# 打包为 JAR
mvn package
```

### 直接运行

打包后，可以直接运行生成的 JAR 文件：

```bash
java -jar target/PortableAppManager.jar
```

## 📖 使用指南

### 添加应用

1. 点击 **➕ Add App** 按钮
2. 选择可执行文件（.exe 或 .bat）
3. 输入应用名称
4. 应用会自动添加到列表并提取图标

### 导出快捷方式

- **导出到 Inks**：点击 **📦 Export to Inks**，快捷方式会保存到 `./Inks/` 目录
- **导出到开始菜单**：点击 **🚀 Export to Start Menu**，快捷方式会添加到用户开始菜单

### 导出软件包

1. 在列表中选择一个或多个应用（使用 Ctrl/Shift 多选）
2. 点击 **📦 Export Package** 按钮
3. 选择保存位置和文件名
4. 等待导出完成

### 导入软件包

1. 点击 **📥 Import Package** 按钮
2. 选择 ZIP 包文件
3. 选择冲突处理策略：
   - **Skip**：跳过已存在的应用
   - **Replace**：替换已存在的应用
   - **Rename**：自动重命名避免冲突（推荐）
4. 等待导入完成

### 合并软件包

1. 点击 **🔀 Merge Packages** 按钮
2. 选择多个 ZIP 包文件
3. 应用会自动合并并去重

## 📁 项目结构

```
PortableAppManager/
├── src/main/java/cn/yenmor/portableappmanager/
│   ├── PortableAppManager.java      # 主应用和UI控制器
│   ├── AppEntry.java                 # 应用数据模型
│   ├── AppListCell.java              # 自定义列表单元格
│   ├── ConfigManager.java            # 配置管理器
│   ├── PackageManager.java           # 软件包管理（导出/导入/合并）
│   ├── PackageEntry.java             # 软件包数据模型
│   ├── PackageMetadata.java          # 软件包元数据
│   ├── ImportStrategy.java           # 导入策略枚举
│   ├── IconExtractor.java            # 图标提取工具
│   ├── SysUtiles.java                # 系统工具类
│   └── ConstVars.java                # 常量定义
├── PortableAppManager/
│   ├── config.json                   # 应用配置文件
│   ├── icons/                        # 应用图标目录
│   └── Inks/                         # 快捷方式目录
└── pom.xml                           # Maven 配置文件
```

## ⚙️ 配置文件

应用配置存储在 `./PortableAppManager/config.json`，包含所有应用的信息：

```json
[
  {
    "id": "uuid",
    "name": "应用名称",
    "path": "相对路径/app.exe",
    "iconPath": "icons/icon.png"
  }
]
```

## 📝 导出包格式

导出的 ZIP 包结构：

```
app_package.zip
├── manifest.json       # 包元数据和应用列表
├── MyApp/              # 应用目录（保持原结构）
│   ├── app.exe
│   ├── *.dll
│   └── ...
└── icons/              # 图标文件
    └── app_icon.png
```

## 🐛 故障排除

### 应用显示为黄色（Missing）
这表示应用文件不存在。可能的原因：
- 应用文件已被移动或删除
- 路径配置错误

**解决方法**：删除该应用并重新添加，或者移动应用文件到正确位置后点击刷新。

### 导出快捷方式失败
确保：
- Inks 目录存在且有写入权限
- 开始菜单目录存在且有写入权限
- 应用文件路径正确

### 导入软件包失败
确保：
- ZIP 文件格式正确
- ZIP 包包含 manifest.json
- 有足够的磁盘空间

## 🔒 安全说明

- **路径遍历防护**：导入时会检查 ZIP 条目路径，防止 ZIP slip 攻击
- **文件验证**：导出前会验证应用文件存在
- **权限检查**：操作前会检查文件和目录权限

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件到 your-email@example.com

## 🙏 致谢

- JavaFX 社区提供的优秀 UI 框架
- Gson 提供的 JSON 序列化工具
- ControlsFX 和其他第三方库的贡献者

---

**Made with ❤️ for portable app enthusiasts**
