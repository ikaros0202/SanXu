# 号码直接编辑：代码交接，验证留待下次

日期：2026-10-02。目录：`D:\codexproject\SanXu`；应用：`com.sanxu.app`。

## 本次授权与停止边界

用户看过首页、组选、位选三张补充稿后回复“暂时差不多，实施吧”，随后明确要求：**“写好代码后就停止任务，我们下次再进行测试。”** 本轮据此完成源码、测试代码和文档编写后停止，不运行 Gradle、lint、单元/仪器测试，不启动新模拟器、不安装或打包。

状态是 **代码已写入、未编译、未测试、未做新 UI 截图验收**。没有新 APK；版本号仍为 1.0.2 / versionCode 3，现有 APK 仍是旧界面。新增测试只有源码，不存在本轮红灯/绿灯结果。此前 160 项 JVM 等历史结果不得计入本轮。

## 五张实现参考

| 页面 | 当前参考 | 决策来源 |
|---|---|---|
| 单选 | [直接改号原图](../.impeccable/mocks/approved/single-direct-edit.png) | 用户重传后说“这两种不错”；保留带气泡原图，未改用 F2 |
| 单选单 | [排列结果原图](../.impeccable/mocks/approved/distinct-permutations.png) | 同上 |
| 首页 | [home-v2](../.impeccable/mocks/design-round-2/home-v2.png) | 本轮三页可继续实施，仍需用户后续看原生效果 |
| 组选 | [group-v1](../.impeccable/mocks/design-round-2/group-v1.png) | 同上 |
| 位选 | [position-v2](../.impeccable/mocks/design-round-2/position-v2.png) | 同上 |

新一轮 `home-v1` 和 `position-v1` 只是生成过程版本；前者开奖号过大、详情归属不清，后者组合公式过大，均已由 v2 取代。提示词在 `docs/ui-research/design-round-2/`。设计图不是运行证据。

## 源码范围

| 文件 | 已写入的行为 |
|---|---|
| `app/.../PickPage.kt` | 四玩法主界面、两列六项预览、剔除恢复、数字集合弹层、倍数弹层、期号/批量/筛选/保存入口 |
| `app/.../NumberStudioComponents.kt` | 三位号码直接编辑、真实变化提示、集合与位置编辑面、原生输入弹层 |
| `app/.../DigitPoolControls.kt` | 保留二级胆拖工具需要的数字键；主选号页不再使用十键墙 |
| `core/domain/.../pick/SingleNumberEditor.kt` | 未收录编辑状态、回改/替换/去重/删除、ASCII 输入与数字集合解析 |
| `core/domain/.../pick/PickModels.kt` | `PickDraft` 尾部新增带默认值的 `singleEdit`；组合、合并和金额引擎未改 |
| `app/.../WorkspaceViewModel.kt` | 新建选号时清除 pending 单选编辑，其他写入继续走既有串行队列 |
| `app/.../HomeDashboard.kt` | 新首页：并排开奖/试机号、十期只读表、真实方案摘要与继续选号 |
| `app/.../HomePage.kt` | 留存开奖档案/详情，压缩其文字和只读内容尺寸 |
| `app/.../NumbersPage.kt` | 单选完整清单支持回改任一条目，包括首页六项预览之外的号码；其他玩法保留剔除明细 |
| `core/ui/.../SanxuTheme.kt`、`WorkspaceComponents.kt` | 暖白页头、石墨编辑区、紧凑字号、6dp 内容圆角 |
| `app/.../SanxuApp.kt`、`MainActivity.kt`、`WorkspaceSheet.kt` | 56dp 底导航、48dp 页头、系统栏/弹层图标随主题切换 |
| `app/.../PlansPage.kt`、`SettingsPage.kt`、`ToolsPage.kt`、`BacktestPage.kt` | 压缩文字和留白；可操作行保留至少48dp，纯只读数据行可更紧凑 |
| `app/src/main/res/values/strings_number_studio.xml` | 新页面与交互文案 |

表内 `app/...` 均为 `app/src/main/kotlin/com/sanxu/`；领域路径为 `core/domain/src/main/kotlin/com/sanxu/domain/`，UI 路径为 `core/ui/src/main/kotlin/com/sanxu/ui/`。走势和彩报通过公共页头、主题、导航和空状态继承密度调整；走势图的计算、绘制、点击和缩放规则未改。

## 原生交互决定

