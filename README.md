<img width="1920" height="1080" alt="B1BA0711E3679DC7A31BD5039F876180" src="https://github.com/user-attachments/assets/e4df8b60-3a46-4b7a-86ff-3649d229f107" /><p align="center">
</p>

<h1 align="center">[更好的坐标导航] Better Coordinate Navigator</h1>

<p align="center">
  <strong>一个轻量、可扩展的 Minecraft 坐标标点与导航框架</strong>
  <br/>
  Waypoints · HUD Navigation · World Markers · Multiplayer Tracking
</p>

<p align="center">
  <a href="https://Minecraft.net">
    <img src="https://img.shields.io/badge/Minecraft-Mod-62B47A?style=for-the-badge&logo=minecraft" />
  </a>
  <a href="https://Minecraft.net">
    <img src="https://img.shields.io/badge/Minecraft-1.20.1-C1AB22?style=for-the-badge&logo=minecraft" />
  </a>
  <a href="https://www.java.com">
    <img src="https://img.shields.io/badge/Java-21+-F89820?style=for-the-badge&logo=java" />
  </a>
  <a href="https://github.com/FinNank1ng/Better-Coordinate-Navigator/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" />
  </a>
  <a href="https://github.com/FinNank1ng/Better-Coordinate-Navigator/stargazers">
    <img src="https://img.shields.io/github/stars/FinNank1ng/Better-Coordinate-Navigator?style=for-the-badge&color=F30525" />
  </a>
  <a href="https://qm.qq.com/q/e6kdQbTVja">
    <img src="https://img.shields.io/badge/QQ%E7%BE%A4-514280270-blue?style=for-the-badge" />
  </a>
</p>

---

## 简介

**Better Coordinate Navigator** 是一个面向 Minecraft 的轻量级坐标标点与导航 Mod。

它提供了一套独立的任务点 / 坐标点管理系统，并结合 **HUD 屏幕导航** 与 **3D 世界标记**，帮助玩家快速定位目标。

目前已经支持多人环境下的**玩家独立任务追踪**，每名玩家可以拥有自己的追踪列表，同时管理员可以为指定玩家设置或取消任务追踪。

项目采用模块化设计，后续可以继续扩展任务系统、路线导航、地图集成等功能。

