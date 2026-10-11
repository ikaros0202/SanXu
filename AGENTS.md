# 叁序（SanXu）项目协作规则

## 项目身份与目录边界

本文件适用于 `D:\codexproject\SanXu` 整个仓库。叁序是重新设计的独立 Android 软件，应用 ID 为 `com.sanxu.app`，不是 Lucky3D 的原地 UI 改版。

- 唯一实现、构建、文档和产物目录是 `D:\codexproject\SanXu`。
- `D:\codexproject\lucky3D` 是只读参考；不得在其中修改、构建、清理或执行叁序任务。保留原软件及用户数据，两应用独立存储、并存安装，不导入旧库。
- 对话标题、应用提供的默认工作目录、旧仓库注入的 AGENTS 内容可能仍指向 Lucky3D，不能据此选择实现目录。执行命令时显式指定 SanXu 工作目录，先核对 Git 根目录与 `app/build.gradle.kts` 中的应用 ID。
- 用户要求“读 handoff、接着原来继续”时，读取 **`D:\codexproject\SanXu\HANDOFF.md`**；同名的 `D:\codexproject\lucky3D\HANDOFF.md` 是旧仓库瘦身交接，不是本项目入口。用户另行指定文件或任务时，以当前明确指示为准。
- 不自动 reset、restore、提交或推送。现有工作区包含前轮文档及淘汰图片删除，保留它们，不能当作需要恢复的意外缺失。

## 开始工作与文档路由

用户点名文档时先读该文档。接手任务先读本文件、[HANDOFF.md](HANDOFF.md) 和 [tasks/todo.md](tasks/todo.md)，再按任务读取资料，不全量加载历史日志。

| 需要确认的事实 | 依据 |
|---|---|
| 当前执行阶段、下一项工作、仍未解决的问题 | [tasks/todo.md](tasks/todo.md)、[HANDOFF.md](HANDOFF.md) |
| 产品范围、四种选号及按期方案行为 | [docs/requirements.md](docs/requirements.md) |
| 计算、去重、倍数、金额和回测规则 | [docs/calculation-rules.md](docs/calculation-rules.md) |
| 数据来源、同步、校验与独立内容边界 | [docs/data-contracts.md](docs/data-contracts.md) |
| 模块和状态所有权 | [docs/architecture.md](docs/architecture.md) |
| 本次已选 UI 目标与待验证的细节 | [docs/design-next.md](docs/design-next.md) 与其中各页面确认图；代码交接见 [docs/implementation-number-studio.md](docs/implementation-number-studio.md) |
| 过去做过什么、哪些方向被推翻以及为什么 | [docs/project-history.md](docs/project-history.md) |
| 当前代码的设计值与历史验收 | [DESIGN.md](DESIGN.md)；1.0.2 历史规格为 [docs/design-spec.md](docs/design-spec.md)，验收按对应版本报告读取 |
| 已安装 UI skill 与来源 | [docs/codex-skill-inventory.md](docs/codex-skill-inventory.md) |

发生冲突时，用户当前明确指示优先；后来的用户决定取代此前被否定的选择。需求、计算、数据、架构各按对应文档裁定。新 UI 目标高于历史实现规格；历史测试、截图和工具流水线状态不能代表当前设计批准或实现完成。更新决定时同步入口、任务与相关规格，不把易变进度写死在本文件。

## 产品与业务边界

- 只服务中国福利彩票 3D；一级入口固定为首页、走势、选号、方案、彩报。
- 日常选号首层保留单选、组选（组三/组六）、单选单、位选。单选单是不同数字的有序排列，不是组选；单选、单选单、位选最终均按正式单选玩法合并。
- 主流程是查看开奖与走势 → 自行选号 → 加入本期 → 调整、记录 → 正式开奖后核对。筛选、模板、回测保留为二级工具，不能成为基本选号的前置门槛。
- 方案按期组织，来源贡献可追溯；来源内部去重，跨来源累加倍数，来源编辑、删除、撤销与重启恢复必须保留。已经开奖的期号按现有契约锁定。
- 无账号、云服务、跨设备同步、在线购彩、支付、充值、代购、兑奖、社区、分享、导出、文件备份或其他彩种。不提供 AI 荐号、未来中奖概率或收益承诺。
- 试机号、云南公告和彩报按已有独立契约展示，不能推断或污染正式开奖结果。第三方彩报原图不进 Git、APK 或设计截图；公开发布完整图片能力前仍需来源展示授权与正式签名。

