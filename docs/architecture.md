# 叁序架构

## 决策

独立Android原生应用；Kotlin、Compose Material3、Navigation3、Room3、Hilt、Flow/StateFlow、OkHttp、kotlinx.serialization、Preferences DataStore。JDK17，API26—37。依赖版本固定在gradle/libs.versions.toml，初始沿用已存在工具链。

四模块：app负责入口、导航、页面；core:domain负责纯Kotlin模型/生成/合并/核对及业务接口；core:data负责Room、适配器和本地业务事务；core:ui负责主题和组件。feature只依赖领域模型/业务接口，不直接访问DAO与远端DTO。

网络 → 整批校验 → 单Room事务 → Flow → ViewModel不可变状态 → Compose。瞬时输入先在状态中计算，草稿持久化完成才标注保存成功。走势构建、历史回测与图片解码离开主线程。

## 选号和清单

PickInput保存用户的原始操作，PickEngine生成规范化候选；PlanEntry保存来源、号码快照、倍数、备注和修订号。PlanSummary按(玩法,号码)聚合。各期通过issue分组，唯一来源ID作为幂等键。

2026-10-02 的直接改号代码在 `PickDraft` 增加带默认值的 `singleEdit: SingleNumberEdit`，保存未收录文本和回改原条目。只有显式收录/更新才写入 `PickInput.manual`；未收录内容不计入方案金额。该字段通过既有草稿 JSON 和 ViewModel 串行队列持久化，不新增 Room 表或迁移；旧 JSON 缺字段时取默认值。对应序列化与恢复测试已编写，但用户要求下次执行，目前未验证。详见 [代码交接](implementation-number-studio.md)。

添加与修改在事务中检查该期是否已开奖，修改使用期望修订号避免覆盖新编辑。删除/撤销同样检查开奖与修订。金额和汇总使用Long及精确算术。持久化失败返回类型化错误。

核对从当前Room开奖记录和不可变原始来源派生，官方修正后Flow自动重算。模板保持输入及条件，不混入某一期候选。回测只读目标期之前数据。

## 新库与隔离

sanxu.db版本1，不读取旧应用沙箱。新预置数据库由新Room schema和验证种子生成，不复制Lucky3D用户数据库。应用ID com.sanxu.app 与旧 com.lucky3d.app 独立。原项目状态清单和哈希在docs/evidence，交付前用工具复核。

数据源适配器与计算样例来自用户拥有的原工程，已独立复制并修改包名；不建立源码路径依赖。全部新界面、日常选号与清单模型在本工程实现。

## 官方依据

- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/topic/architecture/data-layer/offline-first
- https://developer.android.com/reference/kotlin/androidx/room3/Transaction

完整源契约由原项目docs/data-source-and-sync.md提炼，保留数据源隔离、校验、限流、前台周期与图片私有存储；失败绝不推断正式开奖号。

## 技术选择的理由与取舍

保留 Android 原生 Kotlin/Compose，可以直接使用已有经过验证的计算规则与数据适配器，并减少平台桥接和重复实现。新的改动集中在业务模型、模块边界与操作流程，日常功能不需要远端后端；“后端逻辑”由设备内的领域层、Repository 和 Room 事务承担。

四模块隔离纯计算、数据实现、界面系统和应用组合，避免为当前规模增加多余服务与模块。各入口草稿与方案来源分别持久化；ViewModel 通过串行操作队列组织写入，Repository 再以数据库事务、来源 ID 和修订号保证幂等与冲突检测，界面忙态不能代替存储校验。

本地 Release 使用 R8 压缩与资源裁剪，当前仅使用开发测试证书签名。正式分发时需配置专用签名；第三方完整图片展示须先满足来源授权要求。当前版本不接入在线购彩或支付服务。


## 2026-10-11 独立仓库与共享资源

仓库身份与共享资源位置见 [拆分决策](repository-separation.md)。共用工具和黄金样例由 SanXu 的 `shared/fc3d-tools` 唯一维护，通过公开版本化资源包与用户缓存复用；不再依赖相邻 fc3d-resources 仓库。运行 tools/setup-shared-resources.ps1 可获取并校验当前项目固定版本。


## 当前公共资源方式（2026-10-11）

共用工具和黄金样例由 SanXu 的 `shared/fc3d-tools` 唯一维护，通过公开版本化资源包与用户缓存复用；不再依赖相邻 fc3d-resources 仓库。运行 tools/setup-shared-resources.ps1 可获取并校验当前项目固定版本。 具体流程见[版本化工具包方案](shared-resource-packages.md)。
