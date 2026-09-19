# 🐷 小猪猪每日提醒 · Android 桌面挂件

> 一只住在手机桌面上的小猪猪：每天到了你设定的时间，它就弹出通知，喊你去做该做的事。

「猪猪侠提醒」（包名 `com.pighero.reminder`）是一个 **Kotlin 原生 Android 桌面小组件应用**：桌面实时时钟 + 每日定时提醒，到点通过系统「闹钟级」调度弹出通知，手机重启后自动恢复定时。工程**零第三方依赖**，全部使用 Android 框架 API。仓库同时附带一个零门槛、双击即玩的高保真 HTML 交互原型。

## ✨ 功能特性

- **桌面小组件**：小猪英雄形象 + 大号实时时钟（`TextClock`）+ 日期 +「下一条提醒」；默认 4×2，可横向 / 纵向缩放，点击挂件进入设置页。
- **每日定时提醒**：任意添加 / 开关 / 删除提醒（时间 + 内容），数据保存在本地 `SharedPreferences`，每天循环生效。
- **闹钟级准时**：采用 `AlarmManager.setAlarmClock()`，不受 Doze 省电模式影响，并在系统闹钟列表可见；触发后自动顺延到第二天。
- **通知提醒**：高优先级通知渠道，振动 + 小猪大图，点击打开设置页；内置「立即试播」按钮自检通知通道。
- **开机自恢复**：监听开机、应用更新、系统时间 / 时区变化，自动重新调度全部闹钟并刷新挂件。
- **新版本权限适配**：Android 13+ 运行时申请通知权限；Android 12+ 检查精确闹钟权限，未授权时引导跳转系统设置页，绝不崩溃。
- **内置示例提醒**：首次启动自带 07:30 起床、12:00 午饭、18:00 晚饭、21:30 睡觉四条，可自由删改。
- **零依赖、离线可构建**：不引入任何第三方库，纯框架 API，APK 不足 1 MB，代码小巧透明。

## 📦 仓库内容

| 路径 | 说明 |
| --- | --- |
| `prototype/pig-hero-widget-prototype.html` | 高保真交互原型，单文件、双击用浏览器打开即可体验（模拟手机桌面、设置页、到点通知横幅、叮咚提示音） |
| `android/PigHeroReminder/` | Android 原生工程（Kotlin + Gradle Kotlin DSL），可直接编译安装 |
| `使用说明.md` | 面向普通用户的完整使用 / 编译 / 安装图文教程 |

## 🚀 快速体验原型（无需手机）

双击打开 `prototype/pig-hero-widget-prototype.html`（电脑、手机浏览器均可）：

1. 点击桌面上的猪猪侠挂件，进入提醒设置页；
2. 选择时间、填写要做的事，点「＋ 添加提醒」，可随时开关或删除；
3. 点「立即试播一次提醒效果」，预览到点时的顶部通知横幅、小猪喊话与提示音。

> 原型数据保存在浏览器 `localStorage`，仅用于演示；网页关闭后无法保证后台提醒，真机常驻请使用下面的 Android 工程。

## 🛠️ 构建 Android 工程

### 环境要求

- Android Studio（较新版本即可），附带 Android SDK Platform 34 与 JDK 17；
- 或命令行环境：JDK 17 + Android SDK 34 + Gradle 8.9；
- 运行设备 / 模拟器：Android 8.0（API 26）或更高版本。

### 方式一：Android Studio（推荐）

1. Android Studio → **Open**，选择本仓库的 `android/PigHeroReminder` 目录；
2. 首次同步会自动下载 Gradle 8.9 与 Android Gradle Plugin 8.5.2（工程已配置阿里云 Maven 镜像，国内网络友好）；
3. 手机开启「USB 调试」后连接电脑，顶部选中设备，点绿色三角 **Run**，应用自动安装。

> 本仓库未附带 Gradle Wrapper 的 jar 二进制。若 Android Studio 提示找不到 wrapper，可在 *Settings → Build, Execution, Deployment → Build Tools → Gradle* 中把 Gradle 改为本地安装的 8.9，或在工程目录执行一次 `gradle wrapper --gradle-version 8.9` 自动补全。

### 方式二：命令行

