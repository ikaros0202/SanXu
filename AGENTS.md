# 叁序协作规则

- 原项目 `D:\codexproject\lucky3D` 是只读参考。不得修改、构建或清理原目录。
- 新软件使用 `com.sanxu.app`，与旧软件并存，不导入旧用户数据。
- 需求见 docs/requirements.md；技术与数据见 docs/architecture.md；界面见 docs/design-spec.md；当前任务见 tasks/todo.md。
- 不使用 oiloil-ui-ux-guide 和 wireframe-spec。未经明确请求不启动子智能体。
- Kotlin + Compose，四模块，领域纯 Kotlin；feature 不访问 DAO 或远端 DTO。Room 是持久化事实来源。
- 正式开奖、云南公告、试机号和彩报分开校验，网络失败保留本地。没有账号、云服务、支付、分享、导出或其他彩种。
- 新规则与缺陷先写失败测试；数字保留前导零，回测禁止未来数据泄漏。
- 用户操作至少48dp，覆盖深浅色、真实内容和完整状态。
- 只报告实际执行的检查。交付前停止本任务的编译进程及模拟器，并复核原项目指纹。
