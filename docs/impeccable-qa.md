# 叁序 1.0.2 · 原生界面图库

2026-10-02。下面均为真正运行的Android界面，生成效果图单独保存在 `.impeccable/mocks`，不混作实现截图。API26为360×800dp（1080×2400像素），API37为393×851dp（1080×2340像素）；基线字体、深浅色。正常页面使用3399期真实开奖；fixture前缀是可复现的空态/失败状态，2026300是未开奖方案测试期。

核心接受条件：首页五条最近开奖与完整开始选号按钮在导航上方；位选只展开当前位的十个48dp键，满池时明细/金额/保存同屏。未使用局部可见断言替代完整边界检查。所有控件由Compose绘制，第三方彩报原图不进入截图。

## 最终安装版

覆盖安装Release、冷启动保留方案以及真实点击/拖动结果记录在 `release-1.0.2-smoke-api26.json`、`release-1.0.2-smoke-api37.json`。最终截图入口：

| 页面 | API26 | API37 |
|---|---|---|
| 首页浅色 | [查看](evidence/release-1.0.2-api26/home-light.png) | [查看](evidence/release-1.0.2-api37/home-light.png) |
| 位选4个结果、十位展开 | [查看](evidence/release-1.0.2-api26/pick-position-light.png) | [查看](evidence/release-1.0.2-api37/pick-position-light.png) |
| 方案浅色 | [查看](evidence/release-1.0.2-api26/plans-light.png) | [查看](evidence/release-1.0.2-api37/plans-light.png) |
| 首页深色 | [查看](evidence/release-1.0.2-api26/home-dark.png) | [查看](evidence/release-1.0.2-api37/home-dark.png) |
| 位选深色 | [查看](evidence/release-1.0.2-api26/pick-position-dark.png) | [查看](evidence/release-1.0.2-api37/pick-position-dark.png) |
| 走势深色 | [查看](evidence/release-1.0.2-api26/trend-dark.png) | [查看](evidence/release-1.0.2-api37/trend-dark.png) |

## 全部页面与状态