```bash
cd android/PigHeroReminder
gradle assembleDebug      # 需本机已安装 Gradle 8.9；产物见 app/build/outputs/apk/debug/app-debug.apk
gradle installDebug      # 手机已连接并开启 USB 调试时，可直接安装
```

## 📱 把挂件放到桌面

1. 回到手机桌面，**长按桌面空白处** → 点「窗口小工具 / 小组件 / 微件」（不同品牌叫法不同）；
2. 找到 **「猪猪侠提醒」**，长按拖到桌面空位（默认 4×2，可拖拽边缘缩放）；
3. 首次添加会自动打开设置页：选时间、填要做的事、点「＋ 添加提醒」，最后点右上角「完成」；
4. 以后点一下桌面上的挂件（或桌面应用图标「猪猪侠提醒」）即可随时增删改提醒；
5. Android 12+ 首次进入会引导允许「精确闹钟」、Android 13+ 会请求通知权限，请均选择允许。

**国产机型保活**：小米 / 红米、OPPO、vivo、华为等机型若到点不提醒，请在「设置 → 应用管理 → 猪猪侠提醒」中允许**自启动**与**后台运行**，电池管理设为「不限制 / 无限制」，并确认通知权限已开启。

## 🗂️ 工程结构

```
android/PigHeroReminder/
├── settings.gradle.kts / build.gradle.kts     # Gradle 配置（阿里云镜像 + AGP 8.5.2）
└── app/
    ├── build.gradle.kts                       # minSdk 26 / targetSdk 34 / 零第三方依赖
    └── src/main/
        ├── AndroidManifest.xml                # 挂件、接收器、权限注册
        ├── java/com/pighero/reminder/
        │   ├── Reminder.kt                    # 提醒数据模型 + 下次触发时间计算
        │   ├── ReminderStore.kt               # SharedPreferences + JSON 存储
        │   ├── ReminderScheduler.kt           # AlarmManager.setAlarmClock 每日调度
        │   ├── ReminderReceiver.kt            # 到点发通知并重排次日闹钟
        │   ├── BootReceiver.kt                # 开机 / 更新 / 改时间后恢复调度
        │   ├── NotificationHelper.kt          # 通知渠道、通知发送、矢量图转位图
        │   ├── PigWidgetProvider.kt           # 桌面挂件 Provider
        │   └── WidgetConfigureActivity.kt     # 提醒设置页（兼挂件配置页与桌面入口）
        └── res/                               # 布局、小猪矢量图、主题等全部资源
```

## 🧱 技术实现要点

- Kotlin 编写，minSdk 26 / targetSdk 34，AGP 8.5.2 + Gradle 8.9 + JDK 17；
- `setAlarmClock` 闹钟级定时，Doze 省电模式下依然准时，且在系统闹钟界面可见；
- PendingIntent 按提醒 id 分配固定 requestCode，增删改后全量取消再重排，避免「幽灵闹钟」；
- 挂件时钟直接使用系统 `TextClock`（无需自行每秒刷新），「下一条提醒」每小时做一次非精确轮询，并在数据变更时主动刷新；
- 小猪形象全部为手写 `VectorDrawable`（`ic_pig_hero.xml` 等），没有一张位图资源，APK 体积不足 1 MB。

## 🎨 自定义形象

替换以下三个矢量图即可换成任意形象：

- `app/src/main/res/drawable/ic_pig_hero.xml`：挂件形象与通知大图；
- `app/src/main/res/drawable/ic_pig_notification.xml`：通知栏白色剪影；
- `app/src/main/res/drawable/ic_launcher_foreground.xml`：桌面自适应图标前景。

## ⚠️ 免责声明

- 挂件中的小猪形象为**粉丝风格的手绘矢量二次创作**，「猪猪侠 / GG Bond」相关 IP 的一切权利归其权利人所有；
- 本项目仅供个人学习、研究与自用，**请勿用于商业用途或公开分发**；
- 如权利人认为相关内容不妥，可联系作者删除。

## 📄 License

代码部分基于 [MIT License](LICENSE) 开放；美术素材（小猪形象）为粉丝二次创作，仅限个人学习自用，不随 MIT 授权用于商业用途。
