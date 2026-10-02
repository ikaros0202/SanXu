# 叁序整体界面 QA · 1.0.1

依据：用户本轮反馈、[设计规格](design-spec.md)、Android实装。使用`design-qa-checklist`的“证据—问题—复查”方式组织，未使用禁止的两个Skill。此表记录工程检查，不替代用户审美判断。

## 共用标准

- [x] 相同任务的内容和操作归为同一面板；短按钮不独立铺整行。
- [x] 页头56dp、导航64dp加安全区、连接选择栏、统一字体/圆角/留白。
- [x] 按钮与数字键至少48dp；小屏位选键通过实际布局尺寸断言。
- [x] 单选、组三/组六、单选单、位选都能从首层直接进入；说明使用明确示例。
- [x] 位选三位置、明细、金额、倍数、保存、导航在393×851dp和360×800dp可见；1000号码边界已操作。
- [x] 长号码列表独立滚动；000/007等前导零保留；选择同时用勾选、边框与颜色，排除增加删除线。
- [x] 模板40字列表内两行省略，详情展示全文；备注200字可编辑滚动；不将长内容硬塞入首页。
- [x] 保存、撤销、取消、空内容、失败、断网、重试保留明确反馈；提示不盖住工具页操作栏。
- [x] 纸白与夜蓝分别拍摄，实际主题选择与系统栏检查通过。

## 截图索引

两种设备使用相同文件名。以下链接为API37；API26镜像位于 [redesign-api26](evidence/redesign-api26/)。均为实际Android画面。`fixture-`表示可控仓库构造的空/失败状态，不含第三方彩报原图。

| 页面 / 状态 | 浅色 | 深色 | 复查要点 |
|---|---|---|---|
| 首页 | [图](evidence/redesign-api37/home-light.png) | [图](evidence/redesign-api37/home-dark.png) | 开奖/公告整体、试机号标识、三列回顾、本期入口 |
| 单选 | [图](evidence/redesign-api37/pick-single-light.png) | [图](evidence/redesign-api37/pick-single-dark.png) | 批量附属输入区，连续录入及底栏 |
| 组选 | [图](evidence/redesign-api37/pick-group-light.png) | [图](evidence/redesign-api37/pick-group-dark.png) | 组三/组六与直接录入同行 |
| 单选单 | [图](evidence/redesign-api37/pick-distinct-light.png) | [图](evidence/redesign-api37/pick-distinct-dark.png) | 数字池与真实排列预览 |
| 满池位选 | [图](evidence/redesign-api37/pick-position-full-light.png) | [图](evidence/redesign-api37/pick-position-full-dark.png) | 1000组合仍保留明细、保存；没有缩小触控范围 |
| 合并方案 | [图](evidence/redesign-api37/plans-light.png) | [图](evidence/redesign-api37/plans-dark.png) | 号码/注数/金额区分，三列对齐 |
| 选号来源 | [图](evidence/redesign-api37/sources-light.png) | [图](evidence/redesign-api37/sources-dark.png) | 编辑和更多操作归属每条记录；列表滚动 |
| 来源详情 | [图](evidence/redesign-api37/source-detail-light.png) | [图](evidence/redesign-api37/source-detail-dark.png) | 完整号码、备注与上下文 |
| 删除确认 | [图](evidence/redesign-api37/source-delete-light.png) | [图](evidence/redesign-api37/source-delete-dark.png) | 说明具体贡献，支持取消 |
| 走势 | [图](evidence/redesign-api37/trend-light.png) | [图](evidence/redesign-api37/trend-dark.png) | 页头期数、工具栏与表格连续；宽表允许横移 |
| 走势说明 | [图](evidence/redesign-api37/trend-help-light.png) | [图](evidence/redesign-api37/trend-help-dark.png) | 操作说明按需展开 |
| 开奖档案 | [图](evidence/redesign-api37/history-light.png) | [图](evidence/redesign-api37/history-dark.png) | 搜索和总量合并；真实3399期 |
| 开奖详情 | [图](evidence/redesign-api37/history-detail-light.png) | [图](evidence/redesign-api37/history-detail-dark.png) | 属性两列、官方公告明细和长内容滚动 |
| 筛选 | [图](evidence/redesign-api37/tools-filter-light.png) | [图](evidence/redesign-api37/tools-filter-dark.png) | 添加属于标题，当前结果固定底部 |
| 13类条件 | [图](evidence/redesign-api37/condition-types-light.png) | [图](evidence/redesign-api37/condition-types-dark.png) | 两列分类，避免单行横向寻找 |
| 条件编辑 | [图](evidence/redesign-api37/condition-editor-light.png) | [图](evidence/redesign-api37/condition-editor-dark.png) | 类型、范围与确认按任务分组 |
| 模板 | [图](evidence/redesign-api37/templates-light.png) | [图](evidence/redesign-api37/templates-dark.png) | 保存与模板行内操作，不与备注堆叠 |
| 40字模板全文 | [图](evidence/redesign-api37/template-full-title-light.png) | [图](evidence/redesign-api37/template-full-title-dark.png) | 列表截断后有全文入口 |
| 200字备注 | [图](evidence/redesign-api37/notes-light.png) | [图](evidence/redesign-api37/notes-dark.png) | 文本内滚动、计数与完成可见 |
| 1000号码明细 | [图](evidence/redesign-api37/numbers-light.png) | [图](evidence/redesign-api37/numbers-dark.png) | 可排除000、滚至999，再全部恢复 |
| 历史回测 | [图](evidence/redesign-api37/backtest-light.png) | [图](evidence/redesign-api37/backtest-dark.png) | 三项统计统一摘要，列表对齐 |
| 设置 | [图](evidence/redesign-api37/settings-light.png) | [图](evidence/redesign-api37/settings-dark.png) | 主题同行，开关和更新对应内容 |
| 期号编辑 | [图](evidence/redesign-api37/issue-light.png) | [图](evidence/redesign-api37/issue-dark.png) | 输入1999001后取消，原2026300未改变 |
| 空方案 | [图](evidence/redesign-api37/fixture-plans-empty-light.png) | [图](evidence/redesign-api37/fixture-plans-empty-dark.png) | 可直接去选号 |
| 未完成选号 | [图](evidence/redesign-api37/fixture-pick-empty-light.png) | [图](evidence/redesign-api37/fixture-pick-empty-dark.png) | 不完整输入不能保存 |
| 空走势 | [图](evidence/redesign-api37/fixture-trend-empty-light.png) | [图](evidence/redesign-api37/fixture-trend-empty-dark.png) | 明确缺少数据和重试 |
| 彩报不可用 | [图](evidence/redesign-api37/fixture-report-unavailable-light.png) | [图](evidence/redesign-api37/fixture-report-unavailable-dark.png) | 阅读栏保留，无图片时缩放禁用，可重试 |

另外保留 [真实断网状态](evidence/redesign-api37/home-offline-light.png) 和 [来源编辑](evidence/redesign-api37/source-edit-dark.png)。最终压缩Release另有 [API37截图](evidence/release-1.0.1-api37/) 与 [API26截图](evidence/release-1.0.1-api26/)，用于核对交付包而非仅看Debug。

最初的设置深色截图曾在主题更新前拍摄，现已按实际选中状态等待并替换。正常页面的四种选号和首页、方案、走势、工具、详情等逐类人工查看；所有索引文件同时由脚本检查存在。

首页的四张索引截图已替换为最终Release画面：最后收紧20dp组间及卡片内留白后，小屏底部方案入口和导航之间留出完整间隔，其余核心内容仍在首屏。
