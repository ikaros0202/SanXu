# 叁序

独立福彩3D分析、选号和按期核对工具。应用名称叁序，应用ID `com.sanxu.app`，可与Lucky3D同时安装。

- [重点需求](docs/requirements.md)
- [技术架构](docs/architecture.md)
- [设计规格](docs/design-spec.md)
- [计算规则](docs/calculation-rules.md)
- [数据契约](docs/data-contracts.md)
- [本轮验收报告与截图](docs/acceptance-redesign.md)
- [逐页设计检查](docs/redesign-qa.md)
- [1.0.0 历史交付记录](docs/acceptance.md)
- [实施进度](tasks/todo.md)

## 本次交付

1.0.1 整体重设计版：五个主页与全部二级页面共享紧凑页头、连续内容面板和任务操作栏。四种选号同屏切换，金额/倍数/保存同行；方案在合并号码与来源记录间切换；筛选、模板、备注分区。保留原来的计算、持久化、撤销与开奖核对。开发与验证日期：2026-10-02。

直接安装 [Sanxu-1.0.1-local.apk](artifacts/Sanxu-1.0.1-local.apk)，这是经过压缩的本地 Release（约 6.90 MiB，开发测试证书）。需要调试时使用 [Debug APK](artifacts/Sanxu-1.0.1-debug.apk)。APK 不提交到 Git，校验值见 [构建汇总](docs/evidence/redesign-tests-and-apks.json)。原软件和1.0.0安装包均保留；已装叁序1.0.0可直接覆盖升级。

当前视觉以 [第二轮设计规格](docs/design-spec.md) 和 Android 实装截图为准。[旧HTML原型](docs/prototype/index.html) 仅为历史资料，不代表当前布局。

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