| 页面/状态 | API26浅色 | API26深色 | API37浅色 | API37深色 |
|---|---|---|---|---|
| 首页 | [查看](evidence/impeccable-api26/home-light.png) | [查看](evidence/impeccable-api26/home-dark.png) | [查看](evidence/impeccable-api37/home-light.png) | [查看](evidence/impeccable-api37/home-dark.png) |
| 走势 | [查看](evidence/impeccable-api26/trend-light.png) | [查看](evidence/impeccable-api26/trend-dark.png) | [查看](evidence/impeccable-api37/trend-light.png) | [查看](evidence/impeccable-api37/trend-dark.png) |
| 单选 | [查看](evidence/impeccable-api26/pick-single-light.png) | [查看](evidence/impeccable-api26/pick-single-dark.png) | [查看](evidence/impeccable-api37/pick-single-light.png) | [查看](evidence/impeccable-api37/pick-single-dark.png) |
| 组选 | [查看](evidence/impeccable-api26/pick-group-light.png) | [查看](evidence/impeccable-api26/pick-group-dark.png) | [查看](evidence/impeccable-api37/pick-group-light.png) | [查看](evidence/impeccable-api37/pick-group-dark.png) |
| 单选单 | [查看](evidence/impeccable-api26/pick-distinct-light.png) | [查看](evidence/impeccable-api26/pick-distinct-dark.png) | [查看](evidence/impeccable-api37/pick-distinct-light.png) | [查看](evidence/impeccable-api37/pick-distinct-dark.png) |
| 位选满池1000号码 | [查看](evidence/impeccable-api26/pick-position-full-light.png) | [查看](evidence/impeccable-api26/pick-position-full-dark.png) | [查看](evidence/impeccable-api37/pick-position-full-light.png) | [查看](evidence/impeccable-api37/pick-position-full-dark.png) |
| 方案号码 | [查看](evidence/impeccable-api26/plans-light.png) | [查看](evidence/impeccable-api26/plans-dark.png) | [查看](evidence/impeccable-api37/plans-light.png) | [查看](evidence/impeccable-api37/plans-dark.png) |
| 方案来源 | [查看](evidence/impeccable-api26/sources-light.png) | [查看](evidence/impeccable-api26/sources-dark.png) | [查看](evidence/impeccable-api37/sources-light.png) | [查看](evidence/impeccable-api37/sources-dark.png) |
| 历史列表 | [查看](evidence/impeccable-api26/history-light.png) | [查看](evidence/impeccable-api26/history-dark.png) | [查看](evidence/impeccable-api37/history-light.png) | [查看](evidence/impeccable-api37/history-dark.png) |
| 当期开奖详情 | [查看](evidence/impeccable-api26/history-detail-light.png) | [查看](evidence/impeccable-api26/history-detail-dark.png) | [查看](evidence/impeccable-api37/history-detail-light.png) | [查看](evidence/impeccable-api37/history-detail-dark.png) |
| 号码明细 | [查看](evidence/impeccable-api26/numbers-light.png) | [查看](evidence/impeccable-api26/numbers-dark.png) | [查看](evidence/impeccable-api37/numbers-light.png) | [查看](evidence/impeccable-api37/numbers-dark.png) |
| 筛选工具 | [查看](evidence/impeccable-api26/tools-filter-light.png) | [查看](evidence/impeccable-api26/tools-filter-dark.png) | [查看](evidence/impeccable-api37/tools-filter-light.png) | [查看](evidence/impeccable-api37/tools-filter-dark.png) |
| 条件类型 | [查看](evidence/impeccable-api26/condition-types-light.png) | [查看](evidence/impeccable-api26/condition-types-dark.png) | [查看](evidence/impeccable-api37/condition-types-light.png) | [查看](evidence/impeccable-api37/condition-types-dark.png) |
| 条件编辑 | [查看](evidence/impeccable-api26/condition-editor-light.png) | [查看](evidence/impeccable-api26/condition-editor-dark.png) | [查看](evidence/impeccable-api37/condition-editor-light.png) | [查看](evidence/impeccable-api37/condition-editor-dark.png) |
| 方法模板 | [查看](evidence/impeccable-api26/templates-light.png) | [查看](evidence/impeccable-api26/templates-dark.png) | [查看](evidence/impeccable-api37/templates-light.png) | [查看](evidence/impeccable-api37/templates-dark.png) |
| 40字模板详情 | [查看](evidence/impeccable-api26/template-full-title-light.png) | [查看](evidence/impeccable-api26/template-full-title-dark.png) | [查看](evidence/impeccable-api37/template-full-title-light.png) | [查看](evidence/impeccable-api37/template-full-title-dark.png) |
| 200字备注 | [查看](evidence/impeccable-api26/notes-light.png) | [查看](evidence/impeccable-api26/notes-dark.png) | [查看](evidence/impeccable-api37/notes-light.png) | [查看](evidence/impeccable-api37/notes-dark.png) |
| 历史回测 | [查看](evidence/impeccable-api26/backtest-light.png) | [查看](evidence/impeccable-api26/backtest-dark.png) | [查看](evidence/impeccable-api37/backtest-light.png) | [查看](evidence/impeccable-api37/backtest-dark.png) |
| 设置 | [查看](evidence/impeccable-api26/settings-light.png) | [查看](evidence/impeccable-api26/settings-dark.png) | [查看](evidence/impeccable-api37/settings-light.png) | [查看](evidence/impeccable-api37/settings-dark.png) |
| 期号校验 | [查看](evidence/impeccable-api26/issue-light.png) | [查看](evidence/impeccable-api26/issue-dark.png) | [查看](evidence/impeccable-api37/issue-light.png) | [查看](evidence/impeccable-api37/issue-dark.png) |
| 批量录入 | [查看](evidence/impeccable-api26/batch-light.png) | [查看](evidence/impeccable-api26/batch-dark.png) | [查看](evidence/impeccable-api37/batch-light.png) | [查看](evidence/impeccable-api37/batch-dark.png) |
| 玩法说明 | [查看](evidence/impeccable-api26/pick-help-light.png) | [查看](evidence/impeccable-api26/pick-help-dark.png) | [查看](evidence/impeccable-api37/pick-help-light.png) | [查看](evidence/impeccable-api37/pick-help-dark.png) |
| 走势说明 | [查看](evidence/impeccable-api26/trend-help-light.png) | [查看](evidence/impeccable-api26/trend-help-dark.png) | [查看](evidence/impeccable-api37/trend-help-light.png) | [查看](evidence/impeccable-api37/trend-help-dark.png) |
| 来源详情 | [查看](evidence/impeccable-api26/source-detail-light.png) | [查看](evidence/impeccable-api26/source-detail-dark.png) | [查看](evidence/impeccable-api37/source-detail-light.png) | [查看](evidence/impeccable-api37/source-detail-dark.png) |
| 来源删除确认 | [查看](evidence/impeccable-api26/source-delete-light.png) | [查看](evidence/impeccable-api26/source-delete-dark.png) | [查看](evidence/impeccable-api37/source-delete-light.png) | [查看](evidence/impeccable-api37/source-delete-dark.png) |
| 空方案 | [查看](evidence/impeccable-api26/fixture-plans-empty-light.png) | [查看](evidence/impeccable-api26/fixture-plans-empty-dark.png) | [查看](evidence/impeccable-api37/fixture-plans-empty-light.png) | [查看](evidence/impeccable-api37/fixture-plans-empty-dark.png) |
| 空选号 | [查看](evidence/impeccable-api26/fixture-pick-empty-light.png) | [查看](evidence/impeccable-api26/fixture-pick-empty-dark.png) | [查看](evidence/impeccable-api37/fixture-pick-empty-light.png) | [查看](evidence/impeccable-api37/fixture-pick-empty-dark.png) |
| 空走势 | [查看](evidence/impeccable-api26/fixture-trend-empty-light.png) | [查看](evidence/impeccable-api26/fixture-trend-empty-dark.png) | [查看](evidence/impeccable-api37/fixture-trend-empty-light.png) | [查看](evidence/impeccable-api37/fixture-trend-empty-dark.png) |
| 彩报无可选期号 | [查看](evidence/impeccable-api26/fixture-report-unavailable-light.png) | [查看](evidence/impeccable-api26/fixture-report-unavailable-dark.png) | [查看](evidence/impeccable-api37/fixture-report-unavailable-light.png) | [查看](evidence/impeccable-api37/fixture-report-unavailable-dark.png) |
| 彩报有期号但图不可用 | [查看](evidence/impeccable-api26/fixture-report-with-issue-light.png) | [查看](evidence/impeccable-api26/fixture-report-with-issue-dark.png) | [查看](evidence/impeccable-api37/fixture-report-with-issue-light.png) | [查看](evidence/impeccable-api37/fixture-report-with-issue-dark.png) |

| 额外状态 | API26 | API37 |
|---|---|---|
| 来源编辑深色 | [查看](evidence/impeccable-api26/source-edit-dark.png) | [查看](evidence/impeccable-api37/source-edit-dark.png) |
| 离线保留内容与重试 | [查看](evidence/impeccable-api26/home-offline-light.png) | [查看](evidence/impeccable-api37/home-offline-light.png) |
| 长按把手无英文tooltip | [查看](evidence/impeccable-api26/sheet-tooltip-regression.png) | [查看](evidence/impeccable-api37/sheet-tooltip-regression.png) |

离线图含短暂snackbar反馈，持续失败提示位于开奖区；不把短暂反馈当成永久页面结构。彩报实际图片的解码/缩放由两设备ReportFlowTest验证，未保存第三方原图。修复前图片只在 `evidence/impeccable-first` 作为失败证据保留，不是交付界面。

独立评审与修复判断见 [final-review.md](ui-research/final-review.md)。功能、构建、安装包、签名、原项目隔离及收尾见 [验收报告](acceptance-impeccable.md)。静态截图不代表全部动画、物理真机或用户最终审美认可。
