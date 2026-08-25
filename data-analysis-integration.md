# 可行性分析：用 LangChain4j 在 Spring Boot 复刻 data-analysis skill

## 背景与目标

将 Claude Code 的 `data-analysis` skill（数学建模竞赛数据预处理 + 问题理解）复刻为一个 Spring Boot 服务：

- **输入**：前端通过 REST 上传赛题 PDF + 数据 xlsx（服务端本地保存）
- **处理**：LLM agent 编排全流程，确定性清洗交给服务器本地的 Python（conda 环境 + `loader.py`）
- **输出**：`processed_data/` 下的清洗 CSV + `mapping.json` + `problem.md` + `references.md` + `README.md`
- **交互**：REST 上传 + **SSE 流式反馈**（长时间处理时前端持续有反应）
- **模型**：DeepSeek（OpenAI 兼容接口）

## 可行性结论

**可行，无硬性阻塞。** 架构本质 =「LLM agent + 工具集」，LangChain4j 的 `@Tool` 函数调用 + OpenAI 兼容接口正好覆盖。工作量集中在 agent 循环编排、SSE 流式进度、搜索工具接入，不在技术难点。

```
前端 ──上传 PDF+xlsx──▶ Spring Boot (REST + SSE)
                             │
                    ┌────────┴─────────┐
                    │  异步任务/Agent    │  手动循环: model ⟷ tools
                    └────────┬─────────┘
                             │ @Tool 调用
              ┌──────────────┼──────────────────┐
              ▼              ▼                   ▼
      Python 子进程       Web 搜索 API         writeFile/readFile
   (conda + loader.py)   (Tavily / Bing)      (processed_data/ 落盘)
```

## 关键决策点

### 1. 模型接入：DeepSeek
`langchain4j-open-ai` 模块 + `OpenAiChatModel.builder().baseUrl("https://api.deepseek.com").apiKey(...).modelName("deepseek-chat")`。LangChain4j 对 OpenAI 兼容接口原生支持，无需额外适配。

**注意**：确认所用 DeepSeek 模型的上下文窗口（64K 量级）。PDF 文本 + Excel 预览**只喂预览和摘要，不喂全量数据**，避免爆上下文。

### 2. Web 搜索：选第三方 API key，不「自建搜索服务」

**核心事实：LLM 本身没有实时联网能力，"用 DeepSeek 自己实现搜索"在物理上不成立。** 检索必须依赖一个外部数据源。真正的问题是选哪个数据源：

| 方案 | 说明 | 评价 |
|------|------|------|
| **Tavily**（首选） | LangChain4j **官方模块** `langchain4j-tavily`，几行接入 `TavilySearchTool`；专为 LLM agent 设计，返回精炼相关结果；有免费额度 | 最贴合「Claude Code 式搜索」，集成成本最低。缺点：海外服务，大陆服务器直连需验证网络可达性 |
| Azure Bing Search v7（备选） | F0 免费层每月 1000 次 | 无官方 LangChain4j 模块，需 ~30 行 `@Tool` + `RestClient` 薄封装。香港服务器下 Tavily 可达，Bing 仅作备用/降级，**非必需** |
| Brave Search API | 免费 2000 次/月，LLM 友好 | 同薄封装方案 |
| **Semantic Scholar API**（可选补充） | 免费无 key，专查学术文献 | 适合 references.md 的「论文出处」需求，作为第二个搜索 tool 与通用搜索互补 |
| 自建 SearXNG | 自托管元搜索，看似「自己实现」 | 上游封锁、大陆网络、维护成本高，**不推荐作为主方案** |

**结论：直接对接现成 API key，主搜索用 Tavily。** 服务器在香港、网络不受限，排除可达性因素后，按「检索质量 + 集成成本 + 论文场景适配」选：Tavily 专为 LLM agent 设计（去重、相关性排序、干净 snippet），有 LangChain4j 官方模块（`langchain4j-tavily`，集成最快），且 API 支持 `include_domains`/`max_results` 等参数，可把文献检索限定在 arXiv/期刊域。论文元数据用 **Semantic Scholar API**（免费无 key、返回结构化作者/年份/期刊/DOI）作为补充 tool，对症 references.md 的「可验证出处」。agent 侧编排（生成关键词 → 搜索 → 筛选 → 记录真实 URL）由我们自己实现，与 Claude Code 的 WebSearch 工作方式一致。

