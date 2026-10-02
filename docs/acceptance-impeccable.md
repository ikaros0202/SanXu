# 叁序 1.0.2 · 酒红与紧凑布局验收

2026-10-02。本轮回应用户对苍白配色、组件过大、首页缺少排版和满屏数字的反馈。在独立工程 `D:\codexproject\SanXu` 完成原生重设计、安装验收和资源收尾；原 `D:\codexproject\lucky3D` 只读保留。以下区分原生实现结果、技能自动流程限制与用户审美决定。

## 交付内容

- 酒红身份区、暖灰背景、石墨正文，独立定义深色方案，取消青蓝视觉。
- 页头48dp、页面横向12dp、组间8dp，合并内容所属操作，保留至少48dp触控范围。
- 首页以开奖号为焦点，试机号一行、最近五期对齐表、方案摘要与开始选号合为一行；详情承载开奖前回顾与云南公告。
- 位选保留用户选定的B纵向步骤，只有当前位的十个数字键存在；三位摘要、实时组合、金额、倍数与保存保持可见。单选、组选、单选单、位选四入口保留。
- 方案、走势、彩报以及历史、筛选、模板、备注、明细、回测和设置共用同一套视觉。领域、数据库结构、种子和同步规则未改。

文件：`core/ui/.../SanxuTheme.kt`、`WorkspaceComponents.kt`；`app/.../SanxuApp.kt`、`MainActivity.kt`、`HomePage.kt`、`PickPage.kt`、`PlansPage.kt`、`TrendPage.kt`、`ReportPage.kt`、`ToolsPage.kt`、`NumbersPage.kt`、`BacktestPage.kt`、`SettingsPage.kt`、`WorkspaceSheet.kt`；应用版本配置；相应仪器测试、安装验证脚本和本文链接的需求/设计/任务文档。完整逐文件记录以本轮Git变更为准。

## 设计依据与独立复核

