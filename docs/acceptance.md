# 叁序 1.0.0 实施与验收

> 2026-10-02按用户要求清理淘汰/重复设计图片，已移除本文相关图片链接；[清理清单](ui-research/number-studio/image-cleanup.json)保留原路径和哈希。本文测试与验收结论仅为当时历史记录，不代表下一版新交互已实装或验收。


验收日期：2026-10-02。本次交付为独立 Android 本地测试软件，目录 `D:\codexproject\SanXu`，包名 `com.sanxu.app`，版本 1.0.0 / 1。原软件 `D:\codexproject\lucky3D` 保留。

## 交付内容

| 内容 | 文件入口 |
|---|---|
| 提炼后的需求与验收口径 | [requirements.md](requirements.md) |
| 技术选型、分层、事务与数据所有权 | [architecture.md](architecture.md)、[data-contracts.md](data-contracts.md) |
| 选号、合并、金额、遗漏与回测公式 | [calculation-rules.md](calculation-rules.md) |
| 页面层级、视觉规范、状态与交互 | [design-spec.md](design-spec.md) |
| 设计交互示意 | [prototype/index.html](prototype/index.html) |
| 安装包 | [Sanxu-1.0.0-local.apk](../artifacts/Sanxu-1.0.0-local.apk)、[Debug APK](../artifacts/Sanxu-1.0.0-debug.apk) |
| 字节数、哈希、JVM 测试统计 | [test-and-apk-summary.json](evidence/test-and-apk-summary.json) |

所有新增源码、文档和产物均位于新目录。旧项目的 290 个路径指纹与原 Git 状态均通过比对，见 [隔离检查](evidence/data-and-original-verification.json)。两个应用的不同包名与 UID 已在两个模拟器上核实，不能互相覆盖数据。

## 完成的功能与操作验收

| 需求 | 实现与本轮证据 |
|---|---|
| 四种日常选号 | 单选逐注/批量、组三/组六数字池或逐注、单选单排列、位选三位置组合；黄金案例及设备 UI 流程通过 |
| 结果可理解 | 当前号码数、含倍数注数、金额、倍数和加入按钮常驻；完整号码明细在独立页面，可剔除和恢复 |
| 重复与来源 | 同期同正式玩法同号码累加倍数；保存后反馈重复数；保留来源；编辑、删除、撤销通过事务与修订控制 |
| 明确金额 | 012×2 与单选单 012×1 合并为 6 个号码 / 8 注 / 16 元；再加组选与位选后为 10 个号码 / 13 注 / 26 元 |
| 持久化 | 各方式草稿独立；方案、模板、主题保存；Activity 重建、应用强制停止后重启仍保留 26 元清单 |
| 开奖核对 | 以正式号匹配；已开奖来源不可编辑删除；测试中开奖号由 012 修正为 013 后核对更新，原金额不变 |
| 分析工具 | 13 类条件、胆拖、模板、固定当前方法的历史回测；和值 0—3 生成 20 号，排除 000 后 19 号，再恢复 20 号 |
| 历史与内容 | 3399 期正式基线，615 条试机号；完整走势、开奖详情、云南公告、试机号时段规则和前台刷新 |
| 彩报 | 实际缓存图片成功解码，API 37 上 150% 缩放、恢复 100%、前后期入口验证；未保存第三方图片截图 |
| 离线与失败 | Wi-Fi 和移动数据关闭时保留开奖与方案，错误提示与重试入口可用；结束后恢复网络 |
| 外观与密度 | 纸白/墨绿；API 37 约 393×851 dp、API 26 360×800 dp；基准手机核心选号在一屏，较小屏明细可滚动 |

## 实际执行的检查

最新构建命令：

```powershell
.\gradlew.bat test lint :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease --console=plain
```

结果：BUILD SUCCESSFUL，1 分 21 秒；JVM 共 160 个独立测试（领域 57、数据 101、应用 2），0 失败、0 错误、0 跳过。Release 变体重复执行的同组数据测试未重复计数。Lint 0 错误，6 条依赖/工具版本更新提示；保留明确锁定版本。见 [构建记录](evidence/delivery-build.txt)、[Lint](evidence/lint-summary.txt)。

设备测试使用本轮构建的 Debug 与 androidTest APK；通过 `adb shell am instrument` 指定测试类执行。API 26 共 8 项，API 37 共 9 项：

| 测试组 | API 26 | API 37 |
|---|---:|---:|
| WorkspaceFlowTest + ToolsFlowTest + ReplayUiTest | 5 通过 | 5 通过 |
| OfflineFlowTest | 1 通过 | 1 通过 |
| core:data PlanStoreTest | 2 通过 | 2 通过 |
| ReportFlowTest | 未执行该设备项 | 1 通过 |

典型命令如下；进程恢复测试在执行前对新应用进行 `am force-stop`，离线测试执行时先关闭测试设备网络。