**复刻 Claude Code 搜索体验还需 `fetchUrl` 工具**：skill 4.3 有「对关键文献 WebFetch 读摘要」步骤。用 LangChain4j 的 `UrlDocumentLoader` + `Document.toMarkdown()` 即可，几十行。

### 3. 工具粒度：固定工具集，不暴露裸 bash

生产环境不能给 LLM 一个任意执行 shell 的 bash 工具（RCE / prompt injection 风险）。把 skill 拆成结构化 `@Tool`，LLM 只能选参数调用。

### 4. 流式输出：SSE + 结构化进度事件（推荐），token 级流式作为可选增强

**工具循环 + token 逐字流式是 LangChain4j 里最复杂的一块**（多轮 tool call 之间 token 流不连续、要手动续接）。如果目标只是「页面持续有反应」，**用结构化进度事件 SSE 即可完全满足，且实现简单可靠**：

```
{"type":"progress","step":1,"message":"已保存文件，扫描目录..."}
{"type":"tool","name":"extractPdf","status":"start"}
{"type":"tool","name":"extractPdf","status":"done","summary":"2页，843字"}
{"type":"content","file":"problem.md","text":"# 问题描述\n## 场景概述..."}   ← 每步产出
{"type":"done","output":["processed_data/data/*.csv", ...]}
{"type":"error","message":"..."}
```

- 前端用 `EventSource` / fetch + ReadableStream 逐条渲染，长任务也有持续反馈
- Spring MVC 用 `SseEmitter` 即可，无需引入 WebFlux
- 若确实要 ChatGPT 式的逐字输出：仅对「写 problem.md / references.md」的**最终生成轮**用 `StreamingChatLanguageModel` 流式 token，其余仍走进度事件。作为后期增强项，不进 MVP

### 5. Agent 循环：手动循环优先于 `AiServices` 的自动 agent

`AiServices` + `@Tool` 能拿到循环但**对进度事件/流式/中断控制较弱**。要「尽可能复现 Claude Code 的处理逻辑」，推荐**手动循环**：

```
messages = [systemPrompt(skill 工作流), user(...)]
loop:
  resp = chatModel.chat(messages)
  if resp 有 toolCalls:
    逐条执行工具(发进度事件) → 追加 ToolRequest/ToolResult 消息 → continue
  else:
    resp.text 是当前步骤产出 → 发 content 事件(或 writeFile) → 进入下一步骤
```

`ChatLanguageModel` 直接驱动，工具即普通 Java 方法，每步都能插桩发事件。这就是把 Claude Code 的「读题→跑脚本→写文档」循环在 Java 里重演一遍。

## 工具拆分方案（skill 步骤 → @Tool 映射）

| Skill 步骤 | LangChain4j @Tool | 实现 |
|---|---|---|
| 1 文件发现 | `scanDirectory(path)` | Java 遍历，识别 `*题*.pdf`/`附件*.xlsx`/`*.csv`/模板 |
| 2 PDF 提取 | `extractPdf(file)` | `ProcessBuilder` 跑 `loader.py pdf --input`，捕获 UTF-8 输出 |
| 2 Excel 预览 | `inspectExcel(file)` | `ProcessBuilder` 跑 `loader.py excel`，返回各 sheet 列名/类型/缺失 |
| 2 写 problem.md | （模型产出）→ `writeFile` | 模型生成 markdown，落盘 |
| 3 导出清洗 | `exportData(dir)` | `ProcessBuilder` 跑 `loader.py export --dir` |
| 3 读导出结果 | `readFile(relPath)` | 读 mapping.json / csv 前几行喂给模型 |
| 4 文献检索 | `webSearch(queries)` | Tavily 搜索，返回 title/url/snippet 结构化结果 |
| 4 论文元数据 | `semanticScholar(query)` | Semantic Scholar API，返回作者/年份/期刊/DOI，供 references.md 引用 |
| 4 读关键文献 | `fetchUrl(url)` | `UrlDocumentLoader` + toMarkdown，复刻 WebFetch |
| 4 写 references.md | `writeFile` | 同上 |
| 5 写 README.md | `writeFile` | 同上 |
| 6 清理 | `cleanup()` | 删临时文件，限定在任务目录内 |
| 通用 | `listFiles(path)` | 目录浏览 |

