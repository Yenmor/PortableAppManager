# Portable App Manager
# 可移植应用管理器

<div align="center">

**A lightweight tool for managing portable applications on Windows**
**Windows 平台轻量级便携应用管理工具**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## Table of Contents / 目录

- [Features / 功能特性](#features--功能特性)
- [Screenshots / 截图](#screenshots--截图)
- [Installation / 安装](#installation--安装)
- [Usage / 使用方法](#usage--使用方法)
- [Configuration / 配置文件](#configuration--配置文件)
- [Building / 构建](#building--构建)
- [Technology Stack / 技术栈](#technology-stack--技术栈)
- [Contributing / 贡献](#contributing--贡献)
- [License / 许可证](#license--许可证)

---

## Features / 功能特性

### Core Features / 核心功能

- **📦 Application Management / 应用管理**
  - Add portable applications (EXE, BAT files) / 添加便携应用（EXE、BAT 文件）
  - Custom application names / 自定义应用名称
  - Automatic icon extraction / 自动提取应用图标
  - Path validation with visual indicators / 路径验证与可视化标记

- **🎯 Shortcut Creation / 快捷方式创建**
  - Export to custom folder / 导出到自定义文件夹
  - Direct export to Start Menu / 直接导出到开始菜单
  - Automatic working directory configuration / 自动配置工作目录

- **🔧 Application Operations / 应用操作**
  - Delete applications with confirmation / 带确认的删除功能
  - View application details (icon, name, ID, path) / 查看应用详情（图标、名称、ID、路径）
  - Auto-migration from legacy config / 自动从旧配置迁移

- **🎨 Modern UI / 现代化界面**
  - Clean and flat design / 简洁扁平化设计
  - Visual application icons / 可视化应用图标
  - Status indicators for missing apps / 缺失应用的状态标记
  - Real-time statistics / 实时统计信息

### Key Features / 关键特性

✅ **Icon Extraction** - Automatically extracts icons from executable files
**图标提取** - 从可执行文件自动提取图标

✅ **Custom Naming** - Set custom names for your applications
**自定义命名** - 为应用设置自定义名称

✅ **JSON Configuration** - Modern JSON-based configuration format
**JSON 配置** - 现代化的 JSON 配置格式


---

## Screenshots / 截图

### Main Interface / 主界面

The application features a clean, modern interface with:
应用具有简洁现代的界面，包括：

- Application list with icons / 带图标的应用列表
- Intuitive button layout / 直观的按钮布局
- Real-time statistics / 实时统计信息
- Color-coded status indicators / 彩色状态指示器

---

## Installation / 安装

直接使用打包好的 release 版本即可


---

## Usage / 使用方法

### Adding Applications / 添加应用

1. Click **"➕ Add App"** button / 点击 **"➕ Add App"** 按钮
2. Select an executable file (.exe, .bat) / 选择可执行文件（.exe、.bat）
3. Enter a custom name (default: filename) / 输入自定义名称（默认：文件名）
4. Click **"Add"** to confirm / 点击 **"Add"** 确认

The application will automatically:
应用将自动：
- Extract the application icon / 提取应用图标
- Save configuration to JSON / 保存配置到 JSON
- Assign a unique ID / 分配唯一 ID

### Exporting Shortcuts / 导出快捷方式

**To Custom Folder / 导出到自定义文件夹：**
1. Click **"📦 Export to Inks"** / 点击 **"📦 Export to Inks"**
2. Shortcuts are created in `PortableAppManager/Inks/`
   快捷方式创建在 `PortableAppManager/Inks/`

**To Start Menu / 导出到开始菜单：**
1. Click **"🚀 Export to Start Menu"** / 点击 **"🚀 Export to Start Menu"**
2. Shortcuts appear in Windows Start Menu
   快捷方式出现在 Windows 开始菜单中

### Managing Applications / 管理应用

**View Application List / 查看应用列表：**
- Icon (32x32) / 图标（32x32）
- Application name (bold) / 应用名称（加粗）
- Unique ID / 唯一 ID
- File path / 文件路径
- Status indicator for missing apps / 缺失应用的状态指示器

**Delete Application / 删除应用：**
1. Select an application from the list / 从列表中选择应用
2. Click **"🗑️ Delete"** button / 点击 **"🗑️ Delete"** 按钮
3. Confirm the deletion / 确认删除

---

## Configuration / 配置文件

### Config File Location / 配置文件位置

```
PortableAppManager/
├── config.json          # Main configuration (JSON format)
                         # 主配置文件（JSON 格式）
├── config.txt           # Legacy format (auto-migrated)
                         # 旧格式（自动迁移）
├── icons/               # Extracted application icons
                         # 提取的应用图标
└── Inks/                # Exported shortcuts
                         # 导出的快捷方式
```

### Configuration Format / 配置格式

**JSON Format / JSON 格式** (`config.json`):
```json
[
  {
    "id": "a1b2c3d4",
    "name": "OBS Studio",
    "path": "derpy/obs-studio.exe",
    "iconPath": "./PortableAppManager/icons/a1b2c3d4.png"
  },
  {
    "id": "b2c3d4e5",
    "name": "Notepad++",
    "path": "tools/notepad++.exe",
    "iconPath": "./PortableAppManager/icons/b2c3d4e5.png"
  }
]
```

**Field Descriptions / 字段说明：**
- `id`: Unique application identifier / 唯一应用标识符
- `name`: Display name / 显示名称
- `path`: Relative path to executable / 可执行文件的相对路径
- `iconPath`: Path to extracted icon / 提取图标的路径

---

## Building / 构建

### Build Requirements / 构建要求

- Maven 3.6+ / Maven 3.6 或更高版本
- Java 21+ / Java 21 或更高版本

### Build Commands / 构建命令

```bash
# Clean and compile / 清理并编译
mvn clean compile

# Run tests / 运行测试
mvn test

# Package JAR / 打包 JAR
mvn package

# Run application / 运行应用
mvn javafx:run
```

### Creating Custom Runtime with jlink / 使用 jlink 创建自定义运行时

```bash
# Create custom runtime image / 创建自定义运行时镜像
mvn jlink:jlink

# Run the custom image / 运行自定义镜像
./target/portableappmanager/bin/portableappmanager
```

---

## Technology Stack / 技术栈

- **Language / 语言**: Java 21
- **UI Framework / UI 框架**: JavaFX 21.0.6
- **Build Tool / 构建工具**: Maven
- **JSON Library / JSON 库**: Gson 2.10.1
- **Additional Libraries / 附加库**:
  - ControlsFX 11.2.1
  - FormsFX 11.6.0
  - BootstrapFX 0.4.0
  - Ikonli 12.3.1

---

## Project Structure / 项目结构

```
PortableAppManager/
├── src/main/java/cn/yenmor/portableappmanager/
│   ├── PortableAppManager.java    # Main application / 主应用类
│   ├── AppEntry.java               # Data model / 数据模型
│   ├── ConfigManager.java          # Configuration management / 配置管理
│   ├── IconExtractor.java          # Icon extraction / 图标提取
│   ├── AppNameDialog.java          # Naming dialog / 命名对话框
│   ├── AppListCell.java            # List cell renderer / 列表单元格渲染
│   ├── ConstVars.java              # Constants / 常量定义
│   └── SysUtiles.java              # Utility methods / 工具方法
├── src/main/resources/
│   └── styles.css                  # UI styles / UI 样式
└── pom.xml                         # Maven configuration / Maven 配置
```

---

## Contributing / 贡献

Contributions are welcome! / 欢迎贡献！

1. Fork the repository / Fork 仓库
2. Create your feature branch / 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. Commit your changes / 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch / 推送到分支 (`git push origin feature/AmazingFeature`)
5. Open a Pull Request / 打开 Pull Request

---

## License / 许可证

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
本项目采用 MIT 许可证 - 详情参见 [LICENSE](LICENSE) 文件。

---

## Acknowledgments / 致谢

- **JavaFX** - Modern Java UI platform / 现代 Java UI 平台
- **Gson** - Java JSON library / Java JSON 库
- **ControlsFX** - High-quality UI controls for JavaFX / JavaFX 高质量 UI 控件

---

<div align="center">

**Made with ❤️ by yenmor**

**⭐ If you like this project, please give it a star!**
**⭐ 如果你喜欢这个项目，请给它一个星标！**

</div>