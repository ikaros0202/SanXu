# 数据所有权与同步

## 正式开奖

唯一网络来源为中国福利彩票官方公开查询接口：`https://www.cwl.gov.cn/cwl_admin/front/cwlkj/search/kjxx/findDrawNotice`，固定`name=3d`。2017年以前不进入本产品。

发布基线从2017001开始；2026-10-02生成版本截至2026263，共3399期。下载完成后核对结构、日期、数字范围、重复期、年内连续性、总数和哈希，构建与Room schema一致的新预置数据库。个人表为空。

运行时网络结果先经过OfficialDrawValidator，所有新增/修正与同步元数据在Room事务中合并。失败不写入部分批次。Room是正式开奖事实来源，界面不直接消费网络DTO。官方修正会通过Flow刷新走势和方案核对。

自动同步仅发生于启动/前台及用户刷新；沿用SyncCoordinator的冷却与重叠请求保护。首页显示最近期开奖日期；失败保留本地并提供重试、上次成功时间。

## 云南公告

公告通过已验证的云南官方适配器获取，以期号、日期、开奖号三重校验关联全国开奖记录。不能覆盖或补齐全国开奖号。销售、中奖注数、单注金额、可选派奖、奖池和兑奖截止均保留原来源的单位与可空语义，不用缺失值推算金额。

## 试机号

来源为已批准的55125.cn年度公开页面。基线包含2025001—2026264，共615条；2025全年与2026截至打包日可得记录。源网页日期、期号、号码、抓取时间分开保存。

首页只显示北京时间当日值；16:30前允许保留前一日值。16:30后尚未取得当日值显示空状态。更早日期与未来日期不显示在首页。原始历史试机号仍供走势图对应期展示，不能作为正式开奖、回测或方案核对的输入。

首页可见期间以30秒时间脉冲触发策略检查；后台停止，不意味着每30秒发网络请求。RefreshPolicy负责16:30窗口、冷却、每日自动次数和人工重试。退出首页或进入后台即取消该检查。

## 彩报

沿用牛彩网A11彩吧彩报第三版公开页面适配器，限制最近30个北京时间自然日。期号、图片来源主机、尺寸、字节数、类型、完整性与SHA-256均校验；图片只放应用私有缓存。解码失败使该条缓存失效并显示重试，网络失败保留仍有效的本地图片。

界面支持按期选择、前后期、缩放和平移，不提供保存、分享、收藏或购买。第三方图片不提交Git、不内置APK，也不保存到本项目验收截图。展示授权仍需在公开发行前确认；本次仅交付本地测试版。

## 个人数据

方案来源、各入口草稿、模板分别由plan_entries、pick_drafts、saved_methods持久化。主题与应用内提示存Preferences DataStore。新应用不读取旧应用沙箱，不调用破坏性迁移。新数据库版本为1，后续升级必须提供显式迁移。

同一期写操作在事务内检查最新开奖号与修订号。来源ID防止重复请求；数据库失败不会清空当前输入。云备份和系统设备迁移通过manifest与排除规则关闭。卸载会删除这台设备的个人数据。

## 维护命令

```powershell
powershell -ExecutionPolicy Bypass -File tools/fetch-fc3d-seed.ps1 -OutputPath data/fc3d-seed.pending.json
powershell -ExecutionPolicy Bypass -File tools/validate-predevelopment-data.ps1 -SeedPath data/fc3d-seed.pending.json
python tools/fetch-caiba-trial-seed.py --verify core/data/src/main/assets/trial/caiba-55125-trial-seed.json
python tools/test_seed_builder.py
python tools/build-prepackaged-database.py
python tools/verify.py
```

更新正式基线时先审阅待替换文件、通过黄金校验，再替换正式种子并构建新库。生成器使用临时数据库验证完成后替换目标，失败时保留原成品。