**TIPS**
>
> Better Coordinate Navigator 最初是为个人项目《烈阳调查局》开发的专属坐标导航 Mod。
> 随着功能逐渐完善，项目从最初的定制工具逐步独立为一个通用的 Minecraft 坐标标点与导航框架。
>
> 早期开发演示：
>
> - [《烈阳调查局》早期开发演示 ①](https://www.bilibili.com/video/BV16Y8u6iEKh/)
> - [《烈阳调查局》早期开发演示 ②](https://www.bilibili.com/video/BV1UvuG6gENU/)
---

## 核心功能

### 坐标标点

* 创建自定义任务点
* 删除任务点
* 重命名任务点
* 查看任务点详细信息
* 支持自定义描述
* 支持任务状态
* 支持任务点启用 / 禁用
* 坐标数据持久化保存

### HUD 导航

* 屏幕边缘实时显示目标方向
* 根据玩家朝向动态计算目标位置
* 支持目标距离显示
* 支持目标高度差显示
* 支持目标名称显示
* 支持自定义导航图标
* 支持最大显示距离设置
* 支持近距离自动隐藏 HUD 导航

### 3D 世界标记

* 在 Minecraft 世界中显示任务点
* 支持世界空间中的任务标记
* 支持自定义图标
* 支持显示名称
* 支持显示距离
* 支持独立控制世界标记显示

### 多人支持

Better Coordinate Navigator 支持服务器多人环境。

每名玩家拥有独立的任务追踪列表：

```text
Player A
 ├─ 天基炮
 └─ 主城

Player B
 ├─ 地牢入口
 └─ Boss 房间
```

玩家之间的追踪状态互不影响。

同时支持管理员操作：

```text
/bcn marker track player <player> <name>
/bcn marker untrack player <player> <name>
```

管理员可以为指定玩家设置或取消任务追踪。

---

## 自定义图标

任务点支持自定义图标。（文件夹位置可能发生改变，未来考虑为整合包能够方便打包）

图标文件可以放置于：

```text
.minecraft/better_coordinate_navigator/Picture/
```

然后通过命令设置：

```text
/bcn marker icon set <name> <icon>
```

清除：

```text
/bcn marker icon clear <name>
```

这样可以为不同类型的目标使用不同图标，例如：

```text
NPC
Boss
Dungeon
Quest
Location
Custom
```

---

## 命令

### 基础

```text
/bcn help
/bcn list
```

### 任务点

```text
/bcn marker create <pos> <name>
/bcn marker remove <name>
/bcn marker rename <old> <new>
/bcn marker info <name>
```

### 任务追踪

玩家：

```text
/bcn marker track <name>
/bcn marker untrack <name>
/bcn marker cleartrack
```

管理员：

```text
/bcn marker track player <player> <name>
/bcn marker untrack player <player> <name>
```

### 自定义图标

```text
/bcn marker icon set <name> <icon>
/bcn marker icon clear <name>
```

---

## 配置

HUD 与世界标记拥有独立的显示配置，可以控制：

* 是否显示图标
* 是否显示名称
* 是否显示距离
* 是否显示高度差
* HUD 隐藏距离
* 世界标记显示距离
* 图标大小
* 文字缩放
* 以及其他显示参数

配置系统会随着后续版本持续扩展。

---

## 截图

### HUD 导航

<img width="1920" height="1080" alt="DC57A2EF94A217EDCE9BB16636DA4A9A" src="https://github.com/user-attachments/assets/060dfaf3-9bfd-427b-b1cb-88b6db02fbda" />

### 3D 世界标记

<img width="1920" height="1080" alt="B1BA0711E3679DC7A31BD5039F876180" src="https://github.com/user-attachments/assets/61a9618e-c837-415c-9597-52a67586bba4" />


---

## 项目结构

项目采用模块化设计：

```text
data/
    QuestManager
    QuestMarker
    ClientQuestCache

network/
    QuestDataUpdatePacket
    QuestDataRequestPacket
    QuestSyncHelper

command/
    ModCommands

render/
    QuestMarkerHUDRenderer
    QuestMarkerRenderer

config/
    ClientConfig
    ConfigLoader

util/
    ModVersion
```

服务器负责任务点与玩家追踪数据的管理，客户端负责缓存、HUD 与世界标记渲染。

---

## 数据与同步

任务点数据使用 Minecraft `SavedData` 进行持久化。

服务器保存：

* 任务点坐标
* 任务点名称
* 描述
* 图标
* 显示设置
* 任务状态
* 玩家独立追踪列表

客户端通过网络数据包接收当前玩家所需要的数据，并使用客户端缓存进行渲染。

---

## 开发状态

当前版本：

```text
0.3.0.0-alpha
```

当前版本主要目标是完善：

* 多人任务追踪
* HUD 导航
* 世界标记
* 自定义图标
* 数据持久化
* 网络同步
* 配置系统

项目仍处于持续开发阶段，部分 API、配置结构和功能可能在后续版本发生变化。

---

## Roadmap

### 0.3.x

* [x] 基础任务点系统
* [x] HUD 导航
* [x] 3D 世界标记
* [x] 自定义图标
* [x] 距离 / 高度信息
* [x] 数据持久化
* [x] 多玩家独立追踪
* [x] 管理员玩家追踪控制
* [x] 客户端 / 服务端数据同步
* [ ] 更多 HUD 自定义选项
* [ ] 更多世界标记样式
* [ ] 第三人称兼容性优化

### 0.4.x

计划加入：

* [ ] 更完整的任务系统
* [ ] 路线导航
* [ ] 多目标导航
* [ ] 更丰富的任务状态
* [ ] 更完善的地图集成
* [ ] 更高级的导航显示

### Future

* [ ] Route Planning
* [ ] Map Integration
* [ ] Advanced Quest System
* [ ] Marker Categories
* [ ] Extended Navigation API

---

<details>
<summary>**项目贡献与致谢**</summary>

### 项目发起

**作者：星丶白羽莲（Chinese） / FinNank1ng（English） / ShirohaRen（official）**

负责项目整体架构、核心功能、网络同步、HUD 导航、任务点系统以及后续维护。

### 定制项目来源

本项目最初来源于 **《烈阳调查局》** （整合包作者：小水翼XSY）的专属 Mod 定制需求。

早期版本主要围绕项目实际需求进行开发，在持续迭代过程中逐渐抽象出通用的：

- 坐标标点系统
- HUD 导航系统
- 3D 世界标记
- 玩家独立追踪
- 管理员任务控制
- 自定义图标系统
- 服务端数据持久化与客户端同步

最终发展为独立的 **Better Coordinate Navigator** 项目。

### 贡献者

| 贡献者 | 隶属于 | 贡献 |
|---|---|---|
| 星丶白羽莲 | 本项目Mod作者 | 核心开发 / 架构设计 / 维护 |
| 小水翼XSY | 《烈阳调查局》项目组 | 初期需求 / 测试 / 功能反馈 |

### 鸣谢

感谢所有参与测试、反馈 Bug、提出建议以及帮助完善项目的人。

</details>

---

## 作者的链接

**星丶白羽莲**

FinNank1ng / ShirohaRen

* GitHub: [FinNank1ng](https://github.com/FinNank1ng)
* Bilibili: 星丶白羽莲

---

## License

This project is licensed under the **MIT License**.

See [LICENSE](https://github.com/FinNank1ng/Better-Coordinate-Navigator/blob/main/LICENSE) for details.