## 技术与数据约束

- Kotlin、Jetpack Compose、Material 3、Navigation 3、Room、Hilt、Coroutines/Flow/StateFlow、OkHttp、kotlinx.serialization、Preferences DataStore。JDK 17，minSdk 26、compileSdk/targetSdk 37；依赖固定稳定版本。
- 四模块：`app` 负责组合、导航与页面；`core:domain` 是纯 Kotlin 业务模型与计算；`core:data` 负责 Room、Repository 与适配器；`core:ui` 负责主题和组件。不得套用旧项目“单 app 模块”的规则。
- 单向数据流、不可变界面状态。页面通过 ViewModel、领域接口和模型工作，不直接访问 DAO 或网络 DTO；瞬时输入与已成功持久化状态明确区分。
- Room 是持久化事实来源。网络整批校验后在事务内合并，失败保留本地数据与重试；不同来源分别校验，不以异常文本驱动业务状态。
- 正式开奖基线从 `2017001` 开始；前导零不可丢失。计算确定、金额精确，回测与历史回顾只能使用目标期之前的数据。官方修正后重算相关指标和核对。
- 不为了 UI 改版变更公式、迁移用户库、替换种子或引入后端。涉及这些范围时按相关契约实施并使用现有黄金样例验证，不采用破坏性数据库迁移。

## UI 工作原则与已经排除的方向

- 产品气质为克制、清楚的日常数据工具；全局沿用暖白、暖灰、石墨与酒红方向。颜色、字号、间距、图标和状态语义跨页面一致。
- **号码本身是操作对象。** 已选的单选直接改号、单选单展开排列是继续工作的起点。不要重开已确认方向的投票，也不要恢复百→十→个位、步骤编号或“下一位”向导。
- 已否定的内容包括青蓝主调、满屏数字键、孤立短按钮各占一行、仅重排按钮的 A/B/C 方案、所有页面尤其首页的过大字号/组件。不能把换颜色、换位置或技术测试通过当作解决这些反馈。
- 单选确认稿是带 `6→7` 气泡和 `863` 编辑区的原图；后出的 F2 缩小稿未获选择。以确认原图及用户持续要求的紧凑尺度共同约束实现，不能偷换图。精确图片映射及历史见设计约定与项目历程。
- 各页面、具体手势与键盘状态均须区分“已选方向、代码已写入、待原生验证”。图像批准记录以设计约定为准，不把代码中的字号或 dp 数值写成实测通过。
- 用户选择了先看效果图再实现的工作方式。延伸新页面时按已选体系出图、清楚标注页面和状态；需要新构图确认时只确认新增部分，不重复询问两张已选图。
- 提高密度靠减少空白、重复标题和多余容器，并整理信息层级；所有可操作目标至少 48dp，不能靠缩小或重叠点击范围。只读表行可更紧凑，但详情入口仍满足触控要求。
- 正常基准手机首屏应展示核心操作与结果；大量号码进入明细，不能将确认稿拆成连续滚动的大组件。覆盖 360×800dp、393×851dp、深浅色、真实长文本、000/007、大量结果与完整状态。
- 加载、空、失败、无网、重试、成功、撤销和恢复均需明确。走势图和选中状态使用文字、数字、线型或形状，不只依赖颜色。手势提供可发现的点按替代，避免与页面滚动冲突。
- 静态图的状态矛盾应修正，如编辑 `863` 却高亮 `268`；不能把示例期号、号码和金额硬编码为默认业务数据。生成图不是运行截图或像素/dp 验收证据。