```powershell
adb -s emulator-5582 shell am instrument -w -r -e class com.sanxu.WorkspaceFlowTest,com.sanxu.ToolsFlowTest,com.sanxu.ReplayUiTest com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5584 shell am instrument -w -r -e class com.sanxu.OfflineFlowTest com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5582 shell am instrument -w -r com.sanxu.data.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5582 shell am instrument -w -r -e class com.sanxu.ReportFlowTest com.sanxu.app.test/androidx.test.runner.AndroidJUnitRunner
```

设备输出保留在 `evidence/device-*.txt`。业务测试后，开奖详情另补充了中奖总额、可选派奖文字和长内容滚动；最终源码重新完成 JVM、Lint、Debug/Release 构建，最终 Release 在两个设备另做冷启动、深浅色、四选号入口、走势、持久化清单和并存检查。未将 Debug 仪器测试声称为 Release 仪器测试。

```powershell
python tools/verify-installed-release.py emulator-5582
python tools/verify-installed-release.py emulator-5584
```

Release 检查见 [API 26](evidence/release-smoke-api26.json)、[API 37](evidence/release-smoke-api37.json)。最终 APK 通过 `apksigner verify --verbose --print-certs`，v2 签名有效；AAPT 校验名称、包名、版本和 API 26/37，见 [签名](evidence/apk-signature.txt)、[包信息](evidence/apk-badging.txt)。

最终 Release 的 2026263 期开奖详情另经界面树读取确认：销售额 11,993,366 元、中奖总额 8,385,463 元、三玩法中奖注数、奖池及兑奖截止均显示，见 [开奖详情记录](evidence/release-detail-api37.xml)。该期没有可选派奖，不将它视为真实派奖公告的显示验收。

数据检查均实际执行成功：

```powershell
powershell -ExecutionPolicy Bypass -File tools/validate-predevelopment-data.ps1
python tools/fetch-caiba-trial-seed.py --verify core/data/src/main/assets/trial/caiba-55125-trial-seed.json
python tools/test_seed_builder.py
python tools/verify.py
```

正式数据 2017001—2026263，共 3399 期；试机号 2025001—2026264，共 615 条。黄金脚本 6 个属性案例、生成器 3 项测试、Room 库完整性及空个人数据表检查均通过。生成器对输入失败保留已有数据库；校验结果分别见 [golden-validation.txt](evidence/golden-validation.txt)、[trial-seed-validation.txt](evidence/trial-seed-validation.txt)、[seed-builder-validation.txt](evidence/seed-builder-validation.txt)。

## 视觉证据

以下均来自 Android 实际界面，非原型渲染：

| 页面 | API 37 | API 26 |
|---|---|---|
| 单选单 | 截图（历史图片已清理） | 截图（历史图片已清理） |
| 组选 | 截图（历史图片已清理） | 截图（历史图片已清理） |
| 位选 | Release 截图（历史图片已清理） | Release 截图（历史图片已清理） |
| 重复合并反馈 | 截图（历史图片已清理） | 截图（历史图片已清理） |
| 6 号 / 8 注 / 16 元 | 截图（历史图片已清理） | 截图（历史图片已清理） |
| 纸白首页 | Release 截图（历史图片已清理） | Release 截图（历史图片已清理） |
| 墨绿首页 | Release 截图（历史图片已清理） | Release 截图（历史图片已清理） |
| 墨绿走势 | 截图（历史图片已清理） | 截图（历史图片已清理） |

API 26 仪器截图因系统截图接口返回空而使用 Compose 官方 `captureToImage`，仅包含应用内容；Release 冒烟截图使用原生 `adb screencap`。截图中的 2026300 期与 26 元清单是测试输入，APK 内置的个人表为空。

## 文件与实现入口

- `app/src/main/kotlin/com/sanxu/`：五页面、二级工具、导航、状态管理、生命周期同步。
- `core/domain/src/main/kotlin/com/sanxu/`：选号生成与合并、纯计算、回测、核对及接口。
- `core/data/src/main/kotlin/com/sanxu/data/`：新 Room 库、来源事务、草稿/模板、数据校验与网络适配器。
- `core/ui/src/main/kotlin/com/sanxu/ui/SanxuTheme.kt`：颜色、文字层级和公共组件。
- `data/`、`core/data/src/main/assets/`、`testdata/`：真实基线、新预置数据库与黄金案例。
- `app/src/androidTest/`、`core/*/src/test/`、`core/data/src/androidTest/`：可重复执行业务和持久化验证。
- `tools/`：获取/校验/生成基线、统计证据与原项目只读比对。

## 交付边界与剩余限制

- 当前为本地验收版本，使用开发测试签名；正式发行需要专用签名和第三方彩报展示授权。未进行公开发布。
- 官方与第三方网络来源可暂时失败，验收中已验证保留本地与重试；基线截至 2026263，后续由前台同步获取。界面不会补猜缺失的正式结果。
- HTML 原型保留了四方式与合并交互源码。内置浏览器拒绝 `file:` 预览后未绕过限制，因此不将它列为浏览器视觉验收通过；最终视觉与功能以已安装 Android 软件及截图为准。
- 本轮覆盖两台模拟器与对应 API，不声称完成实体手机、所有厂商系统或长时间使用验收。
- 编译进程与本任务模拟器的最终退出状态见 [资源收尾](evidence/cleanup.txt)。
