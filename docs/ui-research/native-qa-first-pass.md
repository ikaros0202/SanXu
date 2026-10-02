# 原生首轮检查与修正批次

2026-10-02；两种设备、深浅色、真实3399期与受控空状态，59张/设备原生截图。

- 主框架与四色选号、方案、走势、彩报失败页和全部二级页在同一批检查；截图有效，无第三方彩报原图。
- 首轮API26首页开始选号底边695dp，导航起点688dp，截断7dp；原assertIsDisplayed允许局部可见。补CompactViewportTest，实际先失败，再合并历史标题/表头并明确小字行高。
- API26位选1000号码结果区贴近固定金额栏；移除重复“组合预览”标题行并减小辅助文字行高，数字键仍48dp。
- API26深色导航栏背景亮度1.0、白色图标对比不足。先加失败测试，再按应用主题重设SystemBarStyle。
- 弹层原生拖动把手出现英文tooltip；首轮把slot改成静态把手，但独立复核确认Material3外层仍包tooltip。该项首轮未解决，后续最终修正见独立评审及验收报告。
- 其他页面的分组、长标题/备注/号码列表、表格、状态和操作无阻断问题。后续只确认本批修正和受影响截图，不继续零散审美调整。

官方依据：[SystemBarStyle](https://developer.android.com/reference/androidx/activity/SystemBarStyle)、[系统栏](https://developer.android.com/develop/ui/compose/system/system-bars)、[Compose状态保存](https://developer.android.com/develop/ui/compose/state-saving)。Material3参数同时核对本工程1.4.0二进制签名。
