# 叁序

独立福彩3D分析、选号和按期核对工具。应用名称叁序，应用ID `com.sanxu.app`，可与Lucky3D同时安装。

**当前状态：五张页面参考已获实施授权，新 UI 源码已写入；按用户“写好代码后就停止，下次测试”的要求，尚未编译、测试、截图验收或打包。现有 APK 仍为旧1.0.2。** 唯一工作目录为 `D:\codexproject\SanXu`，旧 Lucky3D 只读。下一步从 [代码交接与待测清单](docs/implementation-number-studio.md) 开始验证。

先读 [AGENTS.md](AGENTS.md)、[SanXu HANDOFF](HANDOFF.md)、[任务清单](tasks/todo.md) 和 [下一版设计约定](docs/design-next.md)。前轮做过的工作、被否定的尝试及本次接错项目的纠正见 [项目历程](docs/project-history.md)。

- [重点需求](docs/requirements.md)
- [技术架构](docs/architecture.md)
- [下一版 UI 目标与确认原图](docs/design-next.md)
- [1.0.2 历史实现规格](docs/design-spec.md)
- [计算规则](docs/calculation-rules.md)
- [数据契约](docs/data-contracts.md)
- [1.0.2 历史验收报告](docs/acceptance-impeccable.md)
- [逐页设计检查](docs/impeccable-qa.md)
- [竞品研究与取舍](docs/ui-research/competitor-review.md)
- [当前代码设计值（待原生验证）](DESIGN.md)
- [1.0.1 历史交付记录](docs/acceptance-redesign.md)
- [1.0.0 历史交付记录](docs/acceptance.md)
- [实施进度](tasks/todo.md)

## 现有可运行基线：1.0.2

1.0.2：酒红、石墨与暖灰统一配色，48dp页头，首页五期表。位选采用当时选中的B纵向步骤，只展开当前位的十个数字键；这个方向已被后来的用户反馈否定，组件密度也仍未达要求。该版本是功能回归基线，不能当作下一版设计目标。开发与历史验证日期：2026-10-02。

安装 [Sanxu-1.0.2-local.apk](artifacts/Sanxu-1.0.2-local.apk)，约6.90MiB，本地Release、开发证书；[Debug APK](artifacts/Sanxu-1.0.2-debug.apk)。可覆盖升级已装的叁序；原Lucky3D和1.0.0/1.0.1安装包保留。哈希与检查见 [构建汇总](docs/evidence/impeccable-tests-and-apks.json)。

1.0.2已实现的视觉见 [设计规格](docs/design-spec.md) 和 [历史方向约定](docs/ui-research/direction-contract.md)。其中B纵向步骤已被后续反馈推翻；下一版已按五张参考写入源码，约定见 [design-next.md](docs/design-next.md)，尚待验证。生成效果图不是Android实装，旧HTML原型与旧截图说明均为历史资料。

重点体验路径：选号 → 单选输入 012、设为 2 倍 → 加入；切到单选单选择 0/1/2、1 倍 → 加入；方案应为 6 个号码、8 注、16 元，其中 012 为 3 倍。若目标期已经开奖，请选择有效的未开奖期。

## 工程

JDK17、Android SDK37，四个模块app/core:domain/core:data/core:ui。

```powershell
Set-Location 'D:\codexproject\SanXu'
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest --no-parallel
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --no-parallel
.\gradlew.bat :app:assembleRelease --no-parallel
.\gradlew.bat test lint --no-parallel
python tools/verify.py
```

命令按修改风险选择并顺序执行。当前 `tools/verify.py` 因旧 Lucky3D 与历史快照的8个路径差异在首项断言失败，后续数据校验没有执行；见 [续接记录](docs/project-history.md) 和 [本次文档核对](docs/evidence/context-recovery-verification.json)。历史通过记录不等于当前重新通过，不改快照或绕过检查。

本应用只记录与分析，不提供在线购彩、支付、账号、云同步、分享、导出或其他彩种。第三方彩报展示授权仍为公开发行前的独立门禁。原项目为只读参考，所有实现和产物位于本目录。


## 2026-10-11 独立仓库与共享资源

仓库身份与共享资源位置见 [拆分决策](docs/repository-separation.md)。共用工具和黄金样例由 SanXu 的 `shared/fc3d-tools` 唯一维护，通过公开版本化资源包与用户缓存复用；不再依赖相邻 fc3d-resources 仓库。运行 tools/setup-shared-resources.ps1 可获取并校验当前项目固定版本。

2026-10-11：独立验证入口已拆分；`python tools/verify.py` 不再依赖旧项目状态，历史隔离审计须显式执行 `python tools/verify.py --audit-original`，历史8项差异未被改写。


## 当前公共资源方式（2026-10-11）

共用工具和黄金样例由 SanXu 的 `shared/fc3d-tools` 唯一维护，通过公开版本化资源包与用户缓存复用；不再依赖相邻 fc3d-resources 仓库。运行 tools/setup-shared-resources.ps1 可获取并校验当前项目固定版本。 具体流程见[版本化工具包方案](docs/shared-resource-packages.md)。
