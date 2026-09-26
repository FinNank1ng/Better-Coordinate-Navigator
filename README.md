# [更好的坐标导航] Better Coordinate Navigator

![Better Coordinate Navigator](https://github.com/user-attachments/assets/e4df8b60-3a46-4b7a-86ff-3649d229f107)

<p align="center">
  <strong>一个轻量、可扩展的 Minecraft 坐标标点、导航与工作流框架</strong>
  <br/>
  Waypoints · HUD Navigation · World Markers · Multiplayer Tracking · Workflow System
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
  <a href="https://github.com/FinNank1ng/Better-Coordinate-Navigator/releases/latest">
    <img src="https://img.shields.io/github/v/release/FinNank1ng/Better-Coordinate-Navigator?style=for-the-badge&label=Latest%20Release" />
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

**Better Coordinate Navigator** 是一个面向 Minecraft 的轻量级坐标标点、导航与工作流框架。

它提供了一套独立的任务点 / 坐标点管理系统，并结合：

- **HUD 屏幕导航**
- **3D 世界标记**
- **多人独立任务追踪**
- **Xaero Minimap / Xaero World Map 集成**
- **Workflow 工作流系统**

帮助玩家从单纯的“找到一个坐标”，进一步扩展到：

> **标记目标 → 导航目标 → 追踪目标 → 组织流程 → 执行流程**

项目采用模块化设计，各个系统彼此独立，同时保留进一步扩展任务系统、路线导航、自动化与地图集成的空间。

### TIPS

> Better Coordinate Navigator 最初是为个人项目《烈阳调查局》开发的专属坐标导航 Mod。
>
> 随着功能逐渐完善，项目从最初的定制工具逐步独立为一个通用的 Minecraft 坐标标点、导航与工作流框架。
>
> 早期开发演示：
>
> - [《烈阳调查局》早期开发演示 ①](https://www.bilibili.com/video/BV16Y8u6iEKh/)
> - [《烈阳调查局》早期开发演示 ②](https://www.bilibili.com/video/BV1UvuG6gENU/)

---

# 核心功能

## 坐标标点

Better Coordinate Navigator 提供独立的任务点 / 坐标点管理系统。

- 创建自定义任务点
- 删除任务点
- 重命名任务点
- 查看任务点详细信息
- 支持自定义描述
- 支持任务状态
- 支持任务点启用 / 禁用
- 坐标数据持久化保存

任务点不仅可以作为导航目标，也可以作为 Workflow 的触发条件。

---

## HUD 导航

- 屏幕边缘实时显示目标方向
- 根据玩家朝向动态计算目标位置
- 支持目标距离显示
- 支持目标高度差显示
- 支持目标名称显示
- 支持自定义导航图标
- 支持最大显示距离设置
- 支持近距离自动隐藏 HUD 导航

---

## 3D 世界标记

- 在 Minecraft 世界中显示任务点
- 支持世界空间中的任务标记
- 支持自定义图标
- 支持显示名称
- 支持显示距离
- 支持独立控制世界标记显示

---

## 多人支持

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

## Workflow 工作流系统

从 0.5.0.0 开始，Better Coordinate Navigator 正式加入完整的 Workflow System。

Workflow 建立在 BCN 原有 Marker / Navigation 系统之上，用于将多个任务点与动作组织成可执行的流程。

一个简单的 Workflow 可以表示为：

```text
任务点 A
   ↓
发送消息
   ↓
任务点 B
   ↓
执行命令
   ↓
发放物品
   ↓
任务完成
```

### 工作流管理

- 创建工作流
- 删除工作流
- 重命名工作流
- 工作流选择与切换
- 工作流 UUID 标识
- 工作流状态管理
- 工作流持久化保存


动作可以直接通过 Workflow Editor 进行配置。

### Workflow Editor

编辑器采用节点式设计：

```text
┌──────────────┐
│   任务点 A   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   发送消息   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   任务点 B   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   执行命令   │
└──────────────┘
```

#### 编辑器功能

- Workflow Sidebar
- Workflow 搜索
- 节点式画布
- 节点连接
- 节点右键菜单
- 节点编辑
- Action 编辑器
- Marker 选择器
- Workflow 重命名
- Workflow 管理菜单
- 独立 Layout 系统
- 独立 Frame / Sidebar / Canvas / Popup Renderer

---

## Commands

### Workflow Commands

提供专用 Workflow 命令：

```text
/bcn workflow start <workflow>
/bcn workflow pause <workflow>
/bcn workflow stop <workflow>
/bcn workflow reset <workflow>
```

同时支持：

- 工作流名称
- 工作流 UUID
- 目标玩家

### Help System

新增独立 BCN Help 系统：

```text
/bcn help
```

- 分页帮助
- 点击式上一页 / 下一页
- 游戏内命令说明

---

## Xaero Integration

Better Coordinate Navigator 支持与以下 Mod 集成：

- Xaero's Minimap
- Xaero's World Map

Xaero 并不是 BCN 的必需依赖。

当前支持：

- Xaero Minimap Waypoint 同步
- Xaero World Map Waypoint 同步
- BCN Marker 同步到 Xaero
- BCN 第三方 Waypoint 标识
- Xaero 兼容配置

因此，即使没有安装 Xaero，BCN 的核心 Marker、HUD、World Marker 和 Workflow 系统仍然可以正常使用。

---

## 自定义图标

任务点支持自定义图标。

文件夹位置可能随着后续版本发生改变，未来考虑进一步优化整合包中的资源打包方式。

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


## 配置

HUD 与世界标记拥有独立的显示配置，可以控制：

- 是否显示图标
- 是否显示名称
- 是否显示距离
- 是否显示高度差
- HUD 隐藏距离
- 世界标记显示距离
- 图标大小
- 文字缩放
- 以及其他显示参数

配置系统会随着后续版本持续扩展。

---

## Screenshots

### HUD 导航

![HUD Navigation](https://github.com/user-attachments/assets/060dfaf3-9bfd-427b-b1cb-88b6db02fbda)

### 3D 世界标记

![3D World Marker](https://github.com/user-attachments/assets/61a9618e-c837-415c-9597-52a67586bba4)

### Workflow Editor

![Workflow Editor](https://github.com/user-attachments/assets/5b58d1a2-cd89-421a-8d56-da58c2d5fa5f)

![Workflow Action Editor](https://github.com/user-attachments/assets/e37712db-820c-480b-9cea-b22afd2b068b)

![Workflow Action Editor - Action Configuration](https://github.com/user-attachments/assets/9defeb89-67d2-48a1-8483-001c703758cf)


### Workflow Runtime

![Workflow Runtime](https://github.com/user-attachments/assets/6f406473-7317-4d13-b446-e7bd542ece86)

Workflow 可以在实际游戏环境中根据任务点触发并执行对应 Action。

---

## 项目开发历程

Better Coordinate Navigator 从一个面向《烈阳调查局》的专属坐标导航工具开始，逐渐发展为独立的通用框架。

```text
早期定制工具
      ↓
坐标标点系统
      ↓
HUD 导航
      ↓
3D 世界标记
      ↓
多人任务追踪
      ↓
Xaero Integration
      ↓
Workflow System
      ↓
更多 Navigation / Automation / Gameplay 功能
```

---

## 项目贡献与致谢

<details>
<summary><strong>展开项目贡献与致谢</strong></summary>

### 项目发起

作者：星丶白羽莲（Chinese） / FinNank1ng（English） / ShirohaRen（official）

负责项目整体架构、核心功能、网络同步、HUD 导航、任务点系统以及后续维护。

### 定制项目来源

本项目最初来源于《烈阳调查局》（整合包作者：小水翼XSY）的专属 Mod 定制需求。

早期版本主要围绕项目实际需求进行开发，在持续迭代过程中逐渐抽象出通用的：

- 坐标标点系统
- HUD 导航系统
- 3D 世界标记
- 玩家独立追踪
- 管理员任务控制
- 自定义图标系统
- 服务端数据持久化与客户端同步
- Workflow 工作流系统

最终发展为独立的 Better Coordinate Navigator 项目。

### 贡献者

| 贡献者 | 隶属于 | 贡献 |
|---|---|---|
| 星丶白羽莲 | 本项目 Mod 作者 | 核心开发 / 架构设计 / 维护 |
| 小水翼XSY | 《烈阳调查局》项目组 | 初期需求 / 测试 / 功能反馈 |

### 鸣谢

感谢所有参与测试、反馈 Bug、提出建议以及帮助完善项目的人。

</details>

---

## 作者

星丶白羽莲

FinNank1ng / ShirohaRen

- GitHub: [FinNank1ng](https://github.com/FinNank1ng)
- Bilibili: 星丶白羽莲

---

## License

This project is licensed under the MIT License.

See [LICENSE](https://github.com/FinNank1ng/Better-Coordinate-Navigator/blob/main/LICENSE) for details.
