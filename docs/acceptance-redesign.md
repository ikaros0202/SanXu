# 叁序 1.0.1 · 整体重设计交付

2026-10-02。独立工程 `D:\codexproject\SanXu`，应用 `com.sanxu.app`，版本1.0.1 / 2。此轮针对用户指出的整体构思不足、操作割裂和短按钮浪费空间，重新组织五个主页及全部二级页面。上一轮的功能检查不代表用户认可视觉设计，本报告记录本轮实现和工程验证。

## 交付入口

- [本地 Release APK](../artifacts/Sanxu-1.0.1-local.apk)，7,238,353字节，约6.90MiB；[Debug APK](../artifacts/Sanxu-1.0.1-debug.apk)。
- [整体设计规格](design-spec.md)、[重点需求](requirements.md)、[逐页截图与QA](redesign-qa.md)。
- [APK哈希与测试统计](evidence/redesign-tests-and-apks.json)、[签名检查](evidence/redesign-apk-signature.txt)、[包信息](evidence/redesign-apk-badging.txt)。

Release SHA-256：`9af3ea5dd14ee8398281e784a0d6aadb5323d0ad55c30cda4a92932e2ca85e3f`。使用与1.0.0相同的本地开发证书，APK v2签名验证通过，可覆盖已安装的叁序1.0.0。原Lucky3D与叁序包名、UID和数据目录分别独立。

原软件目录保持只读，旧1.0.0产物保留。原项目290个路径指纹、Git状态、3399期种子与数据库完整性检查通过，见 [隔离检查](evidence/data-and-original-verification.json)。本轮没有修改数据源、领域公式、数据库结构或种子数据。

## 页面与流程改动

| 范围 | 本轮结果 | 主要实现文件 |
|---|---|---|
| 公共框架 | 冷白/夜蓝配色；紧凑页头、连续选择栏、统一面板与底部弹层；导航用图标、文字和下划线表示选中 | `SanxuApp.kt`、`WorkspaceSheet.kt`、`core/ui/.../SanxuTheme.kt`、`WorkspaceComponents.kt` |
| 首页、开奖档案 | 三位开奖号成为一个整体；属性与云南公告附属于同期开奖；试机号单独标识；三列位置回顾；点期号进入档案/详情 | `HomePage.kt` |
| 选号 | 四方式连接切换；键盘/数字池/位置网格组成连续输入面板；组合预览与明细位于结果区；金额、倍数、保存同行 | `PickPage.kt` |
| 方案 | 金额、号码数、注数统一摘要；合并号码与选号来源切换；编辑和更多操作归到对应来源；删除前可取消 | `PlansPage.kt` |
| 走势 | 期数和帮助归入页头；快捷定位组成一条工具栏；表格保留完整属性、数字和形状编码；选点操作条附属于表格 | `TrendPage.kt`、`feature/trend/TrendChart.kt` |
| 彩报 | 刷新归入页头；换期、期号和缩放比例组成阅读工具栏；缩放细项按需展开，释放图片高度 | `ReportPage.kt` |
| 筛选、模板、备注 | 三分区；条件添加归入标题栏；13类条件使用两列入口和统一弹层；模板详情能查看完整长标题 | `ToolsPage.kt`、`feature/pick/ConditionEditor.kt` |
| 明细、回测、设置 | 多列号码清单；统计与对齐回测表；主题同行、设置与操作对应排列 | `NumbersPage.kt`、`BacktestPage.kt`、`SettingsPage.kt` |

上述应用文件位于 `app/src/main/kotlin/com/sanxu/`，公共界面位于 `core/ui/src/main/kotlin/com/sanxu/ui/`。测试新增/更新 `DesignEvidence.kt`、`DesignScreensTest.kt`、`WorkspaceFlowTest.kt`、`ToolsFlowTest.kt`、`ReplayUiTest.kt`、`OfflineFlowTest.kt`、`ReportFlowTest.kt`。包版本、README、需求、设计、任务与验证脚本同步更新。

## 实际验证

专用模拟器分别为 `SanXu_Design_API37`（393×851dp，1080×2340px/440dpi）和 `SanXu_Design_API26`（360×800dp，1080×2400px/480dpi）。均使用真实历史开奖和本轮构建的APK。

| 设备检查 | API26 | API37 |
|---|---:|---:|
| 四方式选号、合并、保存撤销、Activity恢复 | 通过 | 通过 |
| 筛选、模板、回测、剔除/恢复号码、主题/系统栏 | 通过 | 通过 |
| 开奖修正后重新核对，已开奖来源锁定 | 通过 | 通过 |
| 强制停止后的清单恢复 | 通过 | 通过 |
| 全部页面、深浅色、1000号码、200字备注、40字模板、取消期号修改 | 通过 | 通过 |
| 来源编辑后撤销，恢复26元/13注/4条记录 | 通过 | 通过 |
| 空方案/空走势/彩报失败重试（受控仓库） | 通过 | 通过 |
| 真实彩报解码、150%缩放、恢复100% | 通过 | 通过 |
| 断网保留开奖与方案、显示错误和重试 | 通过 | 通过 |
| 最终Release冷启动、持久化、主题、四方式、并存安装 | 通过 | 通过 |

