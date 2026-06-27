# AMFram (Android Media Frame)

---

## 中文

专为 **Android 4.4 (API 19)** 及以上版本开发的电子相册应用，将旧设备变身为数码相框。

### 功能

- **多源图片加载** — 支持本地存储、SMB 网络共享、Unsplash 示例图片来源
- **5 种显示模式**
  - **幻灯片** — 横向滑动，自动轮播
  - **淡入淡出** — 渐隐渐显切换
  - **照片墙** — 网格布局，定时随机替换单张图片
  - **Bento** — 宫格排列
  - **日历** — 日历式展示
- **SMB 网络共享** — 通过 smbj 库访问局域网共享文件夹，密码 AES 加密存储
- **沉浸式全屏** — 隐藏状态栏和导航栏，专注图片浏览
- **深色模式** — 支持深色/浅色主题切换
- **定时轮播** — 幻灯片间隔、照片墙刷新间隔均可自定义
- **极速兼容** — 最低支持 Android 4.4 (KitKat)

### 构建

```bash
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

### 技术栈

| 技术 | 版本 |
|------|------|
| Kotlin | 1.8.22 |
| Min SDK | 19 (Android 4.4) |
| Target SDK | 19 |
| 图片加载 | Coil 1.4.0 |
| SMB 协议 | smbj 0.14.0 |
| 序列化 | Gson |
| 异步 | Kotlin Coroutines |
| UI | ViewPager / RecyclerView / Material Components |

### 许可

MIT

---

## English

A digital photo frame app designed for **Android 4.4 (API 19)** and above, turning old devices into a digital photo frame.

### Features

- **Multi-source image loading** — Local storage, SMB network shares, Unsplash sample images
- **5 display modes**
  - **Slide** — Horizontal auto-slideshow with ViewPager
  - **Fade** — Crossfade transitions
  - **FrameWall** — Grid layout with random auto-replacing cells
  - **Bento** — Bentō-style grid arrangement
  - **Calender** — Calendar-style display
- **SMB network sharing** — Access LAN shared folders via the smbj library, with AES-encrypted password storage
- **Immersive fullscreen** — Hides status and navigation bars for distraction-free viewing
- **Dark mode** — Toggle between dark and light themes
- **Auto slideshow** — Configurable interval for slide shows and grid replacements
- **Broad compatibility** — Supports Android 4.4 KitKat and above

### Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Tech Stack

| Technology | Version |
|------------|---------|
| Kotlin | 1.8.22 |
| Min SDK | 19 (Android 4.4) |
| Target SDK | 19 |
| Image Loading | Coil 1.4.0 |
| SMB Protocol | smbj 0.14.0 |
| Serialization | Gson |
| Async | Kotlin Coroutines |
| UI | ViewPager / RecyclerView / Material Components |

### License

MIT