**system prompt** = 把 SKILL.md 的工作流（Step1~7 的指令）改写为 agent 指令，约束：problem.md 只描述不推荐方法、references.md 只记录事实、中文输出等原 skill 的 Constraints 全保留。

**安全约束**：
- `writeFile`/`readFile`/`cleanup` 的路径强约束在 `processed_data/` 与任务目录内，防路径穿越
- 不提供裸 bash tool；python 命令的参数由工具方法固定，不拼接用户输入
- 上传文件先做类型/大小校验

## 落地细节与坑

1. **中文编码**：`ProcessBuilder` 跑 python 时设 `PYTHONIOENCODING=utf-8`，读 `InputStreamReader(utf-8)`。这是 loader.py 本身在 Windows 上就遇到的坑，Java 端同样要处理
2. **Python 环境**：服务器用 Docker 打包 conda 环境（PyPDF2/pandas/openpyxl/pdfminer.six）最省心，或直接 conda env。脚本路径配置化
3. **上下文预算**：PDF 全文可全给（通常几百字~几千字），Excel 只给列名+类型+缺失+前几行样例；mapping.json 全量给
4. **任务管理**：每个上传建 taskId + 独立工作目录（`/data/tasks/{taskId}/`），SSE 按 taskId 订阅；进程中断/超时清理
5. **幂等与清理**：agent 完成或失败后清理临时文件；processed_data 按 taskId 隔离

## 工作量估算（约 5~7 人日）

| 模块 | 估算 |
|------|------|
| Python 环境容器化 + loader.py 适配 | 0.5~1d |
| Spring Boot 骨架 + 上传落盘 + 任务管理 + SSE | 1~1.5d |
| Agent 循环（工具注册 / system prompt / 进度事件） | 1.5~2d |
| 搜索工具（Tavily 或 Bing）+ fetchUrl | 0.5~1d |
| 端到端联调 + 编码坑 + 测试 | 1~1.5d |

## 风险清单

- **DeepSeek 无原生联网** → 已用搜索 API tool 兜底，无阻塞
- **搜索 API 网络可达性** → 服务器在香港、网络不受限，已排除（Tavily 直连可用）
- **流式 + 工具循环复杂度** → 用「进度事件 SSE」降复杂度，token 流作为可选增强
- **上下文窗口** → 预览策略控制，超长附件需截断或分块
- **references.md 真实性** → 必须来自搜索工具的真实结果，禁止模型编造出处（system prompt 强约束）

## 验证方式

1. 准备一份真实国赛题（PDF + 附件 xlsx），上传到接口
2. 观察 SSE 流：逐步出现「扫描→读题→导出→检索→写文档」进度与内容事件
3. 校验 `processed_data/`：CSV 编码/列名、mapping.json 结构、problem.md 结构、references.md 含真实可点 URL
4. 与本地 Claude Code 跑同一 skill 的输出对比，确认流程一致
5. 边界：大附件、GBK 编码 CSV、空 sheet、无数据文件等异常输入不崩

## 待定（实现前确认）

- **已定**：搜索数据源 = Tavily 主搜索（香港服务器可达）+ **Semantic Scholar 学术元数据补充（已确认加入）**
- SSE 流式：MVP 用「结构化进度事件」；token 级逐字流式列为后期可选增强（接口预留扩展点）
- 落地工程：**可行性阶段不涉及**，后续单独定