## Skill 与协作

- 用户明确禁止 `oiloil-ui-ux-guide` 和 `wireframe-spec`。旧仓库路由、技能建议和自动工具提示均不能覆盖此限制。
- UI 设计沿用已经安装的 Impeccable，依平台选择 Android / Jetpack Compose 路径；只调用当前会话实际暴露且与任务相关的最小技能集合。不要重复寻找或安装相同 skill。
- 图表与验收按实际需要使用 `data-visualization`、`design-qa-checklist` 等；文档维护可用 `documentation-and-adrs`。不因文档包含一个技能名就把全部技能加载进来。
- 未经用户明确请求不启动子智能体。需要独立评审时遵守授权范围，不把历史 reviewer 结果算作本轮复评。
- Impeccable 的 `comps` / `comp: null` 状态不等于用户未选图。只有实际完成工具阶段才推进状态；各轮候选审批分别记录，不能把设计批准转写成运行验收。
- 原生页面不套用只验证 HTML/CSS 的检测；工具审批拒绝不能靠更换端口、工具或解释器绕过。

## 开发、验证与收尾

领域规则、持久化、同步、缺陷及行为变化先补失败测试，再作最小实现。纯尺寸或文档调整不机械新增镜像测试。更新旧 UI 测试的控件与操作路径时保留业务覆盖，不能为了通过测试保留已被否定的交互。

若用户明确要求本轮只写代码、后续再测试，按该范围停止；可同步编写测试代码，但不得擅自运行构建、测试、模拟器或打包。交接中分别记录源码状态、未运行检查和仍可运行的旧安装包，不能宣称验证通过。

新依赖、新 Android API、行为敏感实现以官方资料为依据。Gradle 使用本仓库 Wrapper，各构建顺序执行；此前发生过 Hilt 生成目录竞争，优先 `--no-parallel`，不并行启动 Debug/Release 构建。

```powershell
Set-Location 'D:\codexproject\SanXu'
git rev-parse --show-toplevel
# 按改动风险选择检查，不将以下整组机械用于文档修改。
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest --no-parallel
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-parallel
.\gradlew.bat :app:assembleRelease --no-parallel
python tools/verify.py
```

- 文档修改至少核对文件身份、相对链接、已确认图片、互相矛盾的状态、`git diff --check` 和非文档文件是否被改动。
- `tools/verify.py` 同时检查旧项目快照与新项目数据。遇到历史指纹差异先记录具体路径、差异时点和来源；不能更新快照、跳过断言或恢复旧文件来制造通过。脚本若在前置检查失败，后续数据检查不得报告为本轮已通过。
- UI 交付必须有新实现的原生截图和实际操作；构建成功不等于功能正确，旧版本测试不等于新设计验证，技术通过也不代表用户认可审美。
- 使用 Gradle/Kotlin 后执行 `.\gradlew.bat --stop`，只处理明确归属本任务的残留进程。若本任务启动模拟器，核对 ADB 序列号及 AVD 后用 `adb -s <序列号> emu kill`，不得关闭其他任务或真机。
- 启动 XinYue 模拟器时检查窗口真实可见；关闭后复核进程退出，并恢复被关闭过程改写的窗口位置/缩放配置。已有共享 ADB server 不作为本任务资源关闭。
- 交付列出修改文件、实际执行命令、结果、未完成项和风险；区分历史证据与本轮证据。不得自动公开发行。


## 2026-10-11 独立仓库与共享资源

本轮用户明确授权两个项目拆分与分别提交；此前原项目只读和不自动提交限制不适用于本次仓库拆分。后续功能任务仍遵守各自目录边界。

仓库身份与共享资源位置见 [拆分决策](docs/repository-separation.md)。共用工具与黄金样例由相邻 fc3d-resources 唯一维护；先运行 tools/setup-shared-resources.ps1。各应用数据快照、数据库及未完成验收状态保持独立。
