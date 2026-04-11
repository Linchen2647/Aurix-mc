# Aurix Fabric

[简体中文](#简体中文) | [繁體中文](#繁體中文) | [English](#english)

---

## 简体中文

Aurix Fabric 是一个适用于 **Minecraft 1.21.1** 的 **Fabric 客户端模组**。  
这个发布包提供的是 **可直接安装的 `.jar` 项目**，和**源代码项目**。

Aurix 会读取游戏内聊天内容，根据近期对话与记忆资料，自然地在聊天中简短回应，风格偏低存在感、像玩家而不是机器人。

### 发布内容

本次发布重点项目：

- `aurix-fabric-1.1.0.jar`
- `project源码`

### 系统需求

- Minecraft `1.21.1~1.21.11`
- Fabric Loader `0.16.10` 或更新版本
- Fabric API `0.115.4+1.21.1` 或兼容版本
- Java `21`

### 安装方式

1. 安装 **Java 21**
2. 安装 **Fabric Loader**（Minecraft 版本请选 `1.21.1`）
3. 安装 **Fabric API**
4. 将 `aurix-fabric-1.1.0.jar` 放入 Minecraft 的 `mods` 文件夹
5. 启动游戏一次
6. Aurix 会自动建立配置文件
7. 编辑配置文件并填入你的 API 密钥

### 配置文件

Aurix 第一次启动后，会建立配置文件：

```text
.minecraft/config/aurix-client.json
```

预设格式如下：

```json
{
  "enabled": true,
  "apiBaseUrl": "https://integrate.api.nvidia.com/v1",
  "apiKey": "",
  "model": "<provider>/<model>",
  "botName": "Aurix",
  "chatPrefix": "[Aurix] ",
  "minSecondsBetweenReplies": 35,
  "maxContextMessages": 30,
  "triggerWindowMessages": 8,
  "minMessagesBeforeOrganicReply": 4,
  "organicReplyChance": 0.18,
  "maxReplyChars": 70,
  "replyWhenMentioned": true,
  "ignoreOwnMessages": true,
  "logDebug": true
}
```

### 重要字段说明

- `apiKey`：必填，未填写时 Aurix 无法生成回复
- `model`：要使用的模型名称
- `botName`：Aurix 被提及时辨识用的名字
- `chatPrefix`：发出聊天消息时附加的前缀
- `minSecondsBetweenReplies`：最短回复间隔
- `organicReplyChance`：自然插话几率
- `maxReplyChars`：单词回复最大字数

### 指令

Aurix 提供以下客户端指令：

```text
/aurix agent on
/aurix agent off
/aurix agent reload
/aurix memory add <player> <value>
/aurix memory delete <player> <id>
/aurix memory add_global <name> <value>
/aurix memory delete_global <name>
```

### 记忆资料位置

玩家记忆：

```text
.minecraft/config/aurix/memory/players/
```

全局记忆：

```text
.minecraft/config/aurix/memory/global/
```

### 注意事项

- 这是 **客户端模组**，不是服务器插件
- 本模组需要可用的 API 密钥才能生成 AI 回复
- 若没有安装 Fabric API，模组可能无法正常载入
- 若你的 Java 版本不是 21，Minecraft 启动时可能会失败
- Aurix 目前设计偏向简短、自然、低存在感的聊天风格

---

## 繁體中文

Aurix Fabric 是一個適用於 **Minecraft 1.21.1** 的 **Fabric 客戶端模組**。  
這個發佈包提供的是 **可直接安裝的 `.jar` 檔案**，和**原始碼專案**。

Aurix 會讀取遊戲內聊天內容，根據近期對話與記憶資料，自然地在聊天中簡短回應，風格偏低存在感、像玩家而不是機器人。

### 發佈內容

本次發佈重點檔案：

- `aurix-fabric-1.1.0.jar`
- `project源碼`

### 系統需求

- Minecraft `1.21.1~1.21.11`
- Fabric Loader `0.16.10` 或更新版本
- Fabric API `0.115.4+1.21.1` 或相容版本
- Java `21`

### 安裝方式

1. 安裝 **Java 21**
2. 安裝 **Fabric Loader**（Minecraft 版本請選 `1.21.1`）
3. 安裝 **Fabric API**
4. 將 `aurix-fabric-1.1.0.jar` 放入 Minecraft 的 `mods` 資料夾
5. 啟動遊戲一次
6. Aurix 會自動建立設定檔
7. 編輯設定檔並填入你的 API 金鑰

### 設定檔

Aurix 第一次啟動後，會建立設定檔：

```text
.minecraft/config/aurix-client.json
```

預設格式如下：

```json
{
  "enabled": true,
  "apiBaseUrl": "https://integrate.api.nvidia.com/v1",
  "apiKey": "",
  "model": "<provider>/<model>",
  "botName": "Aurix",
  "chatPrefix": "[Aurix] ",
  "minSecondsBetweenReplies": 35,
  "maxContextMessages": 30,
  "triggerWindowMessages": 8,
  "minMessagesBeforeOrganicReply": 4,
  "organicReplyChance": 0.18,
  "maxReplyChars": 70,
  "replyWhenMentioned": true,
  "ignoreOwnMessages": true,
  "logDebug": true
}
```

### 重要欄位說明

- `apiKey`：必填，未填寫時 Aurix 無法產生回覆
- `model`：要使用的模型名稱
- `botName`：Aurix 被提及時辨識用的名字
- `chatPrefix`：送出聊天訊息時附加的前綴
- `minSecondsBetweenReplies`：最短回覆間隔
- `organicReplyChance`：自然插話機率
- `maxReplyChars`：單次回覆最大字數

### 指令

Aurix 提供以下客戶端指令：

```text
/aurix agent on
/aurix agent off
/aurix agent reload
/aurix memory add <player> <value>
/aurix memory delete <player> <id>
/aurix memory add_global <name> <value>
/aurix memory delete_global <name>
```

### 記憶資料位置

玩家記憶：

```text
.minecraft/config/aurix/memory/players/
```

全域記憶：

```text
.minecraft/config/aurix/memory/global/
```

### 注意事項

- 這是 **客戶端模組**，不是伺服器插件
- 本模組需要可用的 API 金鑰才能產生 AI 回覆
- 若沒有安裝 Fabric API，模組可能無法正常載入
- 若你的 Java 版本不是 21，Minecraft 啟動時可能會失敗
- Aurix 目前設計偏向簡短、自然、低存在感的聊天風格

---

## English

Aurix Fabric is a **Fabric client-side mod** for **Minecraft 1.21.1**.  
This release contains a **ready-to-install `.jar` file**, not the full source project.

Aurix reads in-game chat, uses recent conversation context plus lightweight memory files, and replies in a short, low-profile style that feels closer to a player than a chatbot.

### Release contents

Main file in this release:

- `aurix-fabric-1.1.0.jar`
- `project (source)`

### Requirements

- Minecraft `1.21.1~1.21.11`
- Fabric Loader `0.16.10` or newer
- Fabric API `0.115.4+1.21.1` or compatible version
- Java(JDK) `21+`

### Installation

1. Install **Java 21**
2. Install **Fabric Loader** for Minecraft `1.21.1`
3. Install **Fabric API`
4. Put `aurix-fabric-1.1.0.jar` into your Minecraft `mods` folder
5. Launch the game once
6. Aurix will generate its config file automatically
7. Edit the config file and add your API key

### Config file

After the first launch, Aurix creates:

```text
.minecraft/config/aurix-client.json
```

Default example:

```json
{
  "enabled": true,
  "apiBaseUrl": "https://integrate.api.nvidia.com/v1",
  "apiKey": "",
  "model": "<provider>/<model>",
  "botName": "Aurix",
  "chatPrefix": "[Aurix] ",
  "minSecondsBetweenReplies": 35,
  "maxContextMessages": 30,
  "triggerWindowMessages": 8,
  "minMessagesBeforeOrganicReply": 4,
  "organicReplyChance": 0.18,
  "maxReplyChars": 70,
  "replyWhenMentioned": true,
  "ignoreOwnMessages": true,
  "logDebug": true
}
```

### Important settings

- `apiKey`: required; Aurix cannot generate replies without it
- `model`: model name to use for chat generation
- `botName`: name used for mention detection
- `chatPrefix`: prefix added before outgoing chat messages
- `minSecondsBetweenReplies`: minimum delay between replies
- `organicReplyChance`: chance of replying organically
- `maxReplyChars`: maximum reply length

### Commands

Aurix provides the following client commands:

```text
/aurix agent on
/aurix agent off
/aurix agent reload
/aurix memory add <player> <value>
/aurix memory delete <player> <id>
/aurix memory add_global <name> <value>
/aurix memory delete_global <name>
```

### Memory file locations

Player memories:

```text
.minecraft/config/aurix/memory/players/
```

Global memories:

```text
.minecraft/config/aurix/memory/global/
```

### Notes

- This is a **client-side mod**, not a server plugin
- A valid API key is required for AI-generated replies
- Fabric API must be installed for proper loading
- Java 21 is required
- Aurix is designed for short, natural, low-noise chat behavior

---

## License

MIT
