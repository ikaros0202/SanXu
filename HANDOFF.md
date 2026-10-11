# 叁序交接：代码已写入，下次测试

更新：2026-10-02。**下一步是编译与测试，不是继续选图，也不是继续旧 Lucky3D。**

## 工作目录与最新指令

唯一工作目录为 `D:\codexproject\SanXu`，应用 ID `com.sanxu.app`，独立 Android 软件“叁序”。`D:\codexproject\lucky3D` 只读；桌面默认 cwd 和注入的旧 AGENTS 可能仍指向旧工程，执行命令必须显式指定 SanXu。开始时读 [AGENTS.md](AGENTS.md) 和 [tasks/todo.md](tasks/todo.md)。

用户先要求补齐尚未定稿的页面，不急于实施。本轮生成首页、组选、位选三页并修正首页/位选尺度后，用户回复 **“暂时差不多，实施吧”**，接着要求 **“写好代码后就停止任务，我们下次再进行测试。”** 当前按此完成源码和文档后停止，等待下次开展验证；不要自动启动 Gradle、lint、测试、模拟器或打包。

工作分支为 `codex/holistic-redesign`。没有自动提交、推送、公开发布。生产版本仍为 `1.0.2 / versionCode 3`；现有 APK 和安装实例仍是旧界面。工作树已有新 UI 源码，但**未编译、未测试、未产生新运行截图**。历史通过不能计入本轮。

## 已选的五张参考

| 页面 | 精确图片 |
|---|---|
| 单选 | [.impeccable/mocks/approved/single-direct-edit.png](.impeccable/mocks/approved/single-direct-edit.png) |
| 单选单 | [.impeccable/mocks/approved/distinct-permutations.png](.impeccable/mocks/approved/distinct-permutations.png) |
| 首页 | [.impeccable/mocks/design-round-2/home-v2.png](.impeccable/mocks/design-round-2/home-v2.png) |
| 组选 | [.impeccable/mocks/design-round-2/group-v1.png](.impeccable/mocks/design-round-2/group-v1.png) |
| 位选 | [.impeccable/mocks/design-round-2/position-v2.png](.impeccable/mocks/design-round-2/position-v2.png) |

单选仍是用户重传的带“6→7”气泡原图，不能偷换成未选 F2。两张原图已选方向，三张补充稿获继续实施授权；均不代表用户已经认可原生运行效果。不再重问同一实施授权。

已否定：青蓝方向、所有页面过大的字号/组件、主页面数字键墙、百→十→个顺序向导、下一位、首轮仅重排按钮的 A/B/C。静态单选图里编辑863却高亮268的矛盾由实际状态修正。完整沿革和误接旧项目影响见 [项目历程](docs/project-history.md)。

## 代码接手位置

- [PickPage.kt](app/src/main/kotlin/com/sanxu/PickPage.kt)：四玩法直接编辑、两列预览、保留/剔除、期号、批量、倍数、筛选和保存。
- [NumberStudioComponents.kt](app/src/main/kotlin/com/sanxu/NumberStudioComponents.kt)：石墨编辑区、原生输入弹层、任意位拖动和短暂变化提示。
- [SingleNumberEditor.kt](core/domain/src/main/kotlin/com/sanxu/domain/pick/SingleNumberEditor.kt)：未收录文本、回改/替换/去重/删除。`PickDraft.singleEdit` 通过原有草稿 JSON 保存；组合公式和金额引擎未改。
- [HomeDashboard.kt](app/src/main/kotlin/com/sanxu/HomeDashboard.kt)：并排摘要、十期只读表、真实方案摘要；原 `HomePage.kt` 保留档案/详情。
- `NumbersPage.kt`：完整单选清单可回改六项预览外的号码；其他玩法保留原明细剔除。
- 公共主题、导航、系统栏、方案、设置、工具与回测已收紧尺寸。走势和彩报通过公共组件继承；未改图表计算与数据源。

具体文件、交互参数、布局适配和剩余风险集中在 **[implementation-number-studio.md](docs/implementation-number-studio.md)**。当前设计值见 [DESIGN.md](DESIGN.md)，确认图约定见 [design-next.md](docs/design-next.md)。[design-spec.md](docs/design-spec.md) 与 `.impeccable/design.json` 是标记过的1.0.2历史资料。

## 下次验证顺序

1. JVM 单测与编译：新 `SingleNumberEditorTest`、`WorkspaceViewModelTest` 恢复用例以及原领域/数据用例。测试源码已写，本轮没有红绿结果。
2. Debug、androidTest APK、lint 顺序运行，使用 `--no-parallel`，避免之前 Hilt 并行生成竞争。
3. API26 / API37 两种手机：360×800dp、393×851dp，深浅色、48dp目标、真实长内容、007/000、原生键盘、拖动与滚动冲突、1000结果、筛选/剔除恢复、存储失败与进程恢复。
4. `GuidedPickFlowTest` 已替换为 `DirectPickFlowTest`。`WorkspaceFlowTest` 和 `DesignScreensTest` 已改原生输入路径；保留黄金汇总：012×2 + 单选单012的六注×1 = 6号码、8注、16元，后续组选/位选例总计26元。
5. 单屏比例、弹层和交互通过后再修订版本号、构建新 Release、覆盖安装、截图、复评和 APK。本轮没有新包，不用1.0.2包宣称新设计完成。

`tools/verify-installed-release.py` 仍含旧“下一位”、1.0.2断言及待核对的端口映射，更新后再使用。每次启动/停止模拟器以实际 `adb devices`、AVD 名称、SDK、窗口可见性为准，不沿用文档端口猜测。

## 已知限制与保留现场

- 用户禁止 `oiloil-ui-ux-guide`、`wireframe-spec`，也未授权子智能体；本轮未启动子智能体。
- Impeccable 已安装，无需重装。自动流程仍是历史 `comps / comp:null`；绑定 comp 的尝试提示需 reset 后未继续。没有伪造自动门禁、独立复评或 finish。最新 sidecar 标记源码已写、验证延后。
- 旧 Lucky3D 相对初始290路径快照有8个既有差异，详见项目历程。`tools/verify.py` 上次停在原目录断言，后续数据校验未执行。本轮不重跑、不改 manifest、不擅自回滚旧文件。
- 前一对话清理350张图和13个sidecar已完成。既有删除及用户修改均保留，不恢复淘汰图，不 reset/restore。
- 设计准备时曾启动 SanXu API37/5584，后来已关闭并恢复窗口配置；`docs/evidence/number-studio/before-home-api37.png` 只拍到系统画面，不能作为运行验收。用户要求只写代码后没有再启动构建或模拟器；共享 ADB 不关闭。

下次可直接说：

> 在 D:\codexproject\SanXu 读取 HANDOFF.md 和 docs/implementation-number-studio.md，开始测试本轮已写的新 UI。先解决编译与业务问题，再看两种尺寸的原生效果；不要重新做旧项目，也不要把1.0.2截图当作新效果。


## 2026-10-11 仓库与公共工具迁移

默认开发项目为 SanXu，Lucky3D 只在用户明确指定时修改。两个应用已分别提交到 GitHub；SanXu 现为公开仓库。公共工具唯一源码在 SanXu shared/fc3d-tools，通过固定版本 ZIP 与用户缓存分发，不再依赖相邻共享仓库。见 [当前方案](docs/shared-resource-packages.md)。这些变更取代上文历史仓库状态，不代表新 UI 通过 Android 编译或验收。