每台设备10个不同的仪器测试方法通过；进程恢复方法另有一次显式强制停止后的重复执行，不重复计入数量。源状态测试使用可控仓库构造正式开奖修正、空内容和失败状态，文件名前缀`fixture-`对应这些夹具截图。正常页面使用3399期真实基线；未来期`2026300`及方案号码属于测试输入。未把夹具当作远端真实返回。

设备命令与结果保存在 `evidence/redesign-{regression,design,title,offline,report}-api{26,37}.txt`。主要命令：

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
.\gradlew.bat :app:assembleRelease --no-parallel --console=plain
.\gradlew.bat test lint --no-parallel --console=plain
.\gradlew.bat :app:assembleDebugAndroidTest :app:lint --no-parallel --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleRelease --no-parallel --console=plain
.\gradlew.bat :app:lint --no-parallel --console=plain
adb -s emulator-5582 shell am instrument -w -r -e class com.sanxu.WorkspaceFlowTest,com.sanxu.ToolsFlowTest,com.sanxu.ReplayUiTest com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5584 shell am instrument -w -r -e class com.sanxu.DesignScreensTest,com.sanxu.EmptyWorkspaceDesignTest com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner
python tools/verify-installed-release.py emulator-5582
python tools/verify-installed-release.py emulator-5584
python tools/verify.py
```

Debug、测试APK、Release构建通过；`test lint --no-parallel`1分10秒通过。最后仅调整首页20dp总留白分配后，重新构建Debug和Release（3分4秒），独立Lint检查通过（1分35秒），并在两台设备覆盖安装最终Release复核。JVM检查覆盖160个独立测试：domain57、data101、app2，0失败/错误/跳过；未变更模块允许Gradle使用已有检查结果，不将Release变体重复测试再计数。应用Lint为0错误、7警告：6条工具/依赖版本更新提示、1条旧条件类型字符串未使用提示，没有关闭检查或添加忽略规则。

相关构建日志：[Release](evidence/redesign-release-build.txt)、[JVM与Lint](evidence/redesign-checks.txt)、[测试APK与Lint](evidence/redesign-test-build.txt)、[最终Debug与Release](evidence/redesign-final-build.txt)、[最终Lint](evidence/redesign-final-lint.txt)、[Lint详情](evidence/redesign-lint.txt)。Release安装验证：[API26](evidence/release-1.0.1-smoke-api26.json)、[API37](evidence/release-1.0.1-smoke-api37.json)。仪器测试运行于Debug；Release由独立安装导航脚本验证，不混淆二者。

## 发现并处理的问题

1. 首轮截图显示首页新增档案区在小屏底部被截断。移除重复区块，历史数据通过期号详情进入；首页恢复清晰的四组信息。
2. 位选下方空白缺少结果反馈。增加一行组合预览，并在1000个组合下验证明细和保存仍在首屏。
3. 保存模板提示会盖住工具页固定底栏，导致立即点击回测无响应。提示条上移，保存后立即进入回测已在两台设备通过。
4. 主题由DataStore异步更新，过早截图曾捕捉到旧颜色。补充选中语义等待，并重新拍摄设置页；深浅色和系统栏分别验证。
5. 同时执行Release生成与Lint时，Lint读取暂未生成的`Hilt_MainActivity.java`而异常。改为先完整构建Release、再串行检查；两步均通过，未修改应用逻辑或禁用Lint。
6. API26的`svc wifi disable`没有实际关闭Wi-Fi，首次离线用例超时。通过该设备系统设置关闭Wi-Fi，确认`Active default network: none`后重跑通过；随后恢复Wi-Fi与移动数据。
7. 最终包的小屏首页底部卡片与导航过近。将四组信息之间的间距从12dp调为8dp，方案入口内部竖向留白从16dp调为12dp，释放20dp并保留至少72dp的入口高度；重新构建、安装和拍摄两种尺寸的最终Release。

## 交付范围与限制

这是供本地安装体验的开发签名版本，没有公开发布。第三方彩报图片仅在应用私有目录加载，未加入Git、APK或截图；本轮对真实图片做解码与缩放测试，彩报失败页使用夹具留图。公开发行的专用签名和来源授权维持原有边界。

已验证两种尺寸的模拟器，未声称覆盖真实品牌手机或用户对审美的最终认可。源码和实装截图可直接检查，旧HTML原型是历史资料。

资源收尾记录见 [cleanup](evidence/redesign-cleanup.json)。