- 单选：输入只接受 0–3 个 ASCII 数字，保留前导零。点按任意位打开完整号码输入框并选中对应字符；点击“收录”才进入候选。完整号码可上下拖动任一位，累计 32dp 改一格，上增下减，在 0/9 处停止、不循环。手势只属于数字区，外部页面仍可滚动；这些手势参数尚未实机验证。
- 回改：点清单号码将其载入编辑区，仅该原条目高亮；点击“更新”才替换原条目。改成已有号码会合并为一条，并恢复该号码的保留状态，其他剔除不变。输入框内可删除原注或取消回改。新建号码不会虚假高亮另一条记录。
- 未收录输入与已生成结果分开。原始编辑文本及被编辑号码放在 `PickDraft.singleEdit` 中，沿既有 Room 草稿 JSON 保存；不会写进 `PickInput.manual` 或方案金额。旧 JSON 缺字段时使用默认值，不修改 Room schema。草稿保存失败仍沿用现有错误提示。
- 组选/单选单：点石墨输入区调用原生键盘；可输入连续数字或分隔符，重复数字合并，非法字符提示错误。组三/组六可切换，保留按注录入。单选单仍是有序单选排列。
- 位选：三个位置摘要同时可见，按任意顺序编辑各自集合，不存在步骤编号、“下一位”或主页面十数字键盘。组合公式显示未筛选的笛卡尔积，实际保留数/金额由引擎计算。
- 集合真正改变时重新生成结果并清除旧剔除；打开输入框后不改集合直接应用，会保留剔除。每个结果有勾选与划线状态；全部剔除后仍显示可恢复的结果。
- 为紧凑手机节省一行，目标期号移入选号页头，“清空/新建”归入结果栏；倍数从底栏打开弹层。两处是原生布局适配，下一轮需和确认稿比较，不能声称已通过比例验收。
- 首页十条紧凑历史行只读，详情从最新开奖的48dp入口或“全部开奖”进入；不把30dp表行做成按钮。试机号区域可点按刷新/重试，失败保留既有内容，不推断正式开奖。

## 已写测试代码，全部未运行

- `SingleNumberEditorTest`：未收录不计数、前导零、任意位/边界、不完整输入、替换/合并、剔除、删除、旧草稿 JSON 和新状态序列化、非法数字集合输入。
- `WorkspaceViewModelTest`：补充切换玩法、重新创建 ViewModel 恢复未收录输入、新建清除编辑、位选草稿独立保留。
- `DirectPickFlowTest`：替换被否定的 `GuidedPickFlowTest`，覆盖个位先编辑、重建恢复、剔除后原样应用集合、1000结果、单选收录与回改合并。
- `WorkspaceFlowTest`、`DesignScreensTest`：改用原生集合输入和倍数弹层，保留 6号码/8注/16元以及后续26元业务例。
- `CompactViewportTest`：十期最后一行、只读行、详情48dp、底部动作可见性。

## 下次从这里测试

1. 先编译并执行领域/数据/应用 JVM 测试，处理编译问题与用例失败；随后 Debug、androidTest APK 与 lint，顺序运行，`--no-parallel`。
2. 原生验证360×800dp与393×851dp、深浅色。重点看：小屏选号表头/期号是否挤压、首页十期与方案动作是否同屏、键盘是否遮挡确认按钮、回改高亮与真实变化提示、拖动与滚动冲突、0/9边界、长集合/1000号码、全部剔除与筛选排除状态。
3. 业务黄金流程、存储失败、编辑/删除/撤销、进程重启与前导零全部重跑；先准备独立测试数据，不能拿旧装包和旧截图当新结果。
4. `tools/verify-installed-release.py` 仍是旧1.0.2脚本：包含“下一位”操作和旧版本断言；设备端口映射也须按实际 AVD 重新核对。**更新后才用于新 Release**，本轮未运行或改成假通过。
5. 新版本号、Release/APK、截图、图形目录与最终复评在测试通过后再做；未经用户要求不启动子智能体或公开发行。

参考命令（只记录，未执行）：

```powershell
Set-Location 'D:\codexproject\SanXu'
$env:JAVA_HOME='C:\Users\Public\Android\Jdk\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\Public\Android\Sdk'
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest --no-parallel
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-parallel
```

## 证据与工具状态

此次未启动新的编译进程或模拟器。此前准备阶段启动的 SanXu API37/5584 已关闭并恢复窗口配置；当时 `before-home-api37.png` 实际只拍到启动画面，不能用作应用基线截图。本轮没有新界面的截图证据。

Impeccable 自动流水线保持历史 `comps / comp:null`；曾尝试绑定已选 comp，但工具要求 reset，未继续重置。现有图像审批与实现授权单独记录，不伪造 spec、build、review 或 finish 通过。`DESIGN.md` 记录本轮代码中的设计值；`.impeccable/design.json` 标为旧1.0.2目录，原生测试后再重新提取。

原 Lucky3D 的八个既有指纹差异仍见 [项目历程](project-history.md)，本轮不触碰原项目、不改历史 manifest，也不重新执行原项目验证。本次代码交接不是该差异的修复或验证报告。

Compose 手势与输入参考：[拖动](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling)、[文本输入](https://developer.android.com/develop/ui/compose/text/user-input)。文档依据不等于本实现已验证。