已安装 [Impeccable 4.5.0](https://github.com/pbakaus/impeccable/tree/508d7e8955de3b3caf2d8676e85206723d41a887)，固定提交；2026-10-02核验仓库73,903 stars，当日有提交。安装和来源证据见 [Skill基线](codex-skill-inventory.md)。竞品研究使用官方商店页面、发布介绍与可访问公开截图，见 [研究取舍](ui-research/competitor-review.md)，未冒充完整竞品客户端体验。

使用Impeccable的原生Android、Operate与收尾指导及design-qa-checklist；没有使用用户禁止的两项skill。专用finish角色未注册，使用独立通用子智能体承接其五章节评审契约；另一文档智能体记录代码中的设计系统。

原始首页与B稿分别获结构确认；用户后续明确要求加深颜色、缩小组件。compact-v2两稿是修订参考，没有标成新一次用户审批。效果图不进入APK。见 [方向约定](ui-research/direction-contract.md)、[原生区域](ui-research/native-regions.json)、[设计规格](design-spec.md)、[设计系统](../DESIGN.md)。

技能的自动网页阶段工具要求恰好一张approved comp，而本任务两页面分别确认，因此停在comps，未强制跳过、未宣称自动门禁完成。独立评审F1/F2均resolved后，执行`build-phase finish --disposition ship`仍被拒绝（exit2），原因是comps及后续自动阶段未关闭，见 [原始输出](evidence/impeccable-build-phase-finish.txt) 和 [状态快照](evidence/impeccable-build-state.json)。自动流程未闭环，不能借原生复评结果把它写成通过。HTML/CSS detector、网页pixel diff不适用原生Compose；原生dp、完整边界断言、截图和流程测试作为实现证据。缺失的早期五字段卡未追认为已执行。

首轮自身检查发现小屏首页CTA截断7dp、满池结果区拥挤、API26深色系统导航底色错误，均先写失败断言再修正。独立复核另发现Material3外层把手tooltip与无期号彩报恢复文案。把手长按失败测试成功复现英文提示，最终改为`dragHandle=null`，在content中放置静态标记；原生拖动与关闭继续保留。彩报文案按空/非空期号区分，并覆盖双主题。记录见 [首轮检查](ui-research/native-qa-first-pass.md)、[独立评审](ui-research/final-review.md)。

## 已执行验证

| 命令/路径 | 实际结果与证据 |
|---|---|
| `./gradlew.bat test lint --no-parallel` | 成功；独立JVM用例160：domain57、data101、app2，0失败；[日志](evidence/impeccable-checks.txt) |
| `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease --no-parallel` | 最后独立评审修正版成功，1m4s；[日志](evidence/impeccable-review-build.txt) |
| `./gradlew.bat :app:assembleDebugAndroidTest :app:lint --no-parallel` | 密度修正版成功；lint 0 errors、7 warnings，未新增抑制；[报告](evidence/impeccable-lint.txt) |
| `./gradlew.bat :app:lint --no-parallel` | 最后生产修正版成功，1m21s，仍为0 errors、7 warnings；[日志](evidence/impeccable-review-lint.txt) |
| `adb -s <owned-serial> shell am instrument -w -r -e class <classes> com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner` | 每台设备14项独立用例覆盖，重复执行不重复计数；最后修正版API37的7项通过，API26的6项通过、1项测试等待竞态修正后再跑3项通过；[完整汇总](evidence/impeccable-ui-summary.json) |
| `python tools/verify.py` | 收尾复核成功：290个原项目路径指纹和Git状态未变，3399期种子与预装库一致、数据库完整性及分层检查成功；[结果](evidence/data-and-original-verification.json) |

设备为本任务专用 `SanXu_Design_API26 / emulator-5584`（360×800dp）与 `SanXu_Design_API37 / emulator-5582`（393×851dp）。正常基线3399期、2017001–2026263；2026300是尚未开奖方案测试期，彩报空态及最近期号使用明确fixture。未把fixture写入生产种子。

业务覆盖：四选号、0/1→0→2/7生成002/007/102/107、编辑保留其它位、Activity重建、1000号码明细、合并金额26元/10号码/13注/4记录、来源更新/撤销、未来数据隔离与官方修正回放；长模板40字、备注200字、空/错误/取消/重试、离线保留本地。真实缓存彩报解码/缩放已在两台设备通过，测试不截取第三方原图。

复测记录透明保留：API26的系统栏测试在DataStore尚未提交深色选择时读取旧浅色，得到亮度0.992；这是测试只等Compose idle的竞态。补等待深色Selected状态，未改生产主题代码；两设备CompactViewportTest三项均通过。安装脚本也修复了API26在关闭弹层瞬间uiautomator返回空root却读取旧XML的竞态，改为移除旧dump并有界重试。失败日志不删除、不冒充所有首次执行均成功。

失败证据：[步骤红测](evidence/impeccable-guided-red.txt)、[整屏与系统栏红测](evidence/impeccable-viewport-red.txt)、[把手红测](evidence/impeccable-tooltip-red.txt)、[彩报状态红测](evidence/impeccable-review-red.txt)。最新双主题原生图见 [逐页图库](impeccable-qa.md)。

## 安装包与收尾

最终 [Sanxu-1.0.2-local.apk](../artifacts/Sanxu-1.0.2-local.apk)：`com.sanxu.app`，versionName1.0.2 / versionCode3，minSdk26 / targetSdk37；7,238,353字节，约6.90MiB。SHA-256：

`3c1e0f40ca115dddf3d5947daeb033951a338d2af117087bfc7405e6f8542f39`

[Debug APK](../artifacts/Sanxu-1.0.2-debug.apk) 与旧1.0.0/1.0.1产物均保留；[汇总](evidence/impeccable-tests-and-apks.json)。`apksigner verify --verbose --print-certs`成功，v2签名，Android Debug开发证书SHA-256为 `f156a6efd9eb66dcf814f32999ee43ffe05a5e1ab2e30a8f7f9b9e24f3c9e1d3`，见 [签名](evidence/impeccable-apk-signature.txt)、[包元数据](evidence/impeccable-apk-badging.txt)。

两台均实际执行`adb -s <serial> install -r artifacts/Sanxu-1.0.2-local.apk`以及`python tools/verify-installed-release.py <serial>`，结果成功：与旧`com.lucky3d.app`并存、UID及数据目录独立；强制停止后的冷启动正常，26元/10号码/13注/4记录保留；四方法、4号码8元、深浅色、系统返回与下拖关闭验证成功，无SanXu崩溃记录。[API26](evidence/release-1.0.2-smoke-api26.json)、[API37](evidence/release-1.0.2-smoke-api37.json)、[实际首页与位选](evidence/impeccable-release-overview.png)。

独立初评要求修复的F1/F2已在同一评审者的Verdict Pass中均判resolved；`ship`仅覆盖该修复清单及直接回归，不扩展为完整自动流程通过或用户审美认可。

执行`./gradlew.bat --stop`；核实AVD名字及序列号后分别`adb -s emulator-5582 emu kill`、`adb -s emulator-5584 emu kill`。网络恢复、显示覆盖恢复，本任务Gradle/Kotlin/两台模拟器进程均已退出，未关闭其他设备。[收尾记录](evidence/impeccable-cleanup.json)。

## 范围与限制

本轮是本地可安装测试版，使用开发证书，未公开发布。没有物理真机验收；两种模拟器不代表所有设备。原生静态评审不覆盖真实彩报图片的审美、刷新率或所有动画，仪器测试覆盖其解码/缩放功能。当前试机号或网络失败时显示真实状态，不补造数据。第三方彩报图片不在Git、APK或验收截图内。用户未明确认可最终审美，交付原生截图与APK供实际判断。
