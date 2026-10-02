# 叁序

独立福彩3D分析、选号和按期核对工具。应用名称叁序，应用ID `com.sanxu.app`，可与Lucky3D同时安装。

- [重点需求](docs/requirements.md)
- [技术架构](docs/architecture.md)
- [设计规格](docs/design-spec.md)
- [计算规则](docs/calculation-rules.md)
- [数据契约](docs/data-contracts.md)
- [本轮验收报告与截图](docs/acceptance-impeccable.md)
- [逐页设计检查](docs/impeccable-qa.md)
- [竞品研究与取舍](docs/ui-research/competitor-review.md)
- [设计系统](DESIGN.md)
- [1.0.1 历史交付记录](docs/acceptance-redesign.md)
- [1.0.0 历史交付记录](docs/acceptance.md)
- [实施进度](tasks/todo.md)

## 本次交付

1.0.2：酒红、石墨与暖灰统一配色，48dp页头，紧凑首页五期表。位选采用用户确认的B纵向步骤，只展开当前位的十个数字键，已选摘要与组合结果随时可见。全部主页和二级页面同步视觉，计算、数据和方案持久化保持原契约。开发与验证日期：2026-10-02。

安装 [Sanxu-1.0.2-local.apk](artifacts/Sanxu-1.0.2-local.apk)，约6.90MiB，本地Release、开发证书；[Debug APK](artifacts/Sanxu-1.0.2-debug.apk)。可覆盖升级已装的叁序；原Lucky3D和1.0.0/1.0.1安装包保留。哈希与检查见 [构建汇总](docs/evidence/impeccable-tests-and-apks.json)。

当前视觉以 [设计规格](docs/design-spec.md)、[方向约定](docs/ui-research/direction-contract.md) 和本轮Android实装截图为准。生成效果图是设计过程资料，旧HTML原型是历史资料。

重点体验路径：选号 → 单选输入 012、设为 2 倍 → 加入；切到单选单选择 0/1/2、1 倍 → 加入；方案应为 6 个号码、8 注、16 元，其中 012 为 3 倍。若目标期已经开奖，请选择有效的未开奖期。

## 工程

JDK17、Android SDK37，四个模块app/core:domain/core:data/core:ui。

```powershell
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :app:assembleRelease --no-parallel
.\gradlew.bat test lint --no-parallel
python tools/verify.py
```

本应用只记录与分析，不提供在线购彩、支付、账号、云同步、分享、导出或其他彩种。第三方彩报展示授权仍为公开发行前的独立门禁。原项目为只读参考，所有实现和产物位于本目录。
