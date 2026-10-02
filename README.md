# 叁序

独立福彩3D分析、选号和按期核对工具。应用名称叁序，应用ID `com.sanxu.app`，可与Lucky3D同时安装。

- [重点需求](docs/requirements.md)
- [技术架构](docs/architecture.md)
- [设计规格](docs/design-spec.md)
- [计算规则](docs/calculation-rules.md)
- [数据契约](docs/data-contracts.md)
- [验收报告与截图](docs/acceptance.md)
- [可交互设计原型](docs/prototype/index.html)
- [实施进度](tasks/todo.md)

## 本次交付

1.0.0 本地测试版已实现：四种日常选号、按期合并与来源编辑/撤销、草稿恢复、走势、条件筛选/胆拖/模板/回测、开奖详情、试机号和彩报。开发与验收日期：2026-10-02。

直接安装 [Sanxu-1.0.0-local.apk](artifacts/Sanxu-1.0.0-local.apk)，这是经过压缩的本地 Release（约 6.82 MiB，开发测试证书）。需要调试时使用 [Debug APK](artifacts/Sanxu-1.0.0-debug.apk)。APK 不提交到 Git，校验值见 [构建汇总](docs/evidence/test-and-apk-summary.json)。

重点体验路径：选号 → 单选输入 012、设为 2 倍 → 加入；切到单选单选择 0/1/2、1 倍 → 加入；方案应为 6 个号码、8 注、16 元，其中 012 为 3 倍。若目标期已经开奖，请选择有效的未开奖期。

## 工程

JDK17、Android SDK37，四个模块app/core:domain/core:data/core:ui。

```powershell
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest
.\gradlew.bat lint assembleDebug assembleRelease
python tools/verify.py
```

本应用只记录与分析，不提供在线购彩、支付、账号、云同步、分享、导出或其他彩种。第三方彩报展示授权仍为公开发行前的独立门禁。原项目为只读参考，所有实现和产物位于本目录。
